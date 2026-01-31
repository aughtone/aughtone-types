package io.github.aughtone.types.number

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
        // 3.5 rounds to 4 (even)
        assertEquals(4, (BankersValue.fromDouble(3.5) / BankersValue.fromDouble(1.0)).toLong())

        // 2.5 rounds to 2 (even)
        assertEquals(2, (BankersValue.fromDouble(2.5) / BankersValue.fromDouble(1.0)).toLong())

        // 4.5 rounds to 4
        assertEquals(4, (BankersValue.fromDouble(4.5) / BankersValue.fromDouble(1.0)).toLong())

        // Normal rounding
        assertEquals(3, (BankersValue.fromDouble(2.51) / BankersValue.fromDouble(1.0)).toLong())
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
}
