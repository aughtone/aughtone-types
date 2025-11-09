package io.github.aughtone.types.financial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents a monetary value with an optional currency.
 *
 * @property value The numerical value of the money.
 * @property currency The currency of the money, or null if no specific currency is defined.
 *
 * @constructor Creates a new Money instance.
 *
 * @see Currency
 * @see zero
 */
@Serializable
data class Money(
    @SerialName("value")
    val value: Double,
    @SerialName("currency")
    val currency: Currency? = null,
) {
    companion object {
        /**
         * Creates a `Money` instance representing zero value, optionally with a specified currency.
         *
         * @param currency The currency to associate with the zero value. If `null`, no specific currency is set.
         * @return A `Money` instance with a value of 0.0 and the specified (or `null`) currency.
         */
        fun zero(currency: Currency? = null) =
            Money(0.0, currency)
    }
}
