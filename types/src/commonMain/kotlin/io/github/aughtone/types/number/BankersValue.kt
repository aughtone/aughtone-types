package io.github.aughtone.types.number

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor

@Serializable
data class BankersValue(private val cents: Long) : Comparable<BankersValue> {

    operator fun plus(other: BankersValue) = BankersValue(cents + other.cents)
    operator fun minus(other: BankersValue) = BankersValue(cents - other.cents)
    operator fun times(other: BankersValue) = BankersValue(bankersRound(cents.toDouble() * other.cents.toDouble() / 100.0))
    operator fun rem(other: BankersValue) = BankersValue(cents % other.cents)

    operator fun div(other: BankersValue): BankersValue {
        if (other.cents == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other.cents.toDouble()))
    }

    operator fun div(other: Int): BankersValue {
        if (other == 0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other))
    }

    operator fun div(other: Long): BankersValue {
        if (other == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other))
    }

    operator fun div(other: Double): BankersValue {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other))
    }

    fun toLong() = cents
    fun toDouble() = cents / 100.0

    override fun compareTo(other: BankersValue): Int {
        return cents.compareTo(other.cents)
    }

    companion object {
        fun fromLong(value: Long) = BankersValue(value)
        fun fromInt(value: Int) = BankersValue(value.toLong())

        fun fromDouble(dollars: Double): BankersValue {
            return BankersValue(bankersRound(dollars * 100))
        }

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
