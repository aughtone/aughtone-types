package io.github.aughtone.types.locale

// Language codes that name a locale this library carries under a different code, mapped to it.
// Kept by hand; these take precedence over the generated three-letter aliases in languageAliasMap.
private val legacyLanguageCodes: Map<String, String> = mapOf(
    // Deprecated ISO 639 codes still returned by some platforms (e.g. java.util.Locale on Android
    // and JVM < 17), mapped to their modern BCP 47 equivalents.
    "iw" to "he",
    "in" to "id",
    "ji" to "yi",
    // Moldavian, deprecated in favour of Romanian.
    "mo" to "ro",
    // Serbo-Croatian, two- and three-letter. CLDR maps both to sr-Latn, and so does this library:
    // plain sr is Cyrillic, so the script comes with the replacement unless the tag names its own.
    "sh" to "sr-Latn",
    "hbs" to "sr-Latn",
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
        val replacement = parts[0].lowercase().let { legacyLanguageCodes[it] ?: languageAliasMap[it] ?: it }
        val language = replacement.substringBefore('-')
        append(replacement.takeIf { scriptSubtagOf(parts) == null } ?: language)
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
 * A tag naming a script also finds a locale stored without one when that locale is written in it, so
 * every shipped locale is found by its own [Locale.languageTag]: `zh-Hant-TW` finds the locale also
 * reached as `zh-TW`. A locale that names no script is written in its language's default, so
 * `fr-Latn-FR` finds `fr-FR` and `iu-Cans-CA` finds `iu-CA`. A script the locale is not written in is
 * not a match, so `zh-Hans-HK` and `iu-Latn-CA` are `null`.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US").
 * @return The exact matching [Locale], or `null` if not found.
 * @see resolveLocale
 * @see parseLocale
 */
fun localeFor(languageTag: String): Locale? {
    val normalized = normalizeLanguageTag(languageTag)
    localeResourceMap[normalized]?.let { return it }
    // Many locales are keyed without the script they carry, as the platforms report them: zh-TW
    // rather than zh-Hant-TW. Their own languageTag includes it, so try the tag without its script
    // and accept the locale only if it is written in that script.
    val parts = normalized.split('-')
    val script = scriptSubtagOf(parts) ?: return null
    val withoutScript = (parts.take(1) + parts.drop(2)).joinToString("-")
    return localeResourceMap[withoutScript]?.takeIf { it.effectiveScriptCode == script }
}

/** The script subtag of a normalized tag split on `-`, which BCP 47 places straight after the language. */
internal fun scriptSubtagOf(parts: List<String>): String? =
    parts.getOrNull(1)?.takeIf { it.length == 4 && it.all(Char::isLetter) }

/**
 * Resolves a [Locale] instance from the resource map using the BCP 47 "lookup" algorithm.
 *
 * This function provides a consistent, platform-independent way to access locale information.
 * It implements a fallback mechanism by progressively removing subtags from the provided
 * language tag. For example, if "en-GB-scouse" is not found, it will try "en-GB", and then "en".
 *
 * A script named in the tag is kept: the fallback stops at language and script, and never answers
 * in a different script from the one asked for. `sr-Latn-ME` finds Latin `sr`, but `sr-Cyrl-RS`
 * does not fall back to it, and `iu-Latn-CA` does not fall back to syllabic `iu`. Nor does it borrow
 * another region to match a script: `zh-Hant-MO` finds Traditional `zh-Hant`, which has no region,
 * rather than `zh-TW` or `zh-HK`. When the fallback finds nothing, the platform lookup decides, as
 * it does for any tag nothing shipped matches: the JVM, Android and Apple targets return a locale
 * keeping the requested script, Windows does so for a locale it knows and returns `null` otherwise,
 * and JavaScript, Wasm and Linux return `null`.
 *
 * A tag with a region but no script is read as naming the script CLDR expects for that language and
 * region, where it differs from the language's default, so `zh-MO` finds Traditional `zh-Hant` rather
 * than Simplified `zh`, and `pa-PK` behaves as `pa-Arab-PK`. A script named in the tag always wins
 * over the one its region implies, and a tag matching a shipped locale exactly returns that locale.
 *
 * The [languageTag] should be a valid IETF BCP 47 language tag.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US", "zh-Hans-CN").
 * @return The best-matching [Locale] instance, or `null` if no match is found.
 * @see localeFor
 * @see currentNativeLocale
 */
fun resolveLocale(languageTag: String): Locale? =
    resolveShippedLocale(languageTag) ?: localeForNative(withRegionScript(normalizeLanguageTag(languageTag)))

/** [resolveLocale] without its final platform lookup: a shipped locale, or `null`. */
internal fun resolveShippedLocale(languageTag: String): Locale? {
    val normalized = normalizeLanguageTag(languageTag)
    localeFor(normalized)?.let { return it }
    var parts = withRegionScript(normalized).split('-')
    // A requested script is part of what was asked for; stepping below it would answer in another one.
    val floor = if (scriptSubtagOf(parts) != null) 2 else 1
    while (parts.size >= floor && parts.first().isNotEmpty()) {
        localeFor(parts.joinToString("-"))?.let { return it }
        parts = parts.dropLast(1)
    }
    return null
}

/**
 * The locale for a tag the device reported, or for [fallbackTag] when it reported none, as every
 * target's [currentNativeLocale] returns it. A tag resolving to a shipped locale gives that locale,
 * unless the match dropped the device's region: then the shipped locale's language and script are
 * kept with the device's region, so `en-ZW` is English in Zimbabwe rather than plain `en`, and still
 * has a country to take a currency from. Any other well-formed tag keeps its own codes and the English
 * name [parseLocale] gives it. `null` when neither tag is well formed.
 */
internal fun currentLocaleFrom(deviceTag: String?, fallbackTag: String?): Locale? {
    // Every Locale.displayName read asks for the current locale, and a sort compares names many
    // times, so the last answer is kept for as long as the device reports the same tag. A change of
    // device language is a different tag and is resolved afresh. Benign race: two threads may both
    // resolve, and either result is correct.
    lastCurrentLocale?.let { if (it.deviceTag == deviceTag && it.fallbackTag == fallbackTag) return it.locale }
    val locale = deviceTag?.let { localeKeepingRegion(it) } ?: fallbackTag?.let { localeKeepingRegion(it) }
    lastCurrentLocale = CurrentLocaleMemo(deviceTag, fallbackTag, locale)
    return locale
}

/** The shipped locale [tag] resolves to, with [tag]'s region kept if the match dropped it. */
private fun localeKeepingRegion(tag: String): Locale? {
    val device = libraryNamedLocale(tag)
    val shipped = resolveShippedLocale(tag) ?: return device
    val region = device?.regionCode ?: return shipped
    if (shipped.regionCode == region) return shipped
    return englishNamedLocale(shipped.languageCode, shipped.scriptCode, region, variant = null)
}

private class CurrentLocaleMemo(val deviceTag: String?, val fallbackTag: String?, val locale: Locale?)

private var lastCurrentLocale: CurrentLocaleMemo? = null

/**
 * [normalized] with the script its region implies written into it, when the tag names no script and
 * the region's script differs from the language's default: `zh-MO` becomes `zh-Hant-MO`. Any other
 * tag comes back unchanged.
 */
private fun withRegionScript(normalized: String): String {
    val parts = normalized.split('-')
    if (parts.size < 2 || scriptSubtagOf(parts) != null) return normalized
    val script = scriptByLanguageRegion["${parts[0]}-${parts[1]}"] ?: return normalized
    return (listOf(parts[0], script) + parts.drop(1)).joinToString("-")
}

/**
 * Parses a BCP 47 language tag into a [Locale] instance.
 *
 * This function first attempts to find an exact matching locale using [localeFor].
 * If a match is found in the internal resource map, it is returned (preserving any
 * pre-defined display names and metadata).
 *
 * If no exact match is found, it normalizes the tag's casing (language lowercase,
 * script Titlecase, region UPPERCASE) and parses it into its components. The new locale's English
 * name is composed from this library's own names for the language, script and region — the shipped
 * locales', then CLDR's English names, bundled — so `kok-IN` is "Konkani (India)" on every target.
 * Where those leave a subtag unnamed, the platform's English name is used if it has one, and the
 * code itself if not: `en-XX` is "English (XX)". [Locale.displayName] still names it in the user's
 * language where anything can.
 *
 * @param languageTag The IETF BCP 47 language tag (e.g., "en-US", "zh-Hans-CN").
 * @return A [Locale] instance representing the provided tag.
 */
fun parseLocale(languageTag: String): Locale =
    localeFor(languageTag) ?: parsedLocale(languageTag)

/** [languageTag] parsed into its codes and given an English name, or `null` if its language subtag is not 2 to 8 letters. */
internal fun libraryNamedLocale(languageTag: String): Locale? =
    parsedLocale(languageTag).takeIf { locale ->
        locale.languageCode.length in 2..8 && locale.languageCode.all { it in 'a'..'z' }
    }

private fun parsedLocale(languageTag: String): Locale {
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
    val variant = if (variants.isEmpty()) null else variants.joinToString("-")
    return englishNamedLocale(language, script, region, variant)
}

/**
 * A locale with these codes and an English name: composed from this library's names where they name
 * every subtag, otherwise the platform's English name if it has one, otherwise the composition with
 * the unnamed subtags left as codes.
 */
private fun englishNamedLocale(language: String, script: String?, region: String?, variant: String?): Locale {
    val (composed, complete) = composedEnglishName(language, script, region, variant)
    val locale = Locale(
        languageCode = language,
        scriptCode = script,
        regionCode = region,
        variantCode = variant,
        displayName = composed,
    )
    if (complete) return locale
    // The bundled names left a code showing; the platform may know it in English.
    val platformName = localizedDisplayNameForNative(locale, englishReader) ?: return locale
    return Locale(language, region, script, variant, displayName = platformName)
}

private val englishReader: Locale by lazy { Locale("en", displayName = "English") }

/**
 * An English name for a locale from this library's own data, in CLDR's "Language (Script, Region,
 * variant)" shape, and whether every subtag was named. Each subtag is named first as the shipped
 * locales name it, so a composed name agrees with the table, then from CLDR's English names
 * ([EnglishSubtagNames]), and failing both by its code.
 */
internal fun composedEnglishName(language: String, script: String?, region: String?, variant: String?): Pair<String, Boolean> {
    val shipped = shippedSubtagNames
    var complete = true
    fun name(code: String, vararg tables: Map<String, String>): String =
        tables.firstNotNullOfOrNull { it[code] } ?: code.also { complete = false }
    val languageName = name(language, shipped.languages, EnglishSubtagNames.languages)
    val qualifiers = listOfNotNull(
        script?.let { name(it, shipped.scripts, EnglishSubtagNames.scripts) },
        region?.let { name(it, shipped.regions, EnglishSubtagNames.regions) },
        variant,
    )
    val composed = if (qualifiers.isEmpty()) languageName else "$languageName (${qualifiers.joinToString(", ")})"
    return composed to complete
}

/** English names for language, script and region subtags, as the shipped locales' display names give them. */
internal class ShippedSubtagNames(
    val languages: Map<String, String>,
    val scripts: Map<String, String>,
    val regions: Map<String, String>,
)

// Read from the shipped display names, which follow "Language (Script, Region)": "Serbian (Latin)",
// "Chinese (Traditional, Hong Kong)", "Spanish (Latin America)". A script is named only where an
// entry spells it out, so am, whose script is implied, names none. ShippedSubtagNamesTest holds every
// entry to the names read here.
internal val shippedSubtagNames: ShippedSubtagNames by lazy {
    val languages = mutableMapOf<String, String>()
    val scripts = mutableMapOf<String, String>()
    val regions = mutableMapOf<String, String>()
    for (locale in localeResourceMap.values.sortedBy { it.languageTag }) {
        val name = locale.fallbackName
        languages.getOrPut(locale.languageCode) { name.substringBefore(" (") }
        if (locale.variantCode != null || !name.endsWith(")") || " (" !in name) continue
        val qualifiers = name.substringAfter(" (").dropLast(1).split(", ")
        val region = locale.regionCode
        val script = locale.scriptCode
        if (region != null) {
            regions.getOrPut(region) { qualifiers.last() }
            if (script != null && qualifiers.size == 2) scripts.getOrPut(script) { qualifiers.first() }
        } else if (script != null && qualifiers.size == 1) {
            scripts.getOrPut(script) { qualifiers.single() }
        }
    }
    ShippedSubtagNames(languages, scripts, regions)
}

/**
 * Retrieves a native [Locale] instance for a given language tag.
 *
 * This function is an `expect` function, requiring a platform-specific implementation
 * to look up locale information using the native APIs of the target platform (e.g., JVM, Android, Apple platforms and Windows).
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
 * browser, the user's locale on Windows, and the `LC_ALL`, `LC_MESSAGES` and `LANG` environment
 * variables on Linux, where the `C` and `POSIX` locales count as reporting nothing. There is no shared implementation behind
 * them, so the tag a platform reports, and whether it reports one at all, differ.
 *
 * What they report is handled the same way everywhere. A tag that resolves to a shipped locale, as
 * [resolveLocale] would find it, gives that locale, keeping the device's region if the shipped one
 * lacks it: `en-ZW` gives English in Zimbabwe, not plain `en`, so [io.github.aughtone.types.financial.Currency.current] still finds a
 * country. Any other well-formed tag keeps the device's own codes, as [parseLocale] parses it: a
 * device set to Konkani in India gives `kok-IN`. So [Locale.current] equals [localeFor] of its own
 * tag whenever that tag is shipped, and its [Locale.displayName] is in the device's language wherever
 * anything can name it there, "कोंकणी (भारत)" for that device on the JVM.
 *
 * @param fallbackTag An optional IETF BCP 47 language tag to fall back to when the platform reports
 *        nothing, handled the same way as a reported tag. Only a malformed one yields `null`.
 * @return The current native [Locale], the [fallbackTag]'s locale if the platform gave nothing, or
 *         `null` if neither gave a well-formed tag.
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
 * Returns a list of [Locale]s whose name contains the specified [name], in English or in the user's
 * language, so "French" and "français" both find `fr`.
 *
 * @param name The name to search for within the locale's English name or its [Locale.displayName].
 * @param ignoreCase `true` to ignore character case when matching. Defaults to `true`.
 * @return A list of matching [Locale]s.
 */
fun localesByName(name: String, ignoreCase: Boolean = true): List<Locale> =
    availableLocales().filter {
        it.fallbackName.contains(name, ignoreCase) || it.displayName.contains(name, ignoreCase)
    }

/**
 * Returns the name of this [Locale] in the language of [displayIn].
 *
 * The name comes from the first of these that has one:
 *
 * 1. **This library's own names.** In English, the shipped locale table's name; in any other
 *    language, the bundled translations, which are in that language's default script, so a reader
 *    in another script, such as `sr-Latn` or `zh-Hant`, skips them. Where they have a name it is the
 *    same on every target.
 * 2. **The platform's name**, from its own CLDR data: `java.util.Locale` on JVM/Android, `NSLocale`
 *    on Apple platforms, `Intl.DisplayNames` on JS/WasmJS, and the bundled full table on Linux and
 *    Windows.
 * 3. **An English name**: the shipped table's, or the one the locale was built with.
 *
 * Steps 1 and 2 are [localizedDisplayNameOrNull]; this adds the English fallback.
 *
 * **Warning:** Where a name comes from the platform, the exact wording may vary between platforms
 * and OS versions, and the English fallback is silent — a returned name may be the English one
 * rather than a translation. Use [localizedDisplayNameOrNull] to tell those apart. See ADR-0005 and
 * ADR-0006 for the rationale and tradeoffs.
 *
 * @param displayIn The locale whose language the name should be rendered in.
 * Defaults to [Locale.current].
 * @return The localized display name, or an English name as a fallback.
 * @see Locale.displayName
 * @see localizedDisplayNameForNative
 */
fun Locale.localizedDisplayName(displayIn: Locale = Locale.current): String =
    localizedDisplayNameOrNull(displayIn) ?: englishName()

/**
 * Returns the name of this locale in the language of [displayIn], or `null` when neither this
 * library nor the platform has one for that pairing.
 *
 * The same lookup as [localizedDisplayName], without the English fallback: this library's own name
 * first, then the platform's. That fallback is the reason this exists: it returns a real English
 * name, so a caller cannot tell a genuine French name from a silent miss, and the miss is not rare.
 *
 * Use this wherever showing the wrong language matters more than showing nothing: to fall back to
 * something of your own, to mark the entry, or to leave it out. Use [localizedDisplayName] when any
 * readable name will do.
 *
 * @param displayIn The locale whose language the name should be rendered in.
 *   Defaults to [Locale.current], which throws if the platform cannot resolve one — pass a locale
 *   explicitly, or [Locale.currentOrNull], if that is not acceptable here.
 * @return The localized name, or `null` if nothing could provide one.
 * @see localizedDisplayName
 */
fun Locale.localizedDisplayNameOrNull(displayIn: Locale = Locale.current): String? {
    // Name the shipped locale this one matches, so a zh-TW built without its script is named as the
    // shipped Traditional zh-TW is, by the bundled tables and the platform alike.
    val shipped = localeFor(languageTag)
    val target = shipped ?: this
    val language = displayIn.languageCode
    val bundled = if (language == "en") {
        shipped?.fallbackName
    } else {
        bundledNamesLanguage(displayIn)?.let { LocaleDisplayNameSupplement.nameFor(it, target.languageTag) }
    }
    return bundled ?: localizedDisplayNameForNative(target, displayIn)
}

/**
 * This locale's English name: the shipped table's when the library ships it, otherwise the name it
 * was built with, which for a parsed locale is the English name [parseLocale] gave it.
 */
internal fun Locale.englishName(): String = localeFor(languageTag)?.fallbackName ?: fallbackName


/**
 * Retrieves the display name of [locale], localized for the language of [displayIn],
 * using the native platform's CLDR data.
 *
 * This is an `expect` function, requiring a platform-specific implementation. Every platform returns
 * `null` for a pair its data cannot name. [Locale.localizedDisplayNameOrNull] consults it only after
 * this library's own names, and [Locale.localizedDisplayName] replaces a miss with the English name.
 *
 * **Linux and Windows are the exception, and do not consult platform data at all.** Linux ships no
 * CLDR, and Windows names a locale only in the user's interface language, so neither can name an
 * arbitrary pair: those targets answer from a bundled table holding the whole matrix. They are
 * therefore no more likely to return `null` than any other platform — before 4.1.0 Linux returned
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
