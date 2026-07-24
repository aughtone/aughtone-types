package io.github.aughtone.types.financial

import io.github.aughtone.types.locale.localeFor
import io.github.aughtone.types.locale.parseLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrencyLookupTest {
    @Test
    fun `currency resolves for script bearing simplified chinese locale`() {
        assertEquals("CNY", currencyFor(localeFor("zh-CN")!!)?.code)
        assertEquals("SGD", currencyFor(localeFor("zh-SG")!!)?.code)
    }

    @Test
    fun `currency resolves for script bearing traditional chinese locales`() {
        assertEquals("TWD", currencyFor(localeFor("zh-TW")!!)?.code)
        assertEquals("HKD", currencyFor(localeFor("zh-HK")!!)?.code)
    }

    @Test
    fun `currency resolves for serbian locale`() {
        assertEquals("RSD", currencyFor(localeFor("sr-RS")!!)?.code)
    }

    @Test
    fun `currency resolves for uzbek locale`() {
        assertEquals("UZS", currencyFor(localeFor("uz-UZ")!!)?.code)
    }

    @Test
    fun `currency lookup steps down over variant subtags`() {
        assertEquals("EUR", currencyFor(parseLocale("de-DE-1996"))?.code)
    }

    @Test
    fun `currency lookup returns null for unknown locales`() {
        assertNull(currencyFor(parseLocale("xx-YY")))
    }

    @Test
    fun `newly added currencies are available`() {
        assertEquals(646, currencyFor("RWF")?.number)
        assertEquals(0, currencyFor("RWF")?.digits)
        assertEquals(952, currencyFor("XOF")?.number)
        assertEquals(0, currencyFor("XOF")?.digits)
        assertEquals(950, currencyFor("XAF")?.number)
        assertEquals(951, currencyFor("XCD")?.number)
        assertEquals(2, currencyFor("XCD")?.digits)
        assertEquals(967, currencyFor("ZMW")?.number)
        assertEquals(4, currencyFor("UYW")?.digits)
        assertEquals(0, currencyFor("UGX")?.digits)
        assertEquals(924, currencyFor("ZWG")?.number)
    }
}
