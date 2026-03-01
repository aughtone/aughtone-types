package io.github.aughtone.types.locale

/**
 * Retrieves a [Locale] instance from the resource map using the BCP 47 "lookup" algorithm.
 *
 * This function provides a consistent, platform-independent way to access locale information.
 * It implements a fallback mechanism by progressively removing subtags from the provided
 * language tag. For example, if "en-GB-scouse" is not found, it will try "en-GB", and then "en".
 *
 * The [languageTag] should be a valid IETF BCP 47 language tag.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US", "zh-Hans-CN").
 * @return The best-matching [Locale] instance, or `null` if no match is found.
 * @see localeForNative
 * @see currentNativeLocale
 */
fun localeFor(languageTag: String): Locale? {
    var currentTag = languageTag
    while (currentTag.isNotEmpty()) {
        val locale = localeResourceMap[currentTag]
        if (locale != null) {
            return locale
        }
        currentTag = currentTag.substringBeforeLast('-', "")
    }
    return null
}

/**
 * Retrieves a native [Locale] instance for a given language tag.
 *
 * This function is an `expect` function, requiring a platform-specific implementation
 * to look up locale information using the native APIs of the target platform (e.g., JVM, Android, iOS).
 * On platforms where a native lookup is not possible or practical (like Linux or Web), this function
 * should fall back to using the [localeFor] function.
 *
 * **Warning:** The results of this function may vary between platforms due to differences in
 * their underlying locale systems. For consistent results, prefer using [localeFor].
 *
 * @param languageTag The IETF BCP 47 language tag.
 * @return The native [Locale] instance for the given tag, or `null` if the tag is invalid or not supported.
 * @see localeFor
 */
expect fun localeForNative(languageTag: String): Locale?

/**
 * The core, testable implementation for retrieving the current native locale.
 *
 * It takes an optional native language tag and attempts to find a match using the
 * BCP 47 lookup algorithm. If no match is found, it uses the fallback tag.
 *
 * @param nativeTag The language tag from the native platform, or null if unavailable.
 * @param fallbackTag The fallback language tag.
 * @return The determined [Locale].
 */
internal fun getCurrentNativeLocaleImpl(nativeTag: String?, fallbackTag: String): Locale {
    return nativeTag?.let { localeFor(it) } ?: localeFor(fallbackTag)!!
}

/**
 * Retrieves the current default [Locale] for the native platform.
 *
 * This `expect` function is implemented on each platform to query the system's current
 * locale setting. It then delegates to the common [getCurrentNativeLocaleImpl] function
 * to perform the lookup and fallback logic.
 *
 * @param fallbackTag The IETF BCP 47 language tag to use as a fallback if the native locale
 * cannot be resolved. The default value is "en".
 * @return The current native [Locale], or the fallback locale if the lookup fails.
 * @see Locale.Companion.getCurrent
 */
expect fun currentNativeLocale(fallbackTag: String = "en"): Locale
