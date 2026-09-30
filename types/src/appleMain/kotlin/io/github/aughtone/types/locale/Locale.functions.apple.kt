package io.github.aughtone.types.locale

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localeIdentifier
import platform.Foundation.scriptCode
import platform.Foundation.variantCode

actual fun localeForNative(languageTag: String): Locale? {
    val nsLocale = NSLocale(localeIdentifier = languageTag)

    // A validity check: if the identifier is malformed, getting a display name for it will likely fail.
    // We get the display name from the locale itself.
    val selfDisplayName =
        nsLocale.displayNameForKey(NSLocaleIdentifier, nsLocale.localeIdentifier) ?: return null

    // Get the display name in the current system's locale for user-friendliness.
    val currentDisplayName = NSLocale.currentLocale.displayNameForKey(NSLocaleIdentifier, languageTag)

    return Locale(
        languageCode = nsLocale.languageCode,
        regionCode = nsLocale.countryCode?.takeIf { it.isNotEmpty() },
        scriptCode = nsLocale.scriptCode?.takeIf { it.isNotEmpty() },
        variantCode = nsLocale.variantCode?.takeIf { it.isNotEmpty() },
        displayName = currentDisplayName ?: selfDisplayName
    )
}

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? {
    val displayLocale = NSLocale(localeIdentifier = displayIn.languageTag)
    // An unknown language subtag yields no localized name (or is echoed back verbatim),
    // meaning the platform has no CLDR data for it; report null so callers can fall back.
    val languageName = displayLocale.displayNameForKey(NSLocaleLanguageCode, locale.languageCode)
    if (languageName == null || languageName.equals(locale.languageCode, ignoreCase = true)) return null

    val localized = displayLocale.displayNameForKey(NSLocaleIdentifier, locale.languageTag)
        ?.takeIf { it.isNotBlank() }
        ?: return null

    // That check alone is not enough. When Apple knows the language but carries no data for the
    // display locale, it does not echo the subtag — it quietly renders the English form, so a real
    // English word comes back and the miss is invisible. Comparing against the English rendering is
    // what exposes it, and the comparison cannot mean anything when the display language is itself
    // English. Apple's coverage is not the JVM's: it carries Inuktitut, which the JVM does not.
    if (!displayIn.languageCode.equals("en", ignoreCase = true)) {
        val english = NSLocale(localeIdentifier = "en")
        if (localized == english.displayNameForKey(NSLocaleIdentifier, locale.languageTag)) return null
    }
    return localized
}

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val rawTag = NSLocale.currentLocale.localeIdentifier
    val languageTag = rawTag.substringBefore('@').replace('_', '-')
    return resolveLocale(languageTag) ?: fallbackTag?.let { resolveLocale(it) } ?: parseLocale(languageTag)
}

