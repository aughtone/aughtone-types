package io.github.aughtone.types.locale

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val languageTag = posixLocaleTag(
        lcAll = getenv("LC_ALL")?.toKString(),
        lcMessages = getenv("LC_MESSAGES")?.toKString(),
        lang = getenv("LANG")?.toKString(),
    )
    return currentLocaleFrom(languageTag, fallbackTag)
}

actual fun localeForNative(languageTag: String): Locale? {
    // Linux does not provide a standard native API to look up arbitrary locale data without side effects.
    // We fall back to the shared resource map.
    return localeFor(languageTag)
}
