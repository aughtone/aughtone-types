package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins how much of the display-name matrix this platform produces on its own.
 *
 * Platform CLDR moves with operating system and toolchain releases, and it moves silently: a
 * platform that quietly loses a language renders English instead, which looks like a working
 * library. This turns that into a failure.
 *
 * A change here is not automatically a defect — it means the platform's data moved. The response is
 * to regenerate the supplement and update the number, deliberately, having looked at what changed.
 *
 * Measured against JDK 26 CLDR, 2026-09-28.
 */
class JvmCoverageTest {

    private val locales = localeResourceMap.values.distinctBy { it.languageTag }
    private val displayLanguages = locales.map { it.languageCode }.distinct()

    @Test
    fun platformCoverageHasNotMoved() {
        var gaps = 0
        for (tag in displayLanguages) {
            val displayIn = Locale.getLocale(tag) ?: continue
            gaps += locales.count { localizedDisplayNameForNative(it, displayIn) == null }
        }
        assertEquals(
            1084,
            gaps,
            "This platform's own CLDR coverage has changed. Regenerate the supplement and update " +
                "this number once you have looked at what moved."
        )
    }

    @Test
    fun theSupplementClosesEveryGapItCan() {
        // What the supplement cannot close is what has no genuine translation anywhere: either no
        // platform can name the pair at all, or the ones that can return the English word, which the
        // supplement deliberately does not carry. Both render in English — true, untranslated — and
        // both report null here rather than pretending a translation exists.
        val unfilled = mutableListOf<String>()
        for (tag in displayLanguages) {
            val displayIn = Locale.getLocale(tag) ?: continue
            for (target in locales) {
                if (target.localizedDisplayNameOrNull(displayIn = displayIn) == null) {
                    unfilled.add(tag + "/" + target.languageTag)
                }
            }
        }
        assertEquals(
            823,
            unfilled.size,
            "Pairs no platform can name changed. They fall back to English; the set was " +
                "Inuktitut and Tibetan named in the thinnest display languages. Got: " +
                unfilled.sorted().take(12)
        )
    }
}
