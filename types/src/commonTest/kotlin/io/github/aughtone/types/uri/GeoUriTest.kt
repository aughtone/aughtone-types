package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeoUriTest {
    // Spec is at: https://datatracker.ietf.org/doc/html/rfc5870
    private val gri = GeoUri(
        latitude = 48.2010,
        longitude = 16.3695,
        altitude = 183.0,
        uncertainty = 15,
    )

    // Coordinate formatting is platform-stable: plain decimal, no scientific
    // notation, no trailing ".0" (183.0 renders as "183" on every target).

    @Test
    fun `Full GeoUri to string`() {
        assertEquals("geo:48.201,16.3695,183;crs=wgs84;u=15", gri.toString())
    }

    @Test
    fun `GeoUri to string with no uncertainty`() {
        assertEquals("geo:48.201,16.3695,183;crs=wgs84", gri.copy(uncertainty = null).toString())
    }

    @Test
    fun `GeoUri to string with no crs`() {
        assertEquals("geo:48.201,16.3695,183;u=15", gri.copy(crs = null).toString())
    }

    @Test
    fun `GeoUri to string with no crs or uncertainty`() {
        assertEquals(
            "geo:48.201,16.3695,183",
            gri.copy(crs = null, uncertainty = null).toString()
        )
    }

    @Test
    fun `GeoUri to string with no altitude`() {
        assertEquals("geo:48.201,16.3695;crs=wgs84;u=15", gri.copy(altitude = null).toString())
    }

    @Test
    fun `GeoUri to string with no crs or uncertainty or altitude`() {
        assertEquals(
            "geo:48.201,16.3695",
            gri.copy(altitude = null, crs = null, uncertainty = null).toString()
        )
    }

    @Test
    fun `GeoUri with negative coordinates`() {
        assertEquals(
            "geo:-48.201,-16.3695",
            GeoUri(latitude = -48.201, longitude = -16.3695, crs = null).toString()
        )
    }

    @Test
    fun `Small coordinates render without scientific notation`() {
        assertEquals(
            "geo:0.0000001,0",
            GeoUri(latitude = 1.0E-7, longitude = 0.0, crs = null).toString()
        )
    }

    @Test
    fun `Integral coordinates render without trailing decimal`() {
        assertEquals(
            "geo:90,-180,100",
            GeoUri(latitude = 90.0, longitude = -180.0, altitude = 100.0, crs = null).toString()
        )
    }

    @Test
    fun `GeoUri toUri has no authority and keeps parameters in path`() {
        assertEquals(
            Uri(
                scheme = "geo",
                authority = "",
                path = "48.201,16.3695,183;crs=wgs84;u=15",
                query = "",
                fragment = ""
            ),
            gri.toUri()
        )
        assertEquals("geo:48.201,16.3695,183;crs=wgs84;u=15", gri.toUri().toString())
    }

    @Test
    fun `Latitude out of range fails`() {
        assertFailsWith<IllegalArgumentException> { GeoUri(latitude = 90.0001, longitude = 0.0) }
        assertFailsWith<IllegalArgumentException> { GeoUri(latitude = -90.0001, longitude = 0.0) }
    }

    @Test
    fun `Longitude out of range fails`() {
        assertFailsWith<IllegalArgumentException> { GeoUri(latitude = 0.0, longitude = 180.0001) }
        assertFailsWith<IllegalArgumentException> { GeoUri(latitude = 0.0, longitude = -180.0001) }
    }

    @Test
    fun `Negative uncertainty fails`() {
        assertFailsWith<IllegalArgumentException> {
            GeoUri(latitude = 0.0, longitude = 0.0, uncertainty = -1)
        }
    }

//    @Test
//    fun `Full GeoUri equals`() {
//        // equality is mostly straightforward match, but some params are assumed and some are case insensitive.
//        // See: https://datatracker.ietf.org/doc/html/rfc5870
//        fail("Look up the criteria for equality")
//    }

}
