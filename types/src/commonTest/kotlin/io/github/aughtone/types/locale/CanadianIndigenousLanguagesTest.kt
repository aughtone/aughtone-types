package io.github.aughtone.types.locale

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CanadianIndigenousLanguagesTest {

    private val twoLetter = listOf("cr", "oj", "ik", "iu")
    private val threeLetter = listOf(
        "hur", "squ", "str", "kwk", "nuk", "hai", "shs", "oka", "lil",
        "bla", "crg", "chp", "dgr", "gwi", "moh", "cay", "one", "wya", "moe", "atj", "mic", "pqm",
    )

    @Test
    fun `every language resolves with and without Canada`() {
        for (code in twoLetter + threeLetter) {
            val base = localeFor(code)
            assertNotNull(base, "expected a bundled locale for $code")
            assertEquals(code, base.languageTag)
            assertNull(base.regionCode)

            val canadian = localeFor("$code-CA")
            assertNotNull(canadian, "expected a bundled locale for $code-CA")
            assertEquals(code, canadian.languageCode)
            assertEquals("CA", canadian.regionCode)
            assertEquals("$code-CA", canadian.languageTag)
        }
    }

    @Test
    fun `Inupiaq is carried for the United States as well`() {
        assertEquals("ik-US", localeFor("ik-US")?.languageTag)
    }

    @Test
    fun `a three letter language keeps its code through normalization`() {
        assertEquals("hur-CA", normalizeLanguageTag("HUR_ca"))
        assertEquals("moh-CA", parseLocale("moh-ca").languageTag)
        assertEquals("mic-CA", resolveLocale("mic-CA-x-private")?.languageTag)
    }

    @Test
    fun `the ISO 639-2 code of a two letter language normalizes to it`() {
        assertEquals("cr", localeFor("cre")?.languageTag)
        assertEquals("cr-CA", localeFor("cre-CA")?.languageTag)
        assertEquals("oj-CA", localeFor("oji-CA")?.languageTag)
        assertEquals("ik-US", localeFor("ipk-US")?.languageTag)
        assertEquals("iu-CA", localeFor("iku-CA")?.languageTag)
    }

    @Test
    fun `syllabics is the default script for Cree and Ojibwe`() {
        assertEquals("cr-CA", localeFor("cr-Cans-CA")?.languageTag)
        assertEquals("oj-CA", localeFor("oji-Cans-CA")?.languageTag)
        // Latin-script Cree is a different locale and must not be rewritten to syllabics.
        assertEquals("cr-Latn-CA", normalizeLanguageTag("cr-Latn-CA"))
    }

    @Test
    fun `only the member CLDR treats as the macrolanguage folds into it`() {
        // CLDR aliases Woods Cree to cr and Eastern Ojibwa to oj. Plains Cree and Northwestern
        // Ojibwa are distinct languages CLDR does not alias, and they keep their own codes.
        assertEquals("cr-CA", localeFor("cwd-CA")?.languageTag)
        assertEquals("oj-CA", localeFor("ojg-CA")?.languageTag)
        assertEquals("crk-CA", normalizeLanguageTag("crk-CA"))
        assertEquals("ojb-CA", normalizeLanguageTag("ojb-CA"))
        assertNull(localeFor("crk-CA"))
    }

    @Test
    fun `a new locale survives serialization`() {
        val json = Json { encodeDefaults = true }
        val locale = localeFor("hur-CA")
        assertNotNull(locale)
        assertEquals(locale, json.decodeFromString(Locale.serializer(), json.encodeToString(Locale.serializer(), locale)))
    }

    @Test
    fun `the new locales carry English names`() {
        assertEquals("Cree (Canada)", localeFor("cr-CA")?.displayName)
        assertEquals("Halkomelem", localeFor("hur")?.displayName)
        assertEquals("Mohawk (Canada)", localeFor("moh-CA")?.displayName)
        // No CLDR name exists for Squamish, so the name is the ISO 639-3 reference name.
        assertEquals("Squamish", localeFor("squ")?.displayName)
    }

    @Test
    fun `a new locale is named in English and in French on every platform`() {
        val english = localeFor("en")!!
        val french = localeFor("fr")!!
        val mohawk = localeFor("moh")!!

        assertEquals("Mohawk", mohawk.localizedDisplayName(displayIn = english))
        val inFrench = mohawk.localizedDisplayNameOrNull(displayIn = french)
        assertNotNull(inFrench, "expected a French name for moh, from the platform or the supplement")
        assertNotEquals(mohawk.displayName, inFrench)
    }

    @Test
    fun `a language no platform can name falls back to its English name`() {
        val squamish = localeFor("squ-CA")!!
        assertEquals("Squamish (Canada)", squamish.localizedDisplayName(displayIn = localeFor("fr")!!))
    }
}
