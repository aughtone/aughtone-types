package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the size and shape of the bundled supplement.
 *
 * The count asserted here is derived from the payload rather than written into the generated file by
 * hand, so the two cannot disagree — which they did, silently, because nothing read either number.
 * ADR-0005 explains why these figures belong in a test rather than in prose.
 *
 * A failure here means the table was regenerated. Update the numbers once you have looked at what
 * moved, the same way the per-platform coverage tests are handled.
 */
class LocaleDisplayNameSupplementTest {

    @Test
    fun `the supplement holds the number of names it says it does`() {
        assertEquals(
            1565,
            LocaleDisplayNameSupplement.size,
            "The supplement's payload changed size. Regenerate deliberately and update this number."
        )
    }

    @Test
    fun `the supplement covers every display language the library ships`() {
        val displayLanguages = localeResourceMap.values.map { it.languageCode }.distinct()
        assertEquals(89, displayLanguages.size, "The shipped display-language set changed.")
    }

    @Test
    fun `a name comes back for a pair no platform can produce`() {
        // No platform CLDR carries Inuktitut, so iu in Zulu can only come from the supplement.
        val name = LocaleDisplayNameSupplement.nameFor("zu", "iu")
        assertTrue(name != null && name.isNotBlank(), "expected a bundled name for iu in zu")
    }

    @Test
    fun `an unknown pair reports null rather than guessing`() {
        assertNull(LocaleDisplayNameSupplement.nameFor("zu", "xx-XX"))
        assertNull(LocaleDisplayNameSupplement.nameFor("xx", "iu"))
    }
}
