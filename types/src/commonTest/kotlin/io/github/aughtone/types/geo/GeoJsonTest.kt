package io.github.aughtone.types.geo

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GeoJsonTest {

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        classDiscriminator = "type" // Explicitly define for clarity
        explicitNulls = false // Don't include null values in the output
    }

    @Test
    fun `Point serializes and deserializes correctly`() {
        val point = Point(
            coordinates = listOf(-122.4194, 37.7749),
            bbox = listOf(-122.5, 37.7, -122.4, 37.8)
        )
        val pointJsonString = """
            {
                "type": "Point",
                "coordinates": [
                    -122.4194,
                    37.7749
                ],
                "bbox": [
                    -122.5,
                    37.7,
                    -122.4,
                    37.8
                ]
            }
        """.trimIndent()

        // Test serialization
        val encodedJson = json.encodeToString<GeoJson>(point)
        assertEquals(pointJsonString, encodedJson)

        // Test deserialization
        val decodedPoint = json.decodeFromString<GeoJson>(pointJsonString)
        assertEquals(point, decodedPoint)
    }

    @Test
    fun `Polygon with null bbox serializes without bbox key`() {
        val polygon = Polygon(
            coordinates = listOf(
                listOf(
                    listOf(100.0, 0.0),
                    listOf(101.0, 0.0),
                    listOf(101.0, 1.0),
                    listOf(100.0, 1.0),
                    listOf(100.0, 0.0)
                )
            )
        )
        // Note: bbox is null by default and should not be present in the JSON output
        val polygonJsonString = """
            {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            100.0,
                            0.0
                        ],
                        [
                            101.0,
                            0.0
                        ],
                        [
                            101.0,
                            1.0
                        ],
                        [
                            100.0,
                            1.0
                        ],
                        [
                            100.0,
                            0.0
                        ]
                    ]
                ]
            }
        """.trimIndent()

        // Test serialization
        val encodedJson = json.encodeToString<GeoJson>(polygon)
        assertEquals(polygonJsonString, encodedJson)

        // Test deserialization
        val decodedPolygon = json.decodeFromString<GeoJson>(polygonJsonString)
        assertEquals(polygon, decodedPolygon)
    }

    @Test
    fun `Feature with properties and geometry serializes and deserializes`() {
        val feature = Feature(
            geometry = Point(coordinates = listOf(1.0, 2.0)), // bbox is null here
            properties = mapOf("name" to "Test Point"),
            id = "feature1",
            bbox = listOf(1.0, 2.0, 1.0, 2.0)
        )

        // With explicitNulls = false, the inner Point's null bbox is omitted.
        val featureJsonString = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        1.0,
                        2.0
                    ]
                },
                "properties": {
                    "name": "Test Point"
                },
                "id": "feature1",
                "bbox": [
                    1.0,
                    2.0,
                    1.0,
                    2.0
                ]
            }
        """.trimIndent()

        // Test serialization
        val encodedJson = json.encodeToString<GeoJson>(feature)
        assertEquals(featureJsonString, encodedJson)

        // Test deserialization
        val decodedFeature = json.decodeFromString<GeoJson>(featureJsonString)
        assertEquals(feature, decodedFeature)
    }

    @Test
    fun `Feature with null geometry serializes without geometry key`() {
        val featureWithNullGeom = Feature(
            geometry = null,
            properties = mapOf("status" to "No location"),
            id = null, // also test null id
            bbox = null // also test null bbox
        )
        // With explicitNulls = false, geometry, id, and bbox are omitted from the output.
        val jsonString = """
            {
                "type": "Feature",
                "properties": {
                    "status": "No location"
                }
            }
        """.trimIndent()

        // Serialization
        val encoded = json.encodeToString<GeoJson>(featureWithNullGeom)
        assertEquals(jsonString, encoded)

        // Deserialization from a string that is also missing the keys should work
        val decoded = json.decodeFromString<GeoJson>(jsonString)
        assertIs<Feature>(decoded)
        assertNull(decoded.geometry)
        assertNull(decoded.id)
        assertNull(decoded.bbox)
        assertEquals(featureWithNullGeom.properties, decoded.properties)
    }

    @Test
    fun `FeatureCollection with multiple feature types deserializes correctly`() {
        // Deserialization is unaffected by explicitNulls, so this test remains the same.
        // It can correctly handle JSON that has `null` values present.
        val featureCollectionJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Point",
                    "coordinates": [102.0, 0.5]
                  },
                  "properties": {
                    "prop0": "value0"
                  }
                },
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [
                      [102.0, 0.0],
                      [103.0, 1.0],
                      [104.0, 0.0],
                      [105.0, 1.0]
                    ]
                  },
                  "properties": {
                    "prop0": "value0",
                    "prop1": "value1"
                  }
                },
                {
                  "type": "Feature",
                  "geometry": null,
                  "properties": {
                     "name": "Null Island"
                  }
                }
              ]
            }
        """.trimIndent()

        val decodedCollection = json.decodeFromString<GeoJson>(featureCollectionJson)
        assertIs<FeatureCollection>(decodedCollection)
        assertEquals(3, decodedCollection.features.size)

        val firstFeature = decodedCollection.features[0]
        assertNotNull(firstFeature.geometry)
        assertIs<Point>(firstFeature.geometry)
        assertEquals(listOf(102.0, 0.5), (firstFeature.geometry as Point).coordinates)

        val secondFeature = decodedCollection.features[1]
        assertNotNull(secondFeature.geometry)
        assertIs<LineString>(secondFeature.geometry)
        assertEquals(mapOf("prop0" to "value0", "prop1" to "value1"), secondFeature.properties)

        val thirdFeature = decodedCollection.features[2]
        assertNull(thirdFeature.geometry)
        assertEquals(mapOf("name" to "Null Island"), thirdFeature.properties)
    }
}
