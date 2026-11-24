package io.github.aughtone.types.locale

import java.util.Locale as JavaLocale

actual fun localeForNative(languageTag: String): Locale? {
    val parsed = parseLocale(languageTag)
    // The three-argument constructor has no script parameter, so a locale kept in another script was
    // named like the default one: "Inuktitut (Canada)" for iu-Latn-CA. Only a tag can carry the script.
    // Without a script the constructor stays, because forLanguageTag drops variants it finds ill-formed.
    val javaLocale = if (parsed.scriptCode != null) {
        JavaLocale.forLanguageTag(parsed.languageTag)
    } else {
        JavaLocale(parsed.languageCode, parsed.regionCode ?: "", parsed.variantCode ?: "")
    }
    return Locale(
        languageCode = parsed.languageCode,
        regionCode = parsed.regionCode,
        scriptCode = parsed.scriptCode,
        variantCode = parsed.variantCode,
        displayName = javaLocale.displayName
    )
}

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? {
    val target = JavaLocale(locale.languageCode, locale.regionCode ?: "", locale.variantCode ?: "")
    val inLocale = JavaLocale(displayIn.languageCode, displayIn.regionCode ?: "")
    // An untranslated language subtag is echoed back verbatim, meaning the platform has no
    // CLDR data for it; report null so callers can fall back.
    if (target.getDisplayLanguage(inLocale).equals(locale.languageCode, ignoreCase = true)) return null
    return target.getDisplayName(inLocale).takeIf { it.isNotBlank() }
}

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val defaultLocale = JavaLocale.getDefault()
    val tag = buildString {
        append(defaultLocale.language)
        val script = try {
            val method = defaultLocale.javaClass.getMethod("getScript")
            method.invoke(defaultLocale) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
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
    return resolveLocale(tag) ?: fallbackTag?.let { resolveLocale(it) } ?: parseLocale(tag)
}
