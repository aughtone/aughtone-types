package io.github.aughtone.types.locale

import io.github.aughtone.types.financial.Currency
import io.github.aughtone.types.financial.currencyFor
import io.github.aughtone.types.financial.localeToCurrencyMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a specific geographical, political, or cultural region, conforming to
 * IETF BCP 47 standards.
 *
 * This class provides a platform-independent way to handle locale information, integrating:
 * - **ISO 639**: Language codes (e.g., "en", "fr").
 * - **ISO 3166**: Country/Region codes (e.g., "US", "FR", "001").
 * - **ISO 15924**: Script codes (e.g., "Latn", "Hans").
 *
 * It is used to localize data, such as formatting numbers, currencies, and dates, or
 * for selecting language-specific resources in a multiplatform context.
 *
 * **Preference**: This type is intended for use in `commonMain` code to ensure consistency
 * across targets. While platform-native types like `java.util.Locale` or framework types
 * like Compose's `Locale` exist, this version is preferred for cross-platform interoperability.
 *
 * @property languageCode The ISO 639-1 alpha-2 or alpha-3 language code.
 * @property regionCode The ISO 3166-1 alpha-2 country code or UN M.49 numeric-3 area code.
 * @property scriptCode The ISO 15924 alpha-4 script code.
 * @property variantCode Any arbitrary value used to indicate a variation of a [Locale].
 * @property displayName A human-readable name for the locale (e.g., "English (United States)").
 */
@Serializable
data class Locale(
    @SerialName("languageCode")
    val languageCode: String,
    @SerialName("regionCode")
    val regionCode: String? = null,
    @SerialName("scriptCode")
    val scriptCode: String? = null,
    @SerialName("variantCode")
    val variantCode: String? = null,
    @SerialName("displayName")
    val displayName: String
) {
    /**
     * The IETF BCP 47 language tag representation of this locale.
     *
     * The tag is constructed by joining the non-null components with hyphens in the following order:
     * `languageCode`, `scriptCode`, `regionCode`, `variantCode`.
     *
     * Examples:
     * - `en` (language only)
     * - `en-US` (language and region)
     * - `zh-Hans` (language and script)
     * - `zh-Hans-CN` (language, script, and region)
     * - `es-419` (language and variant)
     */
    val languageTag: String = buildString {
        append(languageCode)
        if (scriptCode != null) {
            append("-")
            append(scriptCode)
        }
        if (regionCode != null) {
            append("-")
            append(regionCode)
        }
        if (variantCode != null) {
            append("-")
            append(variantCode)
        }
    }

    companion object {
        /**
         * Returns the system's current default [Locale].
         *
         * This property provides a platform-independent way to access the user's active
         * locale settings. It is resolved using native platform APIs.
         *
         * @throws IllegalStateException if the system locale cannot be determined.
         * @see currentNativeLocale
         */
        val current: Locale
            get() = requireNotNull(currentNativeLocale()) { "Could not determine your locale. Try using getLocale(languageTag) or construct your own." }


        /**
         * Returns the [Currency] associated with the provided IETF BCP 4
         */
        fun getLocale(languageTag: String): Currency? =
            localeToCurrencyMap[languageTag]?.let { currencyFor(it) }
    }
}
