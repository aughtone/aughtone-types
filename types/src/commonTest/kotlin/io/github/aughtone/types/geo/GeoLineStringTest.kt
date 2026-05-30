package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoLineStringTest {

    @Test
    fun `GeoLineString properties`() {
        val coords = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        val ls = GeoLineString(coords)
        assertEquals(coords, ls.coordinates)
    }
}
