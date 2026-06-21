package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.localeResourceMap
import kotlin.test.Test

class CurrencyMismatchTest {
    @Test
    fun findMismatches() {
        val mappedCurrencies = localeToCurrencyMap.values.toSet()
        val definedCurrencies = currencyResourceMap.keys
        val missingCurrencies = mappedCurrencies - definedCurrencies
        println("=== MISSING CURRENCIES IN RESOURCE MAP ===")
        missingCurrencies.sorted().forEach { println(it) }

        val mappedLocales = localeToCurrencyMap.keys
        val definedLocales = localeResourceMap.keys
        val missingLocales = mappedLocales - definedLocales
        println("=== MISSING LOCALES IN RESOURCE MAP ===")
        missingLocales.sorted().forEach { println(it) }
    }
}
