package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoPolygonTest {

    @Test
    fun `GeoPolygon properties`() {
        val coords = listOf(listOf(listOf(1.0, 1.0), listOf(2.0, 1.0), listOf(2.0, 2.0), listOf(1.0, 1.0)))
        val poly = GeoPolygon(coords)
        assertEquals(coords, poly.coordinates)
    }
}
