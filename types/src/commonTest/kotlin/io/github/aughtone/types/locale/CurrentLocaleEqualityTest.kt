package io.github.aughtone.types.locale

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `Locale.current` is the locale `localeFor` returns for the same tag, so the two compare equal and
 * land in the same bucket of a map or set. (#48)
 */
class CurrentLocaleEqualityTest {

    @Test
    fun `the current locale equals the shipped locale for its tag`() {
        val current = Locale.currentOrNull ?: return
        // A device set to a locale this library does not ship has nothing to compare against.
        val shipped = localeFor(current.languageTag) ?: return
        assertEquals(shipped, current)
        assertEquals(shipped.hashCode(), current.hashCode())
    }
}
