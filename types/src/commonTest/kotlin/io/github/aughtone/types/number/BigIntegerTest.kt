package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class BigIntegerTest {
    val num1 = BigInteger("12345678901234567890")
    val num2 = BigInteger("98765432109876543210")
    val num3 = BigInteger("1")
    val num4 = BigInteger("-100")
    val num5 = BigInteger("50")
        val num6 = BigInteger("-1000")

    @Test
    fun `test Addition`() {
        assertEquals(BigInteger("111111111011111111100"), num1 + num2)
        assertEquals(BigInteger("12345678901234567891"), num1 + num3)
        assertEquals(BigInteger("-99"), num3 + num4)
    }


    @Test
    fun `test Subtraction`() {
        assertEquals(BigInteger("86419753208641975320"), num2 - num1)
        assertEquals(BigInteger("-86419753208641975320"), num1 - num2)
        assertEquals(BigInteger("-101"), num4 - num3)
    }

    @Test
    fun `test Multiplication`() {
        assertEquals(BigInteger("1219326311370217952237463801111263526900"), num1 * num2)
        assertEquals(BigInteger("-100"), num3 * num4)
        assertEquals(BigInteger("12345678901234567890"), num1 * num3)
        assertEquals(BigInteger("1"), num3 * num3)
    }

    @Test
    fun `test Division`() {
        assertEquals(BigInteger("80000000000000000000"), num2 / num1)
        assertEquals(BigInteger("0"), num1 / num2)
        assertEquals(BigInteger("-2"), num4 / num5)
        assertEquals(BigInteger("-0.5"), num5 / num4)
        assertEquals(BigInteger("98765432109876543210"), num2 / num3)
    }

    fun `test Remainder`() {
        assertEquals(BigInteger("9876543210"), num2 % num1)
        assertEquals(BigInteger("12345678901234567890"), num1 % num2)
        assertEquals(BigInteger("0"), num4 % num5)
        assertEquals(BigInteger("50"), num5 % num4)
        assertEquals(BigInteger("0"), num2 % num3)
    }

    fun `test Comparison`() {
        assertFalse( num1 > num2)
        assertTrue( num1 < num2)
        assertTrue( num1 == num1)
        assertFalse(num4 > num3)
        assertTrue(num3 < num4)
    }

    fun `test Abs`() {
        assertEquals( BigInteger("100"),num4.abs())
    }

}
