package io.github.aughtone.types.locale

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocaleTest {

    @Test
    fun `test localeFor with full language-region code`() {
        val locale = localeFor("en-US")
        assertNotNull(locale)
        assertEquals("en", locale.languageCode)
        assertEquals("US", locale.regionCode)
        assertEquals("English (United States)", locale.displayName)
    }

    @Test
    fun `test localeFor with language-only code`() {
        val locale = localeFor("fr")
        assertNotNull(locale)
        assertEquals("fr", locale.languageCode)
        assertNull(locale.regionCode)
        assertEquals("French", locale.displayName)
    }

    @Test
    fun `test localeFor with an invalid code`() {
        val locale = localeFor("xx-YY")
        assertNull(locale)
    }

    @Test
    fun `test localeFor with a script code`() {
        val locale = localeFor("zh-Hans")
        assertNotNull(locale)
        assertEquals("zh", locale.languageCode)
        assertEquals("Hans", locale.scriptCode)
        assertNull(locale.regionCode)
        assertEquals("Chinese (Simplified)", locale.displayName)
    }
    
    @Test
    fun `test resolveLocale with fallback`() {
        val locale = resolveLocale("en-XX")
        assertNotNull(locale)
        assertEquals("en", locale.languageCode)
        assertEquals("English", locale.displayName)
    }

    @Test
    fun `test parseLocale with existing locale`() {
        val locale = parseLocale("en-US")
        assertEquals("en", locale.languageCode)
        assertEquals("US", locale.regionCode)
        assertEquals("English (United States)", locale.displayName)
    }

    @Test
    fun `test parseLocale with new locale`() {
        val locale = parseLocale("en-XX")
        assertEquals("en", locale.languageCode)
        assertEquals("XX", locale.regionCode)
        assertEquals("en-XX", locale.displayName)
    }

    @Test
    fun `test parseLocale with complex tag`() {
        val locale = parseLocale("zh-Hans-CN-variant")
        assertEquals("zh", locale.languageCode)
        assertEquals("Hans", locale.scriptCode)
        assertEquals("CN", locale.regionCode)
        assertEquals("variant", locale.variantCode)
    }

    @Test
    fun `test Locale getCurrent returns a valid locale`() {
        // When the platform can determine a system locale, it must resolve.
        // Environments with no system locale at all (e.g. containers without LANG)
        // are documented to throw, which is acceptable here.
        val currentLocale = try {
            Locale.current
        } catch (e: IllegalStateException) {
            return
        } catch (e: IllegalArgumentException) {
            return
        }
        // resolveLocale, since script-bearing tags like zh-Hans-CN resolve by stepdown.
        assertNotNull(resolveLocale(currentLocale.languageTag))
    }

    @Test
    fun `test legacy hebrew code resolves to modern locale`() {
        val locale = resolveLocale("iw-IL")
        assertNotNull(locale)
        assertEquals("he", locale.languageCode)
        assertEquals("IL", locale.regionCode)
    }

    @Test
    fun `the three letter Inuktitut code resolves to iu`() {
        // iku is ISO 639-2/3 for the same language as iu, just the long form.
        assertEquals("iu", localeFor("iku")?.languageTag)
        assertEquals("iu-CA", localeFor("iku-CA")?.languageTag)
    }

    @Test
    fun `Eastern Canadian Inuktitut resolves to iu`() {
        // ike is the syllabics-written member of the iu macrolanguage, which is the variety the
        // bundled iu locale describes, so every lookup answers with iu, as iw does with he.
        assertEquals("iu", localeFor("ike")?.languageTag)
        assertEquals("iu-CA", localeFor("ike-CA")?.languageTag)
        assertEquals("iu", resolveLocale("ike")?.languageTag)
        assertEquals("iu-CA", resolveLocale("ike-CA")?.languageTag)
        assertEquals("iu-CA", resolveLocale("IKE_ca")?.languageTag)
        assertEquals("iu-CA", Locale.getLocale("ike-CA")?.languageTag)
    }

    @Test
    fun `Inuinnaqtun resolves to iu like the other Inuktitut codes`() {
        assertEquals("iu", localeFor("ikt")?.languageTag)
        assertEquals("iu-CA", localeFor("ikt-CA")?.languageTag)
    }

    @Test
    fun `syllabics is iu's default script so naming it changes nothing`() {
        assertEquals("iu-CA", localeFor("iu-Cans-CA")?.languageTag)
        assertEquals("iu", localeFor("iu-Cans")?.languageTag)
        assertEquals("iu-CA", localeFor("ike-Cans-CA")?.languageTag)
        assertEquals("iu-CA", resolveLocale("ike_cans_ca")?.languageTag)
    }

    @Test
    fun `a script that is not the default is kept`() {
        // Latin-script Inuktitut is a real tag; it must not be silently rewritten to syllabics.
        assertEquals("iu-Latn-CA", normalizeLanguageTag("ike-Latn-CA"))
        // And the default-script rule applies only to the language it belongs to.
        assertEquals("sr-Latn-RS", normalizeLanguageTag("sr-Latn-RS"))
    }

    @Test
    fun `test legacy indonesian code resolves to modern locale`() {
        val locale = resolveLocale("in-ID")
        assertNotNull(locale)
        assertEquals("id", locale.languageCode)
    }

    @Test
    fun `test parseLocale normalizes casing for unknown tags`() {
        val locale = parseLocale("en-in")
        assertEquals("en", locale.languageCode)
        assertEquals("IN", locale.regionCode)
        assertEquals("en-IN", locale.languageTag)
    }

    @Test
    fun `test normalize keeps extension subtags lowercase`() {
        assertEquals("th-TH-u-nu-thai", normalizeLanguageTag("th-TH-u-nu-thai"))
        assertEquals("en-x-priv", normalizeLanguageTag("en-X-PRIV"))
    }

    @Test
    fun `test script bearing locale round trips through resolveLocale`() {
        val azeri = localeFor("az")
        assertNotNull(azeri)
        assertEquals("az-Latn", azeri.languageTag)
        assertNotNull(resolveLocale(azeri.languageTag))
    }



    @Test
    fun `test Locale languageTag`() {
        assertEquals("en-US", Locale("en", "US", displayName = "").languageTag)
        assertEquals("zh-Hans-CN", Locale("zh", "CN", "Hans", displayName = "").languageTag)
        assertEquals("de", Locale("de", displayName = "").languageTag)
        assertEquals("es-419", Locale("es", variantCode = "419", displayName = "").languageTag)
    }

    @Test
    fun `test availableLocales returns a non-empty list`() {
        val locales = availableLocales()
        assertTrue(locales.isNotEmpty(), "availableLocales() should not return an empty list")
    }

    @Test
    fun `test localeFor resolves expanded world language set`() {
        val expectedScriptCodes: Map<String, String?> = mapOf(
            "am" to "Ethi", "as" to "Beng", "bn" to "Beng", "bo" to "Tibt",
            "fil" to null, "gu" to "Gujr", "ha" to null, "ig" to null,
            "jv" to null, "km" to "Khmr", "kn" to "Knda", "ku" to null,
            "ky" to "Cyrl", "lo" to "Laoo", "ml" to "Mlym", "mn" to "Cyrl",
            "mr" to "Deva", "my" to "Mymr", "ne" to "Deva", "or" to "Orya",
            "pa" to "Guru", "ps" to "Arab", "sd" to "Arab", "si" to "Sinh",
            "so" to null, "su" to null, "ta" to "Taml", "te" to "Telu",
            "tg" to "Cyrl", "tl" to null, "ug" to "Arab", "ur" to "Arab",
            "yo" to null, "zu" to null
        )
        expectedScriptCodes.forEach { (code, scriptCode) ->
            val locale = localeFor(code)
            assertNotNull(locale, "Expected '$code' in the locale resource map")
            assertEquals(code, locale.languageCode, "Unexpected languageCode for '$code'")
            assertEquals(scriptCode, locale.scriptCode, "Unexpected scriptCode for '$code'")
            assertNull(locale.regionCode, "Base language entry '$code' should not carry a regionCode")
        }
    }

    @Test
    fun `test localizedDisplayName is never blank and falls back for unknown locales`() {
        listOf("en", "fr", "bn", "zh-Hans").forEach { tag ->
            val locale = localeFor(tag)
            assertNotNull(locale)
            val name = locale.localizedDisplayName(displayIn = locale)
            assertTrue(name.isNotBlank(), "localizedDisplayName for '$tag' should not be blank")
        }

        // Unknown locales have no CLDR data on any platform, so the English
        // displayName fallback must be returned.
        val unknown = parseLocale("zz-ZZ")
        assertEquals(unknown.displayName, unknown.localizedDisplayName(displayIn = unknown))
    }

    @Test
    fun `test Locale serialization round trip`() {
        val json = Json { encodeDefaults = true }
        val locale = localeFor("zh-CN")
        assertNotNull(locale)
        val encoded = json.encodeToString(Locale.serializer(), locale)
        assertEquals(locale, json.decodeFromString(Locale.serializer(), encoded))
    }

    @Test
    fun `test localesByName filters correctly`() {
        val englishLocales = localesByName("English")
        assertTrue(englishLocales.isNotEmpty(), "Should find locales with 'English' in the name")
        assertTrue(englishLocales.all { it.displayName.contains("English", ignoreCase = true) })

        val caseInsensitiveLocales = localesByName("english", ignoreCase = true)
        assertEquals(englishLocales.size, caseInsensitiveLocales.size)

        val none = localesByName("NonExistentLocaleName123")
        assertTrue(none.isEmpty(), "Should return empty list for non-existent names")
    }
}
