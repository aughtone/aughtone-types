package io.github.aughtone.types.locale

/**
 * The script each shipped language is written in when its locale names none.
 *
 * Most shipped locales carry no `scriptCode`, because the platforms report them without one: `fr-FR`
 * rather than `fr-Latn-FR`. Such a locale is still written in a script — its language's default — and
 * comparing a requested script against it needs to know which. Without this, `iu-CA` could not be told
 * apart from Latin-script Inuktitut, and `fr-Latn-FR` could not be matched to `fr-FR`.
 *
 * Taken from the Unicode CLDR likely-subtags data, for every language that has a shipped locale with no
 * script, and for Chinese, whose locales all name one, because the bundled names for Chinese are in
 * this script and [bundledNamesLanguage] has to know it. Wyandot (`wya`) has no likely-subtags entry
 * and is written in Latin. Regenerate this when the shipped locale set changes; `ScriptFallbackTest`
 * fails when a language is missing.
 */
internal val defaultScriptByLanguage: Map<String, String> = mapOf(
    "af" to "Latn",
    "ar" to "Arab",
    "atj" to "Latn",
    "az" to "Latn",
    "be" to "Cyrl",
    "bg" to "Cyrl",
    "bla" to "Latn",
    "ca" to "Latn",
    "cay" to "Latn",
    "chp" to "Latn",
    "cr" to "Cans",
    "crg" to "Latn",
    "cs" to "Latn",
    "da" to "Latn",
    "de" to "Latn",
    "dgr" to "Latn",
    "el" to "Grek",
    "en" to "Latn",
    "es" to "Latn",
    "et" to "Latn",
    "eu" to "Latn",
    "fa" to "Arab",
    "fi" to "Latn",
    "fil" to "Latn",
    "fr" to "Latn",
    "gl" to "Latn",
    "gwi" to "Latn",
    "ha" to "Latn",
    "hai" to "Latn",
    "he" to "Hebr",
    "hi" to "Deva",
    "hr" to "Latn",
    "hu" to "Latn",
    "hur" to "Latn",
    "hy" to "Armn",
    "id" to "Latn",
    "ig" to "Latn",
    "ik" to "Latn",
    "is" to "Latn",
    "it" to "Latn",
    "iu" to "Cans",
    "ja" to "Jpan",
    "jv" to "Latn",
    "ka" to "Geor",
    "kk" to "Cyrl",
    "ko" to "Kore",
    "ku" to "Latn",
    "kwk" to "Latn",
    "lil" to "Latn",
    "lt" to "Latn",
    "lv" to "Latn",
    "mic" to "Latn",
    "mk" to "Cyrl",
    "moe" to "Latn",
    "moh" to "Latn",
    "ms" to "Latn",
    "nb" to "Latn",
    "nl" to "Latn",
    "nn" to "Latn",
    "no" to "Latn",
    "nuk" to "Latn",
    "oj" to "Cans",
    "oka" to "Latn",
    "one" to "Latn",
    "pl" to "Latn",
    "pqm" to "Latn",
    "pt" to "Latn",
    "ro" to "Latn",
    "ru" to "Cyrl",
    "shs" to "Latn",
    "sk" to "Latn",
    "sl" to "Latn",
    "so" to "Latn",
    "sq" to "Latn",
    "squ" to "Latn",
    "sr" to "Cyrl",
    "str" to "Latn",
    "su" to "Latn",
    "sv" to "Latn",
    "sw" to "Latn",
    "th" to "Thai",
    "tl" to "Latn",
    "tr" to "Latn",
    "uk" to "Cyrl",
    "vi" to "Latn",
    "wya" to "Latn",
    "yo" to "Latn",
    "zh" to "Hans",
    "zu" to "Latn",
)

/**
 * The script this locale is written in: its own [Locale.scriptCode] if it names one, otherwise its
 * language's default from [defaultScriptByLanguage], or `null` if neither is known.
 */
internal val Locale.effectiveScriptCode: String?
    get() = scriptCode ?: defaultScriptByLanguage[languageCode]

/**
 * The display language to look this reader up under in the bundled name tables, or `null` when the
 * bundled names cannot serve it.
 *
 * The bundled names for a language are written in that language's default script, from CLDR, so
 * the Serbian names are Cyrillic and the Chinese names Simplified. A reader in another script —
 * `sr-Latn`, `zh-Hant` — would get names it did not ask for, so it gets none here and the platform,
 * then English, answers instead. A language whose default script is not recorded is served as is.
 */
internal fun bundledNamesLanguage(reader: Locale): String? {
    val language = reader.languageCode
    val tableScript = defaultScriptByLanguage[language] ?: return language
    return language.takeIf { reader.effectiveScriptCode == tableScript }
}
