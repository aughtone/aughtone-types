package io.github.aughtone.types

import io.github.aughtone.types.geo.GeoPolygon
import io.github.aughtone.types.geo.Winding
import io.github.aughtone.types.geo.windingOf
import io.github.aughtone.types.number.BigDecimal
import io.github.aughtone.types.number.BigInteger
import io.github.aughtone.types.number.RoundingMode
import io.github.aughtone.types.quantitative.Distance
import io.github.aughtone.types.uri.Uri
import io.github.aughtone.types.uri.Url
import io.github.aughtone.types.uri.url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Values at the edge of each type's range are refused cleanly or computed correctly. */
class BoundaryFixesTest {

    @Test
    fun `a Distance refuses a negative or NaN accuracy`() {
        assertFailsWith<IllegalArgumentException> { Distance(10.0, accuracy = -0.1f) }
        assertFailsWith<IllegalArgumentException> { Distance(10.0, accuracy = Float.NaN) }
        assertEquals(0.05f, Distance(10.0, accuracy = 0.05f).accuracy)
    }

    @Test
    fun `adding an infinite Distance leaves the accuracy unknown rather than failing`() {
        val sum = Distance(Double.POSITIVE_INFINITY, 0.1f) + Distance(1.0, 0.1f)
        assertNull(sum.accuracy)
    }

    @Test
    fun `shifting a BigInteger by Int MIN_VALUE terminates`() {
        assertEquals(BigInteger.ZERO, BigInteger.valueOf(12345).shiftLeft(Int.MIN_VALUE))
        assertEquals(BigInteger.valueOf(-1), BigInteger.valueOf(-12345).shiftLeft(Int.MIN_VALUE))
        assertFailsWith<ArithmeticException> { BigInteger.valueOf(1).shiftRight(Int.MIN_VALUE) }
        assertEquals(BigInteger.valueOf(6), BigInteger.valueOf(3).shiftLeft(1))
        assertEquals(BigInteger.valueOf(3), BigInteger.valueOf(6).shiftRight(1))
    }

    @Test
    fun `a negative bit address is refused`() {
        assertFailsWith<IllegalArgumentException> { BigInteger.ONE.setBit(-1) }
        assertFailsWith<IllegalArgumentException> { BigInteger.ONE.clearBit(Int.MIN_VALUE) }
        assertEquals(BigInteger.valueOf(5), BigInteger.ONE.setBit(2))
        assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(5).clearBit(0))
    }

    @Test
    fun `a Url holds the rules url parses by`() {
        assertFailsWith<IllegalArgumentException> { Url("https", "", "example.com", 70000, "/", "", "") }
        assertFailsWith<IllegalArgumentException> { Url("https", "", "example.com", -1, "/", "", "") }
        assertFailsWith<IllegalArgumentException> { Url("https", "", "", null, "/", "", "") }
        assertFailsWith<IllegalArgumentException> { Url("1http", "", "example.com", null, "/", "", "") }
        assertFailsWith<IllegalArgumentException> { Url("https", "", "[::1", null, "/", "", "") }
        assertEquals(url("https://user@[::1]:8080/a?b#c"), Url("https", "user", "[::1]", 8080, "/a", "b", "c"))
    }

    @Test
    fun `converting a Uri with an out-of-range port fails as an argument error`() {
        assertFailsWith<IllegalArgumentException> { Uri("https", "example.com:99999999999", "/", "", "").toUrl() }
        assertFailsWith<IllegalArgumentException> { Uri("https", "example.com:70000", "/", "", "").toUrl() }
    }

    @Test
    fun `the winding of an empty polygon is degenerate and a missing ring is refused`() {
        assertEquals(Winding.Degenerate, GeoPolygon(emptyList()).windingOf())
        val square = GeoPolygon(listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 1.0), listOf(0.0, 0.0))))
        assertEquals(Winding.CounterClockwise, square.windingOf())
        assertFailsWith<IllegalArgumentException> { square.windingOf(1) }
        assertFailsWith<IllegalArgumentException> { GeoPolygon(emptyList()).windingOf(1) }
    }

    private val tiny = BigDecimal.valueOf(1, Int.MAX_VALUE)
    private val huge = BigDecimal.valueOf(1, Int.MIN_VALUE)

    @Test
    fun `BigDecimals with scales at the ends of Int compare correctly`() {
        assertEquals(-1, tiny.compareTo(huge))
        assertEquals(1, huge.compareTo(tiny))
        assertEquals(-1, BigDecimal.valueOf(5, Int.MAX_VALUE).compareTo(BigDecimal.valueOf(3, Int.MIN_VALUE + 1)))
        assertEquals(1, BigDecimal.ONE.compareTo(tiny))
        assertEquals(-1, BigDecimal.valueOf(-1, Int.MIN_VALUE).compareTo(tiny))
    }

    @Test
    fun `BigDecimal arithmetic whose scale cannot be represented fails cleanly`() {
        assertFailsWith<ArithmeticException> { tiny + huge }
        assertFailsWith<ArithmeticException> { tiny - huge }
        assertFailsWith<ArithmeticException> { tiny * tiny }
        assertFailsWith<ArithmeticException> { huge * huge }
        assertFailsWith<ArithmeticException> { huge.setScale(Int.MAX_VALUE) }
    }

    @Test
    fun `dividing by a vastly larger BigDecimal rounds to zero at the requested scale`() {
        assertEquals(BigDecimal("0.00"), tiny.divide(huge, 2, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("0.01"), tiny.divide(huge, 2, RoundingMode.UP))
        assertEquals(BigDecimal("-0.01"), tiny.divide(BigDecimal.valueOf(-1, Int.MIN_VALUE), 2, RoundingMode.UP))
        assertFailsWith<ArithmeticException> { tiny.divide(huge, 2, RoundingMode.UNNECESSARY) }
    }

    @Test
    fun `rescaling far below a BigDecimal's digits rounds to zero`() {
        assertEquals(0, tiny.setScale(Int.MIN_VALUE, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO))
        assertEquals("0E+2147483648", tiny.setScale(Int.MIN_VALUE, RoundingMode.HALF_UP).toString())
        assertFailsWith<ArithmeticException> { tiny.setScale(Int.MIN_VALUE, RoundingMode.UNNECESSARY) }
    }

    @Test
    fun `ordinary BigDecimal arithmetic is unchanged`() {
        assertEquals(BigDecimal("3.75"), BigDecimal("1.25") + BigDecimal("2.5"))
        assertEquals(BigDecimal("3.125"), BigDecimal("1.25") * BigDecimal("2.5"))
        assertEquals(BigDecimal("0.33"), BigDecimal("1").divide(BigDecimal("3"), 2, RoundingMode.HALF_UP))
        assertEquals(BigDecimal("1.3"), BigDecimal("1.25").setScale(1, RoundingMode.HALF_UP))
    }
}
