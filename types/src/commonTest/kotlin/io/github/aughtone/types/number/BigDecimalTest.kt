package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class BigDecimalTest {

    val num1 = BigDecimal("123.45")
    val num2 = BigDecimal("67.89")
    val num3 = BigDecimal("-100.50")
    val num4 = BigDecimal("50")

    @Test
    fun `test Addition`() {
        assertEquals(BigDecimal("191.34"), num1 + num2)
        assertEquals(BigDecimal("22.95"), num1 + num3)
        assertEquals(BigDecimal("173.45"), num1 + num4)
    }

    @Test
    fun `test Subtraction`() {
        assertEquals(BigDecimal("55.56"), num1 - num2)
        assertEquals(BigDecimal("223.95"), num1 - num3)
        assertEquals(BigDecimal("73.45"), num1 - num4)
        assertEquals(BigDecimal("-73.45"), num4 - num1)
    }

    @Test
    fun `test Multiplication`() {
        assertEquals(BigDecimal("8381.6805"), num1 * num2)
        assertEquals(BigDecimal("-5025.0"), num3 * num4)
    }

    @Test
    fun `test Division`() {
        assertEquals(BigDecimal("1.818382530564148"), num1 / num2)
        assertEquals(BigDecimal("-2.01"), num3 / num4)
        assertEquals(BigDecimal("-0.4975124378109452"), num4 / num3)
    }

    @Test
    fun `test Comparison`() {
        assertFalse(num1 > num2)
        assertTrue(num1 > num2)
        assertFalse(num1 == num2)
        assertTrue(num1 == BigDecimal("123.45"))
        assertTrue(num1 > num3)
        assertTrue(num3 < num1)
    }
}
