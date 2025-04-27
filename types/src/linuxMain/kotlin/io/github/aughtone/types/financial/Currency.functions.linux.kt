package io.github.aughtone.types.financial
actual fun currencyFor(currencyCode: String): Currency? = currencyResourceMap[currencyCode]
