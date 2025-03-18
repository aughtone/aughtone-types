package io.github.aughtone.types.financial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class CurrencyTest {
    @Test
    fun `get correct currency`() {
        val currency = currencyFor("CAN")
        assertNotNull(currency)
        assertEquals("CAN", currency.currencyCode)
        assertEquals("$", currency.symbol)
        assertEquals("?", currency.displayName)
        assertEquals(8, currency.numericCode)
    }
}
