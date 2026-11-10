package io.github.aughtone.types.locale

/**
 * The language tag a POSIX environment names for messages, or `null` if it names none.
 *
 * The variables are taken in POSIX precedence: the first of [lcAll], [lcMessages] and [lang] that is
 * set and not empty decides, and the ones behind it are not consulted. A value such as
 * `de_DE.ISO-8859-15@euro` loses its codeset and modifier and becomes `de-DE`. The `C` and `POSIX`
 * locales, with or without a codeset, name no language and give `null`, as does nothing being set.
 *
 * Pure, so it is shared here and tested on every target; the Linux `currentNativeLocale` feeds it
 * the environment.
 */
internal fun posixLocaleTag(lcAll: String?, lcMessages: String?, lang: String?): String? {
    val value = listOf(lcAll, lcMessages, lang).firstOrNull { !it.isNullOrEmpty() } ?: return null
    val name = value.substringBefore('.').substringBefore('@')
    if (name.isEmpty() || name == "C" || name == "POSIX") return null
    return name.replace('_', '-')
}
