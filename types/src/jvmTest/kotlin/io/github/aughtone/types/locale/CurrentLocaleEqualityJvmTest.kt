package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.util.Locale as JavaLocale

/**
 * A device set to a shipped locale gets that locale from `Locale.current`. A device set to one the
 * library does not ship keeps its codes. Either way `displayName` is in the device's language where
 * anything can name it there, and the stored name is English. (#48)
 */
class CurrentLocaleEqualityJvmTest {

    private fun withDefault(tag: String, block: () -> Unit) {
        val saved = JavaLocale.getDefault()
        try {
            JavaLocale.setDefault(JavaLocale.forLanguageTag(tag))
            block()
        } finally {
            JavaLocale.setDefault(saved)
        }
    }

    @Test
    fun `a device set to a shipped locale reports that locale`() {
        for (tag in listOf("fr-CA", "de-DE", "ja-JP", "zh-TW", "zh-Hant-TW", "sr-Latn-RS", "pt-BR")) {
            withDefault(tag) {
                val shipped = assertNotNull(localeFor(tag), tag)
                val current = Locale.current
                assertEquals(shipped, current, tag)
                assertEquals(shipped.hashCode(), current.hashCode(), tag)
                // Equality is the tag alone, so the name needs checking on its own.
                assertEquals(shipped.displayName, current.displayName, tag)
            }
        }
    }

    @Test
    fun `the display name is in the device's language`() {
        withDefault("fr-CA") {
            assertEquals("français (Canada)", Locale.current.displayName)
            assertEquals("French (Canada)", Locale.current.fallbackName)
        }
    }

    @Test
    fun `a device set to an unshipped locale is named in its own language`() {
        withDefault("kok-IN") {
            val current = Locale.current
            assertEquals("kok-IN", current.languageTag)
            assertEquals("कोंकणी (भारत)", current.displayName)
            assertEquals("Konkani (India)", current.fallbackName)
        }
    }

    @Test
    fun `the display name follows a change of device language`() {
        val germany = localeFor("de-DE")!!
        withDefault("en-US") { assertEquals("German (Germany)", germany.displayName) }
        withDefault("fr-FR") { assertEquals("allemand (Allemagne)", germany.displayName) }
    }

    @Test
    fun `a device in a region the library does not ship keeps its region and its currency`() {
        withDefault("en-ZW") {
            assertEquals("en-ZW", Locale.current.languageTag)
            assertEquals("ZW", Locale.current.regionCode)
            assertEquals("ZWG", io.github.aughtone.types.financial.Currency.current.code)
        }
    }
}
