package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.Locale
import io.github.aughtone.types.locale.getCurrent
import io.github.aughtone.types.locale.toLanguageTag

/**
 * Retrieves the [Currency] for a given [Locale].
 *
 * This function takes a [Locale] object and uses its language tag to look up the
 * corresponding currency code in the internal `localeToCurrencyMap`.
 * If no specific locale is provided, it defaults to the current native locale.
 *
 * @param locale The [Locale] for which to find the currency. Defaults to the current
 * platform's locale via [Locale.getCurrent].
 * @return The corresponding [Currency] if a mapping is found, otherwise `null`.
 */
fun Currency.Companion.forLocale(locale: Locale = Locale.getCurrent()): Currency? {
    val languageTag = locale.toLanguageTag()

    // Try to find a currency for the full language tag (e.g., "en-US")
    val currencyCode = localeToCurrencyMap[languageTag]

    // The final currency lookup; fallback to USD if even the fallbackTag fails.
    return currencyCode?.let { currencyFor(it) }
}
