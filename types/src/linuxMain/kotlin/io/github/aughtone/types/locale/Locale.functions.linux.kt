package io.github.aughtone.types.locale

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun currentNativeLocale(fallbackTag: String): Locale {
    val langEnv = getenv("LANG")?.toKString()
    val localeString = langEnv?.split(".")?.firstOrNull()
    val languageTag = localeString?.replace("_", "-")

    return getCurrentNativeLocaleImpl(languageTag, fallbackTag)
}

actual fun localeForNative(languageTag: String): Locale? {
    // Linux does not provide a standard native API to look up arbitrary locale data without side effects.
    // We fall back to the shared resource map.
    return localeFor(languageTag)
}
