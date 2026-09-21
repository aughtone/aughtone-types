package io.github.aughtone.types.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Symbol lookup must either give an unambiguous answer or refuse. A US gallon standing in for an
 * imperial one is a 20 percent error, and it used to happen silently because declaration order
 * decided the winner.
 */
class UnitOfMeasureSymbolTest {

    @Test
    fun `qualified gallon symbols resolve unambiguously`() {
        assertEquals(UnitOfMeasure.Gallon, UnitOfMeasure.findFirst("US gal"))
        assertEquals(UnitOfMeasure.GallonImperial, UnitOfMeasure.findFirst("imp gal"))
        assertEquals(UnitOfMeasure.GallonImperial, UnitOfMeasure.findFirst("UK gal"))
    }

    @Test
    fun `a bare gallon symbol is ambiguous and is refused`() {
        assertNull(UnitOfMeasure.findFirst("gal"))
    }

    @Test
    fun `findAll exposes both gallons so a caller can choose`() {
        val all = UnitOfMeasure.findAll("gal")
        assertEquals(2, all.size)
        assertTrue(UnitOfMeasure.Gallon in all)
        assertTrue(UnitOfMeasure.GallonImperial in all)
    }

    @Test
    fun `an unknown symbol and an ambiguous one are distinguishable`() {
        assertNull(UnitOfMeasure.findFirst("zzz"))
        assertTrue(UnitOfMeasure.findAll("zzz").isEmpty())

        assertNull(UnitOfMeasure.findFirst("gal"))
        assertTrue(UnitOfMeasure.findAll("gal").size > 1)
    }

    @Test
    fun `calorie symbols follow the standard casing`() {
        assertEquals(UnitOfMeasure.Calorie, UnitOfMeasure.findFirst("cal"))
        assertEquals(UnitOfMeasure.Kilocalorie, UnitOfMeasure.findFirst("kcal"))
        assertEquals(UnitOfMeasure.Kilocalorie, UnitOfMeasure.findFirst("kCal"))
        assertEquals("cal", UnitOfMeasure.Calorie.symbol)
        assertEquals("kcal", UnitOfMeasure.Kilocalorie.symbol)
    }

    @Test
    fun `the year symbol resolves and the julian qualifier is separate`() {
        assertEquals(UnitOfMeasure.Year, UnitOfMeasure.findFirst("a"))
        assertEquals(UnitOfMeasure.YearJulian, UnitOfMeasure.findFirst("aj"))
    }
}
