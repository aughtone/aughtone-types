package io.github.aughtone.types.financial

import io.github.aughtone.types.number.BigDecimal

/**
 * Converts a [Double] to a [Money] object, using the specified currency.
 * This is a convenience function for creating Money instances from standard double values.
 *
 * @param currency The optional currency to associate with the money.
 * @return A [Money] object representing the value.
 */
fun Double.toMoney(currency: Currency = Currency.current): Money {
    return Money(this, currency)
}

fun BigDecimal.toMoney(currency: Currency = Currency.current): Money {
    return Money(this, currency)
}
