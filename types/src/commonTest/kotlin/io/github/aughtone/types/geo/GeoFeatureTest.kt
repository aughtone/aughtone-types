package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GeoFeatureTest {

    @Test
    fun `GeoFeature properties`() {
        val geom = GeoPoint(1.0, 2.0)
        val props = mapOf("key" to "value")
        val feature = GeoFeature(geom, props, "id1")
        
        assertEquals(geom, feature.geometry)
        assertEquals(props, feature.properties)
        assertEquals("id1", feature.id)
        assertNull(feature.bbox)
    }
}
