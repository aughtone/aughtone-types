package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * The URI standards each define when two values identify the same thing, and in every case that rule
 * differs from structural equality on the members. Getting this wrong is silent: it shows up as
 * duplicate entries in a set, a missed map lookup, or a `distinct()` that keeps everything.
 */
class UriEquivalenceTest {

    // ---- RFC 8141 §3.1 ----

    @Test
    fun `urn NID is case-insensitive and NSS is not`() {
        assertEquals(urn("urn:ISBN:0451450523"), urn("urn:isbn:0451450523"))
        assertNotEquals(urn("urn:isbn:X"), urn("urn:isbn:x"))
    }

    @Test
    fun `urn r q and f components must be ignored for equivalence`() {
        val bare = urn("urn:isbn:0451450523")
        assertEquals(bare, urn("urn:isbn:0451450523#page1"))
        assertEquals(bare, urn("urn:isbn:0451450523?=version=2"))
        assertEquals(bare, urn("urn:isbn:0451450523?+resolve"))
    }

    @Test
    fun `urn components are preserved even though equality ignores them`() {
        val withFragment = urn("urn:isbn:0451450523#page1")
        assertEquals("page1", withFragment.fComponent)
        assertEquals("urn:isbn:0451450523#page1", withFragment.toString())
    }

    @Test
    fun `urn equivalent values share a hash code and collapse in a set`() {
        val bare = urn("urn:isbn:0451450523")
        assertEquals(bare.hashCode(), urn("urn:ISBN:0451450523#page1").hashCode())
        assertEquals(1, setOf(bare, urn("urn:isbn:0451450523#page1"), urn("urn:ISBN:0451450523")).size)
    }

    @Test
    fun `a different urn is still different`() {
        assertNotEquals(urn("urn:isbn:0451450523"), urn("urn:isbn:0000000000"))
        assertNotEquals(urn("urn:isbn:0451450523"), urn("urn:issn:0451450523"))
    }

    // ---- RFC 3986 §6.2.2.1 ----

    @Test
    fun `uri scheme and authority are case-insensitive however the value was built`() {
        assertEquals(
            Uri("HTTP", "Example.COM", "/path", "", ""),
            Uri("http", "example.com", "/path", "", ""),
        )
    }

    @Test
    fun `url scheme and host are case-insensitive however the value was built`() {
        assertEquals(
            Url("HTTP", "", "Example.COM", null, "/path", "", ""),
            Url("http", "", "example.com", null, "/path", "", ""),
        )
    }

    @Test
    fun `a parsed url equals a directly constructed one`() {
        assertEquals(
            url("HTTPS://Example.COM/path"),
            Url("https", "", "example.com", null, "/path", "", ""),
        )
    }

    @Test
    fun `url path query and fragment stay case-sensitive`() {
        val base = Url("https", "", "example.com", null, "/Path", "", "")
        assertNotEquals(base, Url("https", "", "example.com", null, "/path", "", ""))
        assertNotEquals(base, Url("https", "", "example.com", null, "/Path", "Q=1", ""))
        assertNotEquals(base, Url("https", "", "example.com", null, "/Path", "", "Frag"))
    }

    @Test
    fun `url user info and port still distinguish urls`() {
        val base = Url("https", "", "example.com", null, "/", "", "")
        assertNotEquals(base, Url("https", "user", "example.com", null, "/", "", ""))
        assertNotEquals(base, Url("https", "", "example.com", 8443, "/", "", ""))
    }

    @Test
    fun `case-equivalent urls collapse in a set and share a hash code`() {
        val a = Url("HTTP", "", "Example.COM", null, "/p", "", "")
        val b = Url("http", "", "example.com", null, "/p", "", "")
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(1, setOf(a, b).size)
    }

    @Test
    fun `the supplied case is still preserved in the value and its rendering`() {
        val upper = Url("HTTP", "", "Example.COM", null, "/p", "", "")
        assertEquals("HTTP", upper.scheme)
        assertEquals("Example.COM", upper.host)
    }
}
