package io.github.aughtone.types.locale

/**
 * The script a language is written in for a region, where that differs from the language's default.
 *
 * A tag with a language and a region but no script still implies one: Chinese in Macau is written in
 * Traditional characters, Punjabi in Pakistan in Arabic script. [resolveLocale] reads the script from
 * here when the tag names none, so `zh-MO` resolves among Traditional locales rather than falling back
 * to Simplified `zh`. A pair whose script is the language's default, the great majority, is left out.
 *
 * Taken from the Unicode CLDR 48 likely-subtags data, through ICU4J 78.3, for every shipped language
 * and ISO 3166 country. Regenerate it when the shipped languages change; `RegionScriptJvmTest` compares
 * it against ICU and fails when they differ.
 */
internal val scriptByLanguageRegion: Map<String, String> = mapOf(
    "az-IQ" to "Arab",
    "az-IR" to "Arab",
    "az-RU" to "Cyrl",
    "ha-CM" to "Arab",
    "ha-SD" to "Arab",
    "kk-AF" to "Arab",
    "kk-CN" to "Arab",
    "kk-IR" to "Arab",
    "kk-MN" to "Arab",
    "ku-AM" to "Cyrl",
    "ku-AZ" to "Cyrl",
    "ku-GE" to "Cyrl",
    "ku-IQ" to "Arab",
    "ku-IR" to "Arab",
    "ku-LB" to "Arab",
    "ku-TM" to "Cyrl",
    "ky-CN" to "Arab",
    "ky-TR" to "Latn",
    "mn-CN" to "Mong",
    "ms-CC" to "Arab",
    "pa-PK" to "Arab",
    "sd-IN" to "Deva",
    "sr-ME" to "Latn",
    "sr-RO" to "Latn",
    "sr-TR" to "Latn",
    "tg-PK" to "Arab",
    "ug-KZ" to "Cyrl",
    "ug-MN" to "Cyrl",
    "uz-AF" to "Arab",
    "uz-CN" to "Cyrl",
    "zh-AU" to "Hant",
    "zh-BN" to "Hant",
    "zh-GB" to "Hant",
    "zh-GF" to "Hant",
    "zh-HK" to "Hant",
    "zh-ID" to "Hant",
    "zh-MO" to "Hant",
    "zh-PA" to "Hant",
    "zh-PF" to "Hant",
    "zh-PH" to "Hant",
    "zh-SR" to "Hant",
    "zh-TH" to "Hant",
    "zh-TW" to "Hant",
    "zh-US" to "Hant",
    "zh-VN" to "Hant",
)
