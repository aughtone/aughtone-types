package io.github.aughtone.types.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.toString

class UrnTest {
    // Spec is at: https://datatracker.ietf.org/doc/html/rfc5870

    val testUri = Uri(
        scheme = "urn",
        authority = "test",
        path = "bogus",
        query = "",
        fragment = ""
    )
    private val urn = Urn(
        namespace = "test",
        identity = "bogus"
    )

    @Test
    fun `urn toString is valid`() {
        assertEquals("urn:test:bogus", urn.toString())
    }

    @Test
    fun `urn toUri is valid`() {
        assertEquals(testUri, urn.toUri())
    }

    @Test
    fun `urn can be parsed`() {
        val parsed = urn("urn:test:bogus")
        assertEquals(urn, parsed)
    }

    @Test
    fun `parsing invalid urn string without urn prefix fails`() {
        assertFailsWith<IllegalArgumentException> {
            urn("test:bogus")
        }
    }

    @Test
    fun `parsing invalid urn string without identity fails`() {
        assertFailsWith<IllegalArgumentException> {
            urn("urn:test")
        }
    }
}
