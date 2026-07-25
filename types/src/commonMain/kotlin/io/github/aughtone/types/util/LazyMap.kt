package io.github.aughtone.types.util


/**
 * A map that lazily evaluates its values.
 *
 * This map takes a map of keys to functions that return values.
 * When a value is requested, the corresponding function is called and the result is cached,
 * including `null` results. In single-threaded use, each function is invoked at most once per key.
 * Subsequent requests for the same key will return the cached value.
 *
 * [containsValue] evaluates values lazily, one key at a time, and short-circuits as soon as a
 * match is found; already cached values are checked first.
 *
 * [entries] and [values] are backed by lazy iterators: each element is evaluated (and cached)
 * only when the iteration reaches it.
 *
 * [equals] and [hashCode] follow the [Map] contract (structural equality, symmetric with maps
 * built by `mapOf`). Note that both may force evaluation of every value in this map.
 *
 * This class is not thread-safe. Concurrent access must be synchronized externally, otherwise
 * a value function may be invoked more than once or the cache may be corrupted.
 *
 * @param K The type of the keys in the map.
 * @param V The type of the values in the map.
 * @param lazyVals A map of keys to functions that return values. These functions will be called to produce the value on first access.
 * @param cache A mutable map that is used to cache the values that have been evaluated.
 *              Defaults to a new, empty [mutableMapOf].
 */
class LazyMap<K, V>(val lazyVals: Map<K, () -> V>, val cache: MutableMap<K, V> = mutableMapOf()) :
    Map<K, V> {
    override fun containsKey(key: K): Boolean = lazyVals.containsKey(key)
    override fun isEmpty(): Boolean = lazyVals.isEmpty()
    override val keys: Set<K>
        get() = lazyVals.keys
    override val size: Int
        get() = lazyVals.size

    override fun get(key: K): V? {
        if (cache.containsKey(key)) return cache[key]
        val supplier = lazyVals[key] ?: return null
        val evaluated = supplier()
        cache[key] = evaluated
        return evaluated
    }

    // Evaluates the value for a key known to exist in lazyVals; V itself may be nullable.
    @Suppress("UNCHECKED_CAST")
    private fun evaluate(key: K): V = get(key) as V

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
                        val value = evaluate(key)
                        return object : Map.Entry<K, V> {
                            override val key: K get() = key
                            override val value: V get() = value
                        }
                    }
                }
            }
            override fun contains(element: Map.Entry<K, V>): Boolean =
                containsKey(element.key) && evaluate(element.key) == element.value
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
                    override fun next(): V = evaluate(keyIterator.next())
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
                if (evaluate(key) == value) return true
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Map<*, *>) return false
        if (other.size != size) return false
        @Suppress("UNCHECKED_CAST")
        val that = other as Map<Any?, Any?>
        return lazyVals.keys.all { key -> that.containsKey(key) && that[key] == evaluate(key) }
    }

    override fun hashCode(): Int {
        var hash = 0
        for (key in lazyVals.keys) {
            hash += (key?.hashCode() ?: 0) xor (evaluate(key)?.hashCode() ?: 0)
        }
        return hash
    }
}
