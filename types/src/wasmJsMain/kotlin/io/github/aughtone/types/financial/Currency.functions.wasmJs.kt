package io.github.aughtone.types.financial

actual fun currencyFor(currencyCode: String): Currency? {
    val symbol = wasmJsCurrencySymbolMap[currencyCode] ?: ""
    val codeData = wasmJsCurrencyMap[currencyCode] ?: WasmJsCurrencyResource("", "", 0, "", emptyList())

    return Currency(
        currencyCode = currencyCode,
        symbol = symbol,
        displayName = codeData.currency,
        fractionDigits = codeData.digits,
        numericCode = codeData.number.toInt(),
    )
}
