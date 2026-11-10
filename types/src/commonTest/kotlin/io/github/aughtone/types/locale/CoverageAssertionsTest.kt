package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertFails

/** Holds [assertCoverageNear] to its two jobs: tolerate host drift, and still fail on a broken bridge. */
class CoverageAssertionsTest {

    @Test
    fun `drift seen between developer and CI hosts passes`() {
        // The real case that failed a release: CI's iOS simulator against a developer baseline.
        assertCoverageNear("gaps", baseline = 1199, actual = 1205)
        assertCoverageNear("untranslated", baseline = 910, actual = 912)
    }

    @Test
    fun `a bridge that stops working fails`() {
        // The JS defect shipped in 3.4.0 reported every one of 17,889 pairs as a gap.
        assertFails { assertCoverageNear("gaps", baseline = 1199, actual = 17889) }
    }

    @Test
    fun `a bridge that suddenly names everything fails too`() {
        assertFails { assertCoverageNear("gaps", baseline = 1199, actual = 0) }
    }

    @Test
    fun `a move just past the tolerance fails`() {
        assertFails { assertCoverageNear("gaps", baseline = 1000, actual = 1051) }
    }
}
