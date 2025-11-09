package io.github.aughtone.types.financial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a currency with detailed information, including its code, number,
 * decimal digits, name, associated countries, and symbol.
 *
 * This data class provides a comprehensive representation of a currency, going
 * beyond just the code and symbol. It includes:
 * - **code:** The three-letter ISO 4217 currency code (e.g., "USD", "EUR", "JPY").
 * - **number:** The three-digit numeric ISO 4217 currency code.
 * - **digits:** The number of decimal digits typically used with this currency.
 * - **name:** The full name of the currency (e.g., "US Dollar", "Euro").
 * - **countries:** A list of countries where this currency is used.
 * - **symbol:** The symbol used to represent the currency (e.g., "$", "€", "¥").
 * - **obsolete:** A flag indicating if the currency is obsolete.
 * - **replacedBy:** The currency code of the currency that replaced this one, if obsolete.
 *
 * @property code The three-letter ISO 4217 currency code.
 * @property number The three-digit numeric ISO 4217 currency code.
 * @property digits The number of decimal digits typically used with this currency.
 * @property name The full name of the currency.
 * @property countries A list of countries where this currency is used. Defaults to an empty list.
 * @property symbol The symbol used to represent the currency.
 * @property obsolete A flag indicating if the currency is obsolete. Defaults to false.
 * @property replacedBy The currency code of the currency that replaced this one, if obsolete. Defaults to null.
 *
 * Example usage:
 * ```kotlin
 * val usDollar = Currency("USD", 840, 2, "US Dollar", listOf("United States"), "$")
 * val euro = Currency("EUR", 978, 2, "Euro", listOf("Austria", "Belgium", "Germany"), "€")
 *
 * println("US Dollar code: ${usDollar.code}") // Output: USD
 * println("Euro number: ${euro.number}") // Output: 978
 * println("Countries using US Dollar: ${usDollar.countries}") // Output: [United States]
 */
@Serializable
data class Currency(
    @SerialName("code")
    val code: String,
    @SerialName("number")
    val number: Int,
    @SerialName("digits")
    val digits: Int,
    @SerialName("name")
    val name: String,
    @SerialName("symbol")
    val symbol: String,
    @SerialName("obsolete")
    val obsolete: Boolean = false,
    @SerialName("replacedBy")
    val replacedBy: String? = null,
)
