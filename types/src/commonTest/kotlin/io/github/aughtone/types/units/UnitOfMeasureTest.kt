package io.github.aughtone.types.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnitOfMeasureTest {
    val historicValues = listOf(
        "ACRE",
        "ACRE_FOOT",
        "AMPERE",
        "ARC_MINUTE",
        "ARC_SECOND",
        "ASTRONOMICAL_UNIT",
        "BIT",
        "BUSHEL",
        "BYTE",
        "CALORIE",
        "CARAT",
        "CELSIUS",
        "CENTILITER",
        "CENTIMETER",
        "CUBIC_CENTIMETER",
        "CUBIC_FOOT",
        "CUBIC_INCH",
        "CUBIC_KILOMETER",
        "CUBIC_METER",
        "CUBIC_MILE",
        "CUBIC_YARD",
        "DECAMETER",
        "CUP",
        "DECILITER",
        "DECIMETER",
        "DEGREE",
        "FAHRENHEIT",
        "FATHOM",
        "FLUID_OUNCE",
        "FOODCALORIE",
        "FOOT",
        "FURLONG",
        "GALLON",
        "GALLON_IMPERIAL",
        "GIGABIT",
        "GIGABYTE",
        "GIGAHERTZ",
        "GIGAWATT",
        "GRAM",
        "G_FORCE",
        "HECTARE",
        "HECTOLITER",
        "HECTOPASCAL",
        "HERTZ",
        "HORSEPOWER",
        "INCH",
        "INCH_HG",
        "JOULE",
        "KARAT",
        "KELVIN",
        "KILOBIT",
        "KILOBYTE",
        "KILOCALORIE",
        "KILOGRAM",
        "KILOHERTZ",
        "KILOJOULE",
        "KILOMETER",
        "KILOMETER_PER_HOUR",
        "KILOWATT",
        "KILOWATT_HOUR",
        "LIGHT_YEAR",
        "LITER",
        "LITER_PER_KILOMETER",
        "LITER_PER_100KILOMETERS",
        "LUX",
        "MEGABIT",
        "MEGABYTE",
        "MEGAHERTZ",
        "MEGALITER",
        "MEGAWATT",
        "METER",
        "METER_PER_SECOND",
        "METER_PER_SECOND_SQUARED",
        "METRIC_TON",
        "MICROGRAM",
        "MICROMETER",
        "MICROSECOND",
        "MILE",
        "MILE_PER_GALLON",
        "MILE_PER_HOUR",
        "MILLIAMPERE",
        "MILLIBAR",
        "MILLIGRAM",
        "MILLILITER",
        "MILLIMETER",
        "MILLIMETER_OF_MERCURY",
        "MILLISECOND",
        "MILLIWATT",
        "NANOMETER",
        "NANOSECOND",
        "NAUTICAL_MILE",
        "OHM",
        "OUNCE",
        "OUNCE_TROY",
        "PARSEC",
        "PICOMETER",
        "PINT",
        "POUND",
        "POUND_PER_SQUARE_INCH",
        "QUART",
        "RADIAN",
        "SQUARE_CENTIMETER",
        "SQUARE_FOOT",
        "SQUARE_INCH",
        "SQUARE_KILOMETER",
        "SQUARE_METER",
        "SQUARE_MILE",
        "SQUARE_YARD",
        "STONE",
        "TABLESPOON",
        "TEASPOON",
        "TERABIT",
        "TERABYTE",
        "TON",
        "VOLT",
        "WATT",
        "YARD",
        "DAY",
        "HOUR",
        "MINUTE",
        "SECOND",
        "MONTH",
        "WEEK",
        "YEAR",
        "YEAR_JULIAN"
    )

    @Test
    fun `retains historic enum name`() {
        // All the enum values that may be used in the
        // database or on remote systems must be retained.
        assertTrue(
            UnitOfMeasure.entries.map { it.name }
                .containsAll(historicValues),
            "Missing historic names for the enum."
        )
    }

    @Test
    fun `doesnt have unchecked new values`() {
        // Adding a new value without ensuring it doesn't change
        // may introduce a bug, if it is later removed or renamed.
        assertTrue(
            historicValues.containsAll(UnitOfMeasure.entries.map { it.name }),
            "There are new unchecked values for the enum."
        )
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
