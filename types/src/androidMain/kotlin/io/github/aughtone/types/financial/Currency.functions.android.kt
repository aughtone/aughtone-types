package io.github.aughtone.types.financial

import java.util.Currency as AndroidCurrency

actual fun currencyFor(currencyCode: String): Currency? =
    AndroidCurrency.getInstance(currencyCode)?.let { ac ->
        Currency(
            currencyCode = ac.currencyCode,
            symbol = ac.symbol,
            displayName = ac.displayName,
            fractionDigits = ac.defaultFractionDigits,
            numericCode = ac.numericCode
        )
    }
