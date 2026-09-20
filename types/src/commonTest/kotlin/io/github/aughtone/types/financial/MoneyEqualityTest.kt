package io.github.aughtone.types.financial

import io.github.aughtone.types.number.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Equality on [Money] is about the amount, not how it was written. Scale is preserved in storage but
 * takes no part in equality, so hash-based collections must agree with that — which is the case these
 * tests exist to pin down, because it fails silently when wrong.
 */
class MoneyEqualityTest {

    private val usd = currencyFor("USD")!!
    private val eur = currencyFor("EUR")!!

    @Test
    fun `the same amount written with different scales is equal`() {
        assertEquals(Money(BigDecimal("5.10"), usd), Money(BigDecimal("5.1"), usd))
        assertEquals(Money(BigDecimal("12.500"), usd), Money(BigDecimal("12.5"), usd))
    }

    @Test
    fun `the same amount in a different currency is not equal`() {
        assertNotEquals(Money(BigDecimal("5.10"), usd), Money(BigDecimal("5.10"), eur))
    }

    @Test
    fun `different amounts are not equal`() {
        assertNotEquals(Money(BigDecimal("5.10"), usd), Money(BigDecimal("5.11"), usd))
    }

    @Test
    fun `equal amounts share a hash code`() {
        assertEquals(
            Money(BigDecimal("5.10"), usd).hashCode(),
            Money(BigDecimal("5.1"), usd).hashCode(),
        )
    }

    @Test
    fun `a set treats two spellings of one amount as one entry`() {
        val set = setOf(Money(BigDecimal("5.10"), usd), Money(BigDecimal("5.1"), usd))
        assertEquals(1, set.size)
    }

    @Test
    fun `a map finds an amount stored under a different scale`() {
        val map = mapOf(Money(BigDecimal("5.10"), usd) to "found")
        assertEquals("found", map[Money(BigDecimal("5.1"), usd)])
    }

    @Test
    fun `scale is preserved in storage even though equality ignores it`() {
        val money = Money(BigDecimal("5.0100000"), usd)
        assertEquals(7, money.value.scale)
        assertEquals("5.0100000", money.value.toString())
    }

    @Test
    fun `compareTo orders amounts numerically across scales`() {
        assertEquals(0, Money(BigDecimal("5.10"), usd).compareTo(Money(BigDecimal("5.1"), usd)))
        assertTrue(Money(BigDecimal("5.2"), usd) > Money(BigDecimal("5.10"), usd))
        assertTrue(Money(BigDecimal("5.0"), usd) < Money(BigDecimal("5.10"), usd))
    }

    @Test
    fun `compareTo refuses to order different currencies`() {
        assertFailsWith<IllegalArgumentException> {
            Money(BigDecimal("5.10"), usd).compareTo(Money(BigDecimal("5.10"), eur))
        }
    }

    @Test
    fun `sorting uses the numeric order`() {
        val sorted = listOf(
            Money(BigDecimal("5.20"), usd),
            Money(BigDecimal("5.1"), usd),
            Money(BigDecimal("5.150"), usd),
        ).sorted()
        assertEquals(listOf("5.1", "5.150", "5.20"), sorted.map { it.value.toString() })
    }
}
