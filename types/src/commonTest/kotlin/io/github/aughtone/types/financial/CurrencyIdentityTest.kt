package io.github.aughtone.types.financial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A currency is identified by its ISO 4217 code, and [Currency] compares that way: presentation —
 * `name`, `symbol` — takes no part, so instances built from different sources for one currency are
 * one value. See issue #37, where `Money` had been compensating for the opposite.
 *
 * The last test is the one that keeps this honest. Code-only equality is only correct while the code
 * really does identify an entry, so the shipped map must never carry two entries under one code, and
 * an entry's `code` must match the key it is filed under. If a regeneration breaks that, equality
 * starts conflating two different currencies, and nothing else would catch it.
 */
class CurrencyIdentityTest {

    private val usd = currencyFor("USD")!!

    @Test
    fun `presentation differences do not make a different currency`() {
        assertEquals(usd, usd.copy(name = "United States Dollar", symbol = "US$"))
        assertEquals(usd.hashCode(), usd.copy(name = "United States Dollar").hashCode())
    }

    @Test
    fun `a different code is a different currency`() {
        assertFalse(usd == currencyFor("EUR")!!)
        assertFalse(usd == usd.copy(code = "CAD"))
    }

    @Test
    fun `one currency occupies one slot in a set and one key in a map`() {
        assertEquals(1, setOf(usd, usd.copy(symbol = "US$"), usd.copy(name = "Dollar")).size)
        val byCurrency = mapOf(usd to "first")
        assertEquals("first", byCurrency[usd.copy(symbol = "US$")])
    }

    @Test
    fun `equality is not reference equality and survives serialization shape`() {
        // A decoded payload produces a fresh instance; it must still be the same currency.
        val rebuilt = Currency(usd.code, usd.number, usd.digits, "whatever", "?")
        assertTrue(usd == rebuilt)
        assertEquals(usd.hashCode(), rebuilt.hashCode())
    }

    @Test
    fun `every entry in the shipped map is filed under its own unique code`() {
        val entries = currencyResourceMap.values.toList()
        currencyResourceMap.forEach { (key, currency) ->
            assertEquals(key, currency.code, "currencyResourceMap key $key holds ${currency.code}")
        }
        assertEquals(
            entries.size,
            entries.map { it.code }.distinct().size,
            "two entries share an ISO 4217 code, which would make Currency equality conflate them"
        )
    }
}
