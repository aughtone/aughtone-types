package io.github.aughtone.types.financial

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.currentLocale

actual fun currencyForNative(currencyCode: String): Currency? {
    // Start from the shared resource map for stable code/number/digits, and use the
    // native APIs only to enrich with a localized display name and symbol.
    val resource: Currency? = currencyResourceMap[currencyCode]

    // A currency-style formatter reports the fraction digits and symbol for the
    // requested code; without setting the style, maximumFractionDigits would be 0.
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    formatter.currencyCode = currencyCode

    val nativeName: String? =
        NSLocale.currentLocale.displayNameForKey(NSLocaleCurrencyCode, currencyCode)

    if (resource == null && nativeName == null) {
        // Neither the resource map nor the platform knows this code.
        return null
    }

    return Currency(
        code = currencyCode,
        symbol = resource?.symbol ?: formatter.currencySymbol,
        name = nativeName ?: resource?.name ?: currencyCode,
        digits = resource?.digits ?: formatter.maximumFractionDigits.toInt(),
        number = resource?.number ?: -1, // apple doesn't expose the ISO numeric code.
    )
}
