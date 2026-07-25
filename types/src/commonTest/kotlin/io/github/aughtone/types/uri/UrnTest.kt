package io.github.aughtone.types.uri

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UrnTest {
    // Spec is at: https://www.rfc-editor.org/rfc/rfc8141.html

    val testUri = Uri(
        scheme = "urn",
        authority = "",
        path = "test:bogus",
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
    fun `urn toUri toString has no authority`() {
        assertEquals("urn:test:bogus", urn.toUri().toString())
    }

    @Test
    fun `urn can be parsed`() {
        val parsed = urn("urn:test:bogus")
        assertEquals(urn, parsed)
    }

    @Test
    fun `urn prefix is case insensitive when parsing`() {
        assertEquals(urn, urn("URN:test:bogus"))
        assertEquals(urn, urn("Urn:test:bogus"))
    }

    @Test
    fun `nid comparison is case insensitive`() {
        assertEquals(Urn("ISBN", "0451450523"), Urn("isbn", "0451450523"))
        assertEquals(Urn("ISBN", "x").hashCode(), Urn("isbn", "x").hashCode())
    }

    @Test
    fun `nss comparison is case sensitive`() {
        assertEquals(false, Urn("test", "ABC") == Urn("test", "abc"))
    }

    @Test
    fun `nss may contain colons`() {
        val parsed = urn("urn:test:a:b:c")
        assertEquals("a:b:c", parsed.identity)
    }

    @Test
    fun `rqf components are parsed`() {
        val parsed = urn("urn:example:a?+res?=q=1#frag")
        assertEquals("example", parsed.namespace)
        assertEquals("a", parsed.identity)
        assertEquals("res", parsed.rComponent)
        assertEquals("q=1", parsed.qComponent)
        assertEquals("frag", parsed.fComponent)
        assertEquals("urn:example:a?+res?=q=1#frag", parsed.toString())
    }

    @Test
    fun `q component alone is parsed`() {
        val parsed = urn("urn:example:a?=q=1")
        assertNull(parsed.rComponent)
        assertEquals("q=1", parsed.qComponent)
        assertEquals("a", parsed.identity)
    }

    @Test
    fun `f component alone is parsed`() {
        val parsed = urn("urn:example:a#frag")
        assertEquals("frag", parsed.fComponent)
        assertEquals("a", parsed.identity)
    }

    @Test
    fun `valid nid with hyphen is accepted`() {
        assertEquals("a-b", Urn("a-b", "x").namespace)
    }

    @Test
    fun `single character nid fails`() {
        assertFailsWith<IllegalArgumentException> { Urn("a", "x") }
    }

    @Test
    fun `empty nid fails`() {
        assertFailsWith<IllegalArgumentException> { Urn("", "x") }
        assertFailsWith<IllegalArgumentException> { urn("urn::x") }
    }

    @Test
    fun `nid starting or ending with hyphen fails`() {
        assertFailsWith<IllegalArgumentException> { Urn("-ab", "x") }
        assertFailsWith<IllegalArgumentException> { Urn("ab-", "x") }
    }

    @Test
    fun `nid with invalid characters fails`() {
        assertFailsWith<IllegalArgumentException> { Urn("a!b", "x") }
    }

    @Test
    fun `nid longer than 32 characters fails`() {
        assertFailsWith<IllegalArgumentException> { Urn("a".repeat(33), "x") }
    }

    @Test
    fun `empty nss fails`() {
        assertFailsWith<IllegalArgumentException> { Urn("test", "") }
        assertFailsWith<IllegalArgumentException> { urn("urn:test:") }
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
