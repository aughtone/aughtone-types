package io.github.aughtone.types.locale

import kotlin.test.Test

/**
 * Watches how much of the display-name matrix this platform produces on its own.
 *
 * Platform CLDR moves with operating system and toolchain releases, and it moves silently: a
 * platform that quietly loses a language renders English instead, which looks like a working
 * library. This turns that into a failure.
 *
 * The counts are host data, so they drift a little between OS, simulator and JDK releases; the
 * assertion tolerates that and fails on a broken bridge or a move large enough to regenerate the
 * supplement for. See [assertCoverageNear]. When it does fail, regenerate and update the baseline
 * deliberately, having looked at what changed.
 *
 * Measured against JDK 26 CLDR, 2026-09-28.
 */
class JvmCoverageTest {

    private val locales = localeResourceMap.values.distinctBy { it.languageTag }
    private val displayLanguages = locales.map { it.languageCode }.distinct()

    @Test
    fun platformCoverageStaysNearItsBaseline() {
        var gaps = 0
        for (tag in displayLanguages) {
            val displayIn = Locale.getLocale(tag) ?: continue
            gaps += locales.count { localizedDisplayNameForNative(it, displayIn) == null }
        }
        assertCoverageNear("JVM platform gaps", baseline = 1084, actual = gaps)
    }

    @Test
    fun untranslatedPairsStayNearTheirBaseline() {
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
        println("UNTRANSLATED sample: " + unfilled.sorted().take(12))
        assertCoverageNear("JVM untranslated after the supplement", baseline = 823, actual = unfilled.size)
    }
}
