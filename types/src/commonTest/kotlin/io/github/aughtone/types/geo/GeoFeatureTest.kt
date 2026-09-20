package io.github.aughtone.types.geo

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GeoFeatureTest {

    @Test
    fun `GeoFeature properties`() {
        val geom = GeoPoint(1.0, 2.0)
        val props = buildJsonObject { put("key", "value") }
        val feature = GeoFeature(geom, props, JsonPrimitive("id1"))
        
        assertEquals(geom, feature.geometry)
        assertEquals(props, feature.properties)
        assertEquals(JsonPrimitive("id1"), feature.id)
        assertEquals("value", feature.stringProperty("key"))
        assertNull(feature.bbox)
    }
}
