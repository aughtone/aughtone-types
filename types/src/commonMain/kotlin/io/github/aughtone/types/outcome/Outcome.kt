package io.github.aughtone.types.outcome

import kotlin.coroutines.cancellation.CancellationException

/**
 * The result of an operation that can fail: [Success] carrying a value, or [Failure] carrying the
 * [Throwable] that ended it.
 *
 * This covers the same ground as [kotlin.Result], but as a **sealed class** rather than a `value
 * class` over `Any?`. That difference is the whole reason the type exists. A `value class` has no
 * stable representation outside Kotlin, so a `Result` returned from shared code arrives in Swift,
 * JavaScript or Dart as an opaque box that the calling language cannot take apart. A sealed class
 * compiles to an ordinary class hierarchy on every target: Kotlin callers exhaust it with `when`,
 * and callers in other languages branch on the concrete type and read the payload as data. Failures
 * cross the language boundary as values instead of as thrown exceptions.
 *
 * ```
 * when (val outcome = runOutcome { parse(input) }) {
 *     is Outcome.Success -> render(outcome.data)
 *     is Outcome.Failure -> report(outcome.message)
 * }
 * ```
 *
 * **This type is deliberately not `@Serializable`.** [Failure] holds a live [Throwable], which has no
 * multiplatform serializer and would lose its stack trace and its type on the way through anyway.
 * When an outcome has to be persisted or sent over a wire, collapse it into a type that *is*
 * serializable first — [fold] exists for exactly that.
 *
 * @param T the type of the value carried by a [Success].
 */
sealed class Outcome<out T> {

    /**
     * An operation that completed, carrying its value.
     *
     * @property data The value the operation produced.
     */
    data class Success<out T>(val data: T) : Outcome<T>()

    /**
     * An operation that failed, carrying the [Throwable] that ended it.
     *
     * This is an `Outcome<Nothing>` rather than an `Outcome<T>`, so a single failure value is
     * assignable to an [Outcome] of any type and passes through [map] unchanged.
     *
     * @property exception The throwable that ended the operation.
     */
    data class Failure(val exception: Throwable) : Outcome<Nothing>() {
        /**
         * A message that is always safe to surface: the exception's own message, or its string
         * representation when it has none.
         */
        val message: String get() = exception.message ?: exception.toString()
    }

    /**
     * The previous name of [Failure], kept so code written against 3.3.0 still compiles.
     *
     * It was renamed because every callback in this API already said *failure* — `onFailure`, and
     * `fold`'s second parameter — while the type said *error*, and because `Outcome.Error` reads as
     * a relative of [kotlin.Error], which is a specific severe-throwable type it has nothing to do
     * with.
     */
    @Deprecated(
        "Renamed to Failure, to match the onFailure/fold callbacks and to avoid reading as kotlin.Error.",
        ReplaceWith("Failure"),
    )
    typealias Error = Failure

    /**
     * Runs [block] with the value if this is a [Success], and does nothing otherwise.
     *
     * @param block Called with the value of a [Success].
     * @return This same outcome, so calls can be chained.
     */
    inline fun onSuccess(block: (T) -> Unit): Outcome<T> {
        if (this is Success) block(data)
        return this
    }

    /**
     * Runs [block] with the failure if this is an [Failure], and does nothing otherwise.
     *
     * @param block Called with the [Failure] itself, so both the exception and its [Failure.message] are
     *              reachable.
     * @return This same outcome, so calls can be chained.
     */
    inline fun onFailure(block: (Failure) -> Unit): Outcome<T> {
        if (this is Failure) block(this)
        return this
    }

    /**
     * @return The value of a [Success], or `null` for an [Failure].
     */
    fun dataOrNull(): T? = (this as? Success)?.data

    /**
     * @return The value of a [Success].
     * @throws Throwable the [Failure.exception] itself, unwrapped, if this is an [Failure].
     */
    fun dataOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw exception
    }

    /**
     * Collapses both cases to a single value. This is how an outcome becomes something that can be
     * serialized, rendered, or returned to a caller that has no notion of an outcome.
     *
     * @param onSuccess Called with the value of a [Success].
     * @param onFailure Called with the [Failure] of a failure.
     * @return Whatever the branch that ran returned.
     */
    inline fun <R> fold(onSuccess: (T) -> R, onFailure: (Failure) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(this)
    }

    /**
     * Transforms the value of a [Success], passing an [Failure] through untouched.
     *
     * [transform] is **not** guarded: an exception it throws propagates to the caller. Use
     * [mapCatching] when the transform can fail and that failure belongs in the outcome.
     *
     * @param transform Applied to the value of a [Success].
     * @return A [Success] holding the transformed value, or this same [Failure].
     */
    inline fun <R> map(transform: (T) -> R): Outcome<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /**
     * Transforms the value of a [Success], capturing anything [transform] throws as an [Failure], and
     * passing an existing [Failure] through untouched.
     *
     * A [CancellationException] is still re-thrown rather than captured, on the same reasoning as
     * [runOutcome].
     *
     * @param transform Applied to the value of a [Success].
     * @return A [Success] holding the transformed value, an [Failure] holding whatever [transform]
     *         threw, or this same [Failure].
     */
    inline fun <R> mapCatching(transform: (T) -> R): Outcome<R> = when (this) {
        is Success -> runOutcome { transform(data) }
        is Failure -> this
    }

    companion object {
        /**
         * @param data The value the operation produced.
         * @return A [Success] carrying [data].
         */
        fun <T> success(data: T): Outcome<T> = Success(data)

        /**
         * @param exception The throwable that ended the operation.
         * @return A [Failure] carrying [exception].
         */
        fun failure(exception: Throwable): Outcome<Nothing> = Failure(exception)

        @Deprecated(
            "Renamed to failure, to match Outcome.Failure and the onFailure/fold callbacks.",
            ReplaceWith("Outcome.failure(exception)"),
        )
        fun error(exception: Throwable): Outcome<Nothing> = Failure(exception)
    }
}

/**
 * Runs [block] and wraps what happens in an [Outcome] — the `runCatching` of this package, named to
 * match so the shape is already familiar.
 *
 * A [CancellationException] is **re-thrown rather than captured**. Coroutine cancellation is
 * signalled by throwing that exception, so a builder that swallowed it would leave a cancelled
 * coroutine running as though nothing had happened. Everything else becomes an [Outcome.Failure]
 * holding the original throwable, unwrapped.
 *
 * The function is `inline` and carries no `suspend` modifier, which is what lets it wrap suspending
 * work without the library taking a dependency on coroutines:
 *
 * ```
 * suspend fun load(id: String): Outcome<User> = runOutcome { api.fetchUser(id) }
 * ```
 *
 * @param block The work to run.
 * @return [Outcome.Success] with the value [block] returned, or [Outcome.Failure] with what it threw.
 * @throws CancellationException if [block] throws one, so cancellation is never swallowed.
 */
inline fun <T> runOutcome(block: () -> T): Outcome<T> =
    try {
        Outcome.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Outcome.failure(e)
    }

/**
 * Turns an [Outcome.Failure] into a [Outcome.Success] carrying a fallback value, leaving an existing
 * success alone.
 *
 * This is an extension rather than a member because it widens the value type — the `T : R` bound
 * cannot be expressed on a member function — which is why [kotlin.Result] declares its equivalent
 * the same way.
 *
 * @param transform Called with the [Outcome.Failure] to produce a replacement value.
 * @return This outcome if it is a [Outcome.Success], otherwise a [Outcome.Success] holding the
 *         result of [transform].
 */
inline fun <R, T : R> Outcome<T>.recover(transform: (Outcome.Failure) -> R): Outcome<R> =
    when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> Outcome.success(transform(this))
    }

/**
 * Unwraps the value of a [Outcome.Success], or produces a fallback from the [Outcome.Failure].
 *
 * The counterpart to [Outcome.dataOrNull] and [Outcome.dataOrThrow] for callers that have a sensible
 * default. It is an extension for the same reason as [recover].
 *
 * @param onFailure Called with the [Outcome.Failure] to produce a fallback value.
 * @return The value of a [Outcome.Success], or the result of [onFailure].
 */
inline fun <R, T : R> Outcome<T>.dataOrElse(onFailure: (Outcome.Failure) -> R): R =
    when (this) {
        is Outcome.Success -> data
        is Outcome.Failure -> onFailure(this)
    }
