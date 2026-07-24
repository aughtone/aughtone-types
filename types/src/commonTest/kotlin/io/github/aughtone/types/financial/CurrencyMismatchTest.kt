package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.localeResourceMap
import kotlin.test.Test
import kotlin.test.assertNotNull

class CurrencyMismatchTest {
    @Test
    fun `every mapped currency code exists in the currency resource map`() {
        localeToCurrencyMap.values.toSet().forEach { code ->
            assertNotNull(
                currencyResourceMap[code],
                "Currency $code is mapped from a locale but missing from currencyResourceMap"
            )
        }
    }

    @Test
    fun `every mapped locale tag exists in the locale resource map`() {
        localeToCurrencyMap.keys.forEach { tag ->
            assertNotNull(
                localeResourceMap[tag],
                "Locale $tag is mapped to a currency but missing from localeResourceMap"
            )
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
