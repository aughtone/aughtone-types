package io.github.aughtone.types.units

/**
 * Represents the standard set of metric prefixes used in the International System of Units (SI).
 *
 * Each prefix corresponds to a specific power of 10, ranging from quetta (10^30) to quecto (10^-30).
 * This enum provides a type-safe way to work with these prefixes and their standard symbols.
 *
 * @property symbol The official symbol for the metric prefix (e.g., "k" for kilo, "μ" for micro).
 * @property exponent The power of 10 associated with the prefix (e.g., 3 for kilo, -3 for milli).
 * @see <a href="https://en.wikipedia.org/wiki/Metric_prefix">Metric prefix on Wikipedia</a>
 */
enum class MetricPrefix(val symbol: String, val exponent: Int) {
    Quetta("Q", 30),
    Ronna("R", 27),
    Yotta("Y", 24),
    Zetta("Z", 21),
    Exa("E", 18),
    Peta("P", 15),
    Tera("T", 12),
    Giga("G", 9),
    Mega("M", 6),
    Kilo("k", 3),
    Hecto("h", 2),
    Deca("da", 1),
    Deci("d", -1),
    Centi("c", -2),
    Milli("m", -3),
    Micro("μ", -6),
    Nano("n", -9),
    Pico("p", -12),
    Femto("f", -15),
    Atto("a", -18),
    Zepto("z", -21),
    Yocto("y", -24),
    Ronto("r", -27),
    Quecto("q", -30),
}
