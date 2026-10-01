package io.github.aughtone.types.locale

import kotlin.math.abs
import kotlin.test.assertTrue

/**
 * Asserts that a measured platform-coverage count stays near the baseline it was recorded at.
 *
 * Platform CLDR is host data, not library code: the same build reports slightly different counts on a
 * different OS, simulator runtime or JDK. An exact pin therefore fails on every machine except the one
 * that measured it — CI's iOS simulator reported 1,205 Apple gaps against a baseline of 1,199 taken on
 * a developer machine, and failed a release with nothing wrong in the library.
 *
 * The tolerance absorbs that drift and still fails on what these tests exist to catch: a platform
 * bridge that stops working, which sends the count toward the whole matrix, or a move large enough
 * that the supplement should be regenerated. The actual value is printed on every run, so drift shows
 * in the logs before it trips.
 *
 * @param what What is being counted, for the message.
 * @param baseline The count recorded when the supplement was last generated.
 * @param actual The count measured on this host.
 * @param tolerancePercent How far from [baseline], as a percentage of it, still counts as the same.
 */
internal fun assertCoverageNear(what: String, baseline: Int, actual: Int, tolerancePercent: Int = 5) {
    val tolerance = baseline * tolerancePercent / 100
    println("COVERAGE $what: $actual (baseline $baseline, tolerance ±$tolerance)")
    assertTrue(
        abs(actual - baseline) <= tolerance,
        "$what moved beyond tolerance: baseline $baseline ±$tolerance, actual $actual. A move this " +
            "large means either a platform bridge broke or the platform's CLDR changed enough to " +
            "regenerate the supplement. Look at what moved before updating the baseline."
    )
}
