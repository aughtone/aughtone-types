package io.github.aughtone.types.financial

import java.util.Currency as JvmCurrency

actual fun currencyForNative(currencyCode: String): Currency? =
    try {
        JvmCurrency.getInstance(currencyCode)
    } catch (e: IllegalArgumentException) {
        // getInstance throws for unsupported ISO codes; the contract is to return null.
        null
    }?.let { jvmc ->
        Currency(
            code = jvmc.currencyCode,
            symbol = jvmc.symbol,
            name = jvmc.displayName,
            digits = jvmc.defaultFractionDigits,
            number = jvmc.numericCode,
        )
    }
