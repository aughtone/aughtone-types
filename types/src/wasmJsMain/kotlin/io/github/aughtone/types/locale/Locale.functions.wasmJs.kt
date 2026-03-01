package io.github.aughtone.types.locale

actual fun localeForNative(languageTag: String): Locale? {
    // Browsers don't have a native API to look up arbitrary locale data.
    // We fall back to the shared resource map.
    return localeFor(languageTag)
}

actual fun currentNativeLocale(fallbackTag: String): Locale {
    // XXX: The kotlin-browser dependency is not resolving correctly for wasmJs.
    // As a temporary workaround, we always return the fallback.
    return getCurrentNativeLocaleImpl(null, fallbackTag)
}
