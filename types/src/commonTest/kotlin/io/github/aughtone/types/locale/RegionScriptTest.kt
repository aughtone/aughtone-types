package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * A language-region tag with no script resolves within the script its region implies. (#50)
 */
class RegionScriptTest {

    @Test
    fun `Chinese in Macau finds Traditional Chinese`() {
        assertEquals(localeFor("zh-Hant"), resolveLocale("zh-MO"))
    }

    @Test
    fun `a shipped locale is still found by its own tag`() {
        assertEquals(localeFor("zh-TW"), resolveLocale("zh-TW"))
        assertEquals(localeFor("zh-HK"), resolveLocale("zh-HK"))
        assertEquals(localeFor("fr-CA"), resolveLocale("fr-CA"))
    }

    @Test
    fun `a script named in the tag wins over the one its region implies`() {
        assertEquals("Hans", assertNotNull(resolveLocale("zh-Hans-MO")).effectiveScriptCode)
    }

    @Test
    fun `a region with another script behaves as the tag naming that script`() {
        assertEquals(resolveLocale("pa-Arab-PK"), resolveLocale("pa-PK"))
        assertEquals(resolveLocale("ku-Arab-IQ"), resolveLocale("ku-IQ"))
    }

    @Test
    fun `a region using the language's default script is unaffected`() {
        assertEquals("Hans", assertNotNull(resolveLocale("zh-MY")).effectiveScriptCode)
        assertEquals("Latn", assertNotNull(resolveLocale("de-BE")).effectiveScriptCode)
    }
}
