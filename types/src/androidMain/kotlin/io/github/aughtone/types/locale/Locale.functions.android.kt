package io.github.aughtone.types.locale

import android.os.Build
import java.util.Locale as JavaLocale

/**
 * Whether this device's `java.util.Locale` can carry a script. `forLanguageTag`, the only way to give
 * it one, arrived in API 21; before that the class had no script at all.
 */
private val localeCarriesScript: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

/**
 * The platform locale for [locale], keeping its script where the device can.
 *
 * The three-argument constructor has no script parameter, so a locale in a script other than its
 * language's default was named as though it had none: "Inuktitut (Canada)" for iu-Latn-CA, and plain
 * "Chinese" for both zh-Hans and zh-Hant. Only a tag can carry the script. Without a script the
 * constructor stays, because forLanguageTag drops variants it finds ill-formed.
 */
private fun platformLocale(locale: Locale): JavaLocale =
    if (locale.scriptCode != null && localeCarriesScript) {
        JavaLocale.forLanguageTag(locale.languageTag)
    } else {
        JavaLocale(locale.languageCode, locale.regionCode ?: "", locale.variantCode ?: "")
    }

actual fun localeForNative(languageTag: String): Locale? {
    val parsed = parseLocale(languageTag)
    val javaLocale = platformLocale(parsed)
    return Locale(
        languageCode = parsed.languageCode,
        regionCode = parsed.regionCode,
        scriptCode = parsed.scriptCode,
        variantCode = parsed.variantCode,
        // English, not the device language the no-argument form uses: this is the locale's stored
        // English name, and Locale.displayName finds the name in the reader's language itself.
        displayName = javaLocale.getDisplayName(JavaLocale.ENGLISH)
    )
}

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? {
    // Before API 21 the platform cannot name a script at all, so any name it gives a locale with one
    // would be the name of a different locale. Report no answer, and let the caller fall back.
    if (locale.scriptCode != null && !localeCarriesScript) return null
    val target = platformLocale(locale)
    val inLocale = platformLocale(displayIn)
    // An untranslated language subtag is echoed back verbatim, meaning the platform has no
    // CLDR data for it; report null so callers can fall back.
    if (target.getDisplayLanguage(inLocale).equals(locale.languageCode, ignoreCase = true)) return null
    return target.getDisplayName(inLocale).takeIf { it.isNotBlank() }
}

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val defaultLocale = JavaLocale.getDefault()
    val tag = buildString {
        append(defaultLocale.language)
        // Read directly where it exists rather than by reflection: this runs on every
        // Locale.displayName read. Before API 21 a locale carries no script.
        val script = if (localeCarriesScript) defaultLocale.script else ""
        if (script.isNotEmpty()) {
            append("-")
            append(script)
        }
        val country = defaultLocale.country
        if (country.isNotEmpty()) {
            append("-")
            append(country)
        }
        val variant = defaultLocale.variant
        if (variant.isNotEmpty()) {
            append("-")
            append(variant)
        }
    }
    return currentLocaleFrom(tag, fallbackTag)
}
