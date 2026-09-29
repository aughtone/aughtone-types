---
name: io-github-aughtone-types
description: >-
  Shared value types for Kotlin Multiplatform: money and currency, distances,
  speeds and GPS coordinates, GeoJSON geometry, URLs, URNs and geo URIs,
  locales, units of measure, arbitrary-precision integers and decimals, and
  Outcome — a sealed success-or-failure result type that stands in for
  kotlin.Result, which is a value class and cannot carry a failure out to
  Swift, JavaScript or Dart. Reach for it instead of hand-rolling a money class
  that loses cents through Double, a latitude/longitude pair nothing validates,
  a URL compared with ==, a BigDecimal that only exists on the JVM, or your own
  sealed Result for multiplatform error handling. Every value type is
  @Serializable and behaves identically on JVM, Android, iOS, JS, Wasm and
  Linux. It models
  values; it does not render them — for display formatting such as "3 days
  ago", "1.5 km" or a masked card number, use a formatting library instead.
license: Apache-2.0
metadata:
  version: "4.1.0-alpha1"
  repository: https://github.com/aughtone/aughtone-types
---

# Aught One Types

## What it solves

Cross-platform code needs the same handful of value types over and over: an amount of money that does not lose half a cent, a position on the earth, a length or a speed with a unit attached, a web address you can compare for equality, a language and region, a number too big for `Long`. Each one has a standard that says what it means, and each one is usually reimplemented per project with the standard half-remembered.

This library provides those types once, for every Kotlin target, with the standards actually applied: ISO 4217 for currency, BCP 47 for locales, RFC 7946 for GeoJSON, RFC 3986 for URIs, RFC 8141 for URNs, RFC 5870 for geo URIs. Arbitrary-precision arithmetic is pure Kotlin rather than a JVM shim, so `BigDecimal` means the same thing on iOS and in a browser as it does on the server.

Types are `@Serializable` with explicit `@SerialName` on every property, so the wire format is stable across renames and identical on every platform.

## How it is meant to be used

**Construct, compare and serialize.** Most callers only need the types themselves.

```kotlin
val cad = currencyFor("CAD")!!                    // by ISO 4217 code
val price = Money(BigDecimal("12.50"), cad)
val here = Coordinates(latitude = 45.42, longitude = -75.69)
val trip = Distance(meters = 1500.0)

Json.encodeToString(price)
```

`currencyFor` is overloaded: given a `String` it looks up an ISO 4217 code, and given a `Locale` it resolves that region's currency. `Currency.getCurrency(locale)` is the same lookup as the second, and `availableCurrencies()` lists them all.

**Parse text into a type, and get a typed failure when it is not one.** Every parser has a throwing form and an `OrNull` form; malformed input raises `UriParseException`, which extends `IllegalArgumentException`.

```kotlin
val site = url("https://example.com/a?b=c")     // throws UriParseException
val maybe = urlOrNull(userInput)                 // null instead
val geo = geoUri("geo:48.2,16.3")
```

**Carry failure as data rather than as an exception**, which is what `Outcome` is for. It is the stand-in for `kotlin.Result` in multiplatform code: `Result` is a `value class` over `Any?` with no representation Swift, JavaScript or Dart can take apart, so a failure returned from shared code arrives as an opaque box. `Outcome` is a sealed class and compiles to an ordinary hierarchy everywhere.

```kotlin
when (val result = runOutcome { url(userInput) }) {
    is Outcome.Success -> use(result.data)
    is Outcome.Failure -> report(result.message)
}
```

**Write it as you would write `Result`.** `isSuccess`, `isFailure`, `getOrNull()`, `getOrThrow()`, `exceptionOrNull()`, `fold`, `map`, `mapCatching`, `onSuccess`, `onFailure`, `recover`, `getOrElse` and `getOrDefault` all take what `Result`'s take and return what `Result`'s return, and every failure callback receives the `Throwable` itself. Two deliberate differences: a `Success` carries **`data`**, not `value`; and `Outcome.Failure.message` is a non-null convenience that `Result` has no counterpart for.

`runOutcome` is the `runCatching` of this package, with the trap fixed: it re-throws `CancellationException` rather than capturing it, so wrapping suspending work never swallows coroutine cancellation. It is `inline` and not `suspend`, so it carries no coroutines dependency. `mapCatching` behaves the same way.

`Outcome` is the one type here that is **not** `@Serializable` — it holds a live `Throwable`. Collapse it with `fold` before persisting or sending it.

## Invariants and traps

**`Coordinates` and `GeoPoint` take their arguments in opposite orders.** `Coordinates(latitude, longitude)` follows the conversational order; `GeoPoint(longitude, latitude)` follows RFC 7946, which puts longitude first. Both parameters are `Double`, so swapping them compiles and puts the point in the wrong hemisphere. Use named arguments for `Coordinates`, and remember that anything GeoJSON is longitude-first.

**`Money` equality ignores scale, but serialization preserves it.** `Money(5.1, usd) == Money(5.10, usd)` is `true`, because no arithmetic here can make two spellings of one amount differ in value. The stored scale is untouched and still serialized, so **two equal amounts can serialize differently**. Compare `value.scale` explicitly to ask whether two amounts were *written* the same way. `BigDecimal` is unaffected and keeps JDK-style scale-sensitive equality.

**Comparing `Money` across currencies throws.** `Money` implements `Comparable<Money>`, and ordering two different currencies is not meaningful, so it raises `IllegalArgumentException` rather than inventing an answer. Sorting a mixed-currency list is a runtime failure, not a compile error.

**`Distance` and `Speed` reject negative results instead of clamping them.** `Distance(3.0) - Distance(5.0)` throws; it does not return zero. The same applies to negative scalars in `Speed.times` and `Speed.div`, and to a negative divisor on `Distance`. Guard the call, or compare first, when operands may arrive in either order. A zero distance or speed is perfectly valid — only going below zero is refused.

**`Locale.current` and `Currency.current` throw when the platform cannot say.** Both are `requireNotNull` over a platform lookup, so they raise `IllegalArgumentException` rather than defaulting to English or USD. That makes them a poor default argument, a poor property initialiser and a poor thing to read during composition. Use `Locale.currentOrNull` and `Currency.currentOrNull` where a sensible fallback exists, and keep the throwing form where none does — failing loudly beats silently rendering someone else's language.

**`UnitOfMeasure.findFirst` returns `null` for an ambiguous symbol** rather than guessing by declaration order. `findFirst("gal")` is `null`, because `Gallon` and `GallonImperial` both claim it and picking one silently is a twenty-percent error. Use `findAll(symbol)`, which returns every candidate — empty for unknown, more than one for ambiguous, so the two cases stay distinguishable.

**Invalid GeoJSON is rejected on construction *and* on deserialization.** Minimum position counts, positions of at least two elements, and closed linear rings of at least four positions are all enforced, so a payload that used to decode into a structurally invalid value now throws. Winding order is deliberately **not** enforced: RFC 7946 §3.1.6 tells parsers not to reject polygons that break the right-hand rule, so a clockwise exterior ring round-trips unchanged. Use `GeoPolygon.windingOf()` to inspect it and `rewound()` to correct it, or the `geoPolygon(...)` factory, which refuses bad winding at construction.

**URI types compare by the equivalence their specification defines, not member by member.** A `Urn`'s r-, q- and f-components take no part in equality, because RFC 8141 §3.1 requires they be ignored. `Uri` and `Url` treat scheme and host as case-insensitive and everything else as case-sensitive, per RFC 3986 §6.2.2.1. A `GeoUri` with no `crs` equals one with `"WGS84"`, per RFC 5870. In every case the value you supplied is preserved and serialized unchanged — only the comparison is normalized.

**`localizedDisplayName` falls back to English, silently.** Names come from the platform's own CLDR where it has them, and from a bundled table where it does not, but when neither can supply one the English name comes back with nothing to mark it. Use **`localizedDisplayNameOrNull`** where showing the wrong language is worse than showing nothing: it returns `null` instead of English, so the choice is yours.

**Never depend on the exact name.** Platforms disagree about wording on a large share of the names they all know — a browser and a native platform differ on roughly 40% — so `nl-BE` may be "Nederlands (België)" or "Vlaams" depending on where your code runs. Compare locales, never their rendered names, and keep them out of cache keys and snapshot tests.

**A `LazyMap` value function must be pure.** The cache is an immutable map behind an atomic reference, and under contention a value function may run more than once for the same key — the losing caller discards its own result and returns the winner's. Every caller observes the same value, but a function with side effects will perform them more than once.

## What moved, and what it used to be called

**Removed in 4.0.0**, having been deprecated earlier:

- `Outcome.Error` and `Outcome.error(...)` are gone. They became `Outcome.Failure` and `Outcome.failure(...)` in 3.4.0, where the old names survived as deprecated aliases. `Outcome$Error` no longer exists as a class, so upgrading from 3.3.0 needs a clean rebuild rather than a code change.
- `Locale.toLanguageTag()` is gone; it has been the `Locale.languageTag` property since 2.2.0.
- `UnitOfMeasure.FoodCalorie` is gone. A food calorie *is* a kilocalorie — use `UnitOfMeasure.Kilocalorie`.
- `Distance.times(Distance)` is gone. Metres times metres is an area, and there is no area type.
- `LazyMap`'s `cache` constructor parameter is gone; the cache is internal.

**Changed shape in 4.0.0**, where old code may still compile:

- `Money` equality became numeric, so `5.1` now equals `5.10` where it did not before.
- `Distance` and `Speed` operators throw where they previously floored at zero. This reverses the 3.4.0 change, which had replaced throwing with clamping.
- `Distance.div(Distance)` now returns `Double` rather than `Distance`, because a ratio of two lengths is dimensionless. Call sites that store the result in a `Distance` stop compiling.
- `GeoBoundingBox` is no longer a `GeoGeometry`; RFC 7946 makes a bounding box a `bbox` member rather than a geometry. Use `toBbox()` for the RFC form. Exhaustive `when` expressions over `GeoGeometry` fail to compile.
- `GeoFeature.properties` is `JsonObject?`, was `Map<String, String>?`; `GeoFeature.id` is `JsonPrimitive?`, was `String?`. Read values with the accessors: `feature.stringProperty("name")`, and the `intProperty`, `doubleProperty`, `booleanProperty`, `objectProperty` and `arrayProperty` beside it.
- `Gallon.symbol` is `"US gal"`, was `"gal"`; `Calorie.symbol` is `"cal"` and `Kilocalorie.symbol` is `"kcal"`. Anything rendering `.symbol` produces different output, with no compile error.
- `UnitOfMeasure.findFirst` returns `null` on an ambiguous symbol, where it previously returned the first match by declaration order.

**Changed shape in 4.1.0-alpha1**, breaking source compatibility:

- **`Outcome`'s accessors are renamed to `kotlin.Result`'s.** `dataOrNull` is now `getOrNull`, `dataOrThrow` is `getOrThrow`, and `dataOrElse` is `getOrElse`. There are no deprecated aliases — the old names are gone. `Success.data` is unchanged and is still `data`, not `value`.
- **`Outcome`'s failure callbacks receive the `Throwable`, not the `Outcome.Failure`.** This covers `onFailure`, `fold`'s second parameter, `recover` and `getOrElse`. A body reading `it.exception` becomes `it`; a body reading `it.message` still compiles but now gets the **nullable** `Throwable.message`, so assigning it to a `String` stops compiling. Branch on the type when the non-null `Outcome.Failure.message` is wanted.
- **`Outcome.isSuccess`, `Outcome.isFailure`, `exceptionOrNull()` and `getOrDefault(default)`** are new, and match `kotlin.Result`.

**Changed behaviour in 4.1.0-alpha1**, with no signature change:

- **`localizedDisplayName` was returning English for every locale on the JS target**, in 3.4.0 and 4.0.0, and now returns real translations. Anything rendering locale names on web will change output. The other targets are unaffected in kind, though a few names change where the JVM had been discarding script subtags.
- **`localizedDisplayNameOrNull`, `Locale.currentOrNull` and `Currency.currentOrNull`** are new. Nothing is removed and nothing is renamed.

**Earlier renames still worth knowing**, because code and training data predate them:

- GeoJSON types gained a `Geo` prefix in 3.0.0: `Point` is `GeoPoint`, `Polygon` is `GeoPolygon`, `Feature` is `GeoFeature`, and so on.
- `UnitOfMeasure` and `MetricPrefix` constants became PascalCase in 2.0.0: `KILOBYTE` is `Kilobyte`.
- The Apple framework is `AOTypesKit`; it was `AughtoneTypesKit` before 3.3.0.
- `localeFor(tag)` became a strict exact-match lookup in 2.2.0. For the BCP 47 fallback behaviour it used to have, use `resolveLocale(tag)`.

## What it is not for

This library models values. It does not format them for display, and it has no opinion about how a date, an amount or a distance should read in a sentence — that belongs in a formatting library.

It is not a datetime library. `kotlinx-datetime` is an `api` dependency and is exposed deliberately; instants, durations and time zones come from there.

It is not an internationalization framework. It carries locale and currency identity and can name a locale in the reader's language, but it does not translate anything else, and it does not promise the same wording on every platform.

It does no I/O, has no networking, and reads no files or resources at runtime.

## Calling it from other languages

Kotlin callers on every target need nothing beyond this file. Swift callers do, because the export renames things:

- [Calling it from Swift](references/swift.md)
