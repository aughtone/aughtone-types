package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UrlBuilderTest {

    @Test
    fun `Simple URL`() {
        val url = UrlBuilder().apply {
            scheme = "https"
            host = "www.example.com"
        }.build()

        assertEquals("https://www.example.com", url)
    }

    @Test
    fun `URL with Path Segments`() {
        // Example 2: URL with Path Segments
        val url = UrlBuilder()
            .apply {
                scheme = "http"
                host = "api.example.com"
                addPathSegment("users")
                addPathSegment("profile")
            }
            .build()

        assertEquals("http://api.example.com/users/profile", url)
    }

    @Test
    fun `URL with Path Segments and Query Parameters`() {
        // Example 3: URL with Path Segments and Query Parameters
        val url = UrlBuilder()
            .apply {
                scheme = "https"
                host = "search.example.com"
                port = 8080
                addPathSegments("search", "results")
                addQueryParameter("q", "kotlin url builder")
                addQueryParameter("page", "2")
            }
            .build()

        assertEquals(
            "https://search.example.com:8080/search/results?q=kotlin%20url%20builder&page=2",
            url
        )
    }

    @Test
    fun `URL with Path Segments and Query Parameters and special characters`() {
        val url = UrlBuilder()
            .apply {
                scheme = "https"
                host = "search.example.com"
                port = 8080
                addPathSegments("search with space", "results")
                addQueryParameter("q with space", "kotlin url builder with space")
                addQueryParameter("page", "2")
            }
            .build()

        assertEquals(
            "https://search.example.com:8080/search%20with%20space/results?q%20with%20space=kotlin%20url%20builder%20with%20space&page=2",
            url
        )
    }

    @Test
    fun `URL with non ascii path segment is percent encoded`() {
        val url = UrlBuilder()
            .apply {
                scheme = "https"
                host = "example.com"
                addPathSegment("你好")
            }
            .build()

        assertEquals("https://example.com/%E4%BD%A0%E5%A5%BD", url)
    }

    @Test
    fun `Repeated query parameter names are preserved in order`() {
        val url = UrlBuilder()
            .apply {
                scheme = "https"
                host = "example.com"
                addQueryParameter("tag", "a")
                addQueryParameter("tag", "b")
            }
            .build()

        assertEquals("https://example.com?tag=a&tag=b", url)
    }

    @Test
    fun `Missing scheme or host`() {
        try {
            val url5 = UrlBuilder()
                .apply {
                    addPathSegment("something")
                }
                .build()
            fail("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertEquals(
                "Scheme and host must be set to build a URL",
                e.message
            )
        }


    }
}
