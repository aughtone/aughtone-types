package io.github.aughtone.types.quantitative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AltitudeTest {

    @Test
    fun `Accuracy cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Altitude(1.0, -0.1f)
        }
    }

    @Test
    fun `plus operator`() {
        val a1 = Altitude(10.0, 1.0f)
        val a2 = Altitude(5.0, 2.0f)
        val result = a1 + a2
        assertEquals(15.0, result.meters)
        assertEquals(3.0f, result.accuracy)
    }

    @Test
    fun `plus operator with null accuracy`() {
        val a1 = Altitude(10.0, 1.0f)
        val a2 = Altitude(5.0, null)
        val result = a1 + a2
        assertEquals(15.0, result.meters)
        assertNull(result.accuracy)
    }

    @Test
    fun `minus operator`() {
        val a1 = Altitude(10.0, 1.0f)
        val a2 = Altitude(5.0, 2.0f)
        val result = a1 - a2
        assertEquals(5.0, result.meters)
        assertEquals(3.0f, result.accuracy)
    }

    @Test
    fun `times operator`() {
        val a1 = Altitude(10.0, 1.0f)
        val result = a1 * 2.0
        assertEquals(20.0, result.meters)
        assertEquals(2.0f, result.accuracy)
    }

    @Test
    fun `div operator`() {
        val a1 = Altitude(10.0, 1.0f)
        val result = a1 / 2.0
        assertEquals(5.0, result.meters)
        assertEquals(0.5f, result.accuracy)
    }

    @Test
    fun `div by zero`() {
        val a1 = Altitude(10.0, 1.0f)
        assertFailsWith<ArithmeticException> {
            a1 / 0.0
        }
    }
}
