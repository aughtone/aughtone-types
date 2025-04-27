package io.github.aughtone.types.financial

import java.util.Currency as AndroidCurrency

actual fun currencyFor(currencyCode: String): Currency? =
    AndroidCurrency.getInstance(currencyCode)?.let { ac ->
        Currency(
            code = ac.currencyCode,
            // android doesn't have the correct symbol for some currencies, so fall back to the android resource if needed.
            symbol = currencyResourceMap[currencyCode]?.symbol ?: ac.symbol,
            name = ac.displayName,
            digits = ac.defaultFractionDigits,
            number = ac.numericCode,
            countries = currencyResourceMap[currencyCode]?.countries ?: emptyList()
        )
    }
