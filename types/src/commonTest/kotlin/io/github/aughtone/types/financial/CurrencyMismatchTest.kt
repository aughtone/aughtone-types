package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.localeResourceMap
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class CurrencyMismatchTest {
    @Test
    fun `every mapped currency code exists in the currency resource map`() {
        regionToCurrencyMap.forEach { (region, code) ->
            assertNotNull(
                currencyResourceMap[code],
                "Currency $code is mapped from region $region but missing from currencyResourceMap"
            )
        }
    }

    @Test
    fun `a region's currency is never one the resource map calls obsolete`() {
        regionToCurrencyMap.forEach { (region, code) ->
            assertFalse(currencyResourceMap.getValue(code).obsolete, "$region maps to obsolete $code")
        }
    }

    @Test
    fun `every region bearing locale resolves to a currency`() {
        // Uses the actual generated languageTag, so script-bearing locales like
        // zh-Hans-CN must resolve through the BCP 47 stepdown.
        localeResourceMap.values.filter { it.regionCode != null }.forEach { locale ->
            assertNotNull(
                Currency.getCurrency(locale),
                "No currency resolved for locale ${locale.languageTag}"
            )
        }
    }
}
