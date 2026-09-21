package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * RFC 5870 §3.4.4 defines when two `geo` URIs identify the same location, and that rule is not
 * structural equality on the members: an omitted `crs` and an explicit `"wgs84"` are the same CRS
 * (§3.4.1), and the label is case-insensitive (§3.3).
 */
class GeoUriEqualityTest {

    @Test
    fun `an omitted crs equals an explicit wgs84`() {
        assertEquals(GeoUri(48.2, 16.3, crs = null), GeoUri(48.2, 16.3, crs = "wgs84"))
    }

    @Test
    fun `the crs label is case-insensitive`() {
        assertEquals(GeoUri(48.2, 16.3, crs = "WGS84"), GeoUri(48.2, 16.3, crs = "wgs84"))
    }

    @Test
    fun `equal values share a hash code`() {
        assertEquals(
            GeoUri(48.2, 16.3, crs = null).hashCode(),
            GeoUri(48.2, 16.3, crs = "WGS84").hashCode(),
        )
    }

    @Test
    fun `a set treats the same location written three ways as one entry`() {
        val set = setOf(
            GeoUri(48.2, 16.3, crs = null),
            GeoUri(48.2, 16.3, crs = "wgs84"),
            GeoUri(48.2, 16.3, crs = "WGS84"),
        )
        assertEquals(1, set.size)
    }

    @Test
    fun `a genuinely different crs is not equal`() {
        assertNotEquals(GeoUri(48.2, 16.3, crs = "wgs84"), GeoUri(48.2, 16.3, crs = "moon"))
    }

    @Test
    fun `coordinates and uncertainty still distinguish locations`() {
        assertNotEquals(GeoUri(48.2, 16.3), GeoUri(48.3, 16.3))
        assertNotEquals(GeoUri(48.2, 16.3, uncertainty = 10), GeoUri(48.2, 16.3, uncertainty = 20))
    }

    @Test
    fun `effectiveCrs reports the lowercase default however it was written`() {
        assertEquals("wgs84", GeoUri(48.2, 16.3, crs = null).effectiveCrs)
        assertEquals("wgs84", GeoUri(48.2, 16.3, crs = "WGS84").effectiveCrs)
        assertEquals("moon", GeoUri(48.2, 16.3, crs = "MOON").effectiveCrs)
    }

    @Test
    fun `the supplied value is still preserved and serialized`() {
        val upper = GeoUri(48.2, 16.3, crs = "WGS84")
        assertEquals("WGS84", upper.crs)
        assertEquals("geo:48.2,16.3;crs=WGS84", upper.toString())
    }

    @Test
    fun `the parser emits the preferred lowercase form`() {
        assertEquals("wgs84", geoUri("geo:48.2,16.3;crs=WGS84").crs)
        assertEquals(geoUri("geo:48.2,16.3"), geoUri("geo:48.2,16.3;crs=WGS84"))
    }
}
