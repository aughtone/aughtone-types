package io.github.aughtone.types.financial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {
    private val usd = Currency(
        code = "USD",
        number = 840,
        digits = 2,
        name = "Dollar",
        symbol = "$"
    )

    @Test
    fun `zero factory creates money with zero value`() {
        val zeroMoney = Money.zero(usd)
        assertEquals(0L, zeroMoney.cents)
        assertEquals(usd, zeroMoney.currency)
    }

    @Test
    fun `creating money with Double works`() {
        val money = Money(10.50, usd)
        assertEquals(1050L, money.cents)
        assertEquals(usd, money.currency)
    }

    @Test
    fun `creating money with Long (cents) works`() {
        val money = Money(12345L, usd)
        assertEquals(12345L, money.cents)
        assertEquals(usd, money.currency)
    }

    @Test
    fun `plus and minus operators for Money`() {
        val m1 = Money(1000L, usd)
        val m2 = Money(550L, usd)
        assertEquals(1550L, (m1 + m2).cents)
        assertEquals(450L, (m1 - m2).cents)
    }

    @Test
    fun `plus and minus for Money throws on currency mismatch`() {
        val m1 = Money(10.0, usd)
        val m2 = Money(
            5.50, Currency(
                code = "EUR",
                number = 978,
                digits = 2,
                name = "Euro",
                symbol = "€"
            )
        )
        assertFailsWith<IllegalArgumentException> { m1 + m2 }
        assertFailsWith<IllegalArgumentException> { m1 - m2 }
    }

    @Test
    fun `times and div operators for Money`() {
        val m1 = Money(1000L, usd) // $10.00
        val m2 = Money(200L, usd)  // $2.00
        assertEquals(2000L, (m1 * m2).cents) // $10 * $2 = $20 -> 2000 cents (scaled)
        assertEquals(5.0, m1 / m2, 0.0)
    }

    @Test
    fun `plus and minus operators for Double`() {
        val m1 = Money(1000L, usd)
        assertEquals(1250L, (m1 + 2.50).cents)
        assertEquals(750L, (m1 - 2.50).cents)
    }

    @Test
    fun `times and div operators for Double`() {
        val m1 = Money(1000L, usd)
        assertEquals(2500L, (m1 * 2.5).cents)
        assertEquals(400L, (m1 / 2.5).cents)
    }

    @Test
    fun `plus and minus operators for Long`() {
        val m1 = Money(1000L, usd)
        assertEquals(1250L, (m1 + 250L).cents)
        assertEquals(750L, (m1 - 250L).cents)
    }

    @Test
    fun `times and div operators for Long`() {
        val m1 = Money(1000L, usd)
        assertEquals(2000L, (m1 * 2L).cents)
        assertEquals(500L, (m1 / 2L).cents)
    }

    @Test
    fun `extension functions work`() {
        val money = 123.45.toMoney(usd)
        assertEquals(12345L, money.cents)
        assertEquals(123.45, money.toDouble())
    }

    @Test
    fun `floating point inaccuracy is resolved`() {
        val m1 = 0.1.toMoney()
        val m2 = 0.2.toMoney()
        val result = m1 + m2
        assertEquals(0.3, result.toDouble(), "The sum should be exactly 0.3")
    }
}
