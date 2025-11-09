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
    Map<K, V> by cache {
    override fun containsKey(key: K): Boolean = lazyVals.containsKey(key)
    override fun isEmpty(): Boolean =lazyVals.isEmpty()
    override val keys: Set<K>
        get() = lazyVals.keys
    override val size: Int
        get() = lazyVals.size

    override fun get(key: K): V? =
        cache[key] ?: lazyVals[key]?.let { cache[key] = it(); cache[key] }

    // XXX I'm not sure what I want to do with these functions yet.
    //  They could probably b handled with a wrapper that could call into the cache, but they
    //  are likely to be iterated over anyway.
    override val entries: Set<Map.Entry<K, V>>
        get() = TODO("Not yet implemented")
    override val values: Collection<V>
        get() = TODO("Not yet implemented")

    // XXX This would cause the entire map to be evaluated, defeating the lazyness of the map.
    override fun containsValue(value: V): Boolean {
        throw UnsupportedOperationException("Accessing containsValue() would cause the entire map to be evaluated.")
    }

}
