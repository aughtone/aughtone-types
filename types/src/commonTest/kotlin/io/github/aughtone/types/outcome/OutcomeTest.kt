package io.github.aughtone.types.outcome

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OutcomeTest {

    @Test
    fun `a normal return is a Success carrying the value`() {
        val outcome = runOutcome { 42 }
        assertEquals(42, assertIs<Outcome.Success<Int>>(outcome).data)
        assertEquals(42, outcome.dataOrNull())
        assertEquals(42, outcome.dataOrThrow())
    }

    @Test
    fun `a thrown exception becomes an Error carrying it unchanged`() {
        val boom = IllegalStateException("nope")
        val outcome = runOutcome { throw boom }
        assertSame(boom, assertIs<Outcome.Error>(outcome).exception)
    }

    @Test
    fun `a CancellationException is re-thrown and never swallowed`() {
        assertFailsWith<CancellationException> {
            runOutcome<Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `an Error has no data and re-throws on dataOrThrow`() {
        val boom = IllegalArgumentException("bad")
        val outcome: Outcome<Int> = Outcome.error(boom)
        assertNull(outcome.dataOrNull())
        assertSame(boom, assertFailsWith<IllegalArgumentException> { outcome.dataOrThrow() })
    }

    @Test
    fun `the message of an Error falls back when the exception has none`() {
        assertEquals("boom", Outcome.Error(IllegalStateException("boom")).message)
        assertTrue(Outcome.Error(IllegalStateException()).message.isNotEmpty())
    }

    @Test
    fun `fold collapses both cases to one value`() {
        assertEquals("ok", runOutcome { 1 }.fold(onSuccess = { "ok" }, onFailure = { "err" }))
        assertEquals("err", Outcome.error(IllegalStateException("x")).fold({ "ok" }, { "err" }))
    }

    @Test
    fun `onSuccess fires only on a Success and returns the same outcome`() {
        var seen: Int? = null
        val outcome = runOutcome { 7 }
        assertSame(outcome, outcome.onSuccess { seen = it })
        assertEquals(7, seen)

        var neverSeen: Int? = null
        val failed: Outcome<Int> = Outcome.error(IllegalStateException("x"))
        failed.onSuccess { neverSeen = it }
        assertNull(neverSeen)
    }

    @Test
    fun `onFailure fires only on an Error and returns the same outcome`() {
        val boom = IllegalStateException("x")
        val failed: Outcome<Int> = Outcome.error(boom)
        var seen: Throwable? = null
        assertSame(failed, failed.onFailure { seen = it.exception })
        assertSame(boom, seen)

        var neverSeen: Throwable? = null
        runOutcome { 1 }.onFailure { neverSeen = it.exception }
        assertNull(neverSeen)
    }

    @Test
    fun `map transforms a Success`() {
        assertEquals(4, runOutcome { 2 }.map { it * 2 }.dataOrNull())
    }

    @Test
    fun `map passes an Error through unchanged`() {
        val boom = IllegalStateException("x")
        val mapped = Outcome.error(boom).map { "never" }
        assertSame(boom, assertIs<Outcome.Error>(mapped).exception)
    }

    @Test
    fun `map lets an exception thrown by the transform escape`() {
        assertFailsWith<IllegalArgumentException> {
            runOutcome { 2 }.map { throw IllegalArgumentException("bad") }
        }
    }

    @Test
    fun `mapCatching captures an exception thrown by the transform`() {
        val boom = IllegalArgumentException("bad")
        val mapped = runOutcome { 2 }.mapCatching<Int> { throw boom }
        assertSame(boom, assertIs<Outcome.Error>(mapped).exception)
    }

    @Test
    fun `mapCatching still re-throws a CancellationException`() {
        assertFailsWith<CancellationException> {
            runOutcome { 2 }.mapCatching<Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `recover replaces an Error with a value and leaves a Success alone`() {
        assertEquals(-1, Outcome.error(IllegalStateException("x")).recover { -1 }.dataOrNull())
        assertEquals(5, runOutcome { 5 }.recover { -1 }.dataOrNull())
    }

    @Test
    fun `dataOrElse returns the fallback on an Error and the value on a Success`() {
        val failed: Outcome<Int> = Outcome.error(IllegalStateException("x"))
        assertEquals(-1, failed.dataOrElse { -1 })
        assertEquals(5, runOutcome { 5 }.dataOrElse { -1 })
    }
}
