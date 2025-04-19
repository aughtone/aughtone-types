package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoUriTest {
    // Spec is at: https://datatracker.ietf.org/doc/html/rfc5870
    private val geoUri = GeoUri(
        latitude = 48.2010,
        longitude = 16.3695,
        altitude = 183.0,
        uncertainty = 15,
    )

    @Test
    fun `Full GeoUri to string`() {
        assertEquals("geo:48.201,16.3695,183.0;crs=wgs84;u=15", geoUri.toString())
    }

    @Test
    fun `GeoUri to string with no uncertainty`() {
        assertEquals("geo:48.201,16.3695,183.0;crs=wgs84", geoUri.copy(uncertainty = null).toString())
    }

    @Test
    fun `GeoUri to string with no crs`() {
        assertEquals("geo:48.201,16.3695,183.0;u=15", geoUri.copy(crs = null).toString())
    }

    @Test
    fun `GeoUri to string with no crs or uncertainty`() {
        assertEquals(
            "geo:48.201,16.3695,183.0",
            geoUri.copy(crs = null, uncertainty = null).toString()
        )
    }

    @Test
    fun `GeoUri to string with no altitude`() {
        assertEquals("geo:48.201,16.3695;crs=wgs84;u=15", geoUri.copy(altitude = null).toString())
    }

    @Test
    fun `GeoUri to string with no crs or uncertainty or altitude`() {
        assertEquals(
            "geo:48.201,16.3695",
            geoUri.copy(altitude = null, crs = null, uncertainty = null).toString()
        )
    }

//    @Test
//    fun `Full GeoUri equals`() {
//        // equality is mostly straightforward match, but some params are assumed and some are case insensitive.
//        // See: https://datatracker.ietf.org/doc/html/rfc5870
//        fail("Look up the criteria for equality")
//    }

}
