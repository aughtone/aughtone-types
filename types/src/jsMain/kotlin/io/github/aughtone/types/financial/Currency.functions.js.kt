package io.github.aughtone.types.financial

actual fun currencyFor(currencyCode: String): Currency? {
    val symbol = jsCurrencySymbolMap[currencyCode] ?: ""
    val codeData = jsCurrencyDataMap[currencyCode] ?: CurrencyResource("", "", 0, "", emptyList())

    return Currency(
        currencyCode = currencyCode,
        symbol = symbol,
        displayName = codeData.currency,
        fractionDigits = codeData.digits,
        numericCode = codeData.number.toInt(),
    )
}
