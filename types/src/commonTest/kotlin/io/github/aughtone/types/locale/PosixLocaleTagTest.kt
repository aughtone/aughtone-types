package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PosixLocaleTagTest {

    @Test
    fun `LANG is read when nothing overrides it`() {
        assertEquals("en-CA", posixLocaleTag(lcAll = null, lcMessages = null, lang = "en_CA.UTF-8"))
    }

    @Test
    fun `LC_ALL overrides LC_MESSAGES and LANG`() {
        assertEquals("fr-CA", posixLocaleTag(lcAll = "fr_CA.UTF-8", lcMessages = "de_DE.UTF-8", lang = "en_US.UTF-8"))
    }

    @Test
    fun `LC_MESSAGES overrides LANG`() {
        assertEquals("de-DE", posixLocaleTag(lcAll = null, lcMessages = "de_DE.UTF-8", lang = "en_US.UTF-8"))
    }

    @Test
    fun `an empty variable counts as unset`() {
        assertEquals("en-US", posixLocaleTag(lcAll = "", lcMessages = "", lang = "en_US.UTF-8"))
        assertNull(posixLocaleTag(lcAll = null, lcMessages = null, lang = ""))
    }

    @Test
    fun `the C and POSIX locales name no language`() {
        assertNull(posixLocaleTag(lcAll = null, lcMessages = null, lang = "C"))
        assertNull(posixLocaleTag(lcAll = null, lcMessages = null, lang = "C.UTF-8"))
        assertNull(posixLocaleTag(lcAll = null, lcMessages = null, lang = "POSIX"))
    }

    @Test
    fun `LC_ALL set to C hides the language in LANG`() {
        // LC_ALL wins outright, so a LANG behind it is not consulted.
        assertNull(posixLocaleTag(lcAll = "C", lcMessages = null, lang = "en_CA.UTF-8"))
    }

    @Test
    fun `the codeset and modifier are dropped`() {
        assertEquals("de-DE", posixLocaleTag(lcAll = null, lcMessages = null, lang = "de_DE.ISO-8859-15@euro"))
        assertEquals("sr-RS", posixLocaleTag(lcAll = null, lcMessages = null, lang = "sr_RS@latin"))
        assertEquals("en", posixLocaleTag(lcAll = null, lcMessages = null, lang = "en"))
    }

    @Test
    fun `nothing set gives null`() {
        assertNull(posixLocaleTag(lcAll = null, lcMessages = null, lang = null))
    }
}
