package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoMultiPointTest {

    @Test
    fun `GeoMultiPoint properties`() {
        val coords = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        val mp = GeoMultiPoint(coords)
        assertEquals(coords, mp.coordinates)
    }
}
