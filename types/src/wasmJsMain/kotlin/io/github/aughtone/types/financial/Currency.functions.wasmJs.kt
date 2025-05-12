package io.github.aughtone.types.financial

actual fun currencyForNative(currencyCode: String): Currency? = currencyResourceMap[currencyCode]
