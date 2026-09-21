package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoMultiPolygonTest {

    @Test
    fun `GeoMultiPolygon properties`() {
        // A closed linear ring: four positions, first equal to last, wound counter-clockwise.
        val coords = listOf(
            listOf(
                listOf(
                    listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 0.0),
                ),
            ),
        )
        val mpoly = GeoMultiPolygon(coords)
        assertEquals(coords, mpoly.coordinates)
    }
}
