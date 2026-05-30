package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GeoPointTest {

    @Test
    fun `GeoPoint constructors and properties`() {
        val p2d = GeoPoint(10.0, 20.0)
        assertEquals(10.0, p2d.longitude)
        assertEquals(20.0, p2d.latitude)
        assertNull(p2d.altitude)

        val p3d = GeoPoint(10.0, 20.0, 30.0)
        assertEquals(30.0, p3d.altitude)

        val pArray = GeoPoint(doubleArrayOf(1.0, 2.0, 3.0))
        assertEquals(1.0, pArray.longitude)
        assertEquals(3.0, pArray.altitude)
    }

    @Test
    fun `GeoPoint coordinates list order`() {
        val p = GeoPoint(1.0, 2.0, 3.0)
        assertEquals(listOf(1.0, 2.0, 3.0), p.coordinates)
    }
}
