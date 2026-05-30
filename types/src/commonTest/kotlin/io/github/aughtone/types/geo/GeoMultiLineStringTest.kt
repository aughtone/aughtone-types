package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoMultiLineStringTest {

    @Test
    fun `GeoMultiLineString properties`() {
        val coords = listOf(listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)))
        val mls = GeoMultiLineString(coords)
        assertEquals(coords, mls.coordinates)
    }
}
