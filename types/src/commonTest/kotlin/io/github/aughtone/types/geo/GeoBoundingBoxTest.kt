package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GeoBoundingBoxTest {

    @Test
    fun `constructor with 4 elements`() {
        val bbox = GeoBoundingBox(listOf(10.0, 20.0, 30.0, 40.0))
        assertEquals(10.0, bbox.west)
        assertEquals(20.0, bbox.south)
        assertEquals(30.0, bbox.east)
        assertEquals(40.0, bbox.north)
        assertNull(bbox.minAltitude)
        assertNull(bbox.maxAltitude)
    }

    @Test
    fun `constructor with 6 elements`() {
        val bbox = GeoBoundingBox(listOf(10.0, 20.0, 5.0, 30.0, 40.0, 15.0))
        assertEquals(10.0, bbox.west)
        assertEquals(20.0, bbox.south)
        assertEquals(5.0, bbox.minAltitude)
        assertEquals(30.0, bbox.east)
        assertEquals(40.0, bbox.north)
        assertEquals(15.0, bbox.maxAltitude)
    }

    @Test
    fun `constructor with invalid number of elements fails`() {
        assertFailsWith<IllegalArgumentException> {
            GeoBoundingBox(listOf(10.0, 20.0, 30.0))
        }
        assertFailsWith<IllegalArgumentException> {
            GeoBoundingBox(listOf(1.0, 2.0, 3.0, 4.0, 5.0))
        }
    }

    @Test
    fun `constructor with DoubleArray`() {
        val bbox = GeoBoundingBox(doubleArrayOf(1.0, 2.0, 3.0, 4.0))
        assertEquals(1.0, bbox.west)
        assertEquals(4.0, bbox.north)
    }

    @Test
    fun `toDoubleArray returns 4 elements for 2D`() {
        val bbox = GeoBoundingBox(1.0, 2.0, 3.0, 4.0)
        val array = bbox.toDoubleArray()
        assertEquals(4, array.size)
        assertEquals(1.0, array[0])
        assertEquals(2.0, array[1])
        assertEquals(3.0, array[2])
        assertEquals(4.0, array[3])
    }

    @Test
    fun `toDoubleArray uses single-sided altitude for both bounds`() {
        val minOnly = GeoBoundingBox(1.0, 2.0, 3.0, 4.0, minAltitude = 5.0)
        assertEquals(listOf(1.0, 2.0, 5.0, 3.0, 4.0, 5.0), minOnly.toDoubleArray().toList())
        val maxOnly = GeoBoundingBox(1.0, 2.0, 3.0, 4.0, maxAltitude = 7.0)
        assertEquals(listOf(1.0, 2.0, 7.0, 3.0, 4.0, 7.0), maxOnly.toDoubleArray().toList())
    }

    @Test
    fun `toDoubleArray returns 6 elements for 3D`() {
        val bbox = GeoBoundingBox(1.0, 2.0, 3.0, 4.0, 5.0, 10.0)
        val array = bbox.toDoubleArray()
        assertEquals(6, array.size)
        assertEquals(1.0, array[0])
        assertEquals(2.0, array[1])
        assertEquals(5.0, array[2]) // minAltitude
        assertEquals(3.0, array[3]) // east
        assertEquals(4.0, array[4]) // north
        assertEquals(10.0, array[5]) // maxAltitude
    }
}
