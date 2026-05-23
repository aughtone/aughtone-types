package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse


class BigDecimalTest {

    @Test
    fun `exact binary representation from Double`() {
        val bd = BigDecimal.valueOf(0.1)
        // Double 0.1 is exactly 0.1000000000000000055511151231257827021181583404541015625
        val expected = "0.1000000000000000055511151231257827021181583404541015625"
        assertEquals(expected, bd.toString())
    }

    @Test
    fun `exact decimal representation from String`() {
        val bd = BigDecimal.parseString("0.1")
        assertEquals("0.1", bd.toString())
        assertEquals(BigInteger.ONE, bd.unscaledValue)
        assertEquals(1, bd.scale)
    }

    @Test
    fun `strict equality compared with value scale`() {
        val bd1 = BigDecimal.parseString("2.0")
        val bd2 = BigDecimal.parseString("2.00")
        
        // Strict equality must return false because the scales differ (1 vs 2)
        assertNotEquals(bd1, bd2)
        
        // Ensure same scale and value is equal
        val bd3 = BigDecimal.parseString("2.0")
        assertEquals(bd1, bd3)
    }

    @Test
    fun `numerical value comparison ignores scale`() {
        val bd1 = BigDecimal.parseString("2.0")
        val bd2 = BigDecimal.parseString("2.00")
        
        // compareTo compares exact mathematical values
        assertEquals(0, bd1.compareTo(bd2))
        
        val bd3 = BigDecimal.parseString("2.01")
        assertTrue(bd1 < bd3)
        assertTrue(bd3 > bd2)
    }

    @Test
    fun `non-terminating decimal expansion guard`() {
        val one = BigDecimal.parseString("1")
        val three = BigDecimal.parseString("3")
        
        // 1 / 3 has infinite decimal expansion (0.3333...), so this must throw an ArithmeticException
        assertFailsWith<ArithmeticException> {
            one.divide(three)
        }
    }

    @Test
    fun `context-based rounding termination`() {
        val one = BigDecimal.parseString("1")
        val three = BigDecimal.parseString("3")
        
        // 1 / 3 with scale 2 and HALF_UP rounding should terminate to 0.33
        val result = one.divide(three, 2, RoundingMode.HALF_UP)
        assertEquals("0.33", result.toString())

        // 2 / 3 with scale 2 and HALF_UP rounding should terminate to 0.67
        val two = BigDecimal.parseString("2")
        val result2 = two.divide(three, 2, RoundingMode.HALF_UP)
        assertEquals("0.67", result2.toString())
    }

    @Test
    fun `stripping trailing zeros`() {
        val bd = BigDecimal.parseString("600.0")
        val stripped = bd.stripTrailingZeros()
        
        // 600.0 has unscaled 6000, scale 1. Stripping gives unscaled 6, scale -2.
        assertEquals(BigInteger.valueOf(6), stripped.unscaledValue)
        assertEquals(-2, stripped.scale)
        assertEquals("6E+2", stripped.toString())
    }

    @Test
    fun `basic arithmetic operations`() {
        val a = BigDecimal.parseString("12.34")
        val b = BigDecimal.parseString("5.6")

        assertEquals("17.94", a.add(b).toString())
        assertEquals("6.74", a.subtract(b).toString())
        assertEquals("69.104", a.multiply(b).toString())
        
        // exact division: 12.34 / 2 = 6.17
        assertEquals("6.17", a.divide(BigDecimal.parseString("2")).toString())
    }

    @Test
    fun `comprehensive rounding modes`() {
        val divisor = BigDecimal.parseString("100")
        
        // 5.5 / 100 = 0.055
        val bd55 = BigDecimal.parseString("5.5")
        
        // HALF_UP: 0.055 -> 0.06
        assertEquals("0.06", bd55.divide(divisor, 2, RoundingMode.HALF_UP).toString())
        
        // HALF_DOWN: 0.055 -> 0.05
        assertEquals("0.05", bd55.divide(divisor, 2, RoundingMode.HALF_DOWN).toString())
        
        // HALF_EVEN: 0.055 -> 0.06 (even neighbor)
        assertEquals("0.06", bd55.divide(divisor, 2, RoundingMode.HALF_EVEN).toString())
        
        // 4.5 / 100 = 0.045
        val bd45 = BigDecimal.parseString("4.5")
        // HALF_EVEN: 0.045 -> 0.04 (even neighbor)
        assertEquals("0.04", bd45.divide(divisor, 2, RoundingMode.HALF_EVEN).toString())
        
        // UP: 0.041 -> 0.05; -0.041 -> -0.05
        val bd41 = BigDecimal.parseString("4.1")
        assertEquals("0.05", bd41.divide(divisor, 2, RoundingMode.UP).toString())
        val bdNeg41 = BigDecimal.parseString("-4.1")
        assertEquals("-0.05", bdNeg41.divide(divisor, 2, RoundingMode.UP).toString())
        
        // DOWN: 0.049 -> 0.04; -0.049 -> -0.04
        val bd49 = BigDecimal.parseString("4.9")
        assertEquals("0.04", bd49.divide(divisor, 2, RoundingMode.DOWN).toString())
        val bdNeg49 = BigDecimal.parseString("-4.9")
        assertEquals("-0.04", bdNeg49.divide(divisor, 2, RoundingMode.DOWN).toString())
        
        // CEILING: 0.041 -> 0.05; -0.049 -> -0.04
        assertEquals("0.05", bd41.divide(divisor, 2, RoundingMode.CEILING).toString())
        assertEquals("-0.04", bdNeg49.divide(divisor, 2, RoundingMode.CEILING).toString())
        
        // FLOOR: 0.049 -> 0.04; -0.041 -> -0.05
        assertEquals("0.04", bd49.divide(divisor, 2, RoundingMode.FLOOR).toString())
        assertEquals("-0.05", bdNeg41.divide(divisor, 2, RoundingMode.FLOOR).toString())
        
        // UNNECESSARY: 4.0 / 100 = 0.040 -> 0.04
        val bd40 = BigDecimal.parseString("4")
        assertEquals("0.04", bd40.divide(divisor, 2, RoundingMode.UNNECESSARY).toString())
        
        assertFailsWith<ArithmeticException> {
            bd41.divide(divisor, 2, RoundingMode.UNNECESSARY)
        }
    }

    @Test
    fun `setScale scale adjustment`() {
        val bd = BigDecimal.parseString("1.23456")
        
        // Scale expansion (adds trailing zeros conceptually)
        val expanded = bd.setScale(8)
        assertEquals("1.23456000", expanded.toString())
        assertEquals(8, expanded.scale)
        
        // Scale reduction requiring rounding
        assertEquals("1.23", bd.setScale(2, RoundingMode.DOWN).toString())
        assertEquals("1.23", bd.setScale(2, RoundingMode.HALF_UP).toString())
        assertEquals("1.235", bd.setScale(3, RoundingMode.HALF_UP).toString())
        
        // Scale reduction with no rounding required (trailing zeros)
        val zeros = BigDecimal.parseString("1.2000")
        assertEquals("1.2", zeros.setScale(1).toString())
        assertEquals("1.20", zeros.setScale(2).toString())
        
        // Unnecessary rounding error
        assertFailsWith<ArithmeticException> {
            bd.setScale(2)
        }
    }

    @Test
    fun `verify spec defined big decimal behaviors`() {
        // Row 1: Float 0.1 (Double 0.1) | Init | | 0.1000000000000000055...
        val r1 = BigDecimal.valueOf(0.1)
        assertTrue(r1.toString().startsWith("0.1000000000000000055"))

        // Row 2: String "0.1" | Init | | 0.1
        val r2 = BigDecimal.parseString("0.1")
        assertEquals("0.1", r2.toString())

        // Row 3: "2.0" | equals | "2.00" | False
        val r3a = BigDecimal.parseString("2.0")
        val r3b = BigDecimal.parseString("2.00")
        assertFalse(r3a.equals(r3b))

        // Row 4: "2.0" | compareTo | "2.00" | Equal (0)
        assertEquals(0, r3a.compareTo(r3b))

        // Row 5: "1" | divide | "3" | Error
        val one = BigDecimal.parseString("1")
        val three = BigDecimal.parseString("3")
        assertFailsWith<ArithmeticException> {
            one.divide(three)
        }

        // Row 6: "1" | divide | "3", Scale=2, HALF_UP | "0.33"
        val r6 = one.divide(three, 2, RoundingMode.HALF_UP)
        assertEquals("0.33", r6.toString())

        // Row 7: "600.0" | stripTrailingZeros | | "6E+2"
        val r7 = BigDecimal.parseString("600.0").stripTrailingZeros()
        assertEquals("6E+2", r7.toString())
    }

    @Test
    fun `verify scientific notation parsing edge cases`() {
        // Positive exponent
        val bd1 = BigDecimal.parseString("1.23e4")
        assertEquals("123E+2", bd1.toString())
        assertEquals(-2, bd1.scale)
        assertEquals(BigInteger.valueOf(123), bd1.unscaledValue)

        val bd2 = BigDecimal.parseString("1.23e+4")
        assertEquals("123E+2", bd2.toString())

        // Negative exponent
        val bd3 = BigDecimal.parseString("-1.23e-4")
        assertEquals("-0.000123", bd3.toString())
        assertEquals(6, bd3.scale)
        assertEquals(BigInteger.valueOf(-123), bd3.unscaledValue)

        // Zero exponent
        val bd4 = BigDecimal.parseString("1.23e0")
        assertEquals("1.23", bd4.toString())
        assertEquals(2, bd4.scale)

        // Spaces and case insensitive
        val bd5 = BigDecimal.parseString("  1.23E-4  ")
        assertEquals("0.000123", bd5.toString())
    }

    @Test
    fun `verify initialization from BigInt and scale`() {
        val unscaled = BigInteger.valueOf(12345)
        val bd1 = BigDecimal(unscaled, 3)
        assertEquals("12.345", bd1.toString())

        val bd2 = BigDecimal(unscaled, -3)
        assertEquals("12345E+3", bd2.toString())

        val bdZero = BigDecimal(BigInteger.ZERO, 5)
        assertEquals("0.00000", bdZero.toString())
    }

    @Test
    fun `verify comprehensive setScale rounding modes`() {
        val bd = BigDecimal.parseString("1.234")
        
        // UP: away from zero
        assertEquals("1.24", bd.setScale(2, RoundingMode.UP).toString())
        assertEquals("-1.24", BigDecimal.parseString("-1.234").setScale(2, RoundingMode.UP).toString())

        // DOWN: towards zero
        assertEquals("1.23", bd.setScale(2, RoundingMode.DOWN).toString())
        assertEquals("-1.23", BigDecimal.parseString("-1.234").setScale(2, RoundingMode.DOWN).toString())

        // CEILING: towards positive infinity
        assertEquals("1.24", bd.setScale(2, RoundingMode.CEILING).toString())
        assertEquals("-1.23", BigDecimal.parseString("-1.234").setScale(2, RoundingMode.CEILING).toString())

        // FLOOR: towards negative infinity
        assertEquals("1.23", bd.setScale(2, RoundingMode.FLOOR).toString())
        assertEquals("-1.24", BigDecimal.parseString("-1.234").setScale(2, RoundingMode.FLOOR).toString())

        // HALF_UP: nearest neighbor, tie breaks to rounding up
        assertEquals("1.24", BigDecimal.parseString("1.235").setScale(2, RoundingMode.HALF_UP).toString())
        assertEquals("1.23", BigDecimal.parseString("1.234").setScale(2, RoundingMode.HALF_UP).toString())

        // HALF_DOWN: nearest neighbor, tie breaks to rounding down
        assertEquals("1.23", BigDecimal.parseString("1.235").setScale(2, RoundingMode.HALF_DOWN).toString())
        assertEquals("1.24", BigDecimal.parseString("1.236").setScale(2, RoundingMode.HALF_DOWN).toString())

        // HALF_EVEN: nearest neighbor, tie breaks to even digit
        assertEquals("1.24", BigDecimal.parseString("1.235").setScale(2, RoundingMode.HALF_EVEN).toString())
        assertEquals("1.22", BigDecimal.parseString("1.225").setScale(2, RoundingMode.HALF_EVEN).toString())
    }

    @Test
    fun `verify Kotlin arithmetic operators`() {
        val a = BigDecimal.parseString("10.5")
        val b = BigDecimal.parseString("2.1")

        assertEquals(BigDecimal.parseString("12.6"), a + b)
        assertEquals(BigDecimal.parseString("8.4"), a - b)
        assertEquals(BigDecimal.parseString("22.05"), a * b)
        assertEquals(BigDecimal.parseString("5"), a / b)
        assertEquals(BigDecimal.parseString("-10.5"), -a)
        assertEquals(BigDecimal.ZERO, -BigDecimal.ZERO)
    }
}

