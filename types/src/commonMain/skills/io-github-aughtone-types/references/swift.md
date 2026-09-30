# Calling it from Swift

What the Kotlin API looks like after export, where it differs from the Kotlin form, and which differences are silent. Everything here was read from the generated Objective-C header rather than inferred.

Measured against: Kotlin 2.4.20, `iosSimulatorArm64` debug framework, 2026-09-29.

## The framework and the prefix

The framework is **`AOTypesKit`**. It was `AughtoneTypesKit` before 3.3.0, so older code and older training data both carry the wrong name.

Frameworks in this family follow the pattern `AO<Name>Kit`, so a sibling library's framework is named the same way rather than spelled out in full. Check the library you are actually importing rather than inferring its name from this one.

Every exported type carries an **`AOTK`** prefix: `AOTKMoney`, `AOTKCurrency`, `AOTKLocale`, `AOTKBigDecimal`, `AOTKUnitOfMeasure`, `AOTKGeoUri`, `AOTKUrl`, `AOTKOutcome`. In Swift the prefix is usually elided by the `swift_name` attributes, so you write `Money` and `Outcome`, but you will see `AOTK` in error messages and in any Objective-C you write.

## Top-level functions live in a class named after their file

Kotlin top-level functions have no home in Objective-C, so each file gets a synthetic class. The name is the file name plus `Kt`, which is not discoverable from the Kotlin source you were reading:

| Kotlin | Swift |
|---|---|
| `url(...)`, `urlOrNull(...)`, `uri(...)`, `geoUri(...)` | `UriParsingKt.url(value:)` and so on |
| `runOutcome { ... }` | `OutcomeKt.runOutcome(block:)` |
| `geoPolygon(...)`, `geoPolygonRewinding(...)` | `GeoValidationKt` |
| `urn(...)`, `urnOrNull(...)` | `UrnKt` |

`DistanceKt`, `SpeedKt`, `AltitudeKt`, `AzimuthKt` and `Money_extensionsKt` hold the extension functions on those types. Note the underscore in `Money_extensionsKt`: it comes from the file name, not from any convention.

## Default arguments do not survive the export

This is the difference most likely to produce code that does not compile, because the Kotlin you are copying from leaves the arguments out.

```kotlin
// Kotlin — both parameters have defaults
Money()
Coordinates(latitude = 45.42, longitude = -75.69)
locale.localizedDisplayName()
```

```swift
// Swift — every parameter is required
Money(value: someBigDecimal, currency: someCurrency)
Coordinates(latitude: 45.42, longitude: -75.69, accuracy: nil)
locale.localizedDisplayName(displayIn: Locale.Companion.shared.current)
```

There is no overload with fewer parameters. Supply `nil` for the optional ones.

Note what the last line costs you: `Locale.Companion.shared.current` **throws** when the platform cannot resolve a locale, and in Swift you are forced to write it explicitly wherever Kotlin would have defaulted it. `Locale.Companion.shared.currentOrNull` returns `nil` instead, which is usually what you want at a call site you did not choose to make.

## Overloads that differ only by number type gain a trailing underscore

`Money.div` takes a `Double` in one overload and a `Long` in another. Objective-C cannot express that, so the second is renamed:

```swift
money.div(scalar: 2.0)    // the Double overload
money.div(scalar_: 2)     // the Long overload — note the underscore
```

The underscore is the only thing distinguishing them, and picking the wrong one changes which arithmetic runs. The same pattern appears anywhere else the library overloads on numeric type.

Operators are methods rather than operators: `plus(other:)`, `minus(other:)`, `times(other:)`, `div(other:)`. `Money.div(other:)` returns a `Double` ratio, while `div(scalar:)` returns a `Money`.

## `Outcome` is not a Swift enum, so there is no exhaustive switch

The sealed class exports as three separate classes — `Outcome`, `OutcomeSuccess` and `OutcomeFailure` — and Swift cannot see that the set is closed. Test with `is` or `as?`, and write a default branch the compiler will not write for you:

```swift
let result = OutcomeKt.runOutcome { try? something() }
if let success = result as? OutcomeSuccess { use(success.data) }
else if let failure = result as? OutcomeFailure { report(failure.message) }
```

Adding a case to the sealed class in a future version will not produce a Swift compile error anywhere. That is the reason `Outcome` exists rather than `kotlin.Result` — a `Result` cannot cross into Swift as data at all — but it does not make the check exhaustive.

The whole API arrives on the class, including the parts that are Kotlin extensions — `getOrElse(onFailure:)`, `getOrDefault(defaultValue:)` and `recover(transform:)` export as an Objective-C category, so they read as ordinary methods and need no `OutcomeKt`. Only `runOutcome(block:)` lives on `OutcomeKt`, being a top-level function.

`isSuccess` and `isFailure` are properties; `getOrNull()`, `getOrThrow()` and `exceptionOrNull()` are methods, with the parentheses Kotlin lets you omit. Every failure callback is handed a **`KotlinThrowable`**, not a Swift `Error`: `outcome.onFailure { throwable in … }`, `outcome.fold(onSuccess:onFailure:)`, `getOrElse(onFailure:)` and `recover(transform:)` all take one. Read `throwable.message`, which is optional, or branch to `OutcomeFailure` for the non-optional `message`.

## Nullable and generic numbers arrive boxed

A nullable or generic numeric becomes a boxed class: `accuracy` on `Coordinates` and `Distance` is `AOTKFloat?`, not `Float?`, and a `List<Double>` becomes `[AOTKDouble]`. Unbox before arithmetic.

`GeoPoint` has one initializer taking a raw `KotlinDoubleArray` and another taking `[AOTKDouble]` with a `bbox`; they are different initializers, not one with a default.

## Nothing in this library is `throws`, and an exception crashes the app

This is the most consequential difference on the page, and it is invisible: **no declaration in this library is annotated `@Throws`, so nothing exports as Swift `throws` and nothing gains an `NSError` out-parameter.** Read from the generated header: zero members carry `error:(NSError **)error`, and `getOrThrow` arrives as plain `- (T _Nullable)getOrThrow`.

Kotlin/Native converts a Kotlin exception into a Swift error *only* for a declaration marked `@Throws`. Without it, an exception reaching the Objective-C boundary is an unhandled Kotlin exception and the runtime terminates the process. There is no `catch` to write: `try?` will not compile against a non-throwing method, and `do/catch` cannot save you.

Plenty of this library throws. `Outcome.getOrThrow()` on a failure; `Locale.Companion.shared.current` and `Currency.Companion.shared.current` when the platform cannot resolve one; `Money.compareTo`, `plus`, `minus`, `times` and `div` across two currencies; `Distance` and `Speed` operators that would go below zero; GeoJSON construction that breaks RFC 7946's structural rules; and `url`, `uri`, `geoUri` and `urn` on malformed input.

So on the Swift side, the non-throwing forms are not a convenience — they are the only callable ones:

```swift
// Crashes the app on bad input. No compiler warning, no catch.
let site = UriParsingKt.url(value: userInput)

// The two forms that cannot crash.
let maybe = UriParsingKt.urlOrNull(value: userInput)          // nil
let result = OutcomeKt.runOutcome { UriParsingKt.url(value: userInput) }
if let failure = result as? OutcomeFailure { report(failure.message) }
```

Use the `OrNull` parser, `Locale.Companion.shared.currentOrNull`, `Outcome.getOrNull()` or `getOrDefault`, and `runOutcome { }` to turn a throwing call into data before it reaches the boundary. Check the currencies match before comparing or adding two `Money` values, and the operand order before subtracting a `Distance`, because there is no recovery once the call is made.

## The coordinate-order trap applies here too

`GeoPoint.init(longitude:latitude:)` and `Coordinates.init(latitude:longitude:)` take their arguments in opposite orders, exactly as in Kotlin. Swift's argument labels make this visible at the call site, which Kotlin's positional form does not — so the labels are worth reading rather than autocompleting.

## If you received this as an XCFramework

You are reading this file, so you have the sources jar. A Swift developer given only a binary XCFramework never receives it, and the only guidance that reaches them is the doc comment Kotlin writes into the framework header.

The two traps that exist nowhere else — the trailing-underscore overloads on `Money`, and the opposing argument orders of `GeoPoint` and `Coordinates` — are now in those doc comments as well, so they reach a binary consumer. The rest of this file is about the export itself and has no declaration to hang on.
