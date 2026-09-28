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

/**
 * Calls `Intl.DisplayNames` without the `new` operator, and with the tags as function parameters.
 *
 * Both details are load-bearing, and each hid a silent failure.
 *
 * Kotlin/JS parses a `js(...)` snippet and re-emits it rather than passing it through verbatim, and
 * a `new` expression does not survive that: the call reaches the browser without it and fails with
 * "Constructor Intl.DisplayNames requires 'new'". `Reflect.construct` constructs without the
 * operator, so there is nothing to lose. It is preferred over a `Function` factory, which a strict
 * Content-Security-Policy would refuse.
 *
 * The snippet is emitted into a scope where the compiler has renamed locals, so it can only
 * reference names it controls. A function whose whole body is the snippet has its parameters in
 * scope under their own names; interpolating captured locals throws `ReferenceError`.
 *
 * Both failures were invisible because the caller catches `Throwable`: every lookup returned null
 * and every name silently fell back to English on this target. The gap scan in the tests is what
 * found it.
 */
private fun intlDisplayNameOrNull(displayInTag: String, languageTag: String): String? =
    js("Reflect.construct(Intl.DisplayNames, [[displayInTag], { type: 'language', fallback: 'none' }]).of(languageTag)")

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? = try {
    intlDisplayNameOrNull(displayIn.languageTag, locale.languageTag)?.takeIf { it.isNotBlank() }
} catch (e: Throwable) {
    // Intl.DisplayNames is unavailable (older browsers/Node) or the tag is malformed.
    null
}
