package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.Locale
/**
 * Returns the [Currency] instance for the given [currencyCode], or `null` if no such currency exists.
 *
 * The [currencyCode] should be a valid ISO 4217 currency code (e.g., "USD", "EUR", "JPY").
 *
 * @param currencyCode The ISO 4217 currency code.
 * @return The [Currency] instance for the given code, or `null` if the code is invalid.
 */
fun currencyFor(currencyCode: String): Currency? = currencyResourceMap[currencyCode]

/**
 * Returns the native [Currency] instance for the given [currencyCode], or `null` if no such currency exists natively.
 *
 * This function is expected to be implemented by platform-specific code to retrieve currency information from the native system.
 * On platforms where no such native information is available, this function will return the same thing as [currencyFor].
 *
 * **Warning:** _We recommend you use the [currencyFor] function for consistent results. This function may return different values depending on the platform._
 *
 * @param currencyCode The ISO 4217 currency code.
 * @return The native [Currency] instance for the given code, or `null` if the code is invalid or not supported natively.
 */
expect fun currencyForNative(currencyCode: String): Currency?

/**
 * Returns a list of all [Currency] instances supported by the internal resource map.
 *
 * @return A list of supported [Currency] instances.
 */
fun availableCurrencies(): List<Currency> = currencyResourceMap.values.toList()

/**
 * Returns a list of [Currency] instances whose display name contains the specified [name].
 *
 * @param name The name to search for within the currency's display name.
 * @param ignoreCase `true` to ignore character case when matching. Defaults to `true`.
 * @return A list of matching [Currency] instances.
 */
fun currenciesByName(name: String, ignoreCase: Boolean = true): List<Currency> =
    availableCurrencies().filter { it.name.contains(name, ignoreCase) }

/**
 * Retrieves the [Currency] for a given [Locale].
 *
 * This function delegates to [Currency.Companion.getCurrency] to perform the lookup.
 *
 * @param locale The [Locale] for which to find the currency.
 * @return The corresponding [Currency] if a mapping is found, otherwise `null`.
 */
fun currencyFor(locale: Locale): Currency? = Currency.getCurrency(locale)
