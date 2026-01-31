package io.github.aughtone.types.quantitative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AzimuthTest {

    @Test
    fun `Azimuth must be between 0 and 360`() {
        assertFailsWith<IllegalArgumentException> {
            Azimuth(-1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Azimuth(361.0)
        }
    }

    @Test
    fun `Accuracy cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Azimuth(1.0, -0.1f)
        }
    }

    @Test
    fun `plus operator`() {
        val a1 = Azimuth(10.0, 1.0f)
        val a2 = Azimuth(5.0, 2.0f)
        val result = a1 + a2
        assertEquals(15.0, result.degrees)
        assertEquals(3.0f, result.accuracy)
    }

    @Test
    fun `plus operator with wrap around`() {
        val a1 = Azimuth(350.0, 1.0f)
        val a2 = Azimuth(20.0, 2.0f)
        val result = a1 + a2
        assertEquals(10.0, result.degrees)
        assertEquals(3.0f, result.accuracy)
    }

    @Test
    fun `plus operator with null accuracy`() {
        val a1 = Azimuth(10.0, 1.0f)
        val a2 = Azimuth(5.0, null)
        val result = a1 + a2
        assertEquals(15.0, result.degrees)
        assertNull(result.accuracy)
    }

    @Test
    fun `minus operator`() {
        val a1 = Azimuth(10.0, 1.0f)
        val a2 = Azimuth(5.0, 2.0f)
        val result = a1 - a2
        assertEquals(5.0, result.degrees)
        assertEquals(3.0f, result.accuracy)
    }

    @Test
    fun `minus operator with wrap around`() {
        val a1 = Azimuth(10.0, 1.0f)
        val a2 = Azimuth(20.0, 2.0f)
        val result = a1 - a2
        assertEquals(350.0, result.degrees)
        assertEquals(3.0f, result.accuracy)
    }

    @Test
    fun `times operator`() {
        val a1 = Azimuth(10.0, 1.0f)
        val result = a1 * 2.0
        assertEquals(20.0, result.degrees)
        assertEquals(2.0f, result.accuracy)
    }

    @Test
    fun `div operator`() {
        val a1 = Azimuth(10.0, 1.0f)
        val result = a1 / 2.0
        assertEquals(5.0, result.degrees)
        assertEquals(0.5f, result.accuracy)
    }

    @Test
    fun `div by zero`() {
        val a1 = Azimuth(10.0, 1.0f)
        assertFailsWith<ArithmeticException> {
            a1 / 0.0
        }
    }
}
