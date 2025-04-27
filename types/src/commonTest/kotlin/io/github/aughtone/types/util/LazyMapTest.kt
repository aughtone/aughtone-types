package io.github.aughtone.types.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LazyMapTest {
    // This map should behave just like any other map, except its initialized a little differently.
    var testParamCalled = mutableListOf(false, false, false)
    val lazyMap = lazyMapOf(
        "one" to { testParamCalled[0] = true;"another" },
        "two" to { testParamCalled[1] = true;"go" },
        "three" to { testParamCalled[2] = true;"in a tree" })

    @Test
    fun `size returns correct size`() {
        assertEquals(3, lazyMap.size)
    }

    @Test
    fun `contains key returns true for existing key`() {
        assertTrue(lazyMap.containsKey("three"))
    }

    @Test
    fun `contains key returns false for missing key`() {
        assertFalse(lazyMap.containsKey("bogus"))
    }

    @Test
    fun `is empty returns true for empty map`() {
        val actual = lazyMapOf<String, String>()
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `is empty returns false for map with keys`() {
        assertFalse(lazyMap.isEmpty())
    }

    @Test
    fun `keys returns correct keys`() {
        val keys = lazyMap.keys
        assertTrue(keys.contains("one"))
        assertTrue(keys.contains("two"))
        assertTrue(keys.contains("three"))
    }

    @Test
    fun `returns null if key is missing`() {
        val value: String? = lazyMap["bogus"]
        assertNull(value)
    }

    @Test
    fun `data only loaded when it is first accessed`() {
        assertFalse(testParamCalled[0])
        val value = lazyMap["one"]
        assertTrue(testParamCalled[0])
    }

    @Test
    fun `other data not loaded when adjacent data is first loaded`() {
        assertFalse(testParamCalled[0])
        assertFalse(testParamCalled[1])
        assertFalse(testParamCalled[2])
        val value = lazyMap["two"]
        assertFalse(testParamCalled[0])
        assertTrue(testParamCalled[1])
        assertFalse(testParamCalled[2])
    }

    @Test
    fun `contains value throws exception`() {
        try{
            lazyMap.containsValue("another")
        } catch (e: UnsupportedOperationException) {
            assertEquals("Accessing containsValue() would cause the entire map to be evaluated.", e.message)
        }
    }
}
