package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.Locale
import io.github.aughtone.types.number.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a currency as defined by the ISO 4217 standard.
 *
 * This class provides a comprehensive representation of a currency, including its
 * alphabetic code, numeric code, decimal precision, and associated metadata.
 *
 * It is primarily used by the [Money] class to handle scaling, rounding, and formatting
 * of monetary values across different regions.
 *
 * **Identity is the ISO 4217 [code].** ISO 4217 makes the alphabetic code the identity of a
 * currency; [name] and [symbol] are how it is presented, and [number], [digits], [obsolete] and
 * [replacedBy] are facts about the currency the code already names. Two instances carrying `"USD"`
 * describe the same money however they were built — from the bundled resource map, from a platform
 * lookup, or decoded from a payload someone else wrote with a different `name` or `symbol` — so they
 * compare equal, sit in one bucket of a `Set`, and act as one map key. This is the same principle the
 * URI types and [Locale] follow: equality is what the governing specification says identifies the
 * value, not a field-by-field comparison. Compare the individual properties when a presentation
 * difference is what you actually care about.
 *
 * **Codes are uppercased on the way in**, so `"usd"` is `USD`, [replacedBy] included, however the
 * value was made, including when it is decoded.
 *
 * This is not a data class: `copy` could change the code and keep a name and symbol that belong to
 * another currency, and positional destructuring of seven fields means nothing. Build one from the
 * companion's `invoke`, which reads like a constructor call, or look one up with [currencyFor].
 *
 * @property number The three-digit ISO 4217 numeric currency code (e.g., 840, 978, 392).
 * @property digits The number of decimal digits used to represent the fractional part of the currency.
 *                  This defines the scale factor (`10^digits`) used to convert raw [BigDecimal]
 *                  values into minor units (e.g., 2 for USD cents, 0 for JPY).
 * @property name The full human-readable name of the currency (e.g., "US Dollar", "Euro").
 * @property symbol The symbol used to represent the currency (e.g., "$", "€", "¥").
 * @property obsolete A flag indicating if the currency is no longer in active use.
 */
@Serializable
// @PublishedApi internal rather than private, so code compiled against 4.2's public data-class
// constructor still links; Kotlin source outside the library builds one through the companion's invoke.
class Currency @PublishedApi internal constructor(
    // The wire carries the codes as they were written; the public properties below uppercase them,
    // so a payload with "usd" decodes to "USD" without a custom serializer.
    @SerialName("code")
    private val encodedCode: String,
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
    private val encodedReplacedBy: String? = null,
) {
    /** The three-letter ISO 4217 alphabetic currency code, uppercase (e.g., "USD", "EUR", "JPY"). */
    @Transient
    val code: String = encodedCode.uppercase()

    /** The ISO 4217 code of the currency that replaced this one, uppercase, if obsolete. */
    @Transient
    val replacedBy: String? = encodedReplacedBy?.takeIf { it.isNotBlank() }?.uppercase()

    /** Two currencies are equal when their [code] is, and nothing else takes part; see [Currency]. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Currency) return false
        return code == other.code
    }

    /**
     * Hashes the ISO 4217 [code] alone, so that currencies equal under [equals] land in the same
     * bucket. Hashing any other property would put two equal currencies in different buckets and
     * every hash-based collection would misbehave.
     */
    override fun hashCode(): Int = code.hashCode()

    /** The [code]. */
    override fun toString(): String = code

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
         * Creates a [Currency]. Codes are uppercased; see [Currency].
         *
         * @param code The three-letter ISO 4217 alphabetic currency code.
         * @param number The three-digit ISO 4217 numeric currency code.
         * @param digits The number of decimal digits in the currency's minor unit.
         * @param name The full human-readable name of the currency.
         * @param symbol The symbol used to represent the currency.
         * @param obsolete Whether the currency is no longer in active use.
         * @param replacedBy The ISO 4217 code of the currency that replaced this one, if obsolete.
         */
        operator fun invoke(
            code: String,
            number: Int,
            digits: Int,
            name: String,
            symbol: String,
            obsolete: Boolean = false,
            replacedBy: String? = null,
        ): Currency = Currency(
            encodedCode = code,
            number = number,
            digits = digits,
            name = name,
            symbol = symbol,
            obsolete = obsolete,
            encodedReplacedBy = replacedBy,
        )

        /**
         * Returns the [Currency] associated with the system's current default locale.
         *
         * This property provides a convenient way to retrieve the currency for the user's
         * current region. It relies on the [Locale.current] mapping to determine the
         * correct ISO 4217 code.
         *
         * @throws IllegalArgumentException if the current locale cannot be determined, or it has no
         *   country to take a currency from, such as a bare `en`. Use [currentOrNull] to receive
         *   `null` instead.
         * @see currentOrNull
         * @see Locale.current
         */
        val current: Currency
            get() = requireNotNull(getCurrency(Locale.current)) { "Your locale could not be found, or there was no currency mapped to it. Try getCurrency(Locale) or construct your own." }

        /**
         * Returns the currency for the user's current region, or `null` when it cannot be
         * determined.
         *
         * The same lookup as [current], reported rather than thrown, and `null` covers both ways it
         * can fail: the platform could not resolve a locale at all, or it resolved one with no
         * country to take a currency from. Those are different problems, so use [Locale.currentOrNull] first
         * if you need to tell them apart.
         *
         * @return The current [Currency], or `null` if it could not be determined.
         */
        val currentOrNull: Currency?
            get() = Locale.currentOrNull?.let { getCurrency(it) }

        /**
         * Retrieves the currency associated with the specified locale.
         *
         * The currency comes from the locale's country alone, so the language and script play no
         * part: `en-IN` and `ta-IN` are both `INR`. A locale with no country, such as `en`, has no
         * currency to infer and gives `null`, as does a region without one, such as `AQ` or `419`.
         *
         * @param locale The locale to look up. Defaults to [Locale.current].
         * @return The [Currency] in use in the locale's country, or `null` if there is none to infer.
         */
        fun getCurrency(locale: Locale = Locale.current): Currency? =
            locale.regionCode?.let { regionToCurrencyMap[it] }?.let { currencyFor(it) }
    }

}
