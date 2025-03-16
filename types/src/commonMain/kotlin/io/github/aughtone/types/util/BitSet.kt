package io.github.aughtone.types.util

import kotlin.collections.get

class BitSet(private val size: Int = 1) {
    private val data: LongArray
    private val vectorSize: Int

    init {
        require(size >= 0) { "Size must be non-negative" }
        vectorSize = size
        val arraySize = (size + Long.SIZE_BITS - 1) / Long.SIZE_BITS
        data = LongArray(arraySize)
    }
    /**
     * Constructor that receives an initializer.
     * @param size Size of the BitVector
     * @param initializer initializer function
     */
    constructor(size: Int, initializer: (Int) -> Boolean) : this(size) {
        for (i in 0 until size) {
            set(i, initializer(i))
        }
    }

    /**
     * Sets the bit at the specified index to true.
     *
     * @param index The index of the bit to set.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    fun set(index: Int) {
        checkIndex(index)
        val arrayIndex = index / Long.SIZE_BITS
        val bitIndex = index % Long.SIZE_BITS
        data[arrayIndex] = data[arrayIndex] or (1L shl bitIndex)
    }

    /**
     * Sets the bit at the specified index to the given value.
     *
     * @param index The index of the bit to set.
     * @param value The value to set the bit to (true for 1, false for 0).
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    fun set(index: Int, value: Boolean) {
        checkIndex(index)
        if (value) {
            set(index)
        } else {
            clear(index)
        }
    }

    /**
     * Clears the bit at the specified index (sets it to false).
     *
     * @param index The index of the bit to clear.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    fun clear(index: Int) {
        checkIndex(index)
        val arrayIndex = index / Long.SIZE_BITS
        val bitIndex = index % Long.SIZE_BITS
        data[arrayIndex] = data[arrayIndex] and (1L shl bitIndex).inv()
    }

    /**
     * Checks if the bit at the specified index is set (true).
     *
     * @param index The index of the bit to check.
     * @return true if the bit is set, false otherwise.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    operator fun get(index: Int): Boolean {
        checkIndex(index)
        val arrayIndex = index / Long.SIZE_BITS
        val bitIndex = index % Long.SIZE_BITS
        return (data[arrayIndex] shr bitIndex) and 1L != 0L
    }

    /**
     * Gets the size (number of bits) of this BitVector.
     *
     * @return The size of the BitVector.
     */
    fun size(): Int {
        return vectorSize
    }

    /**
     * Check if a given index is valid
     *
     * @param index The index to check
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    private fun checkIndex(index: Int) {
        if (index < 0 || index >= vectorSize) {
            throw IndexOutOfBoundsException("Index: $index, Size: $vectorSize")
        }
    }

    /**
     * Checks if a given index is within the valid range of the bit vector.
     *
     * @param index The index to check.
     * @return `true` if the index is within bounds (i.e., not negative and less than the vector size), `false` otherwise.
     */
    fun hasIndex(index: Int) = !(index < 0 || index >= vectorSize)

    /**
     * Returns `true` if all bits are set to `true`, `false` otherwise.
     *
     * @return `true` if all bits are set to `true`, `false` otherwise.
     */
    fun all(): Boolean = data.all { it > 0 }

    /**
     * Returns `true` if all bits are set to `false`, `false` otherwise.
     *
     * This method checks if every element in the underlying `data` array is not equal to -1L.
     * -1L in two's complement representation is all bits set to `true`. If none of the elements is -1L
     * that means that there is at least one bit in false. If there is no such a bit set to `true`,
     * that means all bits are set to `false`.
     *
     * @return `true` if all bits are set to `false`, `false` otherwise.
     */
    fun none(): Boolean = data.all { it == 0L }

    /**
     * Returns `true` if any bits are set to `true`, `false` if all bits are `false`.
     *
     * @return `true` if any bits are set to `true`, `false` otherwise.
     */
    fun any(): Boolean = data.any { it != 0L }

    /**
     * Clears all the bits
     */
    fun clear() {
        data.fill(0L)
    }

    /**
     * Returns a string representation of the BitSet in binary format.
     *
     * Each bit in the BitSet is represented by a '1' if it's set (true) or a '0' if it's clear (false).
     * The string is constructed by iterating through the bits from the least significant to the most significant.
     *
     * @return A string representing the BitSet in binary format.
     */
    fun toBinaryString():String {
        val builder = StringBuilder()
        for (i in 0 until vectorSize) {
            builder.append(if (this[i]) '1' else '0')
        }
        return builder.toString()
    }
}
