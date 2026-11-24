package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScriptFallbackTest {

    @Test
    fun `every shipped locale has a known script`() {
        val unknown = localeResourceMap.values.filter { it.effectiveScriptCode == null }.map { it.languageTag }
        assertTrue(unknown.isEmpty(), "shipped locales with no script and no default script: $unknown")
    }

    @Test
    fun `a locale without a script is written in its language's default`() {
        assertEquals("Cans", localeFor("iu-CA")?.effectiveScriptCode)
        assertEquals("Latn", localeFor("fr-FR")?.effectiveScriptCode)
        assertEquals("Hant", localeFor("zh-TW")?.effectiveScriptCode)
    }

    @Test
    fun `a tag naming the default script finds the locale stored without one`() {
        assertEquals(localeFor("fr-FR"), localeFor("fr-Latn-FR"))
        assertEquals(localeFor("en-US"), localeFor("en-Latn-US"))
        assertEquals(localeFor("iu-CA"), localeFor("iu-Cans-CA"))
    }

    @Test
    fun `a tag naming another script does not find the locale`() {
        assertNull(localeFor("iu-Latn-CA"))
        assertNull(localeFor("iu-Latn"))
        assertNull(localeFor("ru-Latn-RU"))
    }

    @Test
    fun `resolving falls back within the requested script`() {
        assertEquals(localeFor("sr"), resolveLocale("sr-Latn-ME"))
        assertEquals(localeFor("fr"), resolveLocale("fr-Latn-XK"))
        assertEquals(localeFor("zh-Hans"), resolveLocale("zh-Hans-MO"))
        assertEquals(localeFor("zh-Hant"), resolveLocale("zh-Hant-MO"))
    }

    @Test
    fun `Traditional Chinese outside Taiwan and Hong Kong resolves on every target`() {
        // Traditional used to ship only with a region, so zh-Hant and zh-Hant-MO found nothing written
        // in it. Borrowing zh-TW would carry Taiwan's region, and with it the wrong currency.
        val hant = localeFor("zh-Hant")
        assertEquals("Hant", hant?.scriptCode)
        assertNull(hant?.regionCode)
        assertEquals(hant, resolveLocale("zh-Hant"))
        assertEquals(hant, resolveLocale("zh-Hant-SG"))
        assertEquals(localeFor("zh-TW"), resolveLocale("zh-Hant-TW"))
    }

    @Test
    fun `resolving never answers in a different script`() {
        // Each of these used to step down to the bare language, written in another script. What the
        // platform lookup returns instead differs by target, but it is never the shipped locale.
        val wrong = listOf("iu-Latn-CA", "sr-Cyrl-RS", "uz-Cyrl-UZ", "zh-Hant-SG", "pa-Arab-PK").mapNotNull { tag ->
            val resolved = resolveLocale(tag) ?: return@mapNotNull null
            "$tag -> ${resolved.languageTag}".takeIf { resolved.effectiveScriptCode != tag.split('-')[1] }
        }
        assertTrue(wrong.isEmpty(), "resolved into another script: $wrong")
    }

    @Test
    fun `a locale kept in another script is not named like the default script`() {
        // Keeping the script in the tag is not enough: a picker shows the name, and both would read
        // "Inuktitut (Canada)". Targets with no platform lookup return null and have nothing to name.
        val same = listOf("iu-Latn-CA" to "iu-CA", "ru-Latn-RU" to "ru-RU").mapNotNull { (tag, default) ->
            val resolved = resolveLocale(tag) ?: return@mapNotNull null
            "$tag -> ${resolved.displayName}".takeIf { resolved.displayName == localeForNative(default)?.displayName }
        }
        assertTrue(same.isEmpty(), "named without the requested script: $same")
    }
}
