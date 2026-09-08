# A Sealed `Outcome` Rather Than `kotlin.Result`

ADR-0004 · 2026-09-06 · Status: accepted
Keywords: why not kotlin.Result, Result value class breaks Swift interop, sealed
          result type for multiplatform, returning failures to Swift and
          JavaScript, why isn't Outcome serializable, exceptions across the
          language boundary, runCatching equivalent, swallowed coroutine
          cancellation, typed error hierarchy vs Throwable

## Context
Shared Kotlin Multiplatform code routinely returns "this worked, or it did not". The stdlib answer is `kotlin.Result`, and inside Kotlin it is a good one. Outside Kotlin it is not usable.

`Result` is a `value class` wrapping `Any?`. Value classes have no stable representation in the generated Objective-C header, the JavaScript output, or a Dart FFI binding — the compiler is free to erase the wrapper or box it, and neither form is something the calling language can take apart. A `Result` returned from a shared module therefore arrives in Swift or JavaScript as an opaque object: the caller can neither ask which case it is nor read the payload. The workaround teams reach for is to unwrap on the Kotlin side and throw instead, which pushes every failure back into exception handling — and an exception thrown out of shared code crosses the boundary badly too, becoming an uncatchable fatal error on some targets.

A second, quieter problem: any `runCatching`-shaped builder that catches `Throwable` also catches `CancellationException`. Coroutine cancellation *is* that exception, so a builder that captures it reports a cancelled coroutine as an ordinary failure and lets the work carry on. `kotlin.runCatching` has this defect and it is a well-known trap.

The same sealed-class pattern had been written by hand in more than one downstream project, each time with a project-specific error type welded into it, which is what prompted moving one general version into this library.

## Decision
Add `io.github.aughtone.types.outcome.Outcome<T>` — a `sealed class` with two cases, `Success<T>(data)` and `Failure(exception: Throwable)` — plus a `runOutcome { }` builder.

1. **Sealed class, not value class.** A sealed hierarchy compiles to ordinary classes on every target. Kotlin callers exhaust it with `when`; Swift and JavaScript callers branch on the concrete type and read the payload as data. Failures cross the language boundary as values rather than as thrown exceptions.
2. **`Failure` carries a plain `Throwable`, and the type has one parameter.** The error is not parameterized (`Outcome<T, E>`) and no error hierarchy ships with it. A second type parameter appears in every signature in every consuming project to buy flexibility most call sites never use, and a library-supplied error enum could only be generic enough to be useless. Callers who want typed errors throw their own sealed exception and `when` on `error.exception`.
3. **`Failure` is `Outcome<Nothing>`.** One failure value is assignable to an `Outcome` of any type, so `map` can pass it through unchanged under covariance rather than rebuilding it.
4. **`runOutcome` re-throws `CancellationException`** before its `catch (e: Throwable)`, so cancellation is never swallowed. It is `inline` and carries no `suspend` modifier, so it wraps suspending work without the library depending on coroutines — `kotlin.coroutines.cancellation.CancellationException` is a stdlib alias and pulls in no artifact.
5. **The type is not `@Serializable`,** departing from the house rule that every type here carries `@Serializable` and `@SerialName`. It holds a live `Throwable`, which has no multiplatform serializer and would lose its type and stack trace in transit regardless. `fold` is the documented way to collapse an outcome into something that *is* serializable, and the class KDoc says so.
6. **Naming follows this type, not `Result`.** `dataOrNull` / `dataOrThrow` / `dataOrElse`, not `getOrNull` / `getOrElse`. Matching half of `Result`'s vocabulary would suggest the rest of it is there too.

## Alternatives rejected
- **Use `kotlin.Result` and document the interop limitation.** Rejected: the limitation is not something a consumer can work around from their side. The value class is the defect.
- **Parameterize the error type as `Outcome<T, E>`.** Rejected: every function signature in every consuming project grows a second type argument, and `runOutcome` has to default `E` to `Throwable` anyway, so the common path pays the cost and gets nothing. A caller who needs a closed error set already has sealed exceptions.
- **Ship a typed error hierarchy alongside it.** Rejected: the versions written by hand downstream each carried a domain-specific hierarchy — HTTP statuses in one case — and none of them generalize. A library error type would either encode one domain's vocabulary or say nothing.
- **Wrap `kotlin.Result` internally and expose a sealed façade.** Rejected: two representations to keep in step, and the `Result` underneath would still be visible in the generated headers.

## Consequences
- Consumers get one shared type instead of a hand-written copy per project, and the cancellation trap is handled once rather than re-litigated each time.
- `Outcome` and `kotlin.Result` now both exist in scope for consumers. No conversion helpers ship in this version; if migration pressure appears, `toResult` / `toOutcome` can be added without a breaking change.
- Because `Failure` is a `data class` wrapping a `Throwable`, and `Throwable` does not override `equals`, two `Failure` values are equal only when they hold the same exception instance. That is the useful behaviour for tests and the only one available, but it is worth knowing before writing an equality assertion.
- The type cannot be persisted or sent over a wire directly. That is a deliberate constraint, not an oversight — see decision 5.

## Amendment — 2026-09-06, after 3.3.0

3.3.0 shipped the failure case as `Outcome.Error`. It is renamed to `Outcome.Failure` in 3.4.0, with `Outcome.Error` kept as a deprecated nested type alias and `Outcome.error(...)` as a deprecated factory, so code written against 3.3.0 still compiles and is told where to go.

The name was wrong on two counts. Every callback in the API already said *failure* — `onFailure`, and `fold`'s second parameter — so the type and the callbacks disagreed with each other in the same file. And `Outcome.Error` reads as a relative of `kotlin.Error`, which is a specific severe-throwable type it has nothing to do with; a case holding an ordinary `Throwable` should not borrow that name.

The rename is a binary break — `Outcome$Error` no longer exists as a class — which strictly argues for a major version. It ships as a minor anyway: 3.3.0 was hours old with no consumer compiled against it, the alias preserves source compatibility, and for a consumer the remedy is a clean and rebuild rather than a code change. The alias goes at 4.0.0.
