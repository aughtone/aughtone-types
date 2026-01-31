package io.github.aughtone.types.quantitative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SpeedTest {

    @Test
    fun `Speed cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Speed(-1.0)
        }
    }

    @Test
    fun `Accuracy cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Speed(1.0, -0.1f)
        }
    }

    @Test
    fun `plus operator`() {
        val s1 = Speed(10.0, 0.1f)
        val s2 = Speed(5.0, 0.2f)
        val result = s1 + s2
        assertEquals(15.0, result.mps)
        assertEquals((0.1f * 10.0f + 0.2f * 5.0f) / 15.0f, result.accuracy, 1e-6f)
    }

    @Test
    fun `plus operator with null accuracy`() {
        val s1 = Speed(10.0, 0.1f)
        val s2 = Speed(5.0, null)
        val result = s1 + s2
        assertEquals(15.0, result.mps)
        assertNull(result.accuracy)
    }

    @Test
    fun `minus operator`() {
        val s1 = Speed(10.0, 0.1f)
        val s2 = Speed(5.0, 0.2f)
        val result = s1 - s2
        assertEquals(5.0, result.mps)
        assertEquals((0.1f * 10.0f + 0.2f * 5.0f) / 5.0f, result.accuracy, 1e-6f)
    }

    @Test
    fun `times operator`() {
        val s1 = Speed(10.0, 0.1f)
        val result = s1 * 2.0
        assertEquals(20.0, result.mps)
        assertEquals(0.1f, result.accuracy)
    }

    @Test
    fun `div operator`() {
        val s1 = Speed(10.0, 0.1f)
        val result = s1 / 2.0
        assertEquals(5.0, result.mps)
        assertEquals(0.1f, result.accuracy)
    }

    @Test
    fun `div by zero`() {
        val s1 = Speed(10.0, 0.1f)
        assertFailsWith<ArithmeticException> {
            s1 / 0.0
        }
    }
}
