package io.github.aughtone.types.geo

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * RFC 7946 §3.1 structural rules are enforced on construction and on deserialization. Winding is
 * deliberately not, because §3.1.6 tells parsers not to reject polygons that break the right-hand
 * rule — so a clockwise exterior ring must survive a round trip untouched.
 */
class GeoValidationTest {

    private val ccwRing = listOf(
        listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 1.0), listOf(0.0, 0.0),
    )
    private val cwRing = ccwRing.reversed()

    @Test
    fun `a LineString needs at least two positions`() {
        assertFailsWith<IllegalArgumentException> { GeoLineString(listOf(listOf(1.0, 2.0))) }
    }

    @Test
    fun `a position needs at least two elements`() {
        assertFailsWith<IllegalArgumentException> {
            GeoLineString(listOf(listOf(1.0), listOf(2.0, 3.0)))
        }
    }

    @Test
    fun `a linear ring must be closed`() {
        val open = listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 1.0))
        assertFailsWith<IllegalArgumentException> { GeoPolygon(listOf(open)) }
    }

    @Test
    fun `a linear ring needs at least four positions`() {
        val short = listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(0.0, 0.0))
        assertFailsWith<IllegalArgumentException> { GeoPolygon(listOf(short)) }
    }

    @Test
    fun `structural rules apply on deserialization too`() {
        val openRing = """{"type":"Polygon","coordinates":[[[0.0,0.0],[1.0,0.0],[1.0,1.0],[0.0,1.0]]]}"""
        assertFailsWith<IllegalArgumentException> {
            Json.decodeFromString(GeoGeometry.serializer(), openRing)
        }
    }

    @Test
    fun `winding is computed from the ring`() {
        assertEquals(Winding.CounterClockwise, GeoPolygon(listOf(ccwRing)).windingOf())
        assertEquals(Winding.Clockwise, GeoPolygon(listOf(cwRing)).windingOf())
    }

    @Test
    fun `geoPolygon rejects a wrongly wound exterior ring`() {
        assertFailsWith<IllegalArgumentException> { geoPolygon(listOf(cwRing)) }
    }

    @Test
    fun `geoPolygon accepts a correctly wound exterior ring`() {
        assertEquals(Winding.CounterClockwise, geoPolygon(listOf(ccwRing)).windingOf())
    }

    @Test
    fun `geoPolygonRewinding accepts any winding and corrects it`() {
        assertEquals(Winding.CounterClockwise, geoPolygonRewinding(listOf(cwRing)).windingOf())
        assertEquals(Winding.CounterClockwise, geoPolygonRewinding(listOf(ccwRing)).windingOf())
    }

    @Test
    fun `rewound puts holes clockwise and the exterior counter-clockwise`() {
        val hole = listOf(
            listOf(0.2, 0.2), listOf(0.4, 0.2), listOf(0.4, 0.4), listOf(0.2, 0.4), listOf(0.2, 0.2),
        )
        val rewound = GeoPolygon(listOf(cwRing, hole)).rewound()
        assertEquals(Winding.CounterClockwise, rewound.windingOf(0))
        assertEquals(Winding.Clockwise, rewound.windingOf(1))
    }

    @Test
    fun `a clockwise exterior ring survives a round trip unchanged`() {
        val original = GeoPolygon(listOf(cwRing))
        val json = Json.encodeToString(GeoGeometry.serializer(), original)
        val decoded = Json.decodeFromString(GeoGeometry.serializer(), json)
        assertEquals(original, decoded)
        assertEquals(Winding.Clockwise, (decoded as GeoPolygon).windingOf())
    }
}
