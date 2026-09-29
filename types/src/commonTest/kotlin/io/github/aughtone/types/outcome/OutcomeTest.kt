package io.github.aughtone.types.outcome

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OutcomeTest {

    @Test
    fun `a normal return is a Success carrying the value`() {
        val outcome = runOutcome { 42 }
        assertEquals(42, assertIs<Outcome.Success<Int>>(outcome).data)
        assertEquals(42, outcome.getOrNull())
        assertEquals(42, outcome.getOrThrow())
    }

    @Test
    fun `a thrown exception becomes a Failure carrying it unchanged`() {
        val boom = IllegalStateException("nope")
        val outcome = runOutcome { throw boom }
        assertSame(boom, assertIs<Outcome.Failure>(outcome).exception)
    }

    @Test
    fun `a CancellationException is re-thrown and never swallowed`() {
        assertFailsWith<CancellationException> {
            runOutcome<Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `a Failure has no data and re-throws on getOrThrow`() {
        val boom = IllegalArgumentException("bad")
        val outcome: Outcome<Int> = Outcome.failure(boom)
        assertNull(outcome.getOrNull())
        assertSame(boom, assertFailsWith<IllegalArgumentException> { outcome.getOrThrow() })
    }

    @Test
    fun `isSuccess and isFailure report the case`() {
        val ok = runOutcome { 1 }
        assertTrue(ok.isSuccess)
        assertFalse(ok.isFailure)

        val failed: Outcome<Int> = Outcome.failure(IllegalStateException("x"))
        assertTrue(failed.isFailure)
        assertFalse(failed.isSuccess)
    }

    @Test
    fun `exceptionOrNull returns the exception of a Failure and null for a Success`() {
        val boom = IllegalStateException("x")
        assertSame(boom, Outcome.failure(boom).exceptionOrNull())
        assertNull(runOutcome { 1 }.exceptionOrNull())
    }

    @Test
    fun `the message of a Failure falls back when the exception has none`() {
        assertEquals("boom", Outcome.Failure(IllegalStateException("boom")).message)
        assertTrue(Outcome.Failure(IllegalStateException()).message.isNotEmpty())
    }

    @Test
    fun `fold collapses both cases to one value and hands the failure branch the exception`() {
        assertEquals("ok", runOutcome { 1 }.fold(onSuccess = { "ok" }, onFailure = { "err" }))

        val boom = IllegalStateException("x")
        var seen: Throwable? = null
        assertEquals("err", Outcome.failure(boom).fold({ "ok" }, { seen = it; "err" }))
        assertSame(boom, seen)
    }

    @Test
    fun `onSuccess fires only on a Success and returns the same outcome`() {
        var seen: Int? = null
        val outcome = runOutcome { 7 }
        assertSame(outcome, outcome.onSuccess { seen = it })
        assertEquals(7, seen)

        var neverSeen: Int? = null
        val failed: Outcome<Int> = Outcome.failure(IllegalStateException("x"))
        failed.onSuccess { neverSeen = it }
        assertNull(neverSeen)
    }

    @Test
    fun `onFailure fires only on a Failure and is handed the exception itself`() {
        val boom = IllegalStateException("x")
        val failed: Outcome<Int> = Outcome.failure(boom)
        var seen: Throwable? = null
        assertSame(failed, failed.onFailure { seen = it })
        assertSame(boom, seen)

        var neverSeen: Throwable? = null
        runOutcome { 1 }.onFailure { neverSeen = it }
        assertNull(neverSeen)
    }

    @Test
    fun `map transforms a Success`() {
        assertEquals(4, runOutcome { 2 }.map { it * 2 }.getOrNull())
    }

    @Test
    fun `map passes a Failure through unchanged`() {
        val boom = IllegalStateException("x")
        val mapped = Outcome.failure(boom).map { "never" }
        assertSame(boom, assertIs<Outcome.Failure>(mapped).exception)
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
        assertSame(boom, assertIs<Outcome.Failure>(mapped).exception)
    }

    @Test
    fun `mapCatching still re-throws a CancellationException`() {
        assertFailsWith<CancellationException> {
            runOutcome { 2 }.mapCatching<Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `recover replaces a Failure with a value and leaves a Success alone`() {
        assertEquals(-1, Outcome.failure(IllegalStateException("x")).recover { -1 }.getOrNull())
        assertEquals(5, runOutcome { 5 }.recover { -1 }.getOrNull())
    }

    @Test
    fun `recover is handed the exception itself`() {
        val boom = IllegalStateException("x")
        var seen: Throwable? = null
        Outcome.failure(boom).recover { seen = it; -1 }
        assertSame(boom, seen)
    }

    @Test
    fun `getOrElse returns the fallback on a Failure and the value on a Success`() {
        val boom = IllegalStateException("x")
        val failed: Outcome<Int> = Outcome.failure(boom)
        var seen: Throwable? = null
        assertEquals(-1, failed.getOrElse { seen = it; -1 })
        assertSame(boom, seen)
        assertEquals(5, runOutcome { 5 }.getOrElse { -1 })
    }

    @Test
    fun `getOrDefault returns the default on a Failure and the value on a Success`() {
        val failed: Outcome<Int> = Outcome.failure(IllegalStateException("x"))
        assertEquals(-1, failed.getOrDefault(-1))
        assertEquals(5, runOutcome { 5 }.getOrDefault(-1))
    }
}
