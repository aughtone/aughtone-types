package io.github.aughtone.types.locale

internal fun normalizeLanguageTag(languageTag: String): String {
    val normalized = languageTag.replace('_', '-')
    val parts = normalized.split('-')
    if (parts.isEmpty() || parts[0].isEmpty()) return ""
    return buildString {
        append(parts[0].lowercase())
        for (i in 1 until parts.size) {
            val part = parts[i]
            append('-')
            when {
                part.length == 4 && part.all { it.isLetter() } -> {
                    append(part.lowercase().replaceFirstChar { it.uppercase() })
                }
                (part.length == 2 && part.all { it.isLetter() }) || (part.length == 3 && part.all { it.isDigit() }) -> {
                    append(part.uppercase())
                }
                else -> {
                    append(part.lowercase())
                }
            }
        }
    }
}

/**
 * Retrieves a [Locale] instance from the internal resource map.
 *
 * This is a strict lookup and does not perform fallback. If an exact match for the
 * provided [languageTag] is not found, this function returns `null`.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US").
 * @return The exact matching [Locale], or `null` if not found.
 * @see resolveLocale
 * @see parseLocale
 */
fun localeFor(languageTag: String): Locale? = localeResourceMap[normalizeLanguageTag(languageTag)]

/**
 * Resolves a [Locale] instance from the resource map using the BCP 47 "lookup" algorithm.
 *
 * This function provides a consistent, platform-independent way to access locale information.
 * It implements a fallback mechanism by progressively removing subtags from the provided
 * language tag. For example, if "en-GB-scouse" is not found, it will try "en-GB", and then "en".
 *
 * The [languageTag] should be a valid IETF BCP 47 language tag.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US", "zh-Hans-CN").
 * @return The best-matching [Locale] instance, or `null` if no match is found.
 * @see localeFor
 * @see currentNativeLocale
 */
fun resolveLocale(languageTag: String): Locale? {
    val normalized = normalizeLanguageTag(languageTag)
    var currentTag = normalized
    while (currentTag.isNotEmpty()) {
        val locale = localeFor(currentTag)
        if (locale != null) {
            return locale
        }
        currentTag = currentTag.substringBeforeLast('-', "")
    }
    return localeForNative(normalized)
}

/**
 * Parses a BCP 47 language tag into a [Locale] instance.
 *
 * This function first attempts to find an exact matching locale using [localeFor].
 * If a match is found in the internal resource map, it is returned (preserving any
 * pre-defined display names and metadata).
 *
 * If no exact match is found, it manually parses the tag into its components and
 * creates a new [Locale] instance.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US", "zh-Hans-CN").
 * @return A [Locale] instance representing the provided tag.
 */
fun parseLocale(languageTag: String): Locale {
    val existing = localeFor(languageTag)
    if (existing != null) return existing

    val parts = languageTag.split('-')
    val language = parts.getOrNull(0) ?: ""
    var script: String? = null
    var region: String? = null
    val variants = mutableListOf<String>()

    for (i in 1 until parts.size) {
        val part = parts[i]
        when {
            part.length == 4 && part.all { it in 'a'..'z' || it in 'A'..'Z' } -> script = part
            (part.length == 2 && part.all { it in 'a'..'z' || it in 'A'..'Z' }) ||
            (part.length == 3 && part.all { it in '0'..'9' }) -> region = part
            else -> variants.add(part)
        }
    }

    return Locale(
        languageCode = language,
        scriptCode = script,
        regionCode = region,
        variantCode = if (variants.isEmpty()) null else variants.joinToString("-"),
        displayName = languageTag
    )
}

/**
 * Retrieves a native [Locale] instance for a given language tag.
 *
 * This function is an `expect` function, requiring a platform-specific implementation
 * to look up locale information using the native APIs of the target platform (e.g., JVM, Android, iOS).
 * On platforms where a native lookup is not possible or practical (like Linux or Web), this function
 * should fall back to using the [resolveLocale] function.
 *
 * **Warning:** The results of this function may vary between platforms due to differences in
 * their underlying locale systems. For consistent results, prefer using [resolveLocale].
 *
 * @param languageTag The IETF BCP 47 language tag.
 * @return The native [Locale] instance for the given tag, or `null` if the tag is invalid or not supported.
 * @see resolveLocale
 */
expect fun localeForNative(languageTag: String): Locale?



/**
 * Retrieves the current default [Locale] for the native platform.
 *
 * This `expect` function is implemented on each platform to query the system's current
 * locale setting. It then delegates to the common [getCurrentNativeLocaleImpl] function
 * to perform the lookup and fallback logic.
 *
 * @param fallbackTag An optional IETF BCP 47 language tag to use as a fallback if the 
 * native locale cannot be resolved.
 * @return The current native [Locale], or the fallback locale if provided and resolved, 
 * otherwise `null`.
 * @see Locale.Companion.getCurrent
 */
expect fun currentNativeLocale(fallbackTag: String? = null): Locale?

/**
 * Returns a list of all [Locale]s supported by the internal resource map.
 * 
 * @return A list of supported [Locale]s.
 */
fun availableLocales(): List<Locale> = localeResourceMap.values.toList()

/**
 * Returns a list of [Locale]s whose display name contains the specified [name].
 *
 * @param name The name to search for within the locale's display name.
 * @param ignoreCase `true` to ignore character case when matching. Defaults to `true`.
 * @return A list of matching [Locale]s.
 */
fun localesByName(name: String, ignoreCase: Boolean = true): List<Locale> =
    availableLocales().filter { it.displayName.contains(name, ignoreCase) }

/**
 * Returns the display name of this [Locale], localized for the language of [displayIn].
 *
 * Resolution is delegated to the platform's own CLDR data via [localizedDisplayNameForNative]:
 * `java.util.Locale` on JVM/Android, `NSLocale` on Apple platforms, and `Intl.DisplayNames`
 * on JS/WasmJS. When the platform has no localized name available (for example on Linux,
 * which ships no native locale-name data, or for unknown language codes), this falls back
 * to the English [Locale.displayName].
 *
 * **Warning:** Because names come from the underlying OS, the exact wording may vary
 * slightly between platforms and OS versions. See ADR 0003 for the rationale and tradeoffs.
 *
 * @param displayIn The locale whose language the name should be rendered in.
 * Defaults to [Locale.current].
 * @return The localized display name, or the English [Locale.displayName] as a fallback.
 * @see localizedDisplayNameForNative
 */
fun Locale.localizedDisplayName(displayIn: Locale = Locale.current): String =
    localizedDisplayNameForNative(this, displayIn) ?: displayName

/**
 * Retrieves the display name of [locale], localized for the language of [displayIn],
 * using the native platform's CLDR data.
 *
 * This is an `expect` function, requiring a platform-specific implementation. Platforms
 * without native locale-name data (like Linux) return `null`, as do all platforms when
 * the language is unknown to their CLDR data.
 *
 * **Warning:** The results of this function may vary between platforms due to differences
 * in their underlying locale systems.
 *
 * @param locale The locale whose name should be produced.
 * @param displayIn The locale whose language the name should be rendered in.
 * @return The localized display name, or `null` when the platform cannot provide one.
 * @see Locale.localizedDisplayName
 */
expect fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String?
