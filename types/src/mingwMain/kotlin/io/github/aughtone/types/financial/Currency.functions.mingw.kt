package io.github.aughtone.types.financial

// Windows describes the currency of a locale, not a currency by its ISO 4217 code, so it has no
// answer to this lookup. The bundled map is what currencyFor returns, as the contract allows.
actual fun currencyForNative(currencyCode: String): Currency? = currencyResourceMap[currencyCode]
