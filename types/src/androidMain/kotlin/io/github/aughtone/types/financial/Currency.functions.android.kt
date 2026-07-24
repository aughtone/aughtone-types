package io.github.aughtone.types.financial

import java.util.Currency as AndroidCurrency

actual fun currencyForNative(currencyCode: String): Currency? =
    try {
        AndroidCurrency.getInstance(currencyCode)
    } catch (e: IllegalArgumentException) {
        // getInstance throws for unsupported ISO codes; the contract is to return null.
        null
    }?.let { ac ->
        Currency(
            code = ac.currencyCode,
            // android doesn't have the correct symbol for some currencies, so fall back to the android resource if needed.
            symbol = currencyResourceMap[currencyCode]?.symbol ?: ac.symbol,
            name = ac.displayName,
            digits = ac.defaultFractionDigits,
            number = ac.numericCode,
        )
    }
