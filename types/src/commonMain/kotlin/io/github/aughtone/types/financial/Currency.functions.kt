package io.github.aughtone.types.financial

/**
 * Returns the [Currency] instance for the given [currencyCode], or `null` if no such currency exists.
 *
 * The [currencyCode] should be a valid ISO 4217 currency code (e.g., "USD", "EUR", "JPY").
 *
 * @param currencyCode The ISO 4217 currency code.
 * @return The [Currency] instance for the given code, or `null` if the code is invalid.
 */
expect fun currencyFor(currencyCode: String): Currency?
