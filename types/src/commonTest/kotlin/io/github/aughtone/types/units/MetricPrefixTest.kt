package io.github.aughtone.types.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetricPrefixTest {
    val historicValues = listOf(
        "QUETTA",
        "RONNA",
        "YOTTA",
        "ZETTA",
        "EXA",
        "PETA",
        "TERA",
        "GIGA",
        "MEGA",
        "KILO",
        "HECTO",
        "DECA",
        "DECI",
        "CENTI",
        "MILLI",
        "MICRO",
        "NANO",
        "PICO",
        "FEMTO",
        "ATTO",
        "ZEPTO",
        "YOCTO",
        "RONTO",
        "QUECTO"
    )

    @Test
    fun `retains historic enum name`() {
        // All the enum values that may be used in the
        // database or on remote systems must be retained.
        assertTrue(
            MetricPrefix.entries.map { it.name }
                .containsAll(historicValues),
            "Missing historic names for the enum."
        )
    }

    @Test
    fun `doesnt have unchecked new values`() {
        // Adding a new value without ensuring it doesn't change
        // may introduce a bug, if it is later removed or renamed.
        assertTrue(
            historicValues.containsAll(MetricPrefix.entries.map { it.name }),
            "There are new unchecked values for the enum."
        )
    }

    @Test
    fun `verify symbols are correct`() {
        assertEquals("Q", MetricPrefix.QUETTA.symbol)
        assertEquals("R", MetricPrefix.RONNA.symbol)
        assertEquals("Y", MetricPrefix.YOTTA.symbol)
        assertEquals("Z", MetricPrefix.ZETTA.symbol)
        assertEquals("E", MetricPrefix.EXA.symbol)
        assertEquals("P", MetricPrefix.PETA.symbol)
        assertEquals("T", MetricPrefix.TERA.symbol)
        assertEquals("G", MetricPrefix.GIGA.symbol)
        assertEquals("M", MetricPrefix.MEGA.symbol)
        assertEquals("k", MetricPrefix.KILO.symbol)
        assertEquals("h", MetricPrefix.HECTO.symbol)
        assertEquals("da", MetricPrefix.DECA.symbol)
        assertEquals("d", MetricPrefix.DECI.symbol)
        assertEquals("c", MetricPrefix.CENTI.symbol)
        assertEquals("m", MetricPrefix.MILLI.symbol)
        assertEquals("μ", MetricPrefix.MICRO.symbol)
        assertEquals("n", MetricPrefix.NANO.symbol)
        assertEquals("p", MetricPrefix.PICO.symbol)
        assertEquals("f", MetricPrefix.FEMTO.symbol)
        assertEquals("a", MetricPrefix.ATTO.symbol)
        assertEquals("z", MetricPrefix.ZEPTO.symbol)
        assertEquals("y", MetricPrefix.YOCTO.symbol)
        assertEquals("r", MetricPrefix.RONTO.symbol)
        assertEquals("q", MetricPrefix.QUECTO.symbol)
    }
}
