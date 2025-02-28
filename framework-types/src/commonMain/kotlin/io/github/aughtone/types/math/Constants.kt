package io.github.aughtone.types.math


/**
 * Sentinel value for [.intCompact] indicating the
 * significand information is only available from `intVal`.
 */
// XXX This is already in BigDecimal
//const val INFLATED = Long.MIN_VALUE
/**
 * This mask is used to obtain the value of an int as if it were unsigned.
 */
const val LONG_MASK = 0xffffffffL

/**
 * This constant limits `mag.length` of BigIntegers to the supported
 * range.
 */
internal val MAX_MAG_LENGTH: Int = Int.MAX_VALUE / Int.SIZE_BITS + 1 //java.lang.Integer.SIZE + 1 // (1 << 26)

/**
 * Bit lengths larger than this constant can cause overflow in searchLen
 * calculation and in BitSieve.singleSearch method.
 */
internal const val PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000

/**
 * The threshold value for using Karatsuba multiplication.  If the number
 * of ints in both mag arrays are greater than this number, then
 * Karatsuba multiplication will be used.   This value is found
 * experimentally to work well.
 */
internal const val KARATSUBA_THRESHOLD = 80

/**
 * The threshold value for using 3-way Toom-Cook multiplication.
 * If the number of ints in each mag array is greater than the
 * Karatsuba threshold, and the number of ints in at least one of
 * the mag arrays is greater than this threshold, then Toom-Cook
 * multiplication will be used.
 */
internal const val TOOM_COOK_THRESHOLD = 240

/**
 * The threshold value for using Karatsuba squaring.  If the number
 * of ints in the number are larger than this value,
 * Karatsuba squaring will be used.   This value is found
 * experimentally to work well.
 */
internal const val KARATSUBA_SQUARE_THRESHOLD = 128

/**
 * The threshold value for using Toom-Cook squaring.  If the number
 * of ints in the number are larger than this value,
 * Toom-Cook squaring will be used.   This value is found
 * experimentally to work well.
 */
internal const val TOOM_COOK_SQUARE_THRESHOLD = 216

/**
 * The threshold value for using Burnikel-Ziegler division.  If the number
 * of ints in the divisor are larger than this value, Burnikel-Ziegler
 * division may be used.  This value is found experimentally to work well.
 */
const val BURNIKEL_ZIEGLER_THRESHOLD = 80

/**
 * The offset value for using Burnikel-Ziegler division.  If the number
 * of ints in the divisor exceeds the Burnikel-Ziegler threshold, and the
 * number of ints in the dividend is greater than the number of ints in the
 * divisor plus this value, Burnikel-Ziegler division will be used.  This
 * value is found experimentally to work well.
 */
const val BURNIKEL_ZIEGLER_OFFSET = 40

/**
 * The threshold value for using Schoenhage recursive base conversion. If
 * the number of ints in the number are larger than this value,
 * the Schoenhage algorithm will be used.  In practice, it appears that the
 * Schoenhage routine is faster for any threshold down to 2, and is
 * relatively flat for thresholds between 2-25, so this choice may be
 * varied within this range for very small effect.
 */
internal const val SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20

/**
 * The threshold value for using squaring code to perform multiplication
 * of a `BigInteger` instance by itself.  If the number of ints in
 * the number are larger than this value, `multiply(this)` will
 * return `square()`.
 */
internal const val MULTIPLY_SQUARE_THRESHOLD = 20

/**
 * The threshold for using an intrinsic version of
 * implMontgomeryXXX to perform Montgomery multiplication.  If the
 * number of ints in the number is more than this value we do not
 * use the intrinsic.
 */
internal const val MONTGOMERY_INTRINSIC_THRESHOLD = 512
