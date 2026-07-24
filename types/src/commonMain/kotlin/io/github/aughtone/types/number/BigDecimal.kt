package io.github.aughtone.types.number

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An arbitrary-precision signed decimal number.
 *
 * A `BigDecimal` consists of an arbitrary precision integer [unscaledValue] and a 32-bit
 * integer [scale]. If zero or positive, the scale is the number of digits to the right of
 * the decimal point. If negative, the unscaled value of the number is multiplied by
 * ten to the power of the negation of the scale.
 *
 * @property unscaledValue The arbitrary-precision integer value.
 * @property scale The scale of this decimal (defaults to 0).
 */
@Serializable
data class BigDecimal(
    @SerialName("unscaledValue")
    val unscaledValue: BigInteger,
    @SerialName("scale")
    val scale: Int = 0
) : Comparable<BigDecimal> {

    private constructor(source: BigDecimal) : this(source.unscaledValue, source.scale)

    /**
     * Constructs a `BigDecimal` from its string representation.
     * @param value The string to parse.
     */
    constructor(value: String) : this(parseString(value))

    /**
     * Constructs a `BigDecimal` from a `Double` value, preserving its binary precision.
     * @param value The double value.
     */
    constructor(value: Double) : this(valueOf(value))

    /**
     * Constructs a `BigDecimal` from a `Long` value.
     * @param value The long value.
     */
    constructor(value: Long) : this(BigInteger.valueOf(value), 0)

    override fun compareTo(other: BigDecimal): Int {
        if (this.scale == other.scale) {
            return this.unscaledValue.compareTo(other.unscaledValue)
        }
        val diff = this.scale - other.scale
        return if (diff < 0) {
            val scaledThis = this.unscaledValue.multiply(powerOfTen(-diff))
            scaledThis.compareTo(other.unscaledValue)
        } else {
            val scaledOther = other.unscaledValue.multiply(powerOfTen(diff))
            this.unscaledValue.compareTo(scaledOther)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigDecimal) return false
        return unscaledValue == other.unscaledValue && scale == other.scale
    }

    override fun hashCode(): Int {
        return 31 * unscaledValue.hashCode() + scale
    }

    override fun toString(): String {
        if (unscaledValue.signum == 0 && scale == 0) return "0"
        val signStr = if (unscaledValue.signum < 0) "-" else ""
        val absStr = unscaledValue.negateIfNegative().toString()
        
        if (scale == 0) {
            return signStr + absStr
        } else if (scale > 0) {
            return if (absStr.length > scale) {
                val dotIdx = absStr.length - scale
                signStr + absStr.substring(0, dotIdx) + "." + absStr.substring(dotIdx)
            } else {
                val zeros = "0".repeat(scale - absStr.length)
                signStr + "0." + zeros + absStr
            }
        } else {
            val exp = -scale
            return signStr + absStr + "E+" + exp
        }
    }

    fun add(other: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scale, other.scale)
        val thisScaled = if (this.scale < maxScale) {
            this.unscaledValue.multiply(powerOfTen(maxScale - this.scale))
        } else {
            this.unscaledValue
        }
        val otherScaled = if (other.scale < maxScale) {
            other.unscaledValue.multiply(powerOfTen(maxScale - other.scale))
        } else {
            other.unscaledValue
        }
        return BigDecimal(thisScaled.add(otherScaled), maxScale)
    }

    fun subtract(other: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scale, other.scale)
        val thisScaled = if (this.scale < maxScale) {
            this.unscaledValue.multiply(powerOfTen(maxScale - this.scale))
        } else {
            this.unscaledValue
        }
        val otherScaled = if (other.scale < maxScale) {
            other.unscaledValue.multiply(powerOfTen(maxScale - other.scale))
        } else {
            other.unscaledValue
        }
        return BigDecimal(thisScaled.subtract(otherScaled), maxScale)
    }

    fun multiply(other: BigDecimal): BigDecimal {
        val newUnscaled = this.unscaledValue.multiply(other.unscaledValue)
        val newScale = this.scale + other.scale
        return BigDecimal(newUnscaled, newScale)
    }

    operator fun plus(other: BigDecimal): BigDecimal = add(other)
    operator fun minus(other: BigDecimal): BigDecimal = subtract(other)
    operator fun times(other: BigDecimal): BigDecimal = multiply(other)
    operator fun div(other: BigDecimal): BigDecimal = divide(other)
    operator fun unaryMinus(): BigDecimal = BigDecimal(-this.unscaledValue, this.scale)

    /**
     * Returns the numerical value of this [BigDecimal] as a [Double].
     *
     * @return The double value, which may result in a loss of precision.
     */
    fun toDouble(): Double = toString().toDouble()

    fun divide(divisor: BigDecimal): BigDecimal {
        if (divisor.unscaledValue.signum == 0) {
            throw ArithmeticException("Division by zero")
        }
        if (this.unscaledValue.signum == 0) {
            return BigDecimal(BigInteger.ZERO, this.scale - divisor.scale)
        }
        val (q, r) = this.unscaledValue.divideAndRemainder(divisor.unscaledValue)
        if (r.signum == 0) {
            return BigDecimal(q, this.scale - divisor.scale)
        }

        // The expansion terminates iff the reduced denominator has only factors of 2 and 5.
        val den = divisor.unscaledValue.negateIfNegative()
        var reduced = den.divide(gcd(this.unscaledValue.negateIfNegative(), den))
        var twos = 0
        var fives = 0
        val two = BigInteger.valueOf(2)
        val five = BigInteger.valueOf(5)
        while (true) {
            val (q2, r2) = reduced.divideAndRemainder(two)
            if (r2.signum != 0) break
            reduced = q2
            twos++
        }
        while (true) {
            val (q5, r5) = reduced.divideAndRemainder(five)
            if (r5.signum != 0) break
            reduced = q5
            fives++
        }
        if (reduced != BigInteger.ONE) {
            throw ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result.")
        }
        val extraDigits = maxOf(twos, fives)
        val exact = this.unscaledValue.multiply(powerOfTen(extraDigits)).divide(divisor.unscaledValue)
        return BigDecimal(exact, this.scale - divisor.scale + extraDigits)
    }

    fun divide(other: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        if (other.unscaledValue.signum == 0) {
            throw ArithmeticException("Division by zero")
        }
        val k = scale + other.scale - this.scale
        val num: BigInteger
        val den: BigInteger
        if (k >= 0) {
            num = this.unscaledValue.multiply(powerOfTen(k))
            den = other.unscaledValue
        } else {
            num = this.unscaledValue
            den = other.unscaledValue.multiply(powerOfTen(-k))
        }

        val (q, r) = num.divideAndRemainder(den)
        if (r.signum == 0) {
            return BigDecimal(q, scale)
        }

        val roundedUnscaled = round(q, r, den, roundingMode)
        return BigDecimal(roundedUnscaled, scale)
    }

    fun setScale(newScale: Int): BigDecimal {
        return setScale(newScale, RoundingMode.UNNECESSARY)
    }

    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        if (newScale == this.scale) return this
        if (newScale > this.scale) {
            val diff = newScale - this.scale
            val newUnscaled = this.unscaledValue.multiply(powerOfTen(diff))
            return BigDecimal(newUnscaled, newScale)
        } else {
            val diff = this.scale - newScale
            val den = powerOfTen(diff)
            val (q, r) = this.unscaledValue.divideAndRemainder(den)
            if (r.signum == 0) {
                return BigDecimal(q, newScale)
            }
            if (roundingMode == RoundingMode.UNNECESSARY) {
                throw ArithmeticException("Rounding necessary")
            }
            val roundedUnscaled = round(q, r, den, roundingMode)
            return BigDecimal(roundedUnscaled, newScale)
        }
    }

    fun stripTrailingZeros(): BigDecimal {
        if (unscaledValue.signum == 0) return ZERO
        
        var currentUnscaled = unscaledValue
        var currentScale = scale
        val ten = BigInteger.TEN
        
        while (currentUnscaled.signum != 0) {
            val (q, r) = currentUnscaled.divideAndRemainder(ten)
            if (r.signum == 0) {
                currentUnscaled = q
                currentScale--
            } else {
                break
            }
        }
        return BigDecimal(currentUnscaled, currentScale)
    }

    companion object {
        val ZERO = BigDecimal(BigInteger.ZERO, 0)
        val ONE = BigDecimal(BigInteger.ONE, 0)
        val TEN = BigDecimal(BigInteger.TEN, 0)

        fun valueOf(value: Long, scale: Int = 0): BigDecimal {
            return BigDecimal(BigInteger.valueOf(value), scale)
        }

        fun valueOf(value: Double): BigDecimal {
            return parseString(value.toString())
        }

        fun valueOfExact(value: Double): BigDecimal {
            if (value.isNaN() || value.isInfinite()) {
                throw ArithmeticException("Cannot convert NaN or Infinite double to BigDecimal")
            }
            val bits = value.toRawBits()
            val sign = if ((bits ushr 63) == 0L) 1 else -1
            val expRaw = ((bits ushr 52) and 0x7FFL).toInt()
            val significandRaw = bits and 0xFFFFFFFFFFFFFL
            
            val m: Long
            val e: Int
            if (expRaw == 0) {
                m = significandRaw
                e = -1074
            } else {
                m = significandRaw or 0x10000000000000L
                e = expRaw - 1075
            }
            
            if (m == 0L) return ZERO
            
            var tempM = m
            var tempE = e
            while (tempM % 2L == 0L && tempE < 0) {
                tempM /= 2L
                tempE++
            }
            
            return if (tempE >= 0) {
                val unscaled = BigInteger.valueOf(sign * tempM).shiftLeft(tempE)
                BigDecimal(unscaled, 0)
            } else {
                val unscaled = BigInteger.valueOf(sign * tempM).multiply(powerOfFive(-tempE))
                BigDecimal(unscaled, -tempE)
            }
        }

        fun parseString(value: String): BigDecimal {
            val clean = value.trim()
            val eIdx = clean.indexOfAny(charArrayOf('e', 'E'))
            val decPart: String
            val exp: Int
            if (eIdx >= 0) {
                decPart = clean.substring(0, eIdx)
                exp = clean.substring(eIdx + 1).toInt()
            } else {
                decPart = clean
                exp = 0
            }
            
            val dotIdx = decPart.indexOf('.')
            val unscaledStr: String
            val decScale: Int
            if (dotIdx >= 0) {
                val integerPart = decPart.substring(0, dotIdx)
                val fractionalPart = decPart.substring(dotIdx + 1)
                unscaledStr = integerPart + fractionalPart
                decScale = fractionalPart.length
            } else {
                unscaledStr = decPart
                decScale = 0
            }
            
            val unscaledValue = BigInteger.parseString(unscaledStr)
            val totalScale = decScale - exp
            return BigDecimal(unscaledValue, totalScale)
        }

        private fun powerOfTen(k: Int): BigInteger {
            var result = BigInteger.ONE
            var base = BigInteger.TEN
            var exp = k
            while (exp > 0) {
                if (exp % 2 == 1) {
                    result = result.multiply(base)
                }
                base = base.multiply(base)
                exp /= 2
            }
            return result
        }

        private fun powerOfFive(k: Int): BigInteger {
            var result = BigInteger.ONE
            var base = BigInteger.valueOf(5)
            var exp = k
            while (exp > 0) {
                if (exp % 2 == 1) {
                    result = result.multiply(base)
                }
                base = base.multiply(base)
                exp /= 2
            }
            return result
        }

        private fun gcd(a: BigInteger, b: BigInteger): BigInteger {
            var x = a
            var y = b
            while (y.signum != 0) {
                val t = x.remainder(y)
                x = y
                y = t
            }
            return x
        }

        private fun BigInteger.negateIfNegative(): BigInteger {
            return if (this.signum < 0) this.negate() else this
        }

        private fun BigInteger.negate(): BigInteger {
            return if (this.signum == 0) this else BigInteger(-this.signum, this.magnitude)
        }

        private fun round(
            q: BigInteger,
            r: BigInteger,
            den: BigInteger,
            mode: RoundingMode
        ): BigInteger {
            val sign = if (q.signum != 0) q.signum else (r.signum * den.signum)
            
            val qAbs = q.negateIfNegative()
            val rAbs = r.negateIfNegative()
            val denAbs = den.negateIfNegative()
            
            val increment: Boolean = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN -> {
                    val rTimes2 = rAbs.multiply(BigInteger.valueOf(2))
                    val cmp = rTimes2.compareTo(denAbs)
                    if (cmp > 0) {
                        true
                    } else if (cmp < 0) {
                        false
                    } else {
                        when (mode) {
                            RoundingMode.HALF_UP -> true
                            RoundingMode.HALF_DOWN -> false
                            RoundingMode.HALF_EVEN -> qAbs.testBit(0)
                        }
                    }
                }
                RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")
            }
            
            return if (increment) {
                val one = BigInteger.ONE
                if (sign > 0) q.add(one) else q.subtract(one)
            } else {
                q
            }
        }
    }
}
