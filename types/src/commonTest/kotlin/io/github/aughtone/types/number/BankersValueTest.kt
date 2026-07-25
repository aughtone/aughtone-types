package io.github.aughtone.types.number

import io.github.aughtone.types.financial.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BankersValueTest {

    @Test
    fun `plus operator`() {
        val v1 = BankersValue.fromLong(100)
        val v2 = BankersValue.fromLong(50)
        val result = v1 + v2
        assertEquals(150, result.toLong())
    }

    @Test
    fun `minus operator`() {
        val v1 = BankersValue.fromLong(100)
        val v2 = BankersValue.fromLong(50)
        val result = v1 - v2
        assertEquals(50, result.toLong())
    }

    @Test
    fun `times operator`() {
        val v1 = BankersValue.fromLong(1000) // 10.00
        val v2 = BankersValue.fromLong(500)  // 5.00
        val result = v1 * v2
        assertEquals(5000, result.toLong()) // 10.00 * 5.00 = 50.00 -> 5000 cents
    }

    @Test
    fun `rem operator`() {
        val v1 = BankersValue.fromLong(105)
        val v2 = BankersValue.fromLong(100)
        val result = v1 % v2
        assertEquals(5, result.toLong())
    }

    @Test
    fun `div operator with another BankersValue`() {
        // Dividing by $1.00 is the identity: $3.50 / $1.00 = $3.50 (350 cents)
        assertEquals(350, (BankersValue.fromDouble(3.5) / BankersValue.fromDouble(1.0)).toLong())
        assertEquals(250, (BankersValue.fromDouble(2.5) / BankersValue.fromDouble(1.0)).toLong())

        // Half-cent quotients use banker's rounding: $0.07 / $2.00 = 3.5 cents -> 4
        assertEquals(4, (BankersValue.fromLong(7) / BankersValue.fromDouble(2.0)).toLong())
        // $0.05 / $2.00 = 2.5 cents -> 2
        assertEquals(2, (BankersValue.fromLong(5) / BankersValue.fromDouble(2.0)).toLong())

        // Normal rounding: $0.10 / $3.00 = 3.33... cents -> 3
        assertEquals(3, (BankersValue.fromLong(10) / BankersValue.fromDouble(3.0)).toLong())
    }

    @Test
    fun `times and div are inverses for exact cases`() {
        val a = BankersValue.fromLong(1000) // $10.00
        val b = BankersValue.fromLong(500)  // $5.00
        assertEquals(5000, (a * b).toLong())
        assertEquals(1000, ((a * b) / b).toLong())
        assertEquals(500, ((a * b) / a).toLong())
    }

    @Test
    fun `times is exact for large amounts`() {
        // 482637353 * 507069465 / 100 = 2447306643747261.45 -> 2447306643747261
        // The old Double-based path returned 2447306643747262.
        val a = BankersValue.fromLong(482637353)
        val b = BankersValue.fromLong(507069465)
        assertEquals(2447306643747261, (a * b).toLong())
    }

    @Test
    fun `times applies bankers rounding on half cents`() {
        // 5 * 30 cents = 150 / 100 = 1.5 -> 2 (even)
        assertEquals(2, (BankersValue.fromLong(5) * BankersValue.fromLong(30)).toLong())
        // 5 * 50 cents = 250 / 100 = 2.5 -> 2 (even)
        assertEquals(2, (BankersValue.fromLong(5) * BankersValue.fromLong(50)).toLong())
        // negative tie: -5 * 30 = -150 / 100 = -1.5 -> -2 (even)
        assertEquals(-2, (BankersValue.fromLong(-5) * BankersValue.fromLong(30)).toLong())
    }

    @Test
    fun `div with negative values uses bankers rounding`() {
        // -5 / 2 = -2.5 -> -2 (even)
        assertEquals(-2, (BankersValue.fromLong(-5) / 2).toLong())
        // -7 / 2 = -3.5 -> -4 (even)
        assertEquals(-4, (BankersValue.fromLong(-7) / 2).toLong())
    }

    @Test
    fun `fromDouble detects half-cent ties exactly`() {
        // 0.575 dollars = 57.5 cents -> 58 (even); the Double product 0.575 * 100
        // is 57.49999999999999, which the old implementation rounded to 57.
        assertEquals(58, BankersValue.fromDouble(0.575).toLong())
        assertEquals(-58, BankersValue.fromDouble(-0.575).toLong())
        // 1.005 dollars = 100.5 cents -> 100 (even)
        assertEquals(100, BankersValue.fromDouble(1.005).toLong())
        // 2.675 dollars = 267.5 cents -> 268 (even)
        assertEquals(268, BankersValue.fromDouble(2.675).toLong())
    }

    @Test
    fun `nan and infinity are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            BankersValue.fromDouble(Double.NaN)
        }
        assertFailsWith<IllegalArgumentException> {
            BankersValue.fromDouble(Double.POSITIVE_INFINITY)
        }
        assertFailsWith<IllegalArgumentException> {
            BankersValue.fromLong(100) / Double.NaN
        }
        assertFailsWith<IllegalArgumentException> {
            BankersValue.fromLong(100) / Double.NEGATIVE_INFINITY
        }
    }

    @Test
    fun `div operator with Int`() {
        // 3.5 rounds to 4
        assertEquals(4, (BankersValue.fromLong(7) / 2).toLong())

        // 2.5 rounds to 2
        assertEquals(2, (BankersValue.fromLong(5) / 2).toLong())
    }

    @Test
    fun `div by zero throws ArithmeticException`() {
        assertFailsWith<ArithmeticException> {
            BankersValue.fromLong(100) / BankersValue.fromLong(0)
        }
        assertFailsWith<ArithmeticException> {
            BankersValue.fromLong(100) / 0
        }
    }

    @Test
    fun `compareTo`() {
        val v1 = BankersValue.fromLong(100)
        val v2 = BankersValue.fromLong(200)
        val v3 = BankersValue.fromLong(100)
        assertTrue(v1 < v2)
        assertTrue(v2 > v1)
        assertEquals(0, v1.compareTo(v3))
    }

    @Test
    fun `equals and hashCode`() {
        val v1 = BankersValue.fromLong(100)
        val v2 = BankersValue.fromLong(100)
        val v3 = BankersValue.fromLong(200)
        assertEquals(v1, v2)
        assertNotEquals(v1, v3)
        assertEquals(v1.hashCode(), v2.hashCode())
    }

    @Test
    fun `toLong and toDouble`() {
        val v = BankersValue.fromLong(12345)
        assertEquals(12345, v.toLong())
        assertEquals(123.45, v.toDouble(), 0.0)
    }

    @Test
    fun `fromDouble bankers rounding`(){
        // 2.5 cents rounds to 2 cents
        assertEquals(2, BankersValue.fromDouble(0.025).toLong())
        // 3.5 cents rounds to 4 cents
        assertEquals(4, BankersValue.fromDouble(0.035).toLong())
    }

    @Test
    fun `companion object factories`() {
        val fromLong = BankersValue.fromLong(123L)
        assertEquals(123, fromLong.toLong())

        val fromInt = BankersValue.fromInt(456)
        assertEquals(456, fromInt.toLong())

        val fromDouble = BankersValue.fromDouble(123.45)
        assertEquals(12345, fromDouble.toLong())
    }

    @Test
    fun `times and div operators for Long`() {
        val resultValue = BankersValue.fromLong(1000L ) / 2L
        assertEquals(500L, resultValue.toLong())
    }
}
