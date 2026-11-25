package io.github.aughtone.types.locale

/**
 * Which form of a locale's name [localizedDisplayName] returns.
 *
 * CLDR names a regional variant two ways. The standard form composes the language and region,
 * "French (Canada)", and every locale has one. The dialect form uses a dedicated name where CLDR has
 * one, "Canadian French" or "Flemish". It reads more naturally on its own, but only some locales have
 * one, so a list mixes the two shapes and a variant sorts away from its language. The same choice as
 * `Intl.DisplayNames`' `languageDisplay` option and ICU's dialect handling.
 *
 * Never persist or compare a value by its ordinal; use the name.
 */
enum class LanguageDisplay {
    /** "French (Canada)": the language with its script and region in parentheses. */
    Standard,

    /** "Canadian French" where CLDR has a dedicated name for the locale, and [Standard] otherwise. */
    Dialect,
}
