package io.github.aughtone.types.number

/**
 * Specifies a rounding behavior for numerical operations which can discard precision.
 */
enum class RoundingMode {
    /**
     * Rounding mode to round away from zero.
     */
    UP,

    /**
     * Rounding mode to round towards zero.
     */
    DOWN,

    /**
     * Rounding mode to round towards positive infinity.
     */
    CEILING,

    /**
     * Rounding mode to round towards negative infinity.
     */
    FLOOR,

    /**
     * Rounding mode to round towards "nearest neighbor" unless both neighbors are equidistant,
     * in which case round up.
     */
    HALF_UP,

    /**
     * Rounding mode to round towards "nearest neighbor" unless both neighbors are equidistant,
     * in which case round down.
     */
    HALF_DOWN,

    /**
     * Rounding mode to round towards the "nearest neighbor" unless both neighbors are equidistant,
     * in which case, round towards the even neighbor.
     */
    HALF_EVEN,

    /**
     * Rounding mode to assert that the requested operation has an exact result, hence no rounding is necessary.
     */
    UNNECESSARY
}
