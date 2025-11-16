package io.github.aughtone.types.units

/**
 * Represents the standard set of metric prefixes used in the International System of Units (SI).
 *
 * Each prefix corresponds to a specific power of 10, ranging from quetta (10^30) to quecto (10^-30).
 * This enum provides a type-safe way to work with these prefixes and their standard symbols.
 *
 * @property symbol The official symbol for the metric prefix (e.g., "k" for kilo, "μ" for micro).
 * @see <a href="https://en.wikipedia.org/wiki/Metric_prefix">Metric prefix on Wikipedia</a>
 */
enum class MetricPrefix(val symbol: String) {
    QUETTA("Q",),
    RONNA("R",),
    YOTTA("Y",),
    ZETTA("Z",),
    EXA("E",),
    PETA("P",),
    TERA("T",),
    GIGA("G",),
    MEGA("M",),
    KILO("k",),
    HECTO("h",),
    DECA("da",),
    DECI("d",),
    CENTI("c",),
    MILLI("m",),
    MICRO("μ",),
    NANO("n",),
    PICO("p",),
    FEMTO("f",),
    ATTO("a",),
    ZEPTO("z",),
    YOCTO("y",),
    RONTO("r",),
    QUECTO("q",),
}
