package io.github.aughtone.types.financial

import io.github.aughtone.types.number.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
        assertEquals(0L, zeroMoney.minorUnits)
        assertEquals(usd, zeroMoney.currency)
    }

    @Test
    fun `creating money with Double works`() {
        val money = Money(10.50, usd)
        assertEquals(1050L, money.minorUnits)
        assertEquals(usd, money.currency)
    }

    @Test
    fun `creating money with Long minor units works`() {
        val money = Money(12345L, usd)
        assertEquals(12345L, money.minorUnits)
        assertEquals(usd, money.currency)
    }

    @Test
    fun `creating money with BigDecimal works`() {
        val bd = BigDecimal("10.50")
        val money = Money(bd, usd)
        assertEquals(1050L, money.minorUnits)
        assertEquals(usd, money.currency)
    }

    @Test
    fun `plus and minus operators for Money`() {
        val m1 = Money(1000L, usd)
        val m2 = Money(550L, usd)
        assertEquals(1550L, (m1 + m2).minorUnits)
        assertEquals(450L, (m1 - m2).minorUnits)
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
        // $10 * $2 = $20
        assertTrue { (m1 * m2).value.compareTo(BigDecimal("20")) == 0 }
        assertEquals(2000L, (m1 * m2).minorUnits)
        assertEquals(5.0, m1 / m2, 0.0)
    }

    @Test
    fun `plus and minus operators for Double`() {
        val m1 = Money(1000L, usd)
        assertEquals(1250L, (m1 + 2.50).minorUnits)
        assertEquals(750L, (m1 - 2.50).minorUnits)
    }

    @Test
    fun `times and div operators for Double`() {
        val m1 = Money(1000L, usd)
        assertEquals(2500L, (m1 * 2.5).minorUnits)
        assertEquals(400L, (m1 / 2.5).minorUnits)
    }

    @Test
    fun `plus and minus operators for Long`() {
        val m1 = Money(1000L, usd)
        // Adding 250 as minor units
        assertEquals(1250L, (m1 + 250L).minorUnits)
        assertEquals(750L, (m1 - 250L).minorUnits)
    }

    @Test
    fun `times and div operators for Long`() {
        val m1 = Money(1000L, usd)
        assertEquals(2000L, (m1 * 2L).minorUnits)
        assertEquals(500L, (m1 / 2L).minorUnits)
    }

    @Test
    fun `extension functions work`() {
        val money = 123.45.toMoney(usd)
        assertEquals(12345L, money.minorUnits)
        assertEquals(123.45, money.toDouble())
    }

    @Test
    fun `floating point inaccuracy is resolved`() {
        val m1 = 0.1.toMoney(usd)
        val m2 = 0.2.toMoney(usd)
        val result = m1 + m2
        assertEquals(0.3, result.toDouble(), "The sum should be exactly 0.3")
    }

    @Test
    fun `money with zero digits JPY`() {
        val jpy = Currency(
            code = "JPY",
            number = 392,
            digits = 0,
            name = "Yen",
            symbol = "¥"
        )
        val money = Money(100.0, jpy)
        assertEquals(100L, money.minorUnits)
        assertEquals(100.0, money.toDouble())
    }

    @Test
    fun `money with three digits KWD`() {
        val kwd = Currency(
            code = "KWD",
            number = 414,
            digits = 3,
            name = "Dinar",
            symbol = "KD"
        )
        val money = Money(10.500, kwd)
        assertEquals(10500L, money.minorUnits)
        assertEquals(10.5, money.toDouble())
    }

    @Test
    fun `tracking sub-minor units`() {
        val money = Money(BigDecimal("1.23456"), usd)
        assertEquals(1.23456, money.toDouble())
        // minorUnits should be rounded to 2 digits for USD
        assertEquals(123L, money.minorUnits)
    }
}
