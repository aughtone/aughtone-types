package io.github.aughtone.types.geo

import io.github.aughtone.types.quantitative.Coordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * `Coordinates` takes latitude first, as the pair is spoken; a GeoJSON position takes longitude first,
 * because RFC 7946 §3.1.1 says so. Both are `Double`, so hand-transposing between them compiles,
 * serializes, and puts the point in the wrong hemisphere.
 *
 * [toGeoPoint] is the conversion that cannot be got wrong. See issue #34, where the first suggestion
 * was a `GeoPoint(Coordinates)` constructor — declined, because GeoJSON's own order is correct for
 * `GeoPoint` and a second constructor would not carry the reordering in its name.
 */
class CoordinatesToGeoPointTest {

    // Ottawa: north of the equator, west of the meridian, so a transposition is unmistakable.
    private val ottawa = Coordinates(latitude = 45.42, longitude = -75.69)

    @Test
    fun `the position comes out longitude first`() {
        val point = ottawa.toGeoPoint()
        assertEquals(listOf(-75.69, 45.42), point.coordinates)
    }

    @Test
    fun `the longitude and latitude are not transposed`() {
        val point = ottawa.toGeoPoint()
        assertEquals(ottawa.longitude, point.coordinates[0], "element 0 must be the longitude")
        assertEquals(ottawa.latitude, point.coordinates[1], "element 1 must be the latitude")
    }

    @Test
    fun `a position of exactly two elements is produced and no bbox`() {
        val point = ottawa.toGeoPoint()
        assertEquals(2, point.coordinates.size)
        assertNull(point.bbox)
    }

    @Test
    fun `accuracy is dropped because GeoJSON has no member for it`() {
        val measured = Coordinates(latitude = 45.42, longitude = -75.69, accuracy = 12.5f)
        assertEquals(measured.toGeoPoint(), ottawa.toGeoPoint())
    }

    @Test
    fun `the round trip through a GeoJSON position preserves the pair`() {
        val point = ottawa.toGeoPoint()
        assertEquals(ottawa.latitude, point.coordinates[1])
        assertEquals(ottawa.longitude, point.coordinates[0])
    }

    @Test
    fun `the extremes convert without being clamped or reordered`() {
        assertEquals(
            listOf(180.0, -90.0),
            Coordinates(latitude = -90.0, longitude = 180.0).toGeoPoint().coordinates
        )
        assertEquals(
            listOf(-180.0, 90.0),
            Coordinates(latitude = 90.0, longitude = -180.0).toGeoPoint().coordinates
        )
    }
}
