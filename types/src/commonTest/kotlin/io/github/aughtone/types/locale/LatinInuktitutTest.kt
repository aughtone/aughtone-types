package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Latin-script Inuktitut ships as `iu-Latn` and `iu-Latn-CA` (#54). Its names in other languages are
 * composed from CLDR; nothing names it in Inuktitut yet.
 */
class LatinInuktitutTest {

    @Test
    fun `Latin-script Inuktitut is shipped`() {
        assertEquals("iu-Latn", localeFor("iu-Latn")?.languageTag)
        assertEquals("iu-Latn-CA", localeFor("iu-Latn-CA")?.languageTag)
        assertEquals("Latn", localeFor("iu-Latn-CA")?.scriptCode)
        assertNotEquals(localeFor("iu-CA"), localeFor("iu-Latn-CA"))
    }

    @Test
    fun `the Inuktitut member codes resolve to it in Latin script`() {
        assertEquals(localeFor("iu-Latn-CA"), localeFor("ike-Latn-CA"))
        assertEquals(localeFor("iu-Latn-CA"), localeFor("ikt-Latn-CA"))
        assertEquals(localeFor("iu-Latn-CA"), resolveLocale("iku-Latn-CA"))
    }

    @Test
    fun `syllabic Inuktitut is unchanged`() {
        assertNull(localeFor("iu-CA")?.scriptCode)
        assertEquals("Cans", localeFor("iu-CA")?.effectiveScriptCode)
        assertEquals(localeFor("iu-CA"), localeFor("iu-Cans-CA"))
    }

    @Test
    fun `its names in other languages come from the bundled tables`() {
        val french = localeFor("fr")!!
        assertEquals("inuktitut (latin, Canada)", localeFor("iu-Latn-CA")!!.localizedDisplayNameOrNull(displayIn = french))
    }
}
