package io.github.aughtone.types.locale

import java.util.Locale as JavaLocale

actual fun localeForNative(languageTag: String): Locale? {
    val parsed = parseLocale(languageTag)
    val javaLocale = JavaLocale(parsed.languageCode, parsed.regionCode ?: "", parsed.variantCode ?: "")
    return Locale(
        languageCode = parsed.languageCode,
        regionCode = parsed.regionCode,
        scriptCode = parsed.scriptCode,
        variantCode = parsed.variantCode,
        displayName = javaLocale.displayName
    )
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
