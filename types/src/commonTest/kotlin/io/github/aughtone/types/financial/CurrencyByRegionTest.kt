package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.parseLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * A locale's currency comes from its country, and the language has no say in it. The lookup used to
 * try whole language tags against a table of 112, so any language and country pair missing from the
 * table had no currency, however obvious the country's was.
 */
class CurrencyByRegionTest {

    private fun codeFor(tag: String): String? = Currency.getCurrency(parseLocale(tag))?.code

    @Test
    fun `a known country with a language pair that was not listed resolves`() {
        assertEquals("INR", codeFor("en-IN"))
        assertEquals("USD", codeFor("fr-US"))
        assertEquals("JPY", codeFor("de-JP"))
    }

    @Test
    fun `languages the old table never listed resolve by their country`() {
        mapOf(
            "fil-PH" to "PHP", "bn-BD" to "BDT", "ur-PK" to "PKR", "ha-NG" to "NGN", "yo-NG" to "NGN",
            "zu-ZA" to "ZAR", "am-ET" to "ETB", "ta-IN" to "INR", "te-IN" to "INR", "mr-IN" to "INR",
            "pa-IN" to "INR",
        ).forEach { (tag, code) -> assertEquals(code, codeFor(tag), tag) }
    }

    @Test
    fun `a script subtag does not get in the way of the country`() {
        assertEquals("INR", codeFor("pa-Guru-IN"))
        assertEquals("SGD", codeFor("zh-Hans-SG"))
    }

    @Test
    fun `pairs the old table did list keep their currency`() {
        assertEquals("USD", codeFor("en-US"))
        assertEquals("EUR", codeFor("fr-FR"))
        assertEquals("CAD", codeFor("fr-CA"))
        assertEquals("CHF", codeFor("de-CH"))
    }

    @Test
    fun `a locale with no country has no currency to infer`() {
        assertNull(codeFor("en"))
        assertNull(codeFor("und"))
        assertNull(codeFor("zh-Hans"))
    }

    @Test
    fun `a region that is not a country with a currency finds nothing`() {
        assertNull(codeFor("es-419"), "Latin America is not one currency")
        assertNull(codeFor("en-AQ"), "Antarctica has no currency")
        assertNull(codeFor("en-ZZ"), "unknown region")
    }

    @Test
    fun `currencyFor a locale agrees with getCurrency`() {
        assertEquals("INR", currencyFor(parseLocale("en-IN"))?.code)
    }

    @Test
    fun `Bulgaria is on the euro`() {
        assertEquals("EUR", codeFor("bg-BG"))
    }

    @Test
    fun `El Salvador uses the dollar rather than the colon the JDK reports`() {
        assertEquals("USD", codeFor("es-SV"))
    }

    @Test
    fun `Kosovo uses the euro though it has no ISO 3166 code`() {
        assertEquals("EUR", codeFor("sq-XK"))
    }

    @Test
    fun `the former Netherlands Antilles use the Caribbean guilder`() {
        assertEquals("XCG", codeFor("nl-CW"))
        assertEquals("XCG", codeFor("nl-SX"))
    }
}
