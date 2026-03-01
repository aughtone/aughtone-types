package io.github.aughtone.types.locale

/**
 * Returns the current default [Locale] for the platform, with a customizable fallback.
 *
 * This is a convenience function that calls [currentNativeLocale]. It allows you to specify a
 * fallback language tag in case the native locale cannot be determined or is not found in the
 * resource map.
 *
 * @param fallbackTag The IETF BCP 47 language tag to use as a fallback. Defaults to "en".
 * @return The current native [Locale], or the fallback if the lookup fails.
 * @see currentNativeLocale
 * @see Locale.Companion.current
 */
fun Locale.Companion.getCurrent(fallbackTag: String = "en") = currentNativeLocale(fallbackTag = fallbackTag)

/**
 * Constructs an IETF BCP 47 language tag from the [Locale] instance.
 *
 * The tag is built from the locale's components in the following order:
 * - language code
 * - script code
 * - region code
 * - variant code
 *
 * Components are separated by hyphens.
 *
 * @return A BCP 47 language tag string (e.g., "en-US", "zh-Hans-CN").
 */
fun Locale.toLanguageTag(): String {
    return buildString {
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
}
