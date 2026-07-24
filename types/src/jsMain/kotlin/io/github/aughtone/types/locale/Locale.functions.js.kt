package io.github.aughtone.types.locale

import kotlinx.browser.window

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val languageTag = try {
        window.navigator.language
    } catch (e: Throwable) {
        null
    }
    val tag = languageTag ?: fallbackTag ?: return null
    return resolveLocale(tag) ?: parseLocale(tag)
}

actual fun localeForNative(languageTag: String): Locale? {
    // Browsers don't have a native API to look up arbitrary locale data.
    // We fall back to the shared resource map.
    return localeFor(languageTag)
}

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? = try {
    val displayInTag = displayIn.languageTag
    val languageTag = locale.languageTag
    val name = js("new Intl.DisplayNames([displayInTag], { type: 'language', fallback: 'none' }).of(languageTag)")
    (name as? String)?.takeIf { it.isNotBlank() }
} catch (e: Throwable) {
    // Intl.DisplayNames is unavailable (older browsers/Node) or the tag is malformed.
    null
}
