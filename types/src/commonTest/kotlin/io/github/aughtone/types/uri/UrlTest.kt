package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlTest {

    private fun url(
        userInfo: String = "",
        host: String = "example.com",
        port: Int? = null,
        path: String = "",
        query: String = "",
        fragment: String = "",
    ) = Url(
        scheme = "https",
        userInfo = userInfo,
        host = host,
        port = port,
        path = path,
        query = query,
        fragment = fragment,
    )

    @Test
    fun `authority omits null port`() {
        assertEquals("example.com", url().authority)
    }

    @Test
    fun `authority includes port when present`() {
        assertEquals("example.com:8080", url(port = 8080).authority)
    }

    @Test
    fun `authority includes userInfo when present`() {
        assertEquals("user:pw@example.com:443", url(userInfo = "user:pw", port = 443).authority)
    }

    @Test
    fun `identity omits empty query and fragment`() {
        assertEquals("/path", url(path = "/path").identity)
    }

    @Test
    fun `identity includes query and fragment when present`() {
        assertEquals(
            "/path?key=value#section1",
            url(path = "/path", query = "key=value", fragment = "section1").identity
        )
    }

    @Test
    fun `toString minimal url`() {
        assertEquals("https://example.com", url().toString())
    }

    @Test
    fun `toString does not duplicate leading slash in path`() {
        assertEquals("https://example.com/path", url(path = "/path").toString())
    }

    @Test
    fun `toString inserts slash before relative path`() {
        assertEquals("https://example.com/path", url(path = "path").toString())
    }

    @Test
    fun `toString full url`() {
        assertEquals(
            "https://user:pw@example.com:443/index.html?a=1#top",
            url(
                userInfo = "user:pw",
                port = 443,
                path = "/index.html",
                query = "a=1",
                fragment = "top"
            ).toString()
        )
    }

    @Test
    fun `toUri keeps userInfo in authority`() {
        assertEquals(
            Uri(
                scheme = "https",
                authority = "user@example.com:8080",
                path = "/p",
                query = "",
                fragment = ""
            ),
            url(userInfo = "user", port = 8080, path = "/p").toUri()
        )
    }

    @Test
    fun `toString round trips through Uri toUrl`() {
        val original = url(userInfo = "user", port = 8080, path = "/p", query = "a=1", fragment = "f")
        assertEquals(original, original.toUri().toUrl())
    }
}
