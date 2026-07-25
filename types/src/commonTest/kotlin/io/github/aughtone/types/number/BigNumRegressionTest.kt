package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BigNumRegressionTest {

    @Test
    fun `single word divisor with high bit set`() {
        // The old signed 64-bit division produced q=3673924563, r=826362539.
        val u = BigInteger("16116354936157110357")
        val v = BigInteger("3752381294")
        val (q, r) = u.divideAndRemainder(v)
        assertEquals("4294967295", q.toString())
        assertEquals("57330627", r.toString())
    }

    @Test
    fun `knuth division with high remainder digit`() {
        // The old signed qHat estimate produced q=9399068681, r=1816005894997149.
        val u = BigInteger("32793488520887023483264471")
        val v = BigInteger("2635268068981226")
        val (q, r) = u.divideAndRemainder(v)
        assertEquals("12444080701", q.toString())
        assertEquals("1716212285345045", r.toString())
    }

    @Test
    fun `knuth division with max quotient digit`() {
        val u = BigInteger("152500343857171811042462722774200382620646182543642353")
        val v = BigInteger("10097063807998904369789058514147")
        val (q, r) = u.divideAndRemainder(v)
        assertEquals("15103434697160265665006", q.toString())
        assertEquals("8778729725674663474767329802471", r.toString())
    }

    @Test
    fun `division reconstruction identity`() {
        val pairs = listOf(
            Pair("16116354936157110357", "3752381294"),
            Pair("32793488520887023483264471", "2635268068981226"),
            Pair("-32793488520887023483264471", "2635268068981226"),
            Pair("32793488520887023483264471", "-2635268068981226"),
            Pair("-152500343857171811042462722774200382620646182543642353", "-10097063807998904369789058514147"),
        )
        for ((uStr, vStr) in pairs) {
            val u = BigInteger(uStr)
            val v = BigInteger(vStr)
            val (q, r) = u.divideAndRemainder(v)
            assertEquals(u, q.multiply(v).add(r), "reconstruction for $uStr / $vStr")
        }
    }

    @Test
    fun `shift right to zero is canonical zero`() {
        val one = BigInteger.valueOf(1)
        // The old implementation returned an invalid value with signum 1 and empty magnitude.
        assertEquals(BigInteger.ZERO, one.shiftRight(1))
        assertEquals(0, one.shiftRight(1).signum)
        assertEquals(0, one.shiftRight(1).compareTo(BigInteger.ZERO))
        assertEquals(BigInteger.ZERO, BigInteger.valueOf(5).shiftRight(3))
        assertEquals(BigInteger.ZERO, BigInteger.valueOf(0xFFFF).shiftRight(16))
        assertEquals(BigInteger.valueOf(1), BigInteger.valueOf(2).shiftRight(1))
    }

    @Test
    fun `invalid internal state is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            BigInteger.ZERO.copy(signum = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            BigInteger.ONE.copy(signum = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            BigInteger.ONE.copy(magnitude = intArrayOf(1, 0))
        }
        assertFailsWith<IllegalArgumentException> {
            BigInteger.ONE.copy(signum = 2)
        }
    }

    @Test
    fun `exact divide beyond two hundred digits`() {
        // 1 / 2^201 terminates with exactly 201 fractional digits; the old
        // implementation gave up after 200 and threw.
        val divisor = BigDecimal(BigInteger.ONE.shiftLeft(201), 0)
        val result = BigDecimal.ONE.divide(divisor)
        assertEquals(201, result.scale)
        assertEquals(0, result.multiply(divisor).compareTo(BigDecimal.ONE))
    }

    @Test
    fun `non terminating divide still throws`() {
        assertFailsWith<ArithmeticException> {
            BigDecimal.ONE.divide(BigDecimal("3"))
        }
        assertFailsWith<ArithmeticException> {
            BigDecimal("1").divide(BigDecimal("0.000007"))
        }
    }

    @Test
    fun `exact divide sign and scale`() {
        assertEquals("-1.25", BigDecimal("-10").divide(BigDecimal("8")).toString())
        assertEquals("5.0", BigDecimal("10.0").divide(BigDecimal("2")).toString())
        assertEquals("0.5", BigDecimal("1").divide(BigDecimal("2")).toString())
        val z = BigDecimal("0.00").divide(BigDecimal("2.0"))
        assertEquals(0, z.unscaledValue.signum)
        assertEquals(1, z.scale)
    }
}
