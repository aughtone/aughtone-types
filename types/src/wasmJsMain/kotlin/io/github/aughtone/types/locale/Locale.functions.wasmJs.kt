package io.github.aughtone.types.locale

actual fun localeForNative(languageTag: String): Locale? {
    // Browsers don't have a native API to look up arbitrary locale data.
    // We fall back to the shared resource map.
    return localeFor(languageTag)
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun getNavigatorLanguage(): String? =
    js("typeof window !== 'undefined' && window.navigator ? window.navigator.language : null")

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val languageTag = getNavigatorLanguage()
    val tag = languageTag ?: fallbackTag ?: return null
    return resolveLocale(tag) ?: parseLocale(tag)
}
