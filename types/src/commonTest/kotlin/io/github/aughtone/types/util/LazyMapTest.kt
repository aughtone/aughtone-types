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
    fun `contains value returns true for existing value`() {
        assertTrue(lazyMap.containsValue("another"))
        assertTrue(lazyMap.containsValue("go"))
        assertTrue(lazyMap.containsValue("in a tree"))
    }

    @Test
    fun `contains value returns false for missing value`() {
        assertFalse(lazyMap.containsValue("bogus"))
    }

    @Test
    fun `values returns all evaluated values`() {
        val vals = lazyMap.values
        assertEquals(3, vals.size)
        assertTrue(vals.contains("another"))
        assertTrue(vals.contains("go"))
        assertTrue(vals.contains("in a tree"))
    }

    @Test
    fun `entries returns all evaluated entries`() {
        val entries = lazyMap.entries
        assertEquals(3, entries.size)
        assertTrue(entries.any { it.key == "one" && it.value == "another" })
    }

    @Test
    fun `accessing entries does not evaluate elements eagerly`() {
        val testCalled = mutableListOf(false, false)
        val testMap = lazyMapOf(
            "a" to { testCalled[0] = true; "first" },
            "b" to { testCalled[1] = true; "second" }
        )
        // Getting entries property should not evaluate anything
        val entries = testMap.entries
        assertFalse(testCalled[0])
        assertFalse(testCalled[1])

        // Accessing size should not evaluate anything
        assertEquals(2, entries.size)
        assertFalse(testCalled[0])
        assertFalse(testCalled[1])

        // Only iterating/pulling the first entry should evaluate just that one
        val iterator = entries.iterator()
        assertTrue(iterator.hasNext())
        val firstEntry = iterator.next()
        assertEquals("a", firstEntry.key)
        assertEquals("first", firstEntry.value)
        assertTrue(testCalled[0])
        assertFalse(testCalled[1])
    }

    @Test
    fun `null values are cached and supplier invoked only once`() {
        var calls = 0
        val map = lazyMapOf<String, String?>("a" to { calls++; null })
        assertNull(map["a"])
        assertNull(map["a"])
        assertEquals(1, calls)
        assertTrue(map.cache.containsKey("a"))
    }

    @Test
    fun `entries iteration yields null values`() {
        val map = lazyMapOf<String, String?>("a" to { null }, "b" to { "value" })
        val entries = map.entries.toList()
        assertEquals(2, entries.size)
        assertEquals("a", entries[0].key)
        assertNull(entries[0].value)
        assertEquals("value", entries[1].value)
        assertTrue(map.values.contains(null))
    }

    @Test
    fun `equals and hashCode match an equivalent plain map`() {
        val map = lazyMapOf("a" to { 1 }, "b" to { 2 })
        val plain = mapOf("a" to 1, "b" to 2)
        assertEquals<Map<String, Int>>(plain, map)
        assertEquals<Map<String, Int>>(map, plain)
        assertEquals(plain.hashCode(), map.hashCode())
    }

    @Test
    fun `equals returns false for a different map`() {
        val map = lazyMapOf("a" to { 1 })
        assertFalse(map == mapOf("a" to 2))
        assertFalse(map == mapOf("b" to 1))
        assertFalse(map == mapOf("a" to 1, "b" to 2))
    }

    @Test
    fun `containsValue evaluates lazily and short-circuits`() {
        val testCalled = mutableListOf(false, false, false)
        val testMap = lazyMapOf(
            "a" to { testCalled[0] = true; "first" },
            "b" to { testCalled[1] = true; "second" },
            "c" to { testCalled[2] = true; "third" }
        )
        // Checking for "second" should only evaluate "a" and "b", and skip "c"
        assertTrue(testMap.containsValue("second"))
        assertTrue(testCalled[0])
        assertTrue(testCalled[1])
        assertFalse(testCalled[2])
    }
}
