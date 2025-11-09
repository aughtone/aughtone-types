package io.github.aughtone.types.util


/**
 * Creates an empty [BitSet].
 */
fun bitSet(): BitSet = BitSet()

/**
 * Creates an empty [BitSet].
 */
fun emptyBitSet(): BitSet = bitSet()

/**
 * Creates a [BitSet] with the specified [size].
 *
 * @param size the size of the bit set.
 * @return a new [BitSet] instance.
 * @throws IllegalArgumentException if [size] is negative.
 */
fun bitSet(size: Int): BitSet = BitSet(size = size)
/**
 * Creates a [BitSet] with the specified [size] and initializes each bit with the result of the given [initializer] function.
 *
 * @param size The number of bits in the [BitSet].
 * @param initializer A function that takes the index of a bit and returns `true` if the bit should be set, `false` otherwise.
 * @return A new [BitSet] instance.
 * @throws IllegalArgumentException if [size] is negative.
 */
fun bitSet(size: Int, initializer: (Int) -> Boolean): BitSet =
    BitSet(size = size, initializer = initializer)

/**
 * Creates a [BitSet] of the specified [size] with all bits set to [all].
 *
 * @param size The size of the [BitSet].
 * @param all The value to which all bits should be initialized.
 * @return A new [BitSet] with all bits initialized to [all].
 */
fun bitSet(size: Int, all: Boolean): BitSet = BitSet(size = size, initializer = { all })
