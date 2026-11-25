package io.github.aughtone.types.locale

/**
 * Names [locale] in the language of [displayIn] from the bundled table, on Linux and Windows.
 *
 * Neither platform has an answer to prefer here. Linux ships no CLDR at all. Windows names a locale
 * only in the user's interface language, so it cannot name one in an arbitrary display language.
 * The bundled table is therefore the platform on both: it carries the whole matrix rather than the
 * gaps, and it ships only in these targets' artifacts. Its names are in each language's default
 * script, so a reader in another script gets none; see [bundledNamesLanguage].
 */
actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? =
    bundledNamesLanguage(displayIn)?.let { LocaleDisplayNameTable.nameFor(it, locale.languageTag) }
