package io.github.aughtone.types.util

/**
 * Creates a new [LazyMap] from a list of key-value pairs, where the value is a lambda that will be evaluated lazily.
 *
 * The value associated with each key will only be computed when it is first accessed.
 *
 * @param entries The key-value pairs to populate the [LazyMap]. Each value is a lambda `() -> V` that returns the value.
 * @return A new [LazyMap] containing the provided key-value pairs.
 */
fun <K, V> lazyMapOf(vararg entries: Pair<K, () -> V>): LazyMap<K, V> = LazyMap(mapOf(*entries))
