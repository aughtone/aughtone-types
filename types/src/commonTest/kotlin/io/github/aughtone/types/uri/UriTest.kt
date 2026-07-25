package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UriTest {

    private fun uri(
        scheme: String = "https",
        authority: String = "example.com",
        path: String = "/p",
        query: String = "",
        fragment: String = "",
    ) = Uri(scheme = scheme, authority = authority, path = path, query = query, fragment = fragment)

    @Test
    fun `toString with authority`() {
        assertEquals(
            "https://example.com/p?a=1#f",
            uri(query = "a=1", fragment = "f").toString()
        )
    }

    @Test
    fun `toString omits empty query and fragment`() {
        assertEquals("https://example.com/p", uri().toString())
    }

    @Test
    fun `toString without authority has no double slash`() {
        assertEquals(
            "urn:test:bogus",
            uri(scheme = "urn", authority = "", path = "test:bogus").toString()
        )
    }

    @Test
    fun `toUrl with host only`() {
        val url = uri().toUrl()
        assertEquals("", url.userInfo)
        assertEquals("example.com", url.host)
        assertEquals(null, url.port)
    }

    @Test
    fun `toUrl with userInfo and port`() {
        val url = uri(authority = "user:pw@example.com:8080").toUrl()
        assertEquals("user:pw", url.userInfo)
        assertEquals("example.com", url.host)
        assertEquals(8080, url.port)
    }

    @Test
    fun `toUrl with ipv6 host and port`() {
        val url = uri(authority = "[::1]:8080").toUrl()
        assertEquals("[::1]", url.host)
        assertEquals(8080, url.port)
    }

    @Test
    fun `toUrl with ipv6 host and no port`() {
        val url = uri(authority = "[2001:db8::1]").toUrl()
        assertEquals("[2001:db8::1]", url.host)
        assertEquals(null, url.port)
    }

    @Test
    fun `toUrl with junk port fails`() {
        assertFailsWith<IllegalArgumentException> {
            uri(authority = "example.com:abc").toUrl()
        }
    }

    @Test
    fun `toUrl with empty port fails`() {
        assertFailsWith<IllegalArgumentException> {
            uri(authority = "example.com:").toUrl()
        }
    }

    @Test
    fun `toUrl with unterminated ipv6 host fails`() {
        assertFailsWith<IllegalArgumentException> {
            uri(authority = "[::1").toUrl()
        }
    }

    @Test
    fun `toUrn parses namespace from path`() {
        assertEquals(
            Urn(namespace = "test", identity = "bogus"),
            uri(scheme = "urn", authority = "", path = "test:bogus").toUrn()
        )
    }
}
