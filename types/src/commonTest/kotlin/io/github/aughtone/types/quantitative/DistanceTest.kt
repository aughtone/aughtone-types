package io.github.aughtone.types.quantitative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DistanceTest {

    @Test
    fun `Distance cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Distance(-1.0)
        }
    }

    @Test
    fun `plus operator`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, 0.2f)
        val result = d1 + d2
        assertEquals(15.0, result.meters)
        assertEquals((0.1f * 10.0f + 0.2f * 5.0f) / 15.0f, result.accuracy!!, 1e-6f)
    }

    @Test
    fun `plus operator with null accuracy`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, null)
        val result = d1 + d2
        assertEquals(15.0, result.meters)
        assertNull(result.accuracy)
    }

    @Test
    fun `plus operator resulting in zero`() {
        val d1 = Distance(0.0, 0.1f)
        val d2 = Distance(0.0, 0.2f)
        val result = d1 + d2
        assertEquals(0.0, result.meters)
        assertNull(result.accuracy)
    }

    @Test
    fun `minus operator`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, 0.2f)
        val result = d1 - d2
        assertEquals(5.0, result.meters)
        assertEquals((0.1f * 10.0f + 0.2f * 5.0f) / 5.0f, result.accuracy!!, 1e-6f)
    }

    @Test
    fun `minus operator resulting in zero`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(10.0, 0.2f)
        val result = d1 - d2
        assertEquals(0.0, result.meters)
        assertNull(result.accuracy)
    }

    @Test
    fun `times operator`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, 0.2f)
        val result = d1 * d2
        assertEquals(50.0, result.meters)
        assertEquals(0.1f + 0.2f, result.accuracy!!, 1e-6f)
    }

    @Test
    fun `times operator with null accuracy`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, null)
        val result = d1 * d2
        assertEquals(50.0, result.meters)
        assertNull(result.accuracy)
    }

    @Test
    fun `div operator`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, 0.2f)
        val result = d1 / d2
        assertEquals(2.0, result.meters)
        assertEquals(0.1f + 0.2f, result.accuracy!!, 1e-6f)
    }

    @Test
    fun `div operator with null accuracy`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(5.0, null)
        val result = d1 / d2
        assertEquals(2.0, result.meters)
        assertNull(result.accuracy)
    }

    @Test
    fun `div by zero`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(0.0, 0.2f)
        assertFailsWith<ArithmeticException> {
            d1 / d2
        }
    }
    
    @Test
    fun `div by Int`() {
        val d1 = Distance(10.0, 0.1f)
        val result = d1 / 2
        assertEquals(5.0, result.meters)
        assertEquals(0.1f, result.accuracy)
    }

    @Test
    fun `div by zero Int`() {
        val d1 = Distance(10.0, 0.1f)
        assertFailsWith<ArithmeticException> {
            d1 / 0
        }
    }

    @Test
    fun `rem operator`() {
        val d1 = Distance(10.0, 0.1f)
        val d2 = Distance(3.0, 0.2f)
        val result = d1 % d2
        assertEquals(1.0, result.meters, 1e-9)
        assertNull(result.accuracy)
    }
}
