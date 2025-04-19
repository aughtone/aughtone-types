package io.github.aughtone.types.financial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class CurrencyTest {
    @Test
    fun `get correct currency`() {
        val currency = currencyFor("CAD")
        assertNotNull(currency)
        assertEquals("CAD", currency.currencyCode)
        assertEquals("$", currency.symbol)
        assertEquals("Canadian Dollar", currency.displayName)
        assertEquals(124, currency.numericCode)
    }
}
