package io.github.aughtone.types.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetricPrefixTest {
    val historicValues = listOf(
        "Quetta",
        "Ronna",
        "Yotta",
        "Zetta",
        "Exa",
        "Peta",
        "Tera",
        "Giga",
        "Mega",
        "Kilo",
        "Hecto",
        "Deca",
        "Deci",
        "Centi",
        "Milli",
        "Micro",
        "Nano",
        "Pico",
        "Femto",
        "Atto",
        "Zepto",
        "Yocto",
        "Ronto",
        "Quecto"
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
        assertEquals("Q", MetricPrefix.Quetta.symbol)
        assertEquals("R", MetricPrefix.Ronna.symbol)
        assertEquals("Y", MetricPrefix.Yotta.symbol)
        assertEquals("Z", MetricPrefix.Zetta.symbol)
        assertEquals("E", MetricPrefix.Exa.symbol)
        assertEquals("P", MetricPrefix.Peta.symbol)
        assertEquals("T", MetricPrefix.Tera.symbol)
        assertEquals("G", MetricPrefix.Giga.symbol)
        assertEquals("M", MetricPrefix.Mega.symbol)
        assertEquals("k", MetricPrefix.Kilo.symbol)
        assertEquals("h", MetricPrefix.Hecto.symbol)
        assertEquals("da", MetricPrefix.Deca.symbol)
        assertEquals("d", MetricPrefix.Deci.symbol)
        assertEquals("c", MetricPrefix.Centi.symbol)
        assertEquals("m", MetricPrefix.Milli.symbol)
        assertEquals("μ", MetricPrefix.Micro.symbol)
        assertEquals("n", MetricPrefix.Nano.symbol)
        assertEquals("p", MetricPrefix.Pico.symbol)
        assertEquals("f", MetricPrefix.Femto.symbol)
        assertEquals("a", MetricPrefix.Atto.symbol)
        assertEquals("z", MetricPrefix.Zepto.symbol)
        assertEquals("y", MetricPrefix.Yocto.symbol)
        assertEquals("r", MetricPrefix.Ronto.symbol)
        assertEquals("q", MetricPrefix.Quecto.symbol)
    }

    @Test
    fun `verify exponents are correct`() {
        assertEquals(30, MetricPrefix.Quetta.exponent)
        assertEquals(27, MetricPrefix.Ronna.exponent)
        assertEquals(24, MetricPrefix.Yotta.exponent)
        assertEquals(21, MetricPrefix.Zetta.exponent)
        assertEquals(18, MetricPrefix.Exa.exponent)
        assertEquals(15, MetricPrefix.Peta.exponent)
        assertEquals(12, MetricPrefix.Tera.exponent)
        assertEquals(9, MetricPrefix.Giga.exponent)
        assertEquals(6, MetricPrefix.Mega.exponent)
        assertEquals(3, MetricPrefix.Kilo.exponent)
        assertEquals(2, MetricPrefix.Hecto.exponent)
        assertEquals(1, MetricPrefix.Deca.exponent)
        assertEquals(-1, MetricPrefix.Deci.exponent)
        assertEquals(-2, MetricPrefix.Centi.exponent)
        assertEquals(-3, MetricPrefix.Milli.exponent)
        assertEquals(-6, MetricPrefix.Micro.exponent)
        assertEquals(-9, MetricPrefix.Nano.exponent)
        assertEquals(-12, MetricPrefix.Pico.exponent)
        assertEquals(-15, MetricPrefix.Femto.exponent)
        assertEquals(-18, MetricPrefix.Atto.exponent)
        assertEquals(-21, MetricPrefix.Zepto.exponent)
        assertEquals(-24, MetricPrefix.Yocto.exponent)
        assertEquals(-27, MetricPrefix.Ronto.exponent)
        assertEquals(-30, MetricPrefix.Quecto.exponent)
    }
}
