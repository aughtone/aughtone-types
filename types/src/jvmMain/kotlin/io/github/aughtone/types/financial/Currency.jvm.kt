package io.github.aughtone.types.financial

import java.util.Currency as JvmCurrency

actual fun currencyForNative(currencyCode: String): Currency? =
    JvmCurrency.getInstance(currencyCode)?.let { jvmc ->
        Currency(
            code = jvmc.currencyCode,
            symbol = jvmc.symbol,
            name = jvmc.displayName,
            digits = jvmc.defaultFractionDigits,
            number = jvmc.numericCode,
        )
    }
