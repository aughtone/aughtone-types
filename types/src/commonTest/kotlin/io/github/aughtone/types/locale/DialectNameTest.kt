package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The dialect form comes from CLDR's dedicated names, bundled on every target, and only on request. */
class DialectNameTest {

    private val english = localeFor("en")!!
    private val french = localeFor("fr")!!
    private val canadianFrench = localeFor("fr-CA")!!

    @Test
    fun `the dialect form uses CLDR's dedicated name`() {
        assertEquals("Canadian French", canadianFrench.localizedDisplayName(english, LanguageDisplay.Dialect))
        assertEquals("français canadien", canadianFrench.localizedDisplayName(french, LanguageDisplay.Dialect))
        assertEquals("Flemish", localeFor("nl-BE")!!.localizedDisplayName(english, LanguageDisplay.Dialect))
        assertEquals("Traditional Chinese", localeFor("zh-Hant")!!.localizedDisplayName(english, LanguageDisplay.Dialect))
    }

    @Test
    fun `the standard form is the default and is unchanged`() {
        assertEquals("French (Canada)", canadianFrench.localizedDisplayName(english))
        assertEquals("French (Canada)", canadianFrench.localizedDisplayName(english, LanguageDisplay.Standard))
    }

    @Test
    fun `a locale with no dialect name is named as in the standard form`() {
        val france = localeFor("fr-FR")!!
        for (reader in listOf(english, french, localeFor("ja")!!, localeFor("iu")!!)) {
            assertEquals(
                france.localizedDisplayName(reader, LanguageDisplay.Standard),
                france.localizedDisplayName(reader, LanguageDisplay.Dialect),
                reader.languageTag,
            )
            assertEquals(
                france.localizedDisplayNameOrNull(reader, LanguageDisplay.Standard),
                france.localizedDisplayNameOrNull(reader, LanguageDisplay.Dialect),
                reader.languageTag,
            )
        }
    }

    @Test
    fun `a locale built without its script finds the shipped locale's dialect name`() {
        assertEquals("Traditional Chinese", Locale("zh", scriptCode = "Hant", displayName = "x").localizedDisplayName(english, LanguageDisplay.Dialect))
    }

    @Test
    fun `dialect names follow the bundled script rule`() {
        // The Serbian dialect names are Cyrillic, so a Latin reader gets none from the bundle.
        assertNull(bundledNamesLanguage(localeFor("sr-Latn")!!)?.let { LocaleDialectNames.nameFor(it, "fr-CA") })
        assertEquals("канадски француски", canadianFrench.localizedDisplayName(localeFor("sr")!!, LanguageDisplay.Dialect))
    }

    @Test
    fun `the table holds the names it was generated with`() {
        assertEquals(1015, LocaleDialectNames.size)
    }
}
