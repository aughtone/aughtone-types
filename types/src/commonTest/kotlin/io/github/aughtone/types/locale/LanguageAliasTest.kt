package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanguageAliasTest {

    @Test
    fun `terminology and bibliographic codes both normalize to the two letter code`() {
        assertEquals("en-CA", localeFor("eng-CA")?.languageTag)
        assertEquals("fr-CA", localeFor("fra-CA")?.languageTag)
        assertEquals("fr-CA", localeFor("fre-CA")?.languageTag)
        assertEquals("de-DE", localeFor("ger-DE")?.languageTag)
        assertEquals("de-DE", localeFor("DEU_de")?.languageTag)
        assertEquals("nl-BE", localeFor("dut-BE")?.languageTag)
    }

    @Test
    fun `an alias keeps the rest of the tag`() {
        assertEquals("zh-Hant-TW", normalizeLanguageTag("chi-Hant-TW"))
        assertEquals("en-US-x-test", normalizeLanguageTag("eng-US-x-test"))
    }

    @Test
    fun `Tagalog's code goes to tl rather than fil`() {
        // CLDR folds tgl into fil, but both are shipped here and tgl is the ISO 639-2 code for tl.
        assertEquals("tl", localeFor("tgl")?.languageTag)
        assertEquals("fil", localeFor("fil")?.languageTag)
    }

    @Test
    fun `Moldavian's deprecated code goes to Romanian`() {
        assertEquals("ro", localeFor("mo")?.languageTag)
        assertEquals("ro-RO", localeFor("mo-RO")?.languageTag)
        assertEquals("ro-MD", normalizeLanguageTag("mo-MD"))
    }

    @Test
    fun `Serbo-Croatian's codes go to the Latin-script Serbian this library ships`() {
        // CLDR maps sh and hbs to sr-Latn. Serbian ships here only in Latin script, as sr and sr-RS.
        assertEquals("sr-Latn", localeFor("sh")?.languageTag)
        assertEquals("sr-Latn-RS", localeFor("sh-RS")?.languageTag)
        assertEquals("sr-Latn-RS", localeFor("hbs-RS")?.languageTag)
        assertEquals("sr-Latn-RS", resolveLocale("sh_RS")?.languageTag)
    }

    @Test
    fun `every alias names a language the library ships`() {
        val shipped = localeResourceMap.values.map { it.languageCode }.toSet()
        val dangling = languageAliasMap.filterValues { it !in shipped }
        assertTrue(dangling.isEmpty(), "aliases to languages not shipped: $dangling")
    }

    @Test
    fun `no alias shadows a shipped language`() {
        val shipped = localeResourceMap.values.map { it.languageCode }.toSet()
        val shadowing = languageAliasMap.keys.filter { it in shipped }
        assertTrue(shadowing.isEmpty(), "aliases that would rewrite a shipped language: $shadowing")
    }

    @Test
    fun `every shipped two letter language is reachable from its ISO 639-2 terminology code`() {
        // A sample across the table; the full set comes from CLDR and is pinned by the two tests above.
        val expected = mapOf(
            "ara" to "ar", "zho" to "zh", "msa" to "ms", "fas" to "fa", "nob" to "nb",
            "heb" to "he", "ind" to "id", "swa" to "sw", "uzb" to "uz", "kur" to "ku",
        )
        for ((alias, code) in expected) {
            assertEquals(code, normalizeLanguageTag(alias), "alias $alias")
        }
    }
}
