package io.github.aughtone.types.quantitative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CoordinatesTest {
    private val testLatitude = 20.05
    private val testLongitude = -15.5
    private val testCoordinate = Coordinates(testLatitude, testLongitude)

    @Test
    fun testToString() {
        assertEquals(
            "Coordinates(latitude=20.05, longitude=-15.5, accuracy=null)",
            testCoordinate.toString()
        )
    }

    @Test
    fun testHashCode() {
        val a: Coordinates = testCoordinate
        val b: Coordinates = Coordinates(testLatitude, testLongitude)

        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a, b)
        assertTrue(a == b)
    }

    @Test
    fun testInfixEquals() {
        val a: Coordinates = testCoordinate
        val b: Coordinates = Coordinates(testLatitude, testLongitude)

        assertTrue(a == b)
    }

    @Test
    fun testInfixAdd() {
        val a: Coordinates = testCoordinate
        val b = a(1.0, 1.0)
        assertNotEquals(a, b)
        assertEquals(21.05, b.latitude)
        assertEquals(-14.5, b.longitude)
    }

    @Test
    fun testSplitCoordinateIntoPair() {
        val actual = testCoordinate.split()
        assertEquals(testCoordinate.latitude, actual.first)
        assertEquals(testCoordinate.longitude, actual.second)
    }

}
