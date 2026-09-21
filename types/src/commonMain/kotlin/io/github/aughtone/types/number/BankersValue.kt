package io.github.aughtone.types.number

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Represents a monetary value stored internally as an integer number of cents (`Long`).
 *
 * Arithmetic operations on [BankersValue] apply banker's rounding (round half to even) to ensure
 * that fractional cents do not accumulate statistical bias over repeated operations.
 *
 * @property cents The value represented in cents (e.g., `100` represents $1.00).
 */
@Serializable
data class BankersValue(private val cents: Long) : Comparable<BankersValue> {

    /** Adds another [BankersValue] to this value. */
    operator fun plus(other: BankersValue) = BankersValue(cents + other.cents)

    /** Subtracts another [BankersValue] from this value. */
    operator fun minus(other: BankersValue) = BankersValue(cents - other.cents)

    /** Multiplies this value by another [BankersValue] using banker's rounding. */
    operator fun times(other: BankersValue) =
        BankersValue(bankersDivide(BigInteger.valueOf(cents).multiply(BigInteger.valueOf(other.cents)), HUNDRED))

    /** Computes the remainder (modulus) of dividing this value by another [BankersValue]. */
    operator fun rem(other: BankersValue) = BankersValue(cents % other.cents)

    /**
     * Divides this value by another [BankersValue] using banker's rounding.
     *
     * @throws ArithmeticException If [other] is zero.
     */
    operator fun div(other: BankersValue): BankersValue {
        if (other.cents == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersDivide(BigInteger.valueOf(cents).multiply(HUNDRED), BigInteger.valueOf(other.cents)))
    }

    /**
     * Divides this value by an [Int] divisor using banker's rounding.
     *
     * @throws ArithmeticException If [other] is zero.
     */
    operator fun div(other: Int): BankersValue {
        if (other == 0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersDivide(BigInteger.valueOf(cents), BigInteger.valueOf(other.toLong())))
    }

    /**
     * Divides this value by a [Long] divisor using banker's rounding.
     *
     * @throws ArithmeticException If [other] is zero.
     */
    operator fun div(other: Long): BankersValue {
        if (other == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersDivide(BigInteger.valueOf(cents), BigInteger.valueOf(other)))
    }

    /**
     * Divides this value by a [Double] divisor using banker's rounding.
     *
     * @throws IllegalArgumentException If [other] is [Double.NaN] or infinite.
     * @throws ArithmeticException If [other] is zero.
     */
    operator fun div(other: Double): BankersValue {
        require(!other.isNaN() && !other.isInfinite()) { "Cannot divide by NaN or Infinite double" }
        if (other == 0.0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other))
    }

    /** Returns the underlying value in cents as a [Long]. */
    fun toLong() = cents

    /** Returns the value as a [Double] in major currency units (e.g., `100` cents becomes `1.0`). */
    fun toDouble() = cents / 100.0

    override fun compareTo(other: BankersValue): Int {
        return cents.compareTo(other.cents)
    }

    companion object {
        private val HUNDRED = BigInteger.valueOf(100)

        /** Creates a [BankersValue] from an amount in cents as a [Long]. */
        fun fromLong(value: Long) = BankersValue(value)

        /** Creates a [BankersValue] from an amount in cents as an [Int]. */
        fun fromInt(value: Int) = BankersValue(value.toLong())

        /**
         * Converts a major currency unit amount in [Double] (e.g., dollars or euros) to a [BankersValue],
         * applying banker's (half-even) rounding to the nearest cent.
         *
         * @throws IllegalArgumentException If [dollars] is [Double.NaN] or infinite.
         */
        fun fromDouble(dollars: Double): BankersValue {
            require(!dollars.isNaN() && !dollars.isInfinite()) { "Cannot convert NaN or Infinite double" }
            // Route through the decimal string form so half-cent ties are detected exactly.
            val cents = BigDecimal.valueOf(dollars)
                .multiply(BigDecimal(HUNDRED, 0))
                .setScale(0, RoundingMode.HALF_EVEN)
            return BankersValue(cents.unscaledValue.toLong())
        }

        // Exact integer division of num by den with banker's (half-even) rounding.
        private fun bankersDivide(num: BigInteger, den: BigInteger): Long =
            BigDecimal(num, 0).divide(BigDecimal(den, 0), 0, RoundingMode.HALF_EVEN).unscaledValue.toLong()

        private fun bankersRound(num: Double): Long {
            val floor = floor(num)
            val ceil = ceil(num)

            return when {
                num == floor -> floor.toLong()
                (num - floor) < 0.5 -> floor.toLong()
                (num - floor) > 0.5 -> ceil.toLong()
                else -> if (floor % 2.0 == 0.0) floor.toLong() else ceil.toLong()
            }
        }
    }
}
