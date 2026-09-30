---
name: io-github-aughtone-types
description: >-
  Hold money without losing cents to Double: ISO 4217 currency by code or
  locale, minor units, banker's half-even rounding. Shared value types for
  Kotlin Multiplatform — JVM, Android, iOS, JS, Wasm, Linux — identical on
  each. Arbitrary-precision decimals and big integers off the JVM; GPS
  latitude and longitude, distance between two points, move a point by
  distance and bearing; distance, speed, altitude and compass heading with
  accuracy; GeoJSON points, polygons and features validated on decode, with
  winding order and bounding boxes; parse and compare URLs, URIs, URNs and
  geo URIs, build a URL with query parameters, percent-encode or URL-encode
  text; BCP 47 locales named in the reader's own language; units of measure
  and SI prefixes by symbol; a bit set and a lazy map; and Outcome, a sealed
  success-or-failure result standing in for kotlin.Result, a value class
  Swift cannot take apart. Models values; does not render them — for "1.5
  km", "$12.50" or "3 days ago" use io.github.aughtone:format-readable.
license: Apache-2.0
metadata:
  version: "4.1.0-SNAPSHOT"
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

**Carry failure as data rather than as an exception**, which is what `Outcome` is for. It is the stand-in for `kotlin.Result` in multiplatform code: `Result` is a `value class` over `Any?`, which has no stable representation outside Kotlin, so a failure returned from shared code reaches a Swift caller as an opaque box it cannot take apart. `Outcome` is a sealed class and compiles to an ordinary class hierarchy on every target, so Swift branches on the concrete type and reads the payload. Kotlin/JS and Kotlin/Wasm callers get the same benefit within Kotlin; this library exports nothing to plain JavaScript.

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

**Every `Money` operation across two currencies throws.** `plus`, `minus`, `times`, `div` and `compareTo` all raise `IllegalArgumentException` rather than inventing an answer, so **sorting or summing a mixed-currency list is a runtime failure, not a compile error**. The symptom is an exception from `sorted()`, `max()`, `sum()` or a `TreeMap` on a collection that looked homogeneous.

**`Currency` equality is its ISO 4217 code, and nothing else.** `name`, `symbol`, `number`, `digits`, `obsolete` and `replacedBy` take no part, so two instances describing one currency are one value however they were built — the bundled map, a platform lookup, a decoded payload with a different `name`. The symptoms to expect: a `Set` or `distinct()` over currencies collapses entries you can see are textually different, `usd.copy(symbol = "US$") == usd` is `true`, and a map keyed by `Currency` finds a value under an instance you did not put there. Compare the individual properties where presentation is what you care about. Every `Money` operation defers to this, `equals` and `compareTo` included. Before 4.1.0 `Currency` compared all seven fields, so amounts from different sources added fine and then threw from `sorted()`.

**`Distance` and `Speed` reject negative results instead of clamping them.** `Distance(3.0) - Distance(5.0)` throws; it does not return zero. The same applies to negative scalars in `Speed.times` and `Speed.div`, and to a negative divisor on `Distance`. Guard the call, or compare first, when operands may arrive in either order. A zero distance or speed is perfectly valid — only going below zero is refused.

**`Locale.current` and `Currency.current` throw when the platform cannot say.** Both are `requireNotNull` over a platform lookup, so they raise `IllegalArgumentException` rather than defaulting to English or USD. That makes them a poor default argument, a poor property initialiser and a poor thing to read during composition. Use `Locale.currentOrNull` and `Currency.currentOrNull` where a sensible fallback exists, and keep the throwing form where none does — failing loudly beats silently rendering someone else's language.

**`UnitOfMeasure.findFirst` returns `null` for an ambiguous symbol** rather than guessing by declaration order. `findFirst("gal")` is `null`, because `Gallon` and `GallonImperial` both claim it and picking one silently is a twenty-percent error. The symptom is a `null` that reads as "unknown unit" when the truth is "more than one unit". Use `findAll(symbol)`, which returns every candidate — empty for unknown, more than one for ambiguous, so the two cases stay distinguishable. **Symbol lookup is case-sensitive**, which is how `GB` (gigabyte) and `Gb` (gigabit) name different units — so never lowercase a symbol before looking it up, or you get an empty list rather than a match.

**Invalid GeoJSON is rejected on construction *and* on deserialization.** Minimum position counts, positions of at least two elements, and closed linear rings of at least four positions are all enforced, so a payload that used to decode into a structurally invalid value now throws. Winding order is deliberately **not** enforced: RFC 7946 §3.1.6 tells parsers not to reject polygons that break the right-hand rule, so a clockwise exterior ring round-trips unchanged. Use `GeoPolygon.windingOf()` to inspect it and `rewound()` to correct it, or the `geoPolygon(...)` factory, which refuses bad winding at construction.

**URI types compare by the equivalence their specification defines, not member by member.** A `Urn`'s r-, q- and f-components take no part in equality, because RFC 8141 §3.1 requires they be ignored. `Uri` and `Url` treat scheme and host as case-insensitive and everything else as case-sensitive, per RFC 3986 §6.2.2.1. A `GeoUri` with no `crs` equals one with `"WGS84"`, per RFC 5870. The symptom is a count that comes out lower than expected: `distinct()`, a `Set` or a map key collapses values you can see are textually different, and two URNs differing only after `?+` or `#` become one entry. The value you supplied is preserved and serialized unchanged — only the comparison is normalized.

**`localizedDisplayName` falls back to English, silently.** Names come from the platform's own CLDR where it has them, and from a bundled table where it does not — including on Linux, which has no system CLDR and answers entirely from the bundled matrix. When neither can supply one the English name comes back with nothing marking it, so **the symptom is an English word appearing mid-sentence in an otherwise translated UI**, which looks like a missing translation in your own resources rather than in the platform's. Use **`localizedDisplayNameOrNull`** where showing the wrong language is worse than showing nothing: it returns `null` instead of English, so the choice is yours.

**Never depend on the exact name.** Platforms disagree about wording on a large share of the names they all know — a browser and a native platform differ on roughly 40% — so `nl-BE` may be "Nederlands (België)" or "Vlaams" depending on where your code runs. Compare locales, never their rendered names, and keep them out of cache keys and snapshot tests.

**A `LazyMap` value function must be pure.** The cache is an immutable map behind an atomic reference, and under contention a value function may run more than once for the same key — the losing caller discards its own result and returns the winner's. Every caller observes the same value, so the symptom is never a wrong lookup: it is the side effect happening twice, a duplicate log line, a counter that over-counts, or work done and thrown away.

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

**Changed shape in 4.1.0**, breaking source compatibility. `Outcome` is aligned with `kotlin.Result`. Read this before the rename list, because **everything else here fails to compile and this one does not**:

- **A failure callback's `it.message` was a non-null `String` and is now the nullable `Throwable.message`.** The callbacks handed to `onFailure`, `fold`, `recover` and `getOrElse` now receive the `Throwable` itself rather than the `Outcome.Failure` wrapper, and `Outcome.Failure.message` — which still exists, and is still non-null — is no longer what `it` refers to. Inside a string template the old code keeps compiling and silently renders the text `null` for an exception carrying no message. `it.message ?: it.toString()` is the non-null equivalent, and is exactly what `Outcome.Failure.message` does. This is the entire silent-breakage surface of the change, and a list of renamed methods does not show it.

Then the renames, all of which the compiler finds, because the old names cease to exist rather than changing meaning:

- **`Outcome`'s accessors are renamed to `kotlin.Result`'s.** `dataOrNull` is now `getOrNull`, `dataOrThrow` is `getOrThrow`, and `dataOrElse` is `getOrElse`. There are no deprecated aliases — the old names are gone. `Success.data` is unchanged and is still `data`, not `value`.
- **A callback body reading `it.exception` becomes just `it`.** A read of `.exception` on an `Outcome.Failure` you reached by a type check or smart cast needs no change at all: `Failure.exception` is untouched. Only the callback parameter changed.
- **`Outcome.isSuccess`, `Outcome.isFailure`, `exceptionOrNull()` and `getOrDefault(default)`** are new, and match `kotlin.Result`.

**Grep your doc comments, not only your source.** A KDoc sample using the old API compiles nowhere, so nothing flags it, and it is the first thing the next reader copies. The first consumer to migrate found ten such samples against four genuine source breakages of the same shape.

**Changed behaviour in 4.1.0**, with no signature change:

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

**Kotlin is the consumer surface, on every target.** Kotlin callers need nothing beyond this file, whether they are on the JVM, Android, Kotlin/JS, Kotlin/Wasm, a native Apple target or Linux.

**Swift callers need one more page,** because the export renames and reshapes things, and because nothing here is annotated `@Throws` — a Kotlin exception reaching the Objective-C boundary terminates the process rather than becoming a Swift error, so the non-throwing forms are the only ones a Swift caller can recover from:

- [Calling it from Swift](references/swift.md)

**There is no hand-written-JavaScript surface.** No declaration carries `@JsExport`, so nothing in this library is reachable from plain JavaScript or TypeScript by name, and the generated type definitions do not describe it. The `js` and `wasmJs` targets are for **Kotlin** code compiled to those platforms, which sees the ordinary Kotlin API. That is also the sense in which `Outcome` helps a browser: Kotlin/JS code reads the sealed hierarchy, where a `kotlin.Result` would arrive as an opaque box.
