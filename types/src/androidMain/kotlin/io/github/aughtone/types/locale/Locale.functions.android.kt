package io.github.aughtone.types.locale

import java.util.Locale as JavaLocale
import java.util.MissingResourceException

actual fun localeForNative(languageTag: String): Locale? {
    return try {
        val javaLocale = JavaLocale.forLanguageTag(languageTag)
        // isO3Language will throw a MissingResourceException if the locale is not valid
        javaLocale.isO3Language
        Locale(
            languageCode = javaLocale.language,
            regionCode = javaLocale.country.takeIf { it.isNotEmpty() },
            scriptCode = javaLocale.script.takeIf { it.isNotEmpty() },
            variantCode = javaLocale.variant.takeIf { it.isNotEmpty() },
            displayName = javaLocale.displayName
        )
    } catch (e: MissingResourceException) {
        null
    }
}

actual fun currentNativeLocale(fallbackTag: String): Locale {
    val languageTag = JavaLocale.getDefault().toLanguageTag()
    return getCurrentNativeLocaleImpl(languageTag, fallbackTag)
}
