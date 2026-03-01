package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
    fun `test Locale getCurrent returns a valid locale`() {
        // This test ensures that the native call for the current locale returns
        // a valid, non-null Locale object on the platform running the test.
        val currentLocale = Locale.getCurrent()
        assertNotNull(currentLocale)
        assertNotNull(localeFor(currentLocale.toLanguageTag()))
    }

    @Test
    fun `test getCurrentNativeLocaleImpl with a nonexistent native locale`() {
        // Simulate a native locale that is NOT in our resource map.
        val result = getCurrentNativeLocaleImpl(nativeTag = "xx-YY", fallbackTag = "fr-CA")
        // The result should be the fallback locale.
        assertEquals("fr-CA", result.toLanguageTag())
    }

    @Test
    fun `test getCurrentNativeLocaleImpl with an existing native locale`() {
        // Simulate a native locale that IS in our resource map.
        val result = getCurrentNativeLocaleImpl(nativeTag = "en-GB", fallbackTag = "fr-CA")
        // The result should be the native locale, not the fallback.
        assertEquals("en-GB", result.toLanguageTag())
    }

    @Test
    fun `test Locale toLanguageTag`() {
        assertEquals("en-US", Locale("en", "US", displayName = "").toLanguageTag())
        assertEquals("zh-Hans-CN", Locale("zh", "CN", "Hans", displayName = "").toLanguageTag())
        assertEquals("de", Locale("de", displayName = "").toLanguageTag())
        assertEquals("es-419", Locale("es", variantCode = "419", displayName = "").toLanguageTag())
    }
}
