package io.github.aughtone.types.financial

import io.github.aughtone.types.number.BigDecimal
import io.github.aughtone.types.number.BigInteger
import io.github.aughtone.types.number.RoundingMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a monetary value, storing the value as a [BigDecimal] for arbitrary precision.
 * This allows tracking of sub-minor units (fractions of a cent) while maintaining
 * currency-aware rounding rules.
 *
 * @property value The monetary value as an arbitrary-precision decimal.
 * @property currency The currency of the money. Defaults to [Currency.current].
 */
@Serializable
data class Money(
    @SerialName("value")
    val value: BigDecimal = BigDecimal.ZERO,
    @SerialName("currency")
    val currency: Currency = Currency.current
) {
    /**
     * Returns the value in minor units (e.g., cents for USD), rounded to the currency's standard digits
     * using Banker's Rounding (HALF_EVEN).
     */
    val minorUnits: Long
        get() = value.setScale(currency.digits, RoundingMode.HALF_EVEN).unscaledValue.toLong()

    /**
     * Property for backward compatibility. Use [minorUnits] instead.
     */
    val cents: Long get() = minorUnits

    /**
     * Secondary constructor to create a `Money` instance from minor units (e.g., cents).
     *
     * @param minorUnits The monetary value in the smallest unit of the currency.
     * @param currency The currency. Defaults to [Currency.current].
     */
    constructor(minorUnits: Long, currency: Currency = Currency.current) : this(
        BigDecimal(BigInteger(minorUnits), currency.digits),
        currency
    )

    /**
     * Secondary constructor to create a `Money` instance from a [Double].
     *
     * @param value The monetary value as a [Double].
     * @param currency The currency. Defaults to [Currency.current].
     */
    constructor(value: Double, currency: Currency = Currency.current) : this(BigDecimal.valueOf(value), currency)

    /**
     * Adds another `Money` object to this one.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun plus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot add money with different currencies." }
        return Money(this.value + other.value, this.currency)
    }

    /**
     * Subtracts another `Money` object from this one.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun minus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot subtract money with different currencies." }
        return Money(this.value - other.value, this.currency)
    }

    /**
     * Multiplies this `Money` object by another `Money` object.
     * Note: Multiplying two monetary values is an unusual operation.
     */
    operator fun times(other: Money): Money {
        require(this.currency == other.currency) { "Cannot multiply money with different currencies." }
        return Money(this.value * other.value, this.currency)
    }

    /**
     * Divides this `Money` object by another `Money` object, returning a [Double] ratio.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    operator fun div(other: Money): Double {
        require(this.currency == other.currency) { "Cannot divide money with different currencies." }
        return this.value.toDouble() / other.value.toDouble()
    }

    /**
     * Adds a scalar [Double] value to this `Money` object.
     */
    operator fun plus(scalar: Double): Money {
        return Money(this.value + BigDecimal(scalar), this.currency)
    }

    /**
     * Subtracts a scalar [Double] value from this `Money` object.
     */
    operator fun minus(scalar: Double): Money {
        return Money(this.value - BigDecimal(scalar), this.currency)
    }

    /**
     * Multiplies this `Money` object by a scalar value.
     */
    operator fun times(scalar: Double): Money {
        return Money(this.value * BigDecimal(scalar), this.currency)
    }

    /**
     * Divides this `Money` object by a scalar value.
     */
    operator fun div(scalar: Double): Money {
        return Money(this.value / BigDecimal(scalar), this.currency)
    }

    /**
     * Adds a scalar [Long] value (in minor units) to this `Money` object.
     */
    operator fun plus(scalar: Long): Money {
        return Money(this.value + BigDecimal(io.github.aughtone.types.number.BigInteger(scalar), currency.digits), this.currency)
    }

    /**
     * Subtracts a scalar [Long] value (in minor units) from this `Money` object.
     */
    operator fun minus(scalar: Long): Money {
        return Money(this.value - BigDecimal(io.github.aughtone.types.number.BigInteger(scalar), currency.digits), this.currency)
    }

    /**
     * Multiplies this `Money` object by a scalar [Long] value.
     */
    operator fun times(scalar: Long): Money {
        return Money(this.value * BigDecimal(scalar), this.currency)
    }

    /**
     * Divides this `Money` object by a scalar [Long] value.
     */
    operator fun div(scalar: Long): Money {
        return Money(this.value / BigDecimal(scalar), this.currency)
    }

    /**
     * Converts this `Money` object to a [Double].
     * Note that this may result in a loss of precision.
     */
    fun toDouble(): Double = value.toDouble()

    companion object {
        /**
         * Creates a `Money` instance representing zero value, optionally with a specified currency.
         *
         * @param currency The currency to associate with the zero value. If `null`, no specific currency is set.
         * @return A `Money` instance with a value of 0 and the specified (or `null`) currency.
         */
        fun zero(currency: Currency = Currency.current) = Money(BigDecimal.ZERO, currency)
    }
}
