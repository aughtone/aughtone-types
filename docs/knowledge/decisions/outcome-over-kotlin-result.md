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

3.3.0 shipped the failure case as `Outcome.Error`. It was renamed to `Outcome.Failure` in 3.4.0, with `Outcome.Error` kept as a deprecated nested type alias and `Outcome.error(...)` as a deprecated factory so code written against 3.3.0 still compiled. Both were removed in 4.0.0.

The name was wrong on two counts. Every callback in the API already said *failure* — `onFailure`, and `fold`'s second parameter — so the type and the callbacks disagreed with each other in the same file. And `Outcome.Error` reads as a relative of `kotlin.Error`, which is a specific severe-throwable type it has nothing to do with; a case holding an ordinary `Throwable` should not borrow that name.

The rename is a binary break — `Outcome$Error` no longer exists as a class — which strictly argues for a major version. It ships as a minor anyway: 3.3.0 was hours old with no consumer compiled against it, the alias preserves source compatibility, and for a consumer the remedy is a clean and rebuild rather than a code change. The alias was removed in 4.0.0.

## Amendment — 2026-09-29, after 4.0.0

**Decision 6 is reversed.** The accessors are now named as `kotlin.Result` names them: `getOrNull`, `getOrThrow`, `getOrElse`, and a new `getOrDefault`, replacing `dataOrNull`, `dataOrThrow` and `dataOrElse`. `isSuccess`, `isFailure` and `exceptionOrNull()` are added for the same reason. The callbacks handed to `onFailure`, `fold`, `recover` and `getOrElse` now receive the `Throwable` itself rather than the `Failure` wrapper, which is what `Result` passes.

The original reasoning — that matching half of `Result`'s vocabulary implies the rest is present — assumed a reader who would be misled by the resemblance. What actually happened is the opposite: the resemblance is what a caller is looking for. `Result` is the type they already know and cannot use across the language boundary, and every consuming project reached for `Outcome` as the thing that stands in for it. A deliberately different vocabulary made every call site a translation exercise, for a distinction the type's own documentation already draws.

The remaining difference in vocabulary is `Success.data` where `Result` has `value`. That one stays: it is the payload accessor consumers already destructure and pattern-match on, and it is the name in every `when` branch written against 3.x and 4.0.0.

This is a source-breaking change shipped in a `-alpha` prerelease, and it was accepted on the understanding that the consumer cost was one edit per project. That estimate was wrong: one sibling library alone has **72 call sites**, which migrating touched 35 files to fix, plus nineteen in its README and KDoc. The remaining consumers have one or two files each.

A first estimate of the callback damage was also too high, and the correction is the useful part. Reads of `.exception` mostly do **not** break: that library had 111 of them and changed none, because they sit on an `Outcome.Failure` reached by a type check or smart cast, and `Failure.exception` is untouched. Only a callback *parameter* changed type. What did need care was narrower and quieter — see below.

The decision stands, for two reasons the corrected number does not touch. The renames are mechanical and the compiler finds every one of them, because the old names cease to exist rather than changing meaning. And the cost only grows: every consumer added before the rename pays it, and after 4.1.0 leaves alpha the rename cannot be made at all without a major version.

**The single silent breakage is a callback body reading `it.message`.** It keeps compiling, because `Throwable.message` exists, but it is `String?` where `Outcome.Failure.message` was a non-null `String`. In a string template that renders the literal text `null` for an exception carrying no message. The first consumer to migrate had fourteen of these, every one of them building a policy-identifier failure message that would have thrown an `IllegalStateException` reading "not a valid policy chain: null" — compiling cleanly the whole way. `it.message ?: it.toString()` is the fix, and is what `Failure.message` does internally.

This is worth stating ahead of the rename list rather than inside it, which is how the shipped skill now orders it: a reader scanning renamed methods has no reason to look for the one change that is not a renamed method.

**Doc comments are the other place to look, and nothing flags them.** That consumer had ten KDoc samples using the old API against four genuine source breakages of the same shape. A sample compiles nowhere, so no build catches it, and it is the first thing the next reader copies.

**A deprecation cycle was the road not taken, and the objection to skipping it is reasonable.** `@Deprecated` with `ReplaceWith` for one minor version would have turned 72 call sites into a single IDE action per consumer, at the cost of carrying dead names briefly. It was skipped because the callback signature change cannot be expressed as a `ReplaceWith` at all — only the three renames can — so the deprecation would have covered the mechanical half of the migration and left the silent half uncovered, while making the release look source-compatible. That reasoning is worth recording rather than leaving the choice looking automatic; a consumer maintainer preferred the deprecation cycle, and the preference is registered without changing the decision.

The lesson about the estimate is worth more than the estimate. It came from counting usage in the consumers that were in front of us, and the library with two orders of magnitude more usage was the one nobody had open. Counting a breaking change's blast radius means counting it in every consumer, not in the consumer that prompted the question.

The parenthetical lesson is about discovery rather than naming: the library's own skill did not mention `Outcome` in its description, so an agent searching for a multiplatform result type ranked this library below several that have nothing to do with the problem. A type that exists to be reached for has to be findable by the words someone with the problem would use — here, the name of the type they are trying to replace.
