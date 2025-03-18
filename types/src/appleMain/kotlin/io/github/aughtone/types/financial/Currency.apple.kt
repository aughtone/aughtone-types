package io.github.aughtone.types.financial

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.currentLocale
import platform.Foundation.preferredLanguages
import platform.Foundation.NSLocaleKey


actual fun currencyFor(currencyCode: String): Currency? {
    // Took hints from:
    //  https://gist.github.com/mattt/6d022b66f08ea8c1b99ebe7e48b95c4b
    // https://stackoverflow.com/questions/73412857/how-to-get-the-currency-symbol-from-locale-swift
    // https://stackoverflow.com/questions/67512302/nsnumberformatter-get-currency-symbol-for-currency-code
    // https://youtrack.jetbrains.com/issue/CMP-6614/iOS-Localization-strings-for-language-qualifiers-that-are-not-the-same-between-platforms-appear-not-translated
    //  what i wans is this in swift: Locale.Currency("USD")

    // See:
    // https://github.com/kotlin-hands-on/native-ui-webinar/blob/f056e3c2b61ac29ec6299b7d817c2339bd5ee56a/shared/src/iosMain/kotlin/location/iOSCountryCodeService.kt#L3
    // https://developer.apple.com/documentation/foundation/locale/currency/3952311-init
    // https://developer.apple.com/documentation/swift/regexcomponent/localizedcurrency(code:locale:)

//    NSLocale.currentLocale

//    val localeList: List<*> = NSBundle.mainBundle.preferredLocalizations
    val result1 = NSLocale.preferredLanguages.map{ NSLocale(it as String) }
    val result2 =result1.map { it.objectForKey(NSLocaleCurrencyCode) }

//    val locale = NSLocale.preferredLanguages.firstOrNull()
//        ?.let { NSLocale(it as String) }
//        ?: NSLocale.currentLocale
//    val script = locale.objectForKey(NSLocaleScriptCode) as? String


    Currency(currencyCode=currencyCode, symbol = "?", displayName = "Unknown", fractionDigits = 2,0)

//    [[NSBundle mainBundle] preferredLocalizations]
//
//    NSLocale.availableLocaleIdentifiers
//
//    NSLocale.
//    NSLocale.availableIdentifiers.firstOrNull {
//
//    }
//    var currencies: [MyCurrency] {
//        return Locale.availableIdentifiers.compactMap {
//            guard let currencyCode = Locale(identifier: $0).currencyCode,
//            let name = Locale.autoupdatingCurrent.localizedString(forCurrencyCode: currencyCode),
//            let symbol = Locale(identifier: $0).currencySymbol  else { return nil }
//            return MyCurrency(code: $0, name: name, symbol: symbol)
//        }
//    }

//    forCurrencyCode in currencyCode.uppercase() {
//
//    }
}

//fun localizedCurrency(forCurrencyCode currencyCode: String) :String? {
//    val languageCode = NSLocale.preferredLanguages.firstOrNull()
//val regionCode =   regionCode  else { return null }
//}
//fun localizedCurrencySymbol(forCurrencyCode currencyCode: String) -> String? {
//    guard let languageCode = languageCode, let regionCode = regionCode else { return nil }
//
//    /*
//     Each currency can have a symbol ($, £, ¥),
//     but those symbols may be shared with other currencies.
//     For example, in Canadian and American locales,
//     the $ symbol on its own implicitly represents CAD and USD, respectively.
//     Including the language and region here ensures that
//     USD is represented as $ in America and US$ in Canada.
//    */
//    let components: [String: String] = [
//    NSLocale.Key.languageCode.rawValue: languageCode,
//    NSLocale.Key.countryCode.rawValue: regionCode,
//    NSLocale.Key.currencyCode.rawValue: currencyCode,
//    ]
//
//    let identifier = Locale.identifier(fromComponents: components)
//
//    return Locale(identifier: identifier).currencySymbol
//}
