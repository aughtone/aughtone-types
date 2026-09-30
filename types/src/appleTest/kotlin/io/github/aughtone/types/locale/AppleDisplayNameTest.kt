package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Apple's CLDR is not the JVM's, so what it covers has to be measured here rather than assumed
 * from the JVM tests. Measured on iosSimulatorArm64: Apple has Inuktitut where the JVM has none,
 * and has nothing for Tibetan, which the JVM also lacks.
 */
class AppleDisplayNameTest {

    private val french = Locale.getLocale("fr")!!

    @Test
    fun localizedDisplayNameOrNullReportsAMissNoPlatformCanFill() {
        // French in Tibetan is no longer a miss: Apple lacks it, another platform had it, and the
        // supplement carries it. Inuktitut in Tibetan no platform can name, so it stays English.
        val tibetan = Locale.getLocale("bo")!!
        val inuktitut = Locale.getLocale("iu")!!

        assertNull(inuktitut.localizedDisplayNameOrNull(displayIn = tibetan))
        assertEquals("Inuktitut", inuktitut.localizedDisplayName(displayIn = tibetan))
    }

    @Test
    fun theSupplementFillsAGapAppleItselfCannot() {
        val tibetan = Locale.getLocale("bo")!!
        val australianEnglish = Locale.getLocale("en-AU")!!

        // Apple has no Tibetan name for this locale; the supplement supplies one from elsewhere.
        // The assertion is that a name arrives and is not merely the English fallback repeated,
        // rather than a literal: CLDR wording moves between releases, and pinning the exact string
        // would make this a test of the data rather than of the mechanism.
        assertNull(localizedDisplayNameForNative(australianEnglish, tibetan))
        val supplied = australianEnglish.localizedDisplayNameOrNull(displayIn = tibetan)
        assertNotNull(supplied)
        assertNotEquals(australianEnglish.displayName, supplied)
    }

    @Test
    fun localizedDisplayNameOrNullReturnsTheNameWhereApplesCldrHasData() {
        val inuktitut = Locale.getLocale("iu")!!
        val self = Locale.getLocale("fr")!!

        // Apple carries Inuktitut; the JVM does not. Coverage is per-platform, not universal.
        assertEquals("ᐅᐃᒍᐃᕐᒥᐅᖅ", french.localizedDisplayNameOrNull(displayIn = inuktitut))
        assertEquals("français", french.localizedDisplayNameOrNull(displayIn = self))
    }

    @Test
    fun englishDisplayLanguageIsNotMistakenForAMiss() {
        // The miss is detected by comparing against the English rendering, so English itself must
        // be exempt or every English lookup would report no data.
        val english = Locale.getLocale("en")!!

        assertEquals("French", french.localizedDisplayNameOrNull(displayIn = english))
    }
}
