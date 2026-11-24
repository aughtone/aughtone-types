package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The Windows answers, which no other target can check: these run only on a Windows host.
 */
class WindowsLocaleTest {

    @Test
    fun `Windows reports the user's locale`() {
        val current = assertNotNull(currentNativeLocale())
        assertTrue(current.languageCode.isNotEmpty())
    }

    @Test
    fun `a locale Windows knows is looked up natively`() {
        val locale = assertNotNull(localeForNative("de-DE"))
        assertEquals("de", locale.languageCode)
        assertEquals("DE", locale.regionCode)
        assertTrue(locale.displayName.isNotBlank())
    }

    @Test
    fun `a locale Windows does not know is a miss rather than placeholder data`() {
        assertNull(localeForNative("qq-QQ"))
    }

    @Test
    fun `display names come from the bundled table`() {
        assertEquals(
            LocaleDisplayNameTable.nameFor("fr", "de-DE"),
            localizedDisplayNameForNative(assertNotNull(localeFor("de-DE")), assertNotNull(localeFor("fr"))),
        )
        assertNotNull(LocaleDisplayNameTable.nameFor("fr", "de-DE"))
    }
}
