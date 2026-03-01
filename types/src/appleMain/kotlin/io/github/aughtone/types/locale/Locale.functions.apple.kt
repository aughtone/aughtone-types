package io.github.aughtone.types.locale

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleIdentifier
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

actual fun currentNativeLocale(fallbackTag: String): Locale {
    val languageTag = NSLocale.currentLocale.localeIdentifier
    return getCurrentNativeLocaleImpl(languageTag, fallbackTag)
}
