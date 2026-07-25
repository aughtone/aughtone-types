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
     * Adds another `Money` object to this one. Currencies are matched by ISO 4217 [Currency.code],
     * so instances sourced from the resource map and from platform-native lookups are compatible.
     * The result keeps this instance's [currency].
     * @throws IllegalArgumentException if the currency codes do not match.
     */
    operator fun plus(other: Money): Money {
        require(this.currency.code == other.currency.code) { "Cannot add money with different currencies." }
        return Money(this.value + other.value, this.currency)
    }

    /**
     * Subtracts another `Money` object from this one. Currencies are matched by ISO 4217
     * [Currency.code]. The result keeps this instance's [currency].
     * @throws IllegalArgumentException if the currency codes do not match.
     */
    operator fun minus(other: Money): Money {
        require(this.currency.code == other.currency.code) { "Cannot subtract money with different currencies." }
        return Money(this.value - other.value, this.currency)
    }

    /**
     * Multiplies this `Money` object by another `Money` object. Currencies are matched by
     * ISO 4217 [Currency.code].
     * Note: Multiplying two monetary values is an unusual operation.
     */
    operator fun times(other: Money): Money {
        require(this.currency.code == other.currency.code) { "Cannot multiply money with different currencies." }
        return Money(this.value * other.value, this.currency)
    }

    /**
     * Divides this `Money` object by another `Money` object, returning a [Double] ratio.
     * Currencies are matched by ISO 4217 [Currency.code].
     * @throws IllegalArgumentException if the currency codes do not match.
     */
    operator fun div(other: Money): Double {
        require(this.currency.code == other.currency.code) { "Cannot divide money with different currencies." }
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
     *
     * The result is exact when the quotient terminates; otherwise it is rounded with
     * Banker's Rounding (HALF_EVEN) at a scale of `max(value.scale, currency.digits) + 2`,
     * keeping two guard digits of sub-minor precision.
     */
    operator fun div(scalar: Double): Money {
        return Money(dividedValue(BigDecimal(scalar)), this.currency)
    }

    /**
     * Adds a scalar [Long] value to this `Money` object.
     *
     * **Warning:** the [Long] operand is interpreted as **minor units** (e.g., cents for USD):
     * `Money(1000L, usd) + 250L` is $10.00 + $2.50. This differs from [times] and [div],
     * where a [Long] is a dimensionless multiplier. Prefer [plusMinorUnits] for clarity.
     */
    operator fun plus(scalar: Long): Money = plusMinorUnits(scalar)

    /**
     * Subtracts a scalar [Long] value from this `Money` object.
     *
     * **Warning:** the [Long] operand is interpreted as **minor units** (e.g., cents for USD).
     * This differs from [times] and [div], where a [Long] is a dimensionless multiplier.
     * Prefer [minusMinorUnits] for clarity.
     */
    operator fun minus(scalar: Long): Money = minusMinorUnits(scalar)

    /**
     * Adds the given number of **minor units** (e.g., cents for USD) to this `Money` object.
     */
    fun plusMinorUnits(minorUnits: Long): Money {
        return Money(this.value + BigDecimal(BigInteger(minorUnits), currency.digits), this.currency)
    }

    /**
     * Subtracts the given number of **minor units** (e.g., cents for USD) from this `Money` object.
     */
    fun minusMinorUnits(minorUnits: Long): Money {
        return Money(this.value - BigDecimal(BigInteger(minorUnits), currency.digits), this.currency)
    }

    /**
     * Multiplies this `Money` object by a **dimensionless** [Long] multiplier:
     * `Money(1000L, usd) * 2L` is $20.00. Note that this differs from [plus]/[minus],
     * where a [Long] is a minor-unit amount.
     */
    operator fun times(scalar: Long): Money {
        return Money(this.value * BigDecimal(scalar), this.currency)
    }

    /**
     * Divides this `Money` object by a **dimensionless** [Long] divisor. Note that this
     * differs from [plus]/[minus], where a [Long] is a minor-unit amount.
     *
     * The result is exact when the quotient terminates; otherwise it is rounded with
     * Banker's Rounding (HALF_EVEN) at a scale of `max(value.scale, currency.digits) + 2`,
     * keeping two guard digits of sub-minor precision.
     */
    operator fun div(scalar: Long): Money {
        return Money(dividedValue(BigDecimal(scalar)), this.currency)
    }

    private fun dividedValue(divisor: BigDecimal): BigDecimal = try {
        this.value / divisor
    } catch (e: ArithmeticException) {
        if (divisor.unscaledValue.signum == 0) throw e
        this.value.divide(
            divisor,
            maxOf(this.value.scale, currency.digits) + 2,
            RoundingMode.HALF_EVEN
        )
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
