@file:OptIn(kotlin.experimental.ExperimentalObjCName::class)

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
    @Throws(IllegalArgumentException::class)
    constructor(@kotlin.native.ObjCName("string") value: String) : this(parseString(value))

    /**
     * Constructs a `BigDecimal` from a `Double` value, preserving its binary precision.
     * @param value The double value.
     */
    @Throws(IllegalArgumentException::class)
    constructor(value: Double) : this(valueOf(value))

    /**
     * Constructs a `BigDecimal` from a `Long` value.
     * @param value The long value.
     */
    constructor(@kotlin.native.ObjCName("integer") value: Long) : this(BigInteger.valueOf(value), 0)

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

        // Past decideByMagnitude the two magnitudes overlap, which bounds the scale difference by the
        // operands' digit counts, so it fits in an Int.
        val diff = checkedScale(this.scale.toLong() - other.scale)
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

        // In Long: a scale near either end of Int would wrap around and invert the comparison.
        val thisLow = minimumDecimalDigits(thisBits).toLong() - this.scale
        val thisHigh = maximumDecimalDigits(thisBits).toLong() - this.scale
        val otherLow = minimumDecimalDigits(otherBits).toLong() - other.scale
        val otherHigh = maximumDecimalDigits(otherBits).toLong() - other.scale

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

    /**
     * This value as `java.math.BigDecimal.toString` writes it: plain notation while the scale is
     * not negative and the adjusted exponent is at least -6, scientific notation otherwise. So
     * `1.25` is `"1.25"`, `0.0000001` is `"1E-7"`, and `1.23E+4` (unscaled 123, scale -2) is
     * `"1.23E+4"`. The adjusted exponent is the exponent of the first digit: `-scale` plus the
     * number of unscaled digits, less one.
     *
     * Scientific notation is what keeps this bounded: a value with a huge scale prints in a few
     * characters rather than one digit per place. Use [toPlainString] for notation without an
     * exponent, and expect it to be as long as the value has places.
     */
    override fun toString(): String {
        val sign = if (unscaledValue.signum < 0) "-" else ""
        val digits = unscaledValue.negateIfNegative().toString()
        if (scale == 0) return sign + digits
        val adjusted = -scale.toLong() + (digits.length - 1)
        if (scale > 0 && adjusted >= -6) return sign + plainDigits(digits)
        return buildString {
            append(sign)
            append(digits[0])
            if (digits.length > 1) append('.').append(digits, 1, digits.length)
            append('E')
            if (adjusted >= 0) append('+')
            append(adjusted)
        }
    }

    /**
     * This value without an exponent, as `java.math.BigDecimal.toPlainString` writes it: `"12300"`
     * for unscaled 123 at scale -2, and `"0.0000001"` for 1 at scale 7. The string is as long as the
     * value has places, so a value with a scale in the millions makes a string in the millions.
     */
    fun toPlainString(): String {
        val sign = if (unscaledValue.signum < 0) "-" else ""
        val digits = unscaledValue.negateIfNegative().toString()
        return when {
            scale == 0 -> sign + digits
            scale < 0 -> sign + (if (unscaledValue.signum == 0) "0" else digits + "0".repeat(-scale))
            else -> sign + plainDigits(digits)
        }
    }

    /** [digits] with the decimal point placed for a positive [scale]. */
    private fun plainDigits(digits: String): String =
        if (digits.length > scale) {
            val point = digits.length - scale
            digits.substring(0, point) + "." + digits.substring(point)
        } else {
            "0." + "0".repeat(scale - digits.length) + digits
        }

    @Throws(ArithmeticException::class)
    fun add(other: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scale, other.scale)
        val thisScaled = if (this.scale < maxScale) {
            this.unscaledValue.multiply(powerOfTen(checkedScale(maxScale.toLong() - this.scale)))
        } else {
            this.unscaledValue
        }
        val otherScaled = if (other.scale < maxScale) {
            other.unscaledValue.multiply(powerOfTen(checkedScale(maxScale.toLong() - other.scale)))
        } else {
            other.unscaledValue
        }
        return BigDecimal(thisScaled.add(otherScaled), maxScale)
    }

    @Throws(ArithmeticException::class)
    fun subtract(other: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scale, other.scale)
        val thisScaled = if (this.scale < maxScale) {
            this.unscaledValue.multiply(powerOfTen(checkedScale(maxScale.toLong() - this.scale)))
        } else {
            this.unscaledValue
        }
        val otherScaled = if (other.scale < maxScale) {
            other.unscaledValue.multiply(powerOfTen(checkedScale(maxScale.toLong() - other.scale)))
        } else {
            other.unscaledValue
        }
        return BigDecimal(thisScaled.subtract(otherScaled), maxScale)
    }

    @Throws(ArithmeticException::class)
    fun multiply(other: BigDecimal): BigDecimal {
        val newUnscaled = this.unscaledValue.multiply(other.unscaledValue)
        val newScale = checkedScale(this.scale.toLong() + other.scale)
        return BigDecimal(newUnscaled, newScale)
    }

    @Throws(ArithmeticException::class)
    operator fun plus(other: BigDecimal): BigDecimal = add(other)
    @Throws(ArithmeticException::class)
    operator fun minus(other: BigDecimal): BigDecimal = subtract(other)
    @Throws(ArithmeticException::class)
    operator fun times(other: BigDecimal): BigDecimal = multiply(other)
    @Throws(ArithmeticException::class)
    operator fun div(other: BigDecimal): BigDecimal = divide(other)
    operator fun unaryMinus(): BigDecimal = BigDecimal(-this.unscaledValue, this.scale)

    /**
     * Returns the numerical value of this [BigDecimal] as a [Double].
     *
     * @return The double value, which may result in a loss of precision.
     */
    fun toDouble(): Double = toString().toDouble()

    /**
     * Divides by [divisor] and requires an exact result.
     *
     * There is no rounding here: if the quotient does not terminate — `1 / 3`, say — this refuses
     * rather than choosing a scale for you. Use the three-argument [divide] to say what scale and
     * rounding you want, which is what [io.github.aughtone.types.financial.Money.div] does.
     *
     * @param divisor The value to divide by.
     * @return The exact quotient.
     * @throws ArithmeticException if [divisor] is zero, or if the quotient has a non-terminating
     *         decimal expansion and so has no exact representation.
     */
    @Throws(ArithmeticException::class)
    fun divide(divisor: BigDecimal): BigDecimal {
        if (divisor.unscaledValue.signum == 0) {
            throw ArithmeticException("Division by zero")
        }
        val quotientScale = checkedScale(this.scale.toLong() - divisor.scale)
        if (this.unscaledValue.signum == 0) {
            return BigDecimal(BigInteger.ZERO, quotientScale)
        }
        val (q, r) = this.unscaledValue.divideAndRemainder(divisor.unscaledValue)
        if (r.signum == 0) {
            return BigDecimal(q, quotientScale)
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
        return BigDecimal(exact, checkedScale(quotientScale.toLong() + extraDigits))
    }

    /**
     * Divides by [other] to the given [scale], rounding as [roundingMode] directs.
     *
     * @param other The value to divide by.
     * @param scale The number of digits after the decimal point in the result.
     * @param roundingMode How to round the digit that falls off the end.
     * @return The quotient at [scale].
     * @throws ArithmeticException if [other] is zero, or if [roundingMode] is
     *         [RoundingMode.UNNECESSARY] and rounding would in fact be needed.
     */
    @Throws(ArithmeticException::class)
    fun divide(other: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        if (other.unscaledValue.signum == 0) {
            throw ArithmeticException("Division by zero")
        }
        val k = scale.toLong() + other.scale - this.scale
        val num: BigInteger
        val den: BigInteger
        if (k >= 0) {
            num = this.unscaledValue.multiply(powerOfTen(checkedScale(k)))
            den = other.unscaledValue
        } else if (-k > maximumDecimalDigits(this.unscaledValue.bitLength()) + 1L) {
            // The divisor dwarfs the dividend, so the quotient at this scale is zero with the whole
            // dividend as remainder. Any denominator over twice the remainder rounds the same way,
            // and building 10^-k itself could need more memory than exists.
            num = this.unscaledValue
            den = powerOfTen(maximumDecimalDigits(this.unscaledValue.bitLength()) + 1)
            if (num.signum == 0) return BigDecimal(BigInteger.ZERO, scale)
            val signed = if (other.unscaledValue.signum < 0) den.negate() else den
            return BigDecimal(round(BigInteger.ZERO, num, signed, roundingMode), scale)
        } else {
            num = this.unscaledValue
            den = other.unscaledValue.multiply(powerOfTen(checkedScale(-k)))
        }

        val (q, r) = num.divideAndRemainder(den)
        if (r.signum == 0) {
            return BigDecimal(q, scale)
        }

        val roundedUnscaled = round(q, r, den, roundingMode)
        return BigDecimal(roundedUnscaled, scale)
    }

    /**
     * Returns this value at [newScale] **without rounding**, which is [RoundingMode.UNNECESSARY].
     *
     * Reducing the scale therefore only succeeds when the digits being dropped are zeros. To drop
     * significant digits, use the two-argument [setScale] and name a rounding mode.
     *
     * @param newScale The scale the result should carry.
     * @return This value at [newScale].
     * @throws ArithmeticException if reaching [newScale] would discard a non-zero digit.
     */
    @Throws(ArithmeticException::class)
    fun setScale(newScale: Int): BigDecimal {
        return setScale(newScale, RoundingMode.UNNECESSARY)
    }

    /**
     * Returns this value at [newScale], rounding as [roundingMode] directs when digits are dropped.
     *
     * @param newScale The scale the result should carry.
     * @param roundingMode How to round when [newScale] is smaller than the current [scale].
     * @return This value at [newScale].
     * @throws ArithmeticException if [roundingMode] is [RoundingMode.UNNECESSARY] and rounding
     *         would in fact be needed.
     */
    @Throws(ArithmeticException::class)
    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        if (newScale == this.scale) return this
        if (newScale > this.scale) {
            val diff = checkedScale(newScale.toLong() - this.scale)
            val newUnscaled = this.unscaledValue.multiply(powerOfTen(diff))
            return BigDecimal(newUnscaled, newScale)
        } else {
            val diff = newScale.toLong().let { this.scale.toLong() - it }
            val digits = maximumDecimalDigits(this.unscaledValue.bitLength()) + 1
            // Dropping more digits than the value has leaves zero with the whole value as remainder;
            // a smaller power of ten than 10^diff rounds the same way and is cheap to build.
            val den = powerOfTen(if (diff > digits) digits else diff.toInt())
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
        
        while (currentUnscaled.signum != 0 && currentScale > Int.MIN_VALUE) {
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

        @Throws(IllegalArgumentException::class)
        fun valueOf(value: Double): BigDecimal {
            return parseString(value.toString())
        }

        /**
         * Converts [value] to the decimal that the [Double] holds **exactly**, binary fraction and
         * all: `valueOfExact(0.1)` is `0.1000000000000000055511151231257827021181583404541015625`.
         *
         * That is almost never what a caller wants for a displayed or stored number — [valueOf]
         * goes through the shortest string that round-trips, giving `0.1`. Reach for this only when
         * the exact binary value matters.
         *
         * @param value The double to convert.
         * @return The exact decimal value of [value].
         * @throws ArithmeticException if [value] is `NaN` or infinite, neither of which is a decimal.
         */
        @Throws(ArithmeticException::class)
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

        @Throws(IllegalArgumentException::class)
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

        /** The scale [value], or an [ArithmeticException] when it does not fit in an Int. */
        private fun checkedScale(value: Long): Int {
            if (value !in Int.MIN_VALUE..Int.MAX_VALUE) throw ArithmeticException("Scale $value is out of range")
            return value.toInt()
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
         * @throws ArithmeticException if [k] is negative.
         */
        private fun powerOfTen(k: Int): BigInteger {
            if (k < 0) throw ArithmeticException("Negative power of ten: $k")
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
