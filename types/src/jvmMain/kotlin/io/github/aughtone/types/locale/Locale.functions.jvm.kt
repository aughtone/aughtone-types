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
        // English, not the device language the no-argument form uses: this is the locale's stored
        // English name, and Locale.displayName finds the name in the reader's language itself.
        displayName = javaLocale.getDisplayName(JavaLocale.ENGLISH)
    )
}

actual fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String? {
    // Built from the tag, not from the three-argument constructor: that constructor has no script
    // parameter, so a script-bearing locale silently lost it and was named as though it had none —
    // "Amharisch" for am-Ethi where every other platform says "Amharisch (Äthiopisch)". It looked
    // like the platforms disagreeing about wording, and was us discarding a subtag.
    val target = JavaLocale.forLanguageTag(locale.languageTag)
    val inLocale = JavaLocale.forLanguageTag(displayIn.languageTag)
    // An untranslated language subtag is echoed back verbatim, meaning the JVM has no
    // CLDR data for it at all; report null so callers can fall back.
    if (target.getDisplayLanguage(inLocale).equals(locale.languageCode, ignoreCase = true)) return null

    val localized = target.getDisplayName(inLocale).takeIf { it.isNotBlank() } ?: return null

    // That check alone is not enough. When the JVM knows the language but has no data for the
    // display locale, it does not echo the subtag — it quietly renders the root form, which is
    // English, so a real English word comes back and the miss is invisible. Comparing against the
    // root rendering is what exposes it. The comparison cannot mean anything when the display
    // language is itself English, because root and English are the same text.
    if (!displayIn.languageCode.equals("en", ignoreCase = true) &&
        localized == target.getDisplayName(JavaLocale.ROOT)
    ) {
        return null
    }
    return localized
}

actual fun currentNativeLocale(fallbackTag: String?): Locale? {
    val defaultLocale = JavaLocale.getDefault()
    val tag = buildString {
        append(defaultLocale.language)
        // Read directly, not by reflection: this runs on every Locale.displayName read, and the JVM
        // target is 17, where getScript always exists.
        val script = defaultLocale.script
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
