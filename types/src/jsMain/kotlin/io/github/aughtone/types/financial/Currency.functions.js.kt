package io.github.aughtone.types.financial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

//{
//    "code": "AED",
//    "number": "784",
//    "digits": 2,
//    "currency": "UAE Dirham",
//    "countries": [
//    "United Arab Emirates (The)"
//    ]
//},
@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class CurrencyCodeData(
    @JsonNames("code")
    val code: String,
    @JsonNames("number")
    val number: String,
    @JsonNames("digits")
    val digits: Int,
    @JsonNames("currency")
    val name: String,
)

//@JsModule("currency-codes")
////@JsNonModule
//external val currencyCodes: dynamic

////// https://www.npmjs.com/package/currency-symbol-map
//@JsModule("currency-symbol-map")
////@JsNonModule
////external val getSymbolFromCurrency: dynamic
//external fun getSymbolFromCurrency(currencyCode: String): String?
@JsModule("currency-symbol-map")
external val currencySymbolMap:dynamic


actual fun currencyFor(currencyCode: String): Currency? {

//    val js = "Intl.NumberFormat(\"${""}\", { style: \"currency\", currency: \"EUR\" })"
//    js("const getSymbolFromCurrency = require(\'currency-symbol-map\')")
//    js(currencySymbolMap)
//    js("""
//
//         module.exports = function getSymbolWithCode (currencyCode) {
//            return currencySymbolMap[currencyCode];
//        }
//
//    """)

//    println(getSymbolWithCode(currencyCode))
    println(currencyCode)
//    println(getSymbolFromCurrency)
//    println(getSymbolFromCurrency(currencyCode))
//    println(currencyCodes)
//    println(currencyCodes.code)
//    println(currencyCodes.code[currencyCode])
//    println(currencyCodes.codes)
//    println(currencyCodes.codes[currencyCode])

    return Currency(
        currencyCode = currencyCode,
        symbol = getSymbolWithCode(currencyCode) ?: "?",
        displayName = "", //codeDetails?.name ?: currencyDataMap[currencyCode]?.displayName ?: currencyCode,
        fractionDigits = 2, //codeDetails?.digits ?: 2,
        numericCode = -1, //codeDetails?.number?.toInt() ?: currencyDataMap[currencyCode]?.numericCode ?: -1
    )
}

//fun formatDetails(currencyCode: String): String {
//    return js("function greet (name) { return \"Hello, \" + name + \"!\";}")
//}

external fun getSymbolWithCode(currencyCode: String): String
