package io.github.aughtone.types.quantitative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoordinatesTest {

    @Test
    fun `Coordinates must have valid latitude and longitude`() {
        assertFailsWith<IllegalArgumentException> {
            Coordinates(91.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Coordinates(-91.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Coordinates(0.0, 181.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Coordinates(0.0, -181.0)
        }
    }

    @Test
    fun `minus operator`() {
        val c1 = Coordinates(0.0, 0.0, accuracy = 1.0f)
        val c2 = Coordinates(0.0, 1.0, accuracy = 2.0f)
        val distance: Distance = c1 - c2
        assertEquals(111195.0, distance.meters, 1.0)
        assertEquals(3.0f, requireNotNull(distance.accuracy) * distance.meters.toFloat() , 0.1f)
    }

    @Test
    fun `plus operator`() {
        val c1 = Coordinates(0.0, 0.0, accuracy = 1.0f)
        val distance = Distance(111195.0, 0.1f)
        val bearing = Azimuth(90.0)
        val c2 = c1.plus(distance, bearing)
        assertEquals(0.0, c2.latitude, 1e-6)
        assertEquals(1.0, c2.longitude, 1e-6)
        assertEquals(1.0f + 11119.5f, requireNotNull(c2.accuracy), 0.1f)
    }

    @Test
    fun `unaryMinus operator`() {
        val c1 = Coordinates(45.0, 90.0, accuracy = 1.0f)
        val c2 = -c1
        assertEquals(-45.0, c2.latitude)
        assertEquals(-90.0, c2.longitude)
        assertEquals(c1.accuracy, c2.accuracy)
    }

    @Test
    fun `add extension`() {
        val c1 = Coordinates(45.0, 90.0)
        val c2 = c1.add(1.0, 1.0)
        assertEquals(46.0, c2.latitude)
        assertEquals(91.0, c2.longitude)
    }

    @Test
    fun `split extension`() {
        val c1 = Coordinates(45.0, 90.0)
        val (lat, lon) = c1.split()
        assertEquals(45.0, lat)
        assertEquals(90.0, lon)
    }
}
