package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The bundled names serve a reader only in the script they are written in, reading the script a
 * region implies as [resolveLocale] does, and find a locale however its tag was spelled.
 */
class BundledNamesReaderTest {

    @Test
    fun `a reader whose region implies another script skips the bundled names`() {
        assertNull(bundledNamesLanguage(parseLocale("sr-ME")))
        assertNull(bundledNamesLanguage(parseLocale("zh-MO")))
        assertNull(bundledNamesLanguage(Locale("zh", "TW", displayName = "x")))
        assertNull(bundledNamesLanguage(parseLocale("pa-PK")))
        assertEquals("sr", bundledNamesLanguage(localeFor("sr-RS")!!))
        assertEquals("zh", bundledNamesLanguage(parseLocale("zh-SG")))
    }

    @Test
    fun `a locale built without its script finds the bundled names of the shipped one`() {
        val bare = Locale("zh", "TW", displayName = "Chinese (Taiwan)")
        val afrikaans = localeFor("af")!!
        assertEquals(
            localeFor("zh-TW")!!.localizedDisplayNameOrNull(displayIn = afrikaans),
            bare.localizedDisplayNameOrNull(displayIn = afrikaans),
        )
    }
}
