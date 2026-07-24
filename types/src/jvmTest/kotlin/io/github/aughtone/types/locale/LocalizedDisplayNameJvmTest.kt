package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocalizedDisplayNameJvmTest {

    @Test
    fun `test localizedDisplayName localizes via jvm cldr`() {
        val german = localeFor("de")
        val french = localeFor("fr")
        assertNotNull(german)
        assertNotNull(french)

        val name = german.localizedDisplayName(displayIn = french)
        assertTrue(
            name.equals("allemand", ignoreCase = true),
            "Expected the French name of German, got '$name'"
        )
    }

    @Test
    fun `test localizedDisplayName falls back to english for unknown language`() {
        val french = localeFor("fr")
        assertNotNull(french)
        val unknown = parseLocale("zz-ZZ")
        assertEquals("zz-ZZ", unknown.localizedDisplayName(displayIn = french))
    }
}
