package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GeometryCollectionTest {

    @Test
    fun `GeometryCollection properties`() {
        val geometries = listOf(GeoPoint(1.0, 2.0))
        val gc = GeometryCollection(geometries)
        assertEquals(geometries, gc.geometries)
    }
}
