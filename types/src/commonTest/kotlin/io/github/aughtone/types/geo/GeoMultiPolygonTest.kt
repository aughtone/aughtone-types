package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoMultiPolygonTest {

    @Test
    fun `GeoMultiPolygon properties`() {
        val coords = listOf(listOf(listOf(listOf(1.0, 1.0), listOf(2.0, 1.0), listOf(1.0, 1.0))))
        val mpoly = GeoMultiPolygon(coords)
        assertEquals(coords, mpoly.coordinates)
    }
}
