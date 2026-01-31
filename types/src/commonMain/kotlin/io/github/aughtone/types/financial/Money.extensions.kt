package io.github.aughtone.types.financial

/**
 * Converts a [Double] to a [Money] object, using the specified currency.
 * This is a convenience function for creating Money instances from standard double values.
 *
 * @param currency The optional currency to associate with the money.
 * @return A [Money] object representing the value.
 */
fun Double.toMoney(currency: Currency? = null): Money {
    return Money(this, currency)
}

/**
 * Converts a [Money] object back to a [Double].
 * Note that this may result in a loss of precision and should primarily be used for display purposes.
 *
 * @return The monetary value as a [Double].
 */
fun Money.toDouble(): Double {
    return this.cents / 100.0
}
