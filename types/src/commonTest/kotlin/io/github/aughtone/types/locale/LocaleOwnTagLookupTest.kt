package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocaleOwnTagLookupTest {

    @Test
    fun `every shipped locale is found by its own language tag`() {
        val lost = localeResourceMap.values.filter { localeFor(it.languageTag) != it }.map { it.languageTag }
        assertTrue(lost.isEmpty(), "shipped locales not found by their own tag: $lost")
    }

    @Test
    fun `a script-bearing tag finds the locale keyed without its script`() {
        assertEquals(localeFor("sr-RS"), localeFor("sr-Latn-RS"))
        assertEquals(localeFor("zh-TW"), localeFor("zh-Hant-TW"))
        assertEquals(localeFor("am"), localeFor("am-Ethi"))
    }

    @Test
    fun `resolving a script-bearing tag keeps its region`() {
        assertEquals("sr-Latn-RS", resolveLocale("sr-Latn-RS")?.languageTag)
        assertEquals("zh-Hant-TW", resolveLocale("zh-Hant-TW")?.languageTag)
        assertEquals("uz-Latn-UZ", resolveLocale("uz-Latn-UZ")?.languageTag)
    }

    @Test
    fun `a script the shipped locale does not carry is not matched`() {
        // zh-HK ships as Traditional, so Simplified Hong Kong Chinese is not it.
        assertNull(localeFor("zh-Hans-HK"))
        assertNull(localeFor("sr-Cyrl-RS"))
    }

    @Test
    fun `parsing a shipped locale's own tag returns the shipped locale`() {
        val shipped = localeFor("zh-TW")!!
        assertEquals(shipped, parseLocale(shipped.languageTag))
    }
}
