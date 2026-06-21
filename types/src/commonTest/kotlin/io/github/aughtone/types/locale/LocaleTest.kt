package io.github.aughtone.types.locale

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
        // This test ensures that the native call for the current locale returns
        // a valid, non-null Locale object on the platform running the test.
        val currentLocale = Locale.current
        assertNotNull(currentLocale)
        assertNotNull(localeFor(currentLocale.languageTag))
    }



    @Test
    fun `test Locale languageTag`() {
        assertEquals("en-US", Locale("en", "US", displayName = "").languageTag)
        assertEquals("zh-Hans-CN", Locale("zh", "CN", "Hans", displayName = "").languageTag)
        assertEquals("de", Locale("de", displayName = "").languageTag)
        assertEquals("es-419", Locale("es", variantCode = "419", displayName = "").languageTag)
    }

    @Test
    fun `test Locale toLanguageTag compatibility`() {
        assertEquals("en-US", Locale("en", "US", displayName = "").toLanguageTag())
    }

    @Test
    fun `test availableLocales returns a non-empty list`() {
        val locales = availableLocales()
        assertTrue(locales.isNotEmpty(), "availableLocales() should not return an empty list")
    }

    @Test
    fun `test localesByName filters correctly`() {
        val englishLocales = localesByName("English")
        assertTrue(englishLocales.isNotEmpty(), "Should find locales with 'English' in the name")
        assertTrue(englishLocales.all { it.displayName?.contains("English", ignoreCase = true) == true })

        val caseInsensitiveLocales = localesByName("english", ignoreCase = true)
        assertEquals(englishLocales.size, caseInsensitiveLocales.size)

        val none = localesByName("NonExistentLocaleName123")
        assertTrue(none.isEmpty(), "Should return empty list for non-existent names")
    }
}
