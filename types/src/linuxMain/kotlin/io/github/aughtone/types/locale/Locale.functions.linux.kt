package io.github.aughtone.types.locale

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val langEnv = getenv("LANG")?.toKString()
    val localeString = langEnv?.split(".")?.firstOrNull()
    val languageTag = localeString?.replace("_", "-")
    val tag = languageTag ?: fallbackTag ?: return null
    return resolveLocale(tag) ?: parseLocale(tag)
}

actual fun localeForNative(languageTag: String): Locale? {
    // Linux does not provide a standard native API to look up arbitrary locale data without side effects.
    // We fall back to the shared resource map.
    return localeFor(languageTag)
}

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? =
    // Linux ships no CLDR, so there is no platform answer to prefer and nothing to supplement:
    // the bundled table IS the platform here. It carries the whole matrix rather than the gaps,
    // and it ships only in this target's artifact.
    LocaleDisplayNameTable.nameFor(displayIn.languageCode, locale.languageTag)
