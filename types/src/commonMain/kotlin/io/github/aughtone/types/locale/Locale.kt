package io.github.aughtone.types.locale

/**
 * Represents a specific geographical, political, or cultural region.
 *
 * This class provides a platform-independent way to handle locale information, conforming to
 * IETF BCP 47 standards. It is used to localize data, such as formatting numbers, currencies,
 * and dates, or for selecting language-specific resources.
 *
 * **Preference**: This type is intended for use in multiplatform `commonMain` code to ensure 
 * consistency across targets. While platform-specific types like `java.util.Locale` or
 * framework types like Compose's `Locale` may exist, this version is preferred when 
 * cross-platform interoperability is the primary goal.
 *
 * @property languageCode The ISO 639-1 alpha-2 or alpha-3 language code (e.g., "en", "fr", "zh").
 * @property regionCode The ISO 3166-1 alpha-2 country code or UN M.49 numeric-3 area code (e.g., "US", "FR", "001").
 * @property scriptCode The ISO 15924 alpha-4 script code (e.g., "Latn", "Hans").
 * @property variantCode Any arbitrary value used to indicate a variation of a [Locale] (e.g., "polyton", "1996").
 * @property displayName A human-readable name for the locale (e.g., "English (United States)").
 */
data class Locale(
    val languageCode: String,
    val regionCode: String? = null,
    val scriptCode: String? = null,
    val variantCode: String? = null,
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
         * Returns the current default [Locale] for the platform.
         *
         * This property provides a quick way to access the system's current locale settings.
         * The exact behavior depends on the underlying platform's native implementation.
         * 
         * This property is maintained for backward compatibility and is guaranteed to return
         * a [Locale], falling back to "en" (English) if the system locale cannot be resolved.
         *
         * @see currentNativeLocale
         */
        val current: Locale
            get() = currentNativeLocale("en")!!
    }
}
