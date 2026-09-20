package io.github.aughtone.types.util

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The failure this guards against is silent: an unsynchronized cache can drop entries or be caught
 * mid-resize, after which a later read returns a wrong value on a key that was written correctly.
 * These tests are JVM-only because they need real parallelism — JS and Wasm cannot exhibit it.
 */
class LazyMapConcurrencyTest {

    private fun <T> race(threads: Int, block: (Int) -> T): List<T> {
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val futures = (0 until threads).map { i ->
            pool.submit<T> { start.await(); block(i) }
        }
        start.countDown()
        val results = futures.map { it.get(30, TimeUnit.SECONDS) }
        pool.shutdown()
        return results
    }

    @Test
    fun `concurrent access to one key gives every caller the identical instance`() {
        val map = lazyMapOf("k" to { Any() })
        val results = race(64) { map["k"] }
        val first = results.first()
        results.forEach { assertSame(first, it) }
    }

    @Test
    fun `concurrent access to many distinct keys leaves the cache complete`() {
        val keys = (0 until 500).map { "k$it" }
        val map = LazyMap(keys.associateWith { k -> { k.removePrefix("k").toInt() } })

        // Distinct keys are the dangerous case: resizing is driven by size, not by collision.
        race(32) { t -> keys.indices.filter { it % 32 == t }.forEach { map["k$it"] } }

        assertEquals(500, map.cache.size)
        keys.forEachIndexed { i, k -> assertEquals(i, map[k], "wrong value cached for $k") }
    }

    @Test
    fun `a cache hit does not re-evaluate`() {
        var calls = 0
        val map = lazyMapOf("k" to { calls++; "v" })
        repeat(10) { map["k"] }
        assertEquals(1, calls)
    }

    @Test
    fun `a value function may run more than once under contention but never yields disagreement`() {
        val map = lazyMapOf("k" to { Any() })
        val results = race(64) { map["k"] }
        assertTrue(results.distinct().size == 1, "callers disagreed about the cached value")
    }
}
