package io.github.aughtone.types.uri

import io.github.aughtone.types.outcome.Outcome
import io.github.aughtone.types.outcome.runOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Round-trip is the property that matters, and it has to hold for the forms the RFCs permit rather
 * than only the tidy ones. Equivalence allows scheme and host to differ in case, since both are
 * case-insensitive and are lowercased on parse; nothing else is normalized.
 */
class UriParsingTest {

    private fun roundTrips(value: String, expected: String = value) =
        assertEquals(expected, url(value).toString(), "round trip failed for $value")

    @Test
    fun `a simple url round trips`() {
        roundTrips("https://example.com/path")
    }

    @Test
    fun `scheme and host are lowercased because they are case-insensitive`() {
        val parsed = url("HTTPS://Example.COM/Path")
        assertEquals("https", parsed.scheme)
        assertEquals("example.com", parsed.host)
        assertEquals("/Path", parsed.path, "the path is case-sensitive and must not be touched")
    }

    @Test
    fun `ports are parsed and rejected when invalid`() {
        assertEquals(8080, url("https://example.com:8080/").port)
        assertNull(url("https://example.com/").port)
        assertFailsWith<UriParseException> { url("https://example.com:not-a-port/") }
        assertFailsWith<UriParseException> { url("https://example.com:99999/") }
    }

    @Test
    fun `an ipv6 host keeps its brackets and can carry a port`() {
        val plain = url("https://[2001:db8::1]/path")
        assertEquals("[2001:db8::1]", plain.host)
        assertNull(plain.port)

        val ported = url("https://[2001:db8::1]:8443/path")
        assertEquals("[2001:db8::1]", ported.host)
        assertEquals(8443, ported.port)
    }

    @Test
    fun `user information is separated from the host`() {
        val parsed = url("https://user:pw@example.com/path")
        assertEquals("user:pw", parsed.userInfo)
        assertEquals("example.com", parsed.host)
    }

    @Test
    fun `query and fragment are separated`() {
        val parsed = url("https://example.com/p?a=1&b=2#frag")
        assertEquals("/p", parsed.path)
        assertEquals("a=1&b=2", parsed.query)
        assertEquals("frag", parsed.fragment)
    }

    @Test
    fun `percent encoding is preserved rather than decoded`() {
        val parsed = url("https://example.com/a%20b?q=%C3%A9")
        assertEquals("/a%20b", parsed.path)
        assertEquals("q=%C3%A9", parsed.query)
    }

    @Test
    fun `dot segments are left alone because resolving them changes the path`() {
        assertEquals("/a/../b", url("https://example.com/a/../b").path)
    }

    @Test
    fun `an empty path is permitted`() {
        assertEquals("", url("https://example.com").path)
    }

    @Test
    fun `malformed input is refused`() {
        assertFailsWith<UriParseException> { url("not a url") }
        assertFailsWith<UriParseException> { url("https:///path") }      // empty authority
        assertFailsWith<UriParseException> { url("/just/a/path") }       // no scheme
        assertFailsWith<UriParseException> { url("1http://example.com") } // scheme must start with a letter
    }

    @Test
    fun `a url without an authority is refused`() {
        assertFailsWith<UriParseException> { url("mailto:someone@example.com") }
    }

    @Test
    fun `the OrNull variant returns null instead of throwing`() {
        assertNull(urlOrNull("not a url"))
        assertEquals("example.com", urlOrNull("https://example.com")?.host)
    }

    @Test
    fun `a generic uri accepts forms a url does not`() {
        val parsed = uri("mailto:someone@example.com")
        assertEquals("mailto", parsed.scheme)
        assertEquals("someone@example.com", parsed.path)
        assertNull(uriOrNull("no-scheme-here"))
    }

    @Test
    fun `a geo uri round trips with its optional parts`() {
        // Not byte-identical: GeoUri.toString always emits ";crs=wgs84", which RFC 5870 permits to
        // be omitted when it is the default. The property that holds is that re-parsing the rendered
        // form yields the same value — equivalence, not identical text.
        val minimal = geoUri("geo:48.2,16.3")
        assertEquals(minimal, geoUri(minimal.toString()))
        assertEquals("wgs84", minimal.crs)

        val full = geoUri("geo:48.2,16.3,183;crs=wgs84;u=25")
        assertEquals(48.2, full.latitude)
        assertEquals(16.3, full.longitude)
        assertEquals(183.0, full.altitude)
        assertEquals("wgs84", full.crs)
        assertEquals(25, full.uncertainty)
        assertEquals(full, geoUri(full.toString()))
    }

    @Test
    fun `geo uri parameter names are case-insensitive`() {
        assertEquals(25, geoUri("geo:48.2,16.3;CRS=wgs84;U=25").uncertainty)
    }

    @Test
    fun `an out of range geo coordinate is refused by the parser`() {
        assertFailsWith<UriParseException> { geoUri("geo:91.0,16.3") }
        assertNull(geoUriOrNull("geo:48.2,999.0"))
    }

    @Test
    fun `a malformed geo uri is refused`() {
        assertFailsWith<UriParseException> { geoUri("geo:48.2") }
        assertFailsWith<UriParseException> { geoUri("https://example.com") }
        assertFailsWith<UriParseException> { geoUri("geo:48.2,16.3;u=many") }
    }

    @Test
    fun `urn parsing reports the named exception and has an OrNull variant`() {
        assertFailsWith<UriParseException> { urn("not-a-urn") }
        assertNull(urnOrNull("not-a-urn"))
        assertEquals("isbn", urn("urn:isbn:0451450523").namespace)
    }

    @Test
    fun `failure as data is available by composition rather than a second API`() {
        val outcome = runOutcome { url("not a url") }
        val failure = assertIs<Outcome.Failure>(outcome)
        assertIs<UriParseException>(failure.exception)

        assertIs<Outcome.Success<Url>>(runOutcome { url("https://example.com") })
    }
}
