package io.github.aughtone.types.locale

import io.github.aughtone.types.financial.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

    @Test
    fun `localizedDisplayNameOrNull reports a miss that no platform and no table can fill`() {
        // French in Inuktitut is no longer a miss: the JVM lacks it, but another platform had it and
        // the supplement carries it. What remains unfillable is a pair no platform can name at all —
        // Inuktitut rendered in Tibetan — and there localizedDisplayName still answers in English.
        val tibetan = Locale.getLocale("bo")!!
        val inuktitut = Locale.getLocale("iu")!!

        assertNull(inuktitut.localizedDisplayNameOrNull(displayIn = tibetan))
        assertEquals("Inuktitut", inuktitut.localizedDisplayName(displayIn = tibetan))
    }

    @Test
    fun `the supplement fills a gap the jvm itself cannot`() {
        val inuktitut = Locale.getLocale("iu")!!
        val french = Locale.getLocale("fr")!!

        // The JVM has no Inuktitut data at all, so this can only come from the bundled supplement.
        assertNull(localizedDisplayNameForNative(french, inuktitut))
        assertEquals("ᐅᐃᒍᐃᕐᒥᐅᖅ", french.localizedDisplayNameOrNull(displayIn = inuktitut))
    }

    @Test
    fun `localizedDisplayNameOrNull returns the name where the platform has one`() {
        val french = Locale.getLocale("fr")!!
        val english = Locale.getLocale("en")!!

        assertEquals("French", french.localizedDisplayNameOrNull(displayIn = english))
    }

    @Test
    fun `currentOrNull agrees with current when the platform can resolve a locale`() {
        // Both read the same platform lookup, so on a host that has one they must not disagree.
        assertEquals(Locale.current, Locale.currentOrNull)
        assertEquals(Currency.current, Currency.currentOrNull)
    }
}
