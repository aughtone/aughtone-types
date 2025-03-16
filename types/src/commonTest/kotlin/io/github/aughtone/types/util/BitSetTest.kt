package io.github.aughtone.types.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class BitSetTest {

    val bits = BitSet(10)

    @Test
    fun `size returns correct size`() {
        assertEquals(10, bits.size())
    }

    @Test
    fun `throws exception with size less than 0`() {
        try {
            BitSet(-1)
            fail("Should have thrown an exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Size must be non-negative", e.message)
        }
    }

    @Test
    fun `default size is 1`() {
        assertEquals(1, BitSet().size())
    }

    @Test
    fun `set a bit at index 2`() {
        bits.set(2)
        assertFalse(bits[0])
        assertFalse(bits[1])
        assertTrue(bits[2])
        assertFalse(bits[3])
    }

    @Test
    fun `clear a bit at index 5`() {
        bits.set(5)
        assertTrue(bits[5])
        bits.clear(5)
        assertFalse(bits[5])
    }

    @Test
    fun `created with all true`() {
        val set = BitSet(5, { true })
        assertTrue(set[0])
        assertTrue(set[1])
        assertTrue(set[2])
        assertTrue(set[3])
        assertTrue(set[4])
    }

    @Test
    fun `created with all false`() {
        val set = BitSet(5, { false })
        assertFalse(set[0])
        assertFalse(set[1])
        assertFalse(set[2])
        assertFalse(set[3])
        assertFalse(set[4])
    }

    @Test
    fun `size with large size`() {
        assertEquals(3554431, BitSet(size = 3554431).size())
    }

    @Test
    fun `size consistent after set`() {
        bits.set(9)
        assertEquals(10, bits.size())
    }

    @Test
    fun `size consistent after clear`() {
        bits.clear()
        assertEquals(10, bits.size())
    }

    @Test
    fun `toBinaryString displays bits`() {
        bits.set(0)
        bits.set(3)
        bits.set(7)
        assertEquals("1001000100", bits.toBinaryString())
    }

    @Test
    fun `check if a bit index is valid for this bitset`() {
        assertTrue(bits.hasIndex(9))
        assertFalse(bits.hasIndex(11))
    }

    @Test
    fun `check no bits are set`() {
        assertTrue(bits.none())
    }

    @Test
    fun `check all bits are set`() {
        val set = BitSet(5, { true })
        assertTrue(set.all())
    }

    @Test
    fun `check any bits are set`() {
        bits.set(5)
        assertTrue(bits.any())
    }

}
