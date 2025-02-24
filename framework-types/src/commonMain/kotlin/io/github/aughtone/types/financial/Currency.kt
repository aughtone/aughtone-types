package io.github.aughtone.types.financial

import kotlinx.serialization.Serializable

/**
 * Represents a currency with its code and symbol.
 *
 * This data class holds the essential information about a currency, including its
 * unique three-letter currency code (e.g., "USD", "EUR", "JPY") and its associated
 * symbol (e.g., "$", "€", "¥").
 *
 * @property currencyCode The three-letter ISO 4217 currency code.  Must be uppercase.
 * @property symbol The symbol used to represent the currency.
 *
 * Example usage:
 * ```kotlin
 * val usDollar = Currency("USD", "$")
 * val euro = Currency("EUR", "€")
 * val japaneseYen = Currency("JPY", "¥")
 *
 * println("Currency code for US Dollar: ${usDollar.currencyCode}") // Output: USD
 * println("Symbol for Euro: ${euro.symbol}") // Output: €
 * ```
 */
@Serializable
data class Currency(val currencyCode: String, val symbol: String)
