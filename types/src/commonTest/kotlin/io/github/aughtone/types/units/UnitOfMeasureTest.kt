package io.github.aughtone.types.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnitOfMeasureTest {

    @Test
    fun `ensure enum entry count is stable`() {
        // This test helps detect if entries are accidentally added or removed.
        // If you are intentionally changing the number of entries, update this value.
        // Its important that entry names do not change, so that serialized versions of them
        // dont cause an exception. If something must be changed, deprecate and ass a new one.
        assertEquals(125, UnitOfMeasure.entries.size)
    }

    @Test
    fun `findFirst handles primary symbol`() {
        assertEquals(UnitOfMeasure.KILOGRAM, UnitOfMeasure.findFirst("kg"))
    }

    @Test
    fun `findFirst handles alt symbol`() {
        assertEquals(UnitOfMeasure.GRAM, UnitOfMeasure.findFirst("gm"))
    }

    @Test
    fun `findFirst returns null for missing symbol`() {
        assertNull(UnitOfMeasure.findFirst("bogus"))
    }

    @Test
    fun `findFirst handles null input`() {
        assertNull(UnitOfMeasure.findFirst(null))
    }

    @Test
    fun `findFirst prioritizes primary symbol over alt symbol`() {
        // "a" is the primary symbol for YEAR and an alt symbol for YEAR_JULIAN.
        // findFirst should return YEAR.
        assertEquals(UnitOfMeasure.YEAR, UnitOfMeasure.findFirst("a"))
    }

    @Test
    fun `findAll returns all matches with correct priority`() {
        // "a" is the primary symbol for YEAR and an alt symbol for YEAR_JULIAN.
        // The result should contain both, with YEAR appearing first.
        val expected = listOf(UnitOfMeasure.YEAR, UnitOfMeasure.YEAR_JULIAN)
        assertEquals(expected, UnitOfMeasure.findAll("a"))
    }

    @Test
    fun `findAll returns multiple alt matches`() {
        // "gal" is the primary symbol for GALLON and an alt for GALLON_IMPERIAL
        val expected = listOf(UnitOfMeasure.GALLON, UnitOfMeasure.GALLON_IMPERIAL)
        assertEquals(expected, UnitOfMeasure.findAll("gal"))
    }

    @Test
    fun `findAll returns empty list for missing symbol`() {
        assertEquals(emptyList(), UnitOfMeasure.findAll("bogus"))
    }
}
