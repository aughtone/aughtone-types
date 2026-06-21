package io.github.aughtone.types.util


/**
 * A map that lazily evaluates its values.
 *
 * This map takes a map of keys to functions that return values.
 * When a value is requested, the corresponding function is called and the result is cached.
 * Subsequent requests for the same key will return the cached value.
 *
 * The implementation of [containsValue] is throwing [UnsupportedOperationException] because
 * it would require iterating through all entries, and it would evaluate all the lazy values.
 * This behaviour would negate the lazy nature of this map, so it is not supported.
 *
 * @param K The type of the keys in the map.
 * @param V The type of the values in the map.
 * @param lazyVals A map of keys to functions that return values. These functions will be called to produce the value on first access.
 * @param cache A mutable map that is used to cache the values that have been evaluated.
 *              Defaults to a new, empty [mutableMapOf].
 *              This map acts as a delegate for the [Map] interface.
 *
 * The implementation of [entries] and [values] are currently marked as [TODO].
 * This is because they would require evaluating all the lazy values in order to be correctly implemented.
 * This behaviour would negate the lazy nature of this map, so it is not yet implemented.
 */
class LazyMap<K, V>(val lazyVals: Map<K, () -> V>, val cache: MutableMap<K, V> = mutableMapOf()) :
    Map<K, V> {
    override fun containsKey(key: K): Boolean = lazyVals.containsKey(key)
    override fun isEmpty(): Boolean = lazyVals.isEmpty()
    override val keys: Set<K>
        get() = lazyVals.keys
    override val size: Int
        get() = lazyVals.size

    override fun get(key: K): V? =
        cache[key] ?: lazyVals[key]?.let {
            val evaluated = it()
            cache[key] = evaluated
            evaluated
        }

    override val entries: Set<Map.Entry<K, V>>
        get() = object : Set<Map.Entry<K, V>> {
            override val size: Int get() = lazyVals.size
            override fun isEmpty(): Boolean = lazyVals.isEmpty()
            override fun iterator(): Iterator<Map.Entry<K, V>> {
                val keyIterator = lazyVals.keys.iterator()
                return object : Iterator<Map.Entry<K, V>> {
                    override fun hasNext(): Boolean = keyIterator.hasNext()
                    override fun next(): Map.Entry<K, V> {
                        val key = keyIterator.next()
                        val value = get(key) ?: error("Key not found in lazyVals during iteration")
                        return object : Map.Entry<K, V> {
                            override val key: K get() = key
                            override val value: V get() = value
                        }
                    }
                }
            }
            override fun contains(element: Map.Entry<K, V>): Boolean {
                val value = get(element.key)
                return value != null && value == element.value
            }
            override fun containsAll(elements: Collection<Map.Entry<K, V>>): Boolean =
                elements.all { contains(it) }
        }

    override val values: Collection<V>
        get() = object : Collection<V> {
            override val size: Int get() = lazyVals.size
            override fun isEmpty(): Boolean = lazyVals.isEmpty()
            override fun iterator(): Iterator<V> {
                val keyIterator = lazyVals.keys.iterator()
                return object : Iterator<V> {
                    override fun hasNext(): Boolean = keyIterator.hasNext()
                    override fun next(): V {
                        val key = keyIterator.next()
                        return get(key) ?: error("Key not found in lazyVals during iteration")
                    }
                }
            }
            override fun contains(element: V): Boolean = containsValue(element)
            override fun containsAll(elements: Collection<V>): Boolean =
                elements.all { contains(it) }
        }

    override fun containsValue(value: V): Boolean {
        if (cache.containsValue(value)) return true
        for (key in lazyVals.keys) {
            if (!cache.containsKey(key)) {
                if (get(key) == value) return true
            }
        }
        return false
    }
}
