package io.github.aughtone.types.financial

import java.util.Currency as JvmCurrency

actual fun currencyFor(currencyCode: String): Currency? =
    JvmCurrency.getInstance(currencyCode)?.let { jvmc ->
        Currency(
            currencyCode = jvmc.currencyCode,
            symbol = jvmc.symbol,
            displayName = jvmc.displayName,
            fractionDigits = jvmc.defaultFractionDigits,
            numericCode = jvmc.numericCode
        )
    }
