package io.github.aughtone.types.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GeoFeatureCollectionTest {

    @Test
    fun `GeoFeatureCollection properties`() {
        val features = listOf(GeoFeature(GeoPoint(1.0, 2.0)))
        val collection = GeoFeatureCollection(features)
        
        assertEquals(features, collection.features)
        assertNull(collection.bbox)
    }
}
