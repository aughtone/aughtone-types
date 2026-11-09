package io.github.aughtone.types.locale

// Language codes that name a locale this library carries under a different code, mapped to it.
// Kept by hand; these take precedence over the generated three-letter aliases in languageAliasMap.
private val legacyLanguageCodes: Map<String, String> = mapOf(
    // Deprecated ISO 639 codes still returned by some platforms (e.g. java.util.Locale on Android
    // and JVM < 17), mapped to their modern BCP 47 equivalents.
    "iw" to "he",
    "in" to "id",
    "ji" to "yi",
    // Inuktitut. iku is the ISO 639-2/3 code for the iu macrolanguage itself; ike (Eastern Canadian
    // Inuktitut) and ikt (Inuinnaqtun, Western) are its two ISO 639-3 members. This library carries
    // Inuktitut once, as iu, so all three resolve to it.
    "iku" to "iu",
    "ike" to "iu",
    "ikt" to "iu",
    // ISO 639-2 for Tagalog. CLDR folds both tl and tgl into fil, but this library ships tl and fil
    // as separate locales, so tgl goes to the one it is the code for.
    "tgl" to "tl",
)

// The script a language is written in when its tag names none, so naming it adds nothing:
// iu-Cans-CA is iu-CA. iu, cr and oj mean syllabics here, as they do in CLDR's likely subtags; a
// Latin-script tag such as cr-Latn-CA is a different locale and keeps its script.
private val defaultScripts: Map<String, String> = mapOf(
    "iu" to "Cans",
    "cr" to "Cans",
    "oj" to "Cans",
)

internal fun normalizeLanguageTag(languageTag: String): String {
    val normalized = languageTag.replace('_', '-')
    val parts = normalized.split('-')
    if (parts.isEmpty() || parts[0].isEmpty()) return ""
    return buildString {
        val language = parts[0].lowercase().let { legacyLanguageCodes[it] ?: languageAliasMap[it] ?: it }
        append(language)
        var afterSingleton = false
        for (i in 1 until parts.size) {
            val part = parts[i]
            val segment = when {
                // Everything after a singleton (extension or private-use marker like
                // "u" or "x") stays lowercase per BCP 47 canonical form.
                afterSingleton -> part.lowercase()
                part.length == 1 -> {
                    afterSingleton = true
                    part.lowercase()
                }
                part.length == 4 && part.all { it.isLetter() } -> {
                    val script = part.lowercase().replaceFirstChar { it.uppercase() }
                    // A script subtag directly after the language that is that language's default
                    // carries no information, so it is dropped rather than kept as a second spelling.
                    if (i == 1 && defaultScripts[language] == script) continue
                    script
                }
                (part.length == 2 && part.all { it.isLetter() }) || (part.length == 3 && part.all { it.isDigit() }) ->
                    part.uppercase()
                else -> part.lowercase()
            }
            append('-')
            append(segment)
        }
    }
}

/**
 * Retrieves a [Locale] instance from the internal resource map.
 *
 * This is a strict lookup and does not perform fallback. If an exact match for the
 * provided [languageTag] is not found, this function returns `null`.
 *
 * The tag is normalized first, and that includes the language code: a three-letter or deprecated
 * code for a language with a two-letter one is replaced by it, as BCP 47 requires, so `eng-CA` finds
 * `en-CA` and `cre` finds `cr`. A language with only a three-letter code, such as `hur`, keeps it.
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
 * If no exact match is found, it normalizes the tag's casing (language lowercase,
 * script Titlecase, region UPPERCASE) and manually parses it into its components,
 * creating a new [Locale] instance.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US", "zh-Hans-CN").
 * @return A [Locale] instance representing the provided tag.
 */
fun parseLocale(languageTag: String): Locale {
    val existing = localeFor(languageTag)
    if (existing != null) return existing

    val normalized = normalizeLanguageTag(languageTag)
    val parts = normalized.split('-')
    val language = parts.getOrNull(0) ?: ""
    var script: String? = null
    var region: String? = null
    val variants = mutableListOf<String>()
    var afterSingleton = false

    for (i in 1 until parts.size) {
        val part = parts[i]
        when {
            afterSingleton -> variants.add(part)
            part.length == 1 -> {
                afterSingleton = true
                variants.add(part)
            }
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
        displayName = normalized
    )
}

/**
 * Retrieves a native [Locale] instance for a given language tag.
 *
 * This function is an `expect` function, requiring a platform-specific implementation
 * to look up locale information using the native APIs of the target platform (e.g., JVM, Android, iOS).
 * On platforms where a native lookup is not possible or practical (like Linux or Web), this function
 * should fall back to the strict [localeFor] lookup. Implementations must not call
 * [resolveLocale], which itself delegates to this function and would recurse.
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
 * This `expect` function is implemented once per platform, each querying whatever its system
 * exposes — `java.util.Locale` on the JVM and Android, `NSLocale` on Apple targets, `Intl` in a
 * browser, and the `LC_ALL`, `LC_MESSAGES` and `LANG` environment variables on Linux, where the
 * `C` and `POSIX` locales count as reporting nothing. There is no shared implementation behind
 * them, so the tag a platform reports, and whether it reports one at all, differ.
 *
 * @param fallbackTag An optional IETF BCP 47 language tag to fall back to when the platform reports
 *        nothing. It is resolved the same way any tag is, so an unrecognised one still yields `null`.
 * @return The current native [Locale], the resolved [fallbackTag] if the platform gave nothing, or
 *         `null` if neither could be resolved.
 * @see Locale.current
 * @see Locale.currentOrNull
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
 * slightly between platforms and OS versions, and the English fallback is silent — a returned
 * name may be the English one rather than a translation. Use [localizedDisplayNameOrNull] to tell
 * those apart. See ADR 0003 for the rationale and tradeoffs.
 *
 * @param displayIn The locale whose language the name should be rendered in.
 * Defaults to [Locale.current].
 * @return The localized display name, or the English [Locale.displayName] as a fallback.
 * @see localizedDisplayNameForNative
 */
fun Locale.localizedDisplayName(displayIn: Locale = Locale.current): String =
    localizedDisplayNameOrNull(displayIn) ?: displayName

/**
 * Returns the name of this locale in the language of [displayIn], or `null` when the platform has
 * no localized name for that pairing.
 *
 * The same lookup as [localizedDisplayName], without the English fallback. That fallback is the
 * reason this exists: it returns a real English name, so a caller cannot tell a genuine French name
 * from a silent miss, and the miss is not rare. Coverage depends on the platform's own CLDR data,
 * a small fraction of pairings have none, and Linux ships no data at all — so every name is a miss
 * there and the fallback hides it completely.
 *
 * Use this wherever showing the wrong language matters more than showing nothing: to fall back to
 * something of your own, to mark the entry, or to leave it out. Use [localizedDisplayName] when any
 * readable name will do.
 *
 * @param displayIn The locale whose language the name should be rendered in.
 *   Defaults to [Locale.current], which throws if the platform cannot resolve one — pass a locale
 *   explicitly, or [Locale.currentOrNull], if that is not acceptable here.
 * @return The localized name, or `null` if the platform could not provide one.
 * @see localizedDisplayName
 */
fun Locale.localizedDisplayNameOrNull(displayIn: Locale = Locale.current): String? =
    localizedDisplayNameForNative(this, displayIn)
        ?: LocaleDisplayNameSupplement.nameFor(displayIn.languageCode, languageTag)

/**
 * Retrieves the display name of [locale], localized for the language of [displayIn],
 * using the native platform's CLDR data.
 *
 * This is an `expect` function, requiring a platform-specific implementation. Every platform returns
 * `null` for a pair its data cannot name, which is what [Locale.localizedDisplayNameOrNull] reports
 * and what [Locale.localizedDisplayName] replaces with the English name.
 *
 * **Linux is the exception, and does not consult platform data at all.** It ships no CLDR, so there
 * is nothing to query: that target answers from a bundled table holding the whole matrix. It is
 * therefore no more likely to return `null` than any other platform — before 4.1.0 it returned
 * `null` for everything.
 *
 * **Warning:** The results of this function vary between platforms, and not slightly. Of the pairs
 * every platform can name, a browser and a native platform disagree on the wording of roughly 40%,
 * so `nl-BE` may be "Nederlands (België)" or "Vlaams" depending on where the code runs. Compare
 * locales, never their rendered names, and keep rendered names out of cache keys and snapshot tests.
 *
 * @param locale The locale whose name should be produced.
 * @param displayIn The locale whose language the name should be rendered in.
 * @return The localized display name, or `null` when the platform cannot provide one.
 * @see Locale.localizedDisplayName
 */
expect fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String?
