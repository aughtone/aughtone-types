package io.github.aughtone.types.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The digital-information units follow a rule: a prefixed byte carries the unambiguous IEC symbol as
 * its primary and the decimal spelling as an alternative, and a prefixed bit carries the spelled-out
 * `bit` form as its primary and the short form as an alternative.
 *
 * Both `Giga` entries used to break it — `Gigabyte("GB")` and `Gigabit("Gb")`, each missing its primary
 * and its alternative — so `findAll("GiB")` and `findAll("Gbit")` were empty while every other prefix
 * in both families resolved. See issue #36.
 *
 * This is written as the rule rather than as the two entries, so a prefix added later is held to it.
 */
class DigitalUnitSymbolTest {

    private val bytes = mapOf(
        "Ki" to UnitOfMeasure.Kilobyte,
        "Mi" to UnitOfMeasure.Megabyte,
        "Gi" to UnitOfMeasure.Gigabyte,
        "Ti" to UnitOfMeasure.Terabyte,
    )

    private val bits = mapOf(
        "k" to UnitOfMeasure.Kilobit,
        "M" to UnitOfMeasure.Megabit,
        "G" to UnitOfMeasure.Gigabit,
        "T" to UnitOfMeasure.Terabit,
    )

    @Test
    fun `every prefixed byte has the IEC symbol as its primary`() {
        bytes.forEach { (iec, unit) ->
            assertEquals("${iec}B", unit.symbol, "$unit should lead with its IEC symbol")
        }
    }

    @Test
    fun `every prefixed byte keeps the decimal spelling as an alternative`() {
        // kB is lowercase by SI convention; the rest take the uppercase prefix.
        val decimal = mapOf(
            UnitOfMeasure.Kilobyte to "kB",
            UnitOfMeasure.Megabyte to "MB",
            UnitOfMeasure.Gigabyte to "GB",
            UnitOfMeasure.Terabyte to "TB",
        )
        decimal.forEach { (unit, sym) ->
            assertTrue(sym in unit.altSymbols, "$unit should accept $sym as an alternative")
        }
    }

    @Test
    fun `every prefixed bit has the spelled out bit form as its primary`() {
        bits.forEach { (prefix, unit) ->
            assertEquals("${prefix}bit", unit.symbol, "$unit should lead with its spelled-out symbol")
        }
    }

    @Test
    fun `every prefixed bit keeps the short form as an alternative`() {
        bits.forEach { (prefix, unit) ->
            assertTrue("${prefix}b" in unit.altSymbols, "$unit should accept ${prefix}b as an alternative")
        }
    }

    @Test
    fun `every symbol in both families resolves by lookup`() {
        val expected = bytes.keys.map { "${it}B" } + listOf("kB", "MB", "GB", "TB") +
            bits.keys.map { "${it}bit" } + bits.keys.map { "${it}b" }
        expected.forEach { sym ->
            assertTrue(
                UnitOfMeasure.findAll(sym).isNotEmpty(),
                "findAll(\"$sym\") is empty, so that symbol names nothing"
            )
        }
    }

    @Test
    fun `bit and byte symbols never collide`() {
        // Lookup is case-sensitive, which is the only reason GB and Gb can both mean something.
        val all = (bytes.values + bits.values).flatMap { listOf(it.symbol) + it.altSymbols }
        assertEquals(all.size, all.distinct().size, "a symbol is claimed by more than one unit: $all")
        bits.keys.forEach { prefix ->
            assertEquals(
                1,
                UnitOfMeasure.findAll("${prefix}b").size,
                "${prefix}b should name exactly one unit"
            )
        }
    }
}
