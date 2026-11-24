package io.github.aughtone.types.locale

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.windows.GetLocaleInfoEx
import platform.windows.GetUserDefaultLocaleName
import platform.windows.IsValidLocaleName
import platform.windows.LOCALE_NAME_MAX_LENGTH
import platform.windows.LOCALE_SLOCALIZEDDISPLAYNAME
import platform.windows.WCHARVar

/**
 * The user's locale as Windows names it, such as `en-US`, or `null` if Windows reports none.
 *
 * This is the locale the user chose for formats, which is the one that carries their country, and so
 * their currency. A name may carry a sort order after an underscore, `de-DE_phoneb`; that is not part
 * of a language tag and is dropped.
 */
@OptIn(ExperimentalForeignApi::class)
private fun userLocaleName(): String? = memScoped {
    val buffer = allocArray<WCHARVar>(LOCALE_NAME_MAX_LENGTH)
    if (GetUserDefaultLocaleName(buffer, LOCALE_NAME_MAX_LENGTH) == 0) return null
    buffer.toKString().substringBefore('_').takeIf { it.isNotEmpty() }
}

/**
 * The name of the locale [localeName] in the user's interface language, or `null` if Windows has none.
 */
@OptIn(ExperimentalForeignApi::class)
private fun windowsDisplayName(localeName: String): String? = memScoped {
    val length = GetLocaleInfoEx(localeName, LOCALE_SLOCALIZEDDISPLAYNAME.toUInt(), null, 0)
    if (length == 0) return null
    val buffer = allocArray<WCHARVar>(length)
    if (GetLocaleInfoEx(localeName, LOCALE_SLOCALIZEDDISPLAYNAME.toUInt(), buffer, length) == 0) return null
    buffer.toKString().takeIf { it.isNotBlank() }
}

@OptIn(ExperimentalForeignApi::class)
actual fun localeForNative(languageTag: String): Locale? {
    val parsed = parseLocale(languageTag)
    // Windows 10 and later answer GetLocaleInfoEx for any well-formed name, with placeholder data for
    // one they do not know, so ask first whether Windows knows the locale, and report a miss instead.
    if (IsValidLocaleName(parsed.languageTag) == 0) return null
    val displayName = windowsDisplayName(parsed.languageTag) ?: return null
    return Locale(
        languageCode = parsed.languageCode,
        regionCode = parsed.regionCode,
        scriptCode = parsed.scriptCode,
        variantCode = parsed.variantCode,
        displayName = displayName,
    )
}

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val tag = userLocaleName() ?: fallbackTag ?: return null
    return resolveLocale(tag) ?: fallbackTag?.let { resolveLocale(it) } ?: parseLocale(tag)
}
