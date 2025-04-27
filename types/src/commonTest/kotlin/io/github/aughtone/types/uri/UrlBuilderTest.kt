package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UrlBuilderTest {
    val testChineseParam = "你好 你好"
    val testEncodedChineseParam = "你好+你好"

    private val encoder = UrlEncoder

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
            "https://search.example.com:8080/search/results?q=kotlin+url+builder&page=2",
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
            "https://search.example.com:8080/search+with+space/results?q+with+space=kotlin+url+builder+with+space&page=2",
            url
        )
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
