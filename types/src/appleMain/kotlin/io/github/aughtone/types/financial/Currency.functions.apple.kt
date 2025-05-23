package io.github.aughtone.types.financial

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.NSNumberFormatter
import platform.Foundation.preferredLanguages


actual fun currencyForNative(currencyCode: String): Currency? {
    // Took hints from Gemini and these links:
    //  https://gist.github.com/mattt/6d022b66f08ea8c1b99ebe7e48b95c4b
    // https://stackoverflow.com/questions/73412857/how-to-get-the-currency-symbol-from-locale-swift
    // https://stackoverflow.com/questions/67512302/nsnumberformatter-get-currency-symbol-for-currency-code
    // https://youtrack.jetbrains.com/issue/CMP-6614/iOS-Localization-strings-for-language-qualifiers-that-are-not-the-same-between-platforms-appear-not-translated
    // https://github.com/kotlin-hands-on/native-ui-webinar/blob/f056e3c2b61ac29ec6299b7d817c2339bd5ee56a/shared/src/iosMain/kotlin/location/iOSCountryCodeService.kt#L3
    // https://developer.apple.com/documentation/foundation/locale/currency/3952311-init
    // https://developer.apple.com/documentation/swift/regexcomponent/localizedcurrency(code:locale:)


    // XXX  what i really want is this in swift: Locale.Currency("USD"), but swift support is not ready for that yet.

    val current: NSLocale = NSLocale()
    val result1: List<NSLocale> = NSLocale.preferredLanguages.map { NSLocale(it as String) }
    val result2: NSLocale =
        result1.first { currencyCode == it.objectForKey(NSLocaleCurrencyCode).toString() }
    val formatter = NSNumberFormatter()
    formatter.setLocale(result2)

    val currency = Currency(
        code = formatter.currencyCode,
        symbol = formatter.currencySymbol, //result2.objectForKey(NSLocaleCurrencySymbol).toString(),
        name = current.displayNameForKey(NSLocaleCurrencyCode, formatter.currencyCode)
            ?: currencyResourceMap[formatter.currencyCode]?.name
            ?: formatter.internationalCurrencySymbol, // international currency symbol is the same as the symbol, but we can use it as a backup.
        digits = formatter.maximumFractionDigits.toInt(), //2,
        number = currencyResourceMap[formatter.currencyCode]?.number
            ?: -1 // if we don't get this, apple codes doesn't seem to have ean equivalent.
    )
    return currency
}
