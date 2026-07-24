package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.Locale
import io.github.aughtone.types.number.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a currency as defined by the ISO 4217 standard.
 *
 * This data class provides a comprehensive representation of a currency, including its
 * alphabetic code, numeric code, decimal precision, and associated metadata.
 *
 * It is primarily used by the [Money] class to handle scaling, rounding, and formatting
 * of monetary values across different regions.
 *
 * @property code The three-letter ISO 4217 alphabetic currency code (e.g., "USD", "EUR", "JPY").
 * @property number The three-digit ISO 4217 numeric currency code (e.g., 840, 978, 392).
 * @property digits The number of decimal digits used to represent the fractional part of the currency.
 *                  This defines the scale factor (`10^digits`) used to convert raw [BigDecimal]
 *                  values into minor units (e.g., 2 for USD cents, 0 for JPY).
 * @property name The full human-readable name of the currency (e.g., "US Dollar", "Euro").
 * @property symbol The symbol used to represent the currency (e.g., "$", "€", "¥").
 * @property obsolete A flag indicating if the currency is no longer in active use.
 * @property replacedBy The ISO 4217 code of the currency that replaced this one, if obsolete.
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
) {
    /**
     * The scale factor used to convert the arbitrary-precision [BigDecimal] value to
     * its decimal representation (e.g., 100.0 for currencies with 2 digits).
     * Calculated as 10 raised to the power of [digits].
     */
    val factor: Double
        get() {
            var f = 1.0
            repeat(digits) { f *= 10.0 }
            return f
        }

    companion object {
        /**
         * Returns the [Currency] associated with the system's current default locale.
         *
         * This property provides a convenient way to retrieve the currency for the user's
         * current region. It relies on the [Locale.current] mapping to determine the
         * correct ISO 4217 code.
         *
         * @throws IllegalStateException if the current locale or its associated currency cannot be determined.
         * @see Locale.current
         */
        val current: Currency
            get() = requireNotNull(getCurrency(Locale.current)) { "Your locale could not be found, or there was no currency mapped to it. Try getCurrency(Locale) or construct your own." }

        /**
         * Retrieves the currency associated with the specified locale.
         *
         * The lookup follows a BCP 47 stepdown: the full language tag is tried first,
         * then candidates with the script and variant subtags dropped. Since the region
         * is what selects a currency, language-region is preferred before the bare
         * language (e.g. `zh-Hans-CN` -> `zh-CN` -> `zh-Hans` -> `zh`).
         *
         * @param locale The locale to look up. Defaults to [Locale.current].
         * @return The [Currency] associated with the locale, or `null` if not found.
         */
        fun getCurrency(locale: Locale = Locale.current): Currency? =
            currencyCodeFor(locale)?.let { currencyFor(it) }

        private fun currencyCodeFor(locale: Locale): String? {
            val language = locale.languageCode
            val script = locale.scriptCode
            val region = locale.regionCode
            val candidates = buildList {
                add(locale.languageTag)
                if (region != null) {
                    if (script != null) add("$language-$script-$region")
                    add("$language-$region")
                }
                if (script != null) add("$language-$script")
                add(language)
            }.distinct()
            for (tag in candidates) {
                localeToCurrencyMap[tag]?.let { return it }
            }
            return null
        }
    }

}
