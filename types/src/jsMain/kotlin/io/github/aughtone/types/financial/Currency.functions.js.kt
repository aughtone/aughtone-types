package io.github.aughtone.types.financial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.Json as KotlinJson

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


actual fun currencyFor(currencyCode: String): Currency? {
    val codeDetails: CurrencyCodeData? = code(currencyCode)?.let {
        KotlinJson.decodeFromString<CurrencyCodeData>(it)
    }
    return Currency(
        currencyCode = currencyCode,
        symbol = getSymbolFromCurrency(currencyCode) ?: "?",
        displayName = codeDetails?.name ?: currencyDataMap[currencyCode]?.displayName
        ?: currencyCode,
        fractionDigits = codeDetails?.digits ?: 2,
        numericCode = codeDetails?.number?.toInt() ?: currencyDataMap[currencyCode]?.numericCode
        ?: -1
    )
}


@JsModule("currency-codes")
@JsNonModule
external fun code(code: String): String?


@JsModule("currency-symbol-map")
@JsNonModule
external fun getSymbolFromCurrency(code: String): String?
