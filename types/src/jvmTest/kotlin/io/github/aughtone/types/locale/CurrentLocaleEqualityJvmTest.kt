package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.util.Locale as JavaLocale

/**
 * A device set to a shipped locale gets that locale from `Locale.current`, named from the locale
 * table rather than by the platform, whatever the device's own display language. (#48)
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
            }
        }
    }

    @Test
    fun `the name comes from the locale table, not the device's language`() {
        withDefault("fr-CA") {
            assertEquals("French (Canada)", Locale.current.displayName)
        }
    }
}
