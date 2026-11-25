package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Plain Serbian is Cyrillic, as CLDR has it, and Latin Serbian ships under its own tags (#51).
 */
class SerbianScriptTest {

    @Test
    fun `plain Serbian is Cyrillic`() {
        assertNull(localeFor("sr")?.scriptCode)
        assertEquals("Cyrl", localeFor("sr")?.effectiveScriptCode)
        assertEquals("Cyrl", localeFor("sr-RS")?.effectiveScriptCode)
        assertEquals(localeFor("sr-RS"), localeFor("sr-Cyrl-RS"))
        assertEquals(localeFor("sr"), localeFor("sr-Cyrl"))
    }

    @Test
    fun `Latin Serbian ships under its own tags`() {
        assertEquals("sr-Latn", localeFor("sr-Latn")?.languageTag)
        assertEquals("sr-Latn-RS", localeFor("sr-Latn-RS")?.languageTag)
        assertEquals("Serbian (Latin, Serbia)", localeFor("sr-Latn-RS")?.fallbackName)
    }

    @Test
    fun `Serbo-Croatian codes resolve to Latin Serbian`() {
        assertEquals("sr-Latn", localeFor("sh")?.languageTag)
        assertEquals("sr-Latn-RS", localeFor("sh-RS")?.languageTag)
        assertEquals("sr-Latn-RS", localeFor("hbs-RS")?.languageTag)
        // A script named in the tag is kept rather than doubled.
        assertEquals("sr-Cyrl-RS", normalizeLanguageTag("sh-Cyrl-RS"))
        assertEquals("sr-Latn-RS", normalizeLanguageTag("sh-Latn-RS"))
    }

    @Test
    fun `Serbian in Montenegro resolves to Latin`() {
        assertEquals(localeFor("sr-Latn"), resolveLocale("sr-ME"))
    }

    @Test
    fun `bundled Serbian names are Cyrillic and not served to a Latin reader`() {
        val french = localeFor("fr")!!
        assertEquals("sr", bundledNamesLanguage(localeFor("sr-RS")!!))
        assertNull(bundledNamesLanguage(localeFor("sr-Latn-RS")!!))
        assertNull(bundledNamesLanguage(localeFor("zh-TW")!!))
        assertEquals("zh", bundledNamesLanguage(localeFor("zh-CN")!!))
        assertEquals("fr", bundledNamesLanguage(french))
        assertNotNull(localeFor("sr")!!.localizedDisplayNameOrNull(displayIn = french))
    }
}
