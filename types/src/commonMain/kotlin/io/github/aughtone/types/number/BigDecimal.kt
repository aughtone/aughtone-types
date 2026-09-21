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

    /**
     * Compares this value to [other] by numeric value, ignoring scale, so `2.0` and `2.00` compare
     * equal even though they are not [equals].
     *
     * Most comparisons are settled without aligning the two scales. Aligning means multiplying one
     * side up to the other's scale, which costs work proportional to the operands for a question
     * that is usually decided by the sign or by sheer size — `0.5` is smaller than `1000.00`, and
     * nothing needs to be multiplied to know it. Only when the two are close enough that their
     * decimal magnitudes overlap does this fall through to the exact alignment.
     *
     * @param other The value to compare against.
     * @return A negative number, zero, or a positive number as this value is less than, equal to,
     *   or greater than [other].
     */
    override fun compareTo(other: BigDecimal): Int {
        if (this.scale == other.scale) {
            return this.unscaledValue.compareTo(other.unscaledValue)
        }

        val thisSign = this.unscaledValue.signum
        val otherSign = other.unscaledValue.signum
        if (thisSign != otherSign) return thisSign.compareTo(otherSign)
        if (thisSign == 0) return 0

        decideByMagnitude(other, thisSign)?.let { return it }

        val diff = this.scale - other.scale
        return if (diff < 0) {
            val scaledThis = this.unscaledValue.multiply(powerOfTen(-diff))
            scaledThis.compareTo(other.unscaledValue)
        } else {
            val scaledOther = other.unscaledValue.multiply(powerOfTen(diff))
            this.unscaledValue.compareTo(scaledOther)
        }
    }

    /**
     * Attempts to decide the comparison from decimal magnitude alone, returning `null` when the two
     * are close enough that only exact alignment can separate them.
     *
     * The magnitude of a value is the position of its leading digit — its unscaled digit count less
     * its scale. Digit counts are bracketed from the bit length rather than computed exactly,
     * because an exact count costs about as much as the alignment being avoided. When the brackets
     * are disjoint the answer is certain; when they touch, this gives up rather than guessing.
     *
     * @param other The value being compared against.
     * @param sign The shared sign of both values, which is never zero here.
     * @return The comparison result, or `null` if it could not be decided cheaply.
     */
    private fun decideByMagnitude(other: BigDecimal, sign: Int): Int? {
        val thisBits = this.unscaledValue.bitLength()
        val otherBits = other.unscaledValue.bitLength()

        val thisLow = minimumDecimalDigits(thisBits) - this.scale
        val thisHigh = maximumDecimalDigits(thisBits) - this.scale
        val otherLow = minimumDecimalDigits(otherBits) - other.scale
        val otherHigh = maximumDecimalDigits(otherBits) - other.scale

        // A larger magnitude means a larger absolute value, which for two negatives means smaller.
        if (thisLow > otherHigh) return sign
        if (otherLow > thisHigh) return -sign
        return null
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

        /**
         * The largest power of ten held in [CACHED_POWERS_OF_TEN].
         *
         * Scale differences in practice are small — a currency amount against a rate, a value
         * against a rounding target — so a table this size answers essentially every alignment the
         * arithmetic performs, at a cost of a few kilobytes held once for the life of the process.
         */
        /**
         * The number of decimal digits a value of [bitLength] bits is guaranteed to have at least.
         *
         * The smallest value with this many bits is `2^(bitLength - 1)`, and multiplying by
         * `log10(2)` in integer arithmetic — deliberately rounding the constant down — keeps this a
         * true lower bound rather than an estimate that is occasionally one too high.
         */
        private fun minimumDecimalDigits(bitLength: Int): Int {
            if (bitLength <= 1) return 1
            return ((bitLength - 1) * 301_029L / 1_000_000L).toInt() + 1
        }

        /**
         * The number of decimal digits a value of [bitLength] bits can have at most.
         *
         * The largest value with this many bits is `2^bitLength - 1`, and the constant is rounded
         * up here for the same reason it is rounded down in [minimumDecimalDigits]: the pair has to
         * bracket the true count from both sides for a comparison between brackets to be sound.
         */
        private fun maximumDecimalDigits(bitLength: Int): Int {
            if (bitLength <= 1) return 1
            return (bitLength * 301_030L / 1_000_000L).toInt() + 1
        }

        private const val MAX_CACHED_POWER_OF_TEN = 64

        /**
         * Powers of ten from `10^0` up to `10^`[MAX_CACHED_POWER_OF_TEN], indexed by exponent.
         *
         * Every operation that has to bring two scales together multiplies through a power of ten,
         * and before this table existed each of those calls rebuilt the value from scratch. That is
         * why the table is built eagerly and in one pass: each entry is the previous entry times
         * ten, so the whole thing costs sixty-four small multiplications once, rather than a
         * logarithmic number of increasingly large ones on every call.
         */
        private val CACHED_POWERS_OF_TEN: Array<BigInteger> = run {
            val powers = arrayOfNulls<BigInteger>(MAX_CACHED_POWER_OF_TEN + 1)
            var current = BigInteger.ONE
            powers[0] = current
            for (exponent in 1..MAX_CACHED_POWER_OF_TEN) {
                current = current.multiply(BigInteger.TEN)
                powers[exponent] = current
            }
            @Suppress("UNCHECKED_CAST")
            powers as Array<BigInteger>
        }

        /**
         * Returns `10^`[k], from [CACHED_POWERS_OF_TEN] where possible.
         *
         * Exponents past the table are computed by binary exponentiation, seeded from the largest
         * cached power so the table still does most of the work. They are rare: an exponent above
         * [MAX_CACHED_POWER_OF_TEN] means a scale difference of more than sixty-four decimal
         * places.
         *
         * @param k The exponent, which must not be negative.
         * @return Ten raised to [k].
         */
        private fun powerOfTen(k: Int): BigInteger {
            if (k <= MAX_CACHED_POWER_OF_TEN) return CACHED_POWERS_OF_TEN[k]

            var result = CACHED_POWERS_OF_TEN[MAX_CACHED_POWER_OF_TEN]
            var remaining = k - MAX_CACHED_POWER_OF_TEN
            while (remaining > MAX_CACHED_POWER_OF_TEN) {
                result = result.multiply(CACHED_POWERS_OF_TEN[MAX_CACHED_POWER_OF_TEN])
                remaining -= MAX_CACHED_POWER_OF_TEN
            }
            return result.multiply(CACHED_POWERS_OF_TEN[remaining])
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
