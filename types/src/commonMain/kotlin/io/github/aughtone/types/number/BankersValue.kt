package io.github.aughtone.types.number

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor

@Serializable
data class BankersValue(private val cents: Long) : Comparable<BankersValue> {

    operator fun plus(other: BankersValue) = BankersValue(cents + other.cents)
    operator fun minus(other: BankersValue) = BankersValue(cents - other.cents)

    operator fun times(other: BankersValue) =
        BankersValue(bankersDivide(BigInteger.valueOf(cents).multiply(BigInteger.valueOf(other.cents)), HUNDRED))

    operator fun rem(other: BankersValue) = BankersValue(cents % other.cents)

    operator fun div(other: BankersValue): BankersValue {
        if (other.cents == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersDivide(BigInteger.valueOf(cents).multiply(HUNDRED), BigInteger.valueOf(other.cents)))
    }

    operator fun div(other: Int): BankersValue {
        if (other == 0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersDivide(BigInteger.valueOf(cents), BigInteger.valueOf(other.toLong())))
    }

    operator fun div(other: Long): BankersValue {
        if (other == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersDivide(BigInteger.valueOf(cents), BigInteger.valueOf(other)))
    }

    operator fun div(other: Double): BankersValue {
        require(!other.isNaN() && !other.isInfinite()) { "Cannot divide by NaN or Infinite double" }
        if (other == 0.0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other))
    }

    fun toLong() = cents
    fun toDouble() = cents / 100.0

    override fun compareTo(other: BankersValue): Int {
        return cents.compareTo(other.cents)
    }

    companion object {
        private val HUNDRED = BigInteger.valueOf(100)

        fun fromLong(value: Long) = BankersValue(value)
        fun fromInt(value: Int) = BankersValue(value.toLong())

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
