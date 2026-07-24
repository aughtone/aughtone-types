package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UrlEncoderTest {

    private val encoder = UrlEncoder

    // Reference vectors verified against python urllib.parse.quote / quote_plus.

    @Test
    fun `ascii unreserved characters pass through`() {
        assertEquals("AZaz09-._~", encoder.encode("AZaz09-._~"))
    }

    @Test
    fun `space encodes as percent twenty`() {
        assertEquals("value%20with%20space", encoder.encode("value with space"))
    }

    @Test
    fun `reserved characters are percent encoded`() {
        assertEquals(
            "some%2Fpath%20with%3Aspace%2Fand%20%22more%22",
            encoder.encode("some/path with:space/and \"more\"")
        )
    }

    @Test
    fun `star is encoded and tilde is not`() {
        assertEquals("%2A~", encoder.encode("*~"))
    }

    @Test
    fun `percent sign is encoded`() {
        assertEquals("100%25", encoder.encode("100%"))
    }

    @Test
    fun `latin1 encodes as utf8 bytes`() {
        assertEquals("%C3%A9", encoder.encode("é")) // é
    }

    @Test
    fun `cjk encodes as utf8 bytes`() {
        assertEquals("%E4%BD%A0%E5%A5%BD", encoder.encode("你好"))
    }

    @Test
    fun `astral characters encode as four utf8 bytes`() {
        // U+1F600, a surrogate pair; must not be CESU-8 encoded.
        assertEquals("%F0%9F%98%80", encoder.encode("😀"))
    }

    @Test
    fun `unpaired surrogate fails to encode`() {
        // Built at runtime: a lone-surrogate string literal is mangled to '?'
        // by the Kotlin/JS compiler's UTF-8 output.
        assertFailsWith<IllegalArgumentException> {
            encoder.encode(Char(0xD83D).toString())
        }
    }

    @Test
    fun `decode reverses encode`() {
        val original = "some/path with:space/and \"more\" 你好 😀 ~*%"
        assertEquals(original, encoder.decode(encoder.encode(original)))
    }

    @Test
    fun `decode keeps plus as literal plus`() {
        assertEquals("a+b", encoder.decode("a+b"))
    }

    @Test
    fun `decode accepts lowercase hex`() {
        assertEquals("é", encoder.decode("%c3%a9"))
    }

    @Test
    fun `decode multibyte utf8`() {
        assertEquals("你好", encoder.decode("%E4%BD%A0%E5%A5%BD"))
        assertEquals("😀", encoder.decode("%F0%9F%98%80"))
    }

    @Test
    fun `decode fails on truncated percent sequence`() {
        assertFailsWith<IllegalArgumentException> { encoder.decode("%") }
        assertFailsWith<IllegalArgumentException> { encoder.decode("abc%2") }
    }

    @Test
    fun `decode fails on non hex digits`() {
        assertFailsWith<IllegalArgumentException> { encoder.decode("%GG") }
    }

    @Test
    fun `decode fails on invalid utf8 bytes`() {
        // A lone UTF-8 continuation lead byte is not a valid sequence.
        assertFailsWith<IllegalArgumentException> { encoder.decode("%E4") }
    }

    @Test
    fun `form data encodes space as plus`() {
        assertEquals("value+with+space", encoder.encodeFormData("value with space"))
    }

    @Test
    fun `form data keeps star bare and encodes tilde`() {
        assertEquals("*%7E", encoder.encodeFormData("*~"))
    }

    @Test
    fun `form data encodes non ascii as utf8 bytes`() {
        assertEquals(
            "%E4%BD%A0%E5%A5%BD+%E4%BD%A0%E5%A5%BD",
            encoder.encodeFormData("你好 你好")
        )
    }

    @Test
    fun `form data decode maps plus to space`() {
        assertEquals("a b", encoder.decodeFormData("a+b"))
    }

    @Test
    fun `form data round trip`() {
        val original = "kotlin url builder with space 你好"
        assertEquals(original, encoder.decodeFormData(encoder.encodeFormData(original)))
    }
}
