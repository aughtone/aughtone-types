package io.github.aughtone.types.financial

import io.github.aughtone.types.number.BankersValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a monetary value, storing the total value in cents as a [Long] to avoid floating-point errors.
 *
 * @property cents The total monetary value in cents.
 * @property currency The currency of the money, or null if no specific currency is defined.
 *
 * @constructor Creates a new Money instance from a [Long] representing cents.
 *
 * @see Currency
 * @see BankersValue
 */
@Serializable
data class Money(
    @SerialName("cents")
    val cents: Long,
    @SerialName("currency")
    val currency: Currency? = null,
) {
    /**
     * Secondary constructor to create a `Money` instance from a [Double].
     * The double is converted to cents using banker's rounding.
     *
     * @param value The monetary value as a [Double].
     * @param currency The optional currency.
     */
    constructor(value: Double, currency: Currency? = null) : this(
        BankersValue.fromDouble(value).toLong(), currency
    )

    /**
     * Adds another `Money` object to this one.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun plus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot add money with different currencies." }
        return Money(this.cents + other.cents, this.currency)
    }

    /**
     * Subtracts another `Money` object from this one.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun minus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot subtract money with different currencies." }
        return Money(this.cents - other.cents, this.currency)
    }

    /**
     * Multiplies this `Money` object by another `Money` object.
     * The currency of the result is preserved from the left-hand operand.
     * Note: Multiplying two monetary values is an unusual operation. This operation multiplies the underlying
     * cent values and scales the result back, effectively `(this * other) / 100`.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun times(other: Money): Money {
        require(this.currency == other.currency) { "Cannot multiply money with different currencies." }
        val resultValue = BankersValue.fromLong(this.cents) * BankersValue.fromLong(other.cents)
        return Money(resultValue.toLong(), this.currency)
    }

    /**
     * Divides this `Money` object by another `Money` object, returning a [Double] ratio.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun div(other: Money): Double {
        require(this.currency == other.currency) { "Cannot divide money with different currencies." }
        if (other.cents == 0L) throw ArithmeticException("Division by zero")
        return this.cents.toDouble() / other.cents.toDouble()
    }

    /**
     * Adds a scalar [Double] value to this `Money` object.
     * The scalar is treated as a dollar amount and converted to cents before addition.
     */
    operator fun plus(scalar: Double): Money {
        val scalarCents = BankersValue.fromDouble(scalar).toLong()
        return Money(this.cents + scalarCents, this.currency)
    }

    /**
     * Subtracts a scalar [Double] value from this `Money` object.
     * The scalar is treated as a dollar amount and converted to cents before subtraction.
     */
    operator fun minus(scalar: Double): Money {
        val scalarCents = BankersValue.fromDouble(scalar).toLong()
        return Money(this.cents - scalarCents, this.currency)
    }

    /**
     * Multiplies this `Money` object by a scalar value.
     * The currency of the result is preserved.
     */
    operator fun times(scalar: Double): Money {
        val resultValue = BankersValue.fromLong(this.cents) * BankersValue.fromDouble(scalar)
        return Money(resultValue.toLong(), this.currency)
    }

    /**
     * Divides this `Money` object by a scalar value.
     * The currency of the result is preserved.
     */
    operator fun div(scalar: Double): Money {
        if (scalar == 0.0) throw ArithmeticException("Division by zero")
        val resultValue = BankersValue.fromLong(this.cents) / scalar
        return Money(resultValue.toLong(), this.currency)
    }

    /**
     * Adds a scalar [Long] value (in cents) to this `Money` object.
     */
    operator fun plus(scalar: Long): Money {
        return Money(this.cents + scalar, this.currency)
    }

    /**
     * Subtracts a scalar [Long] value (in cents) from this `Money` object.
     */
    operator fun minus(scalar: Long): Money {
        return Money(this.cents - scalar, this.currency)
    }

    /**
     * Multiplies this `Money` object by a scalar [Long] value.
     */
    operator fun times(scalar: Long): Money {
        return Money(this.cents * scalar, this.currency)
    }

    /**
     * Divides this `Money` object by a scalar [Long] value.
     * Uses banker's rounding for the result.
     */
    operator fun div(scalar: Long): Money {
        if (scalar == 0L) throw ArithmeticException("Division by zero")
        val resultValue = BankersValue.fromLong(this.cents) / scalar
        return Money(resultValue.toLong(), this.currency)
    }

    companion object {
        /**
         * Creates a `Money` instance representing zero value, optionally with a specified currency.
         *
         * @param currency The currency to associate with the zero value. If `null`, no specific currency is set.
         * @return A `Money` instance with a value of 0 and the specified (or `null`) currency.
         */
        fun zero(currency: Currency? = null) = Money(0L, currency)
    }
}
