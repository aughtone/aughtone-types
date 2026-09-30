package io.github.aughtone.types.financial

import io.github.aughtone.types.number.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every operation on [Money] must agree about whether two amounts are in the same currency. They did
 * not: the arithmetic operators matched on ISO 4217 `code` while `equals` and `compareTo` matched on
 * whole-`Currency` equality, so a pair of amounts built from differently-sourced [Currency] instances
 * added happily and then threw from `sorted()`. See issue #37.
 *
 * The fix put the identity rule on [Currency] itself — see [CurrencyIdentityTest] — so every guard
 * here reads the one definition rather than restating it. These tests hold [Money] to it.
 *
 * They also pin the `Comparable` contract, which is what makes a half-fix worse than the bug: had
 * `compareTo` been relaxed alone, it would have returned `0` for a pair `equals` called unequal, and a
 * `TreeMap` and a `HashMap` would disagree about how many keys they hold.
 */
class MoneyCurrencyIdentityTest {

    /** USD as the resource map has it. */
    private val usd = currencyFor("USD")!!

    /** The same currency as another source might describe it: same code, different presentation. */
    private val usdOtherSource = usd.copy(name = "United States Dollar", symbol = "US$")

    private val eur = currencyFor("EUR")!!

    private fun five(currency: Currency) = Money(BigDecimal("5.00"), currency)

    @Test
    fun `the two instances really do carry different presentation`() {
        // The premise. If the two instances were identical the rest of the file would test nothing.
        assertTrue(
            usd.name != usdOtherSource.name && usd.symbol != usdOtherSource.symbol,
            "expected the two Currency instances to differ in name and symbol"
        )
        // And they are nonetheless one currency, because the ISO 4217 code is the identity.
        assertEquals(usd.code, usdOtherSource.code)
        assertEquals(usd, usdOtherSource)
    }

    @Test
    fun `arithmetic accepts the same currency from a different source`() {
        assertEquals("10.00", (five(usd) + five(usdOtherSource)).value.toString())
        assertEquals("0.00", (five(usd) - five(usdOtherSource)).value.toString())
    }

    @Test
    fun `comparison accepts the same currency from a different source`() {
        assertEquals(0, five(usd).compareTo(five(usdOtherSource)))
    }

    @Test
    fun `sorting a list assembled from two sources does not throw`() {
        val sorted = listOf(Money(BigDecimal("3.00"), usdOtherSource), five(usd)).sorted()
        assertEquals("3.00", sorted.first().value.toString())
    }

    @Test
    fun `equality accepts the same currency from a different source`() {
        assertEquals(five(usd), five(usdOtherSource))
    }

    @Test
    fun `equal amounts hash alike so hash based collections agree`() {
        assertEquals(five(usd).hashCode(), five(usdOtherSource).hashCode())
        assertEquals(1, setOf(five(usd), five(usdOtherSource)).size)
    }

    @Test
    fun `compareTo returning zero agrees with equals`() {
        // The Comparable contract. Checked both ways round, over same-source, cross-source and
        // genuinely different currencies.
        val pairs = listOf(
            five(usd) to five(usd),
            five(usd) to five(usdOtherSource),
            five(usd) to Money(BigDecimal("5.0000"), usdOtherSource),
            five(usd) to Money(BigDecimal("6.00"), usd),
        )
        for ((a, b) in pairs) {
            assertEquals(
                a == b,
                a.compareTo(b) == 0,
                "compareTo and equals disagree for ${a.value}/${a.currency.code} vs ${b.value}/${b.currency.code}"
            )
        }
    }

    @Test
    fun `a genuinely different currency is still refused everywhere`() {
        assertTrue(five(usd) != five(eur))
        assertTrue(runCatching { five(usd).compareTo(five(eur)) }.isFailure)
        assertTrue(runCatching { five(usd) + five(eur) }.isFailure)
    }
}
