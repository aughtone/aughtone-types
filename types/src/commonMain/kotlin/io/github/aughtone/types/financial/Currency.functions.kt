package io.github.aughtone.types.financial

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
