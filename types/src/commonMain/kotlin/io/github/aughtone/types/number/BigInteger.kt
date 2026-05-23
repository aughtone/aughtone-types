package io.github.aughtone.types.number

/**
 * An arbitrary-precision integer mathematically equivalent to an infinite-width integer.
 * Implemented in pure Kotlin for cross-platform support.
 */
class BigInteger internal constructor(
    val signum: Int,
    internal val magnitude: IntArray // little-endian: index 0 is least significant 32-bits
) : Comparable<BigInteger> {

    init {
        require(signum in -1..1) { "Signum must be -1, 0, or 1" }
        if (signum == 0) {
            require(magnitude.isEmpty()) { "Magnitude must be empty if signum is 0" }
        }
    }

    override fun compareTo(other: BigInteger): Int {
        if (this.signum != other.signum) {
            return this.signum.compareTo(other.signum)
        }
        if (this.signum == 0) return 0
        
        val cmp = compareMagnitude(this.magnitude, other.magnitude)
        return if (this.signum > 0) cmp else -cmp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigInteger) return false
        if (signum != other.signum) return false
        if (magnitude.size != other.magnitude.size) return false
        for (i in magnitude.indices) {
            if (magnitude[i] != other.magnitude[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = signum
        for (i in magnitude.indices) {
            result = 31 * result + magnitude[i]
        }
        return result
    }

    override fun toString(): String {
        return toString(10)
    }

    fun toString(radix: Int): String {
        require(radix in 2..36) { "Radix out of range" }
        if (signum == 0) return "0"
        
        val radixBig = valueOf(radix.toLong())
        var current = this.abs()
        val sb = StringBuilder()
        
        while (current.signum != 0) {
            val (q, r) = current.divideAndRemainder(radixBig)
            val digit = if (r.magnitude.isEmpty()) 0 else r.magnitude[0]
            sb.append(digitToChar(digit))
            current = q
        }
        
        if (signum < 0) {
            sb.append('-')
        }
        return sb.reverse().toString()
    }

    private fun abs(): BigInteger {
        return if (signum < 0) BigInteger(1, magnitude) else this
    }

    // --- Core Math ---
    
    fun add(other: BigInteger): BigInteger {
        if (this.signum == 0) return other
        if (other.signum == 0) return this
        
        if (this.signum == other.signum) {
            return BigInteger(this.signum, addMagnitude(this.magnitude, other.magnitude))
        }
        
        // Opposite signs: subtract the smaller magnitude from the larger
        val cmp = compareMagnitude(this.magnitude, other.magnitude)
        if (cmp == 0) return ZERO
        
        val resultMag = if (cmp > 0) {
            subtractMagnitude(this.magnitude, other.magnitude)
        } else {
            subtractMagnitude(other.magnitude, this.magnitude)
        }
        val resultSign = if (cmp > 0) this.signum else other.signum
        
        return BigInteger(resultSign, resultMag)
    }

    fun subtract(other: BigInteger): BigInteger {
        if (other.signum == 0) return this
        if (this.signum == 0) return BigInteger(-other.signum, other.magnitude)
        
        if (this.signum != other.signum) {
            return BigInteger(this.signum, addMagnitude(this.magnitude, other.magnitude))
        }
        
        val cmp = compareMagnitude(this.magnitude, other.magnitude)
        if (cmp == 0) return ZERO
        
        val resultMag = if (cmp > 0) {
            subtractMagnitude(this.magnitude, other.magnitude)
        } else {
            subtractMagnitude(other.magnitude, this.magnitude)
        }
        val resultSign = if (cmp > 0) this.signum else -other.signum
        
        return BigInteger(resultSign, resultMag)
    }

    fun multiply(other: BigInteger): BigInteger {
        if (this.signum == 0 || other.signum == 0) return ZERO
        val resultSign = this.signum * other.signum
        val resultMag = multiplyMagnitude(this.magnitude, other.magnitude)
        return BigInteger(resultSign, resultMag)
    }

    fun divide(other: BigInteger): BigInteger {
        return divideAndRemainder(other).first
    }

    fun remainder(other: BigInteger): BigInteger {
        return divideAndRemainder(other).second
    }

    operator fun plus(other: BigInteger): BigInteger = add(other)
    operator fun minus(other: BigInteger): BigInteger = subtract(other)
    operator fun times(other: BigInteger): BigInteger = multiply(other)
    operator fun div(other: BigInteger): BigInteger = divide(other)
    operator fun rem(other: BigInteger): BigInteger = remainder(other)
    operator fun unaryMinus(): BigInteger = if (this.signum == 0) this else BigInteger(-this.signum, this.magnitude)


    fun divideAndRemainder(other: BigInteger): Pair<BigInteger, BigInteger> {
        if (other.signum == 0) throw ArithmeticException("BigInteger divide by zero")
        if (this.signum == 0) return Pair(ZERO, ZERO)
        
        val (qMag, rMag) = divideMagnitude(this.magnitude, other.magnitude)
        
        val qSign = if (qMag.isEmpty()) 0 else this.signum * other.signum
        val rSign = if (rMag.isEmpty()) 0 else this.signum
        
        return Pair(BigInteger(qSign, qMag), BigInteger(rSign, rMag))
    }

    fun mod(m: BigInteger): BigInteger {
        if (m.signum <= 0) throw ArithmeticException("BigInteger modulus must be positive")
        val result = this.remainder(m)
        return if (result.signum < 0) result.add(m) else result
    }

    fun modInverse(m: BigInteger): BigInteger {
        if (m.signum <= 0) throw ArithmeticException("BigInteger modulus must be positive")
        if (m == ONE) return ZERO
        
        var t = ZERO
        var newT = ONE
        var r = m
        var newR = this.mod(m)
        
        while (newR.signum != 0) {
            val quotient = r.divide(newR)
            
            val tempT = t.subtract(quotient.multiply(newT))
            t = newT
            newT = tempT
            
            val tempR = r.subtract(quotient.multiply(newR))
            r = newR
            newR = tempR
        }
        
        if (r.compareTo(ONE) > 0) {
            throw ArithmeticException("BigInteger and Modulus are not coprime")
        }
        if (t.signum < 0) {
            t = t.add(m)
        }
        return t
    }

    fun modPow(exponent: BigInteger, m: BigInteger): BigInteger {
        if (m.signum <= 0) throw ArithmeticException("BigInteger modulus must be positive")
        if (m == ONE) return ZERO
        
        var base = this.mod(m)
        var exp = exponent
        
        if (exp.signum < 0) {
            base = base.modInverse(m)
            exp = exp.abs()
        }
        
        var result = ONE
        while (exp.signum > 0) {
            if (exp.testBit(0)) {
                result = result.multiply(base).mod(m)
            }
            base = base.multiply(base).mod(m)
            exp = exp.shiftRight(1)
        }
        return result
    }

    fun and(other: BigInteger): BigInteger {
        val len = maxOf(this.magnitude.size, other.magnitude.size) + 2
        val a = this.toTwosComplement(len)
        val b = other.toTwosComplement(len)
        val res = IntArray(len) { i -> a[i] and b[i] }
        return fromTwosComplement(res)
    }

    fun or(other: BigInteger): BigInteger {
        val len = maxOf(this.magnitude.size, other.magnitude.size) + 2
        val a = this.toTwosComplement(len)
        val b = other.toTwosComplement(len)
        val res = IntArray(len) { i -> a[i] or b[i] }
        return fromTwosComplement(res)
    }

    fun xor(other: BigInteger): BigInteger {
        val len = maxOf(this.magnitude.size, other.magnitude.size) + 2
        val a = this.toTwosComplement(len)
        val b = other.toTwosComplement(len)
        val res = IntArray(len) { i -> a[i] xor b[i] }
        return fromTwosComplement(res)
    }

    fun not(): BigInteger {
        val len = this.magnitude.size + 2
        val a = this.toTwosComplement(len)
        val res = IntArray(len) { i -> a[i].inv() }
        return fromTwosComplement(res)
    }

    fun shiftLeft(n: Int): BigInteger {
        if (n == 0 || this.signum == 0) return this
        if (n < 0) return shiftRight(-n)
        
        val wordShift = n / 32
        val bitShift = n % 32
        
        val newMag = IntArray(this.magnitude.size + wordShift + 1)
        var carry = 0L
        for (i in this.magnitude.indices) {
            val v = this.magnitude[i].toUInt().toLong()
            val shifted = (v shl bitShift) or carry
            newMag[i + wordShift] = shifted.toInt()
            carry = shifted ushr 32
        }
        if (carry > 0) {
            newMag[newMag.size - 1] = carry.toInt()
        }
        return BigInteger(this.signum, stripLeadingZeros(newMag, newMag.size))
    }

    fun shiftRight(n: Int): BigInteger {
        if (n == 0 || this.signum == 0) return this
        if (n < 0) return shiftLeft(-n)
        
        val wordShift = n / 32
        val bitShift = n % 32

        if (this.signum >= 0) {
            if (wordShift >= this.magnitude.size) return ZERO
            
            val newSize = this.magnitude.size - wordShift
            val newMag = IntArray(newSize)
            var carry = 0L
            for (i in this.magnitude.indices.reversed()) {
                val v = this.magnitude[i].toUInt().toLong()
                val shifted = (v or (carry shl 32)) ushr bitShift
                val targetIdx = i - wordShift
                if (targetIdx >= 0) {
                    newMag[targetIdx] = shifted.toInt()
                }
                carry = v and ((1L shl bitShift) - 1)
            }
            return BigInteger(1, stripLeadingZeros(newMag, newMag.size))
        } else {
            val neededLen = maxOf(1, this.magnitude.size - wordShift + 2)
            val twos = this.toTwosComplement(neededLen + wordShift)
            
            val shiftedTwos = IntArray(neededLen)
            for (i in 0 until neededLen) {
                val idx = i + wordShift
                val v1 = if (idx < twos.size) twos[idx].toUInt().toLong() else 0xFFFFFFFFL
                val v2 = if (idx + 1 < twos.size) twos[idx + 1].toUInt().toLong() else 0xFFFFFFFFL
                val shifted = ((v2 shl 32) or v1) ushr bitShift
                shiftedTwos[i] = shifted.toInt()
            }
            return fromTwosComplement(shiftedTwos)
        }
    }

    fun testBit(n: Int): Boolean {
        require(n >= 0) { "Negative bit address" }
        val wordIdx = n / 32
        val bitIdx = n % 32
        
        if (this.signum >= 0) {
            if (wordIdx >= this.magnitude.size) return false
            return (this.magnitude[wordIdx] and (1 shl bitIdx)) != 0
        } else {
            var carry = 1L
            var twosDigit = 0L
            for (i in 0..wordIdx) {
                val magVal = if (i < magnitude.size) magnitude[i].toUInt().toLong() else 0L
                val inverted = magVal.inv() and 0xFFFFFFFFL
                val sum = inverted + carry
                twosDigit = sum and 0xFFFFFFFFL
                carry = sum ushr 32
            }
            return (twosDigit and (1L shl bitIdx)) != 0L
        }
    }

    fun setBit(n: Int): BigInteger {
        return this.or(ONE.shiftLeft(n))
    }

    fun clearBit(n: Int): BigInteger {
        return this.and(ONE.shiftLeft(n).not())
    }

    private fun toTwosComplement(len: Int): IntArray {
        val result = IntArray(len)
        if (this.signum >= 0) {
            for (i in 0 until minOf(len, magnitude.size)) {
                result[i] = magnitude[i]
            }
        } else {
            var carry = 1L
            for (i in 0 until len) {
                val magVal = if (i < magnitude.size) magnitude[i].toUInt().toLong() else 0L
                val inverted = magVal.inv() and 0xFFFFFFFFL
                val sum = inverted + carry
                result[i] = sum.toInt()
                carry = sum ushr 32
            }
        }
        return result
    }

    companion object {
        val ZERO = BigInteger(0, IntArray(0))
        val ONE = BigInteger(1, intArrayOf(1))
        val TEN = BigInteger(1, intArrayOf(10))

        fun valueOf(value: Long): BigInteger {
            if (value == 0L) return ZERO
            if (value == Long.MIN_VALUE) {
                return BigInteger(-1, intArrayOf(0, 0x80000000.toInt()))
            }
            
            val signum = if (value < 0) -1 else 1
            val absVal = if (value < 0) -value else value
            
            val high = (absVal ushr 32).toInt()
            val low = absVal.toInt()
            
            val mag = if (high == 0) {
                intArrayOf(low)
            } else {
                intArrayOf(low, high)
            }
            return BigInteger(signum, mag)
        }
        
        fun parseString(value: String, radix: Int = 10): BigInteger {
            require(radix in 2..36) { "Radix out of range" }
            val cleanValue = value.trim()
            if (cleanValue.isEmpty()) throw NumberFormatException("Zero-length string")
            
            var sign = 1
            var start = 0
            if (cleanValue[0] == '-') {
                sign = -1
                start = 1
            } else if (cleanValue[0] == '+') {
                start = 1
            }
            
            if (start == cleanValue.length) throw NumberFormatException("Zero-length numeric string")
            
            val radixBig = valueOf(radix.toLong())
            var result = ZERO
            for (i in start until cleanValue.length) {
                val digit = digitVal(cleanValue[i], radix)
                if (digit < 0) throw NumberFormatException("Invalid character: ${cleanValue[i]}")
                result = result.multiply(radixBig).add(valueOf(digit.toLong()))
            }
            
            if (result.signum == 0) return ZERO
            return BigInteger(sign * result.signum, result.magnitude)
        }

        fun fromByteArray(bytes: ByteArray): BigInteger {
            if (bytes.isEmpty()) throw NumberFormatException("Zero-length byte array")
            val signum = if (bytes[0] < 0) -1 else if (bytes.all { it == 0.toByte() }) 0 else 1
            if (signum == 0) return ZERO

            val magnitude: IntArray
            if (signum > 0) {
                var start = 0
                while (start < bytes.size && bytes[start] == 0.toByte()) {
                    start++
                }
                if (start == bytes.size) return ZERO
                val len = bytes.size - start
                
                val intLen = (len + 3) / 4
                magnitude = IntArray(intLen)
                for (i in 0 until len) {
                    val byteVal = bytes[bytes.size - 1 - i].toInt() and 0xFF
                    val intIdx = i / 4
                    val bitShift = (i % 4) * 8
                    magnitude[intIdx] = magnitude[intIdx] or (byteVal shl bitShift)
                }
            } else {
                val inverted = ByteArray(bytes.size) { i -> (bytes[i].toInt().inv() and 0xFF).toByte() }
                
                var carry = 1
                for (i in inverted.indices.reversed()) {
                    val sum = (inverted[i].toInt() and 0xFF) + carry
                    inverted[i] = (sum and 0xFF).toByte()
                    carry = sum ushr 8
                }
                
                var start = 0
                while (start < inverted.size && inverted[start] == 0.toByte()) {
                    start++
                }
                val len = inverted.size - start
                val intLen = (len + 3) / 4
                magnitude = IntArray(intLen)
                for (i in 0 until len) {
                    val byteVal = inverted[inverted.size - 1 - i].toInt() and 0xFF
                    val intIdx = i / 4
                    val bitShift = (i % 4) * 8
                    magnitude[intIdx] = magnitude[intIdx] or (byteVal shl bitShift)
                }
            }
            return BigInteger(signum, stripLeadingZeros(magnitude, magnitude.size))
        }

        private fun compareMagnitude(m1: IntArray, m2: IntArray): Int {
            if (m1.size < m2.size) return -1
            if (m1.size > m2.size) return 1
            for (i in m1.indices.reversed()) {
                val a = m1[i].toUInt()
                val b = m2[i].toUInt()
                if (a < b) return -1
                if (a > b) return 1
            }
            return 0
        }

        private fun addMagnitude(m1: IntArray, m2: IntArray): IntArray {
            var carry = 0L
            val result = IntArray(maxOf(m1.size, m2.size) + 1)
            var i = 0
            while (i < m1.size || i < m2.size) {
                val a = if (i < m1.size) m1[i].toUInt().toLong() else 0L
                val b = if (i < m2.size) m2[i].toUInt().toLong() else 0L
                val sum = a + b + carry
                result[i] = sum.toInt()
                carry = sum ushr 32
                i++
            }
            if (carry > 0) {
                result[i] = carry.toInt()
                return result
            }
            return stripLeadingZeros(result, i)
        }

        private fun subtractMagnitude(m1: IntArray, m2: IntArray): IntArray {
            var borrow = 0L
            val result = IntArray(m1.size)
            for (i in m1.indices) {
                val a = m1[i].toUInt().toLong()
                val b = if (i < m2.size) m2[i].toUInt().toLong() else 0L
                val diff = a - b - borrow
                result[i] = diff.toInt()
                borrow = if (diff < 0) 1L else 0L
            }
            return stripLeadingZeros(result, result.size)
        }

        private fun stripLeadingZeros(mag: IntArray, len: Int): IntArray {
            var actualLen = len
            while (actualLen > 0 && mag[actualLen - 1] == 0) {
                actualLen--
            }
            if (actualLen == len && len == mag.size) return mag
            if (actualLen == 0) return IntArray(0)
            return mag.copyOf(actualLen)
        }

        private fun multiplyMagnitude(m1: IntArray, m2: IntArray): IntArray {
            if (m1.isEmpty() || m2.isEmpty()) return IntArray(0)
            val result = IntArray(m1.size + m2.size)
            for (i in m1.indices) {
                val a = m1[i].toUInt().toLong()
                var carry = 0L
                for (j in m2.indices) {
                    val b = m2[j].toUInt().toLong()
                    val prod = a * b + result[i + j].toUInt().toLong() + carry
                    result[i + j] = prod.toInt()
                    carry = prod ushr 32
                }
                result[i + m2.size] = carry.toInt()
            }
            return stripLeadingZeros(result, result.size)
        }

        private fun divideMagnitude(u: IntArray, v: IntArray): Pair<IntArray, IntArray> {
            if (v.isEmpty()) throw ArithmeticException("BigInteger divide by zero")
            val cmp = compareMagnitude(u, v)
            if (cmp < 0) return Pair(IntArray(0), u)
            if (cmp == 0) return Pair(intArrayOf(1), IntArray(0))
            
            if (v.size == 1) {
                return divideMagnitudeSingleDigit(u, v[0].toUInt())
            }
            return divideMagnitudeKnuth(u, v)
        }

        private fun divideMagnitudeSingleDigit(u: IntArray, divisor: UInt): Pair<IntArray, IntArray> {
            val quotient = IntArray(u.size)
            var remainder = 0L
            val d = divisor.toLong()
            for (i in u.indices.reversed()) {
                val current = (remainder shl 32) or u[i].toUInt().toLong()
                quotient[i] = (current / d).toInt()
                remainder = current % d
            }
            val q = stripLeadingZeros(quotient, quotient.size)
            val r = if (remainder == 0L) IntArray(0) else intArrayOf(remainder.toInt())
            return Pair(q, r)
        }

        private fun divideMagnitudeKnuth(u: IntArray, v: IntArray): Pair<IntArray, IntArray> {
            val n = v.size
            val m = u.size - n
            
            val shift = v[n - 1].toUInt().countLeadingZeroBits()
            val normalizedV = shiftLeftMagnitude(v, shift)
            val normalizedU = shiftLeftMagnitude(u, shift, extraDigit = true)
            
            val q = IntArray(m + 1)
            val vN1 = normalizedV[n - 1].toUInt().toLong()
            val vN2 = normalizedV[n - 2].toUInt().toLong()
            
            for (j in m downTo 0) {
                val uJn = normalizedU[j + n].toUInt().toLong()
                val uJn1 = normalizedU[j + n - 1].toUInt().toLong()
                val uJn2 = if (j + n - 2 >= 0) normalizedU[j + n - 2].toUInt().toLong() else 0L
                
                var qHat = ((uJn shl 32) or uJn1) / vN1
                var rHat = ((uJn shl 32) or uJn1) % vN1
                
                while (qHat >= 4294967296L || (qHat.toULong() * vN2.toULong()) > ((rHat.toULong() shl 32) + uJn2.toULong())) {
                    qHat--
                    rHat += vN1
                    if (rHat >= 4294967296L) break
                }
                
                var borrow = 0L
                for (i in 0 until n) {
                    val prod = qHat * normalizedV[i].toUInt().toLong()
                    val diff = normalizedU[j + i].toUInt().toLong() - (prod and 0xFFFFFFFFL) - borrow
                    normalizedU[j + i] = diff.toInt()
                    borrow = (prod ushr 32) - (diff shr 32)
                }
                val diff2 = normalizedU[j + n].toUInt().toLong() - borrow
                normalizedU[j + n] = diff2.toInt()
                
                if (diff2 < 0) {
                    qHat--
                    var carry = 0L
                    for (i in 0 until n) {
                        val sum = normalizedU[j + i].toUInt().toLong() + normalizedV[i].toUInt().toLong() + carry
                        normalizedU[j + i] = sum.toInt()
                        carry = sum ushr 32
                    }
                    normalizedU[j + n] = (normalizedU[j + n].toUInt().toLong() + carry).toInt()
                }
                
                q[j] = qHat.toInt()
            }
            
            val remainder = shiftRightMagnitude(normalizedU.copyOf(n), shift)
            return Pair(
                stripLeadingZeros(q, q.size),
                stripLeadingZeros(remainder, remainder.size)
            )
        }

        private fun shiftLeftMagnitude(mag: IntArray, shift: Int, extraDigit: Boolean = false): IntArray {
            if (shift == 0) {
                return if (extraDigit) {
                    val res = IntArray(mag.size + 1)
                    mag.copyInto(res)
                    res
                } else {
                    mag.copyOf()
                }
            }
            val n = mag.size
            val resSize = if (extraDigit) n + 1 else n
            val res = IntArray(resSize)
            
            var carry = 0L
            for (i in 0 until n) {
                val v = mag[i].toUInt().toLong()
                val shifted = (v shl shift) or carry
                res[i] = shifted.toInt()
                carry = shifted ushr 32
            }
            if (carry > 0 && extraDigit) {
                res[n] = carry.toInt()
            } else if (carry > 0 && !extraDigit) {
                val expanded = IntArray(n + 1)
                res.copyInto(expanded)
                expanded[n] = carry.toInt()
                return expanded
            }
            return res
        }

        private fun shiftRightMagnitude(mag: IntArray, shift: Int): IntArray {
            if (shift == 0) return mag.copyOf()
            val n = mag.size
            val res = IntArray(n)
            var carry = 0L
            for (i in n - 1 downTo 0) {
                val v = mag[i].toUInt().toLong()
                val shifted = (v or (carry shl 32)) ushr shift
                res[i] = shifted.toInt()
                carry = v and ((1L shl shift) - 1)
            }
            return stripLeadingZeros(res, res.size)
        }

        private fun fromTwosComplement(twos: IntArray): BigInteger {
            if (twos.isEmpty()) return ZERO
            val isNegative = (twos.last() and (1 shl 31)) != 0
            
            if (!isNegative) {
                val mag = stripLeadingZeros(twos, twos.size)
                if (mag.isEmpty()) return ZERO
                return BigInteger(1, mag)
            } else {
                val mag = IntArray(twos.size)
                var carry = 1L
                for (i in twos.indices) {
                    val inverted = twos[i].toUInt().toLong().inv() and 0xFFFFFFFFL
                    val sum = inverted + carry
                    mag[i] = sum.toInt()
                    carry = sum ushr 32
                }
                val finalMag = stripLeadingZeros(mag, mag.size)
                if (finalMag.isEmpty()) {
                    return ZERO
                }
                return BigInteger(-1, finalMag)
            }
        }

        private fun digitVal(ch: Char, radix: Int): Int {
            val d = when (ch) {
                in '0'..'9' -> ch - '0'
                in 'a'..'z' -> ch - 'a' + 10
                in 'A'..'Z' -> ch - 'A' + 10
                else -> -1
            }
            return if (d in 0 until radix) d else -1
        }

        private fun digitToChar(digit: Int): Char {
            return if (digit < 10) {
                '0' + digit
            } else {
                'a' + (digit - 10)
            }
        }
    }
}
