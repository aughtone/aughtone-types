package io.github.aughtone.types.financial

/**
 * This currency rebuilt with some fields changed, as another source might describe it. [Currency]
 * has no `copy`, because changing its code that way would keep a name and symbol from another
 * currency; tests that need a variant build one explicitly.
 */
internal fun Currency.restated(
    code: String = this.code,
    name: String = this.name,
    symbol: String = this.symbol,
): Currency = Currency(code, number, digits, name, symbol, obsolete, replacedBy)
