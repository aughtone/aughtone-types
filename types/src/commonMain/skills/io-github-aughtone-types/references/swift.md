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

There is no overload with fewer parameters. Supply `nil` for the optional ones, and the default explicitly where there is one: `locale.localizedDisplayName(displayIn: reader, languageDisplay: .standard)`.

Note what the last line costs you: `Locale.Companion.shared.current` **throws** when the platform cannot resolve a locale, and in Swift you are forced to write it explicitly wherever Kotlin would have defaulted it. `Locale.Companion.shared.currentOrNull` returns `nil` instead, which is usually what you want at a call site you did not choose to make.

## `Locale` and `Currency` have no initializer

Neither type exposes a constructor, so there is no `Locale(...)` or `Currency(...)` initializer in Swift. Kotlin's `Locale("en", "US", displayName = ...)` is a call to the companion's `invoke`, and Swift reaches it through the companion, with every argument supplied:

```swift
let locale = Locale.Companion.shared.invoke(languageCode: "en", regionCode: "US", scriptCode: nil, variantCode: nil, displayName: "English (United States)")
let usd = Currency.Companion.shared.invoke(code: "USD", number: 840, digits: 2, name: "US Dollar", symbol: "$", obsolete: false, replacedBy: nil)
```

Prefer a lookup where one exists, since a shipped locale or currency carries its proper name: `Locale_functionsKt.localeFor(languageTag:)`, `Locale_functionsKt.parseLocale(languageTag:)` and `Currency_functionsKt.currencyFor(currencyCode:)`. Neither type has `doCopy` any more, so build a new value rather than copying one.

## Overloads on number type have their own labels

Objective-C cannot overload on a parameter's type, so where Kotlin does, each form carries a label of its own. The label says what the number means:

```swift
money.plus(scalar: 2.5)        // adds 2.5 currency units
money.plus(minorUnits: 250)    // adds 250 cents
money.times(scalar: 1.5)       // a Double factor
money.times(count: 3)          // a whole-number factor
money.div(count: 3)
Money(amount: 5.0, currency: usd)     // the Double constructor
BigDecimal(string: "1.50")            // and BigDecimal(value:) for a Double, BigDecimal(integer:) for a Long
```

The `Int` and `Long` forms of `toDistance`, `toSpeed`, `toAzimuth` and `toAltitude`, and `BankersValue.div(Int)`, are not exported: convert to `Double`, or to `Int64` for `BankersValue`, and call the remaining form. Before 4.3.0 the extra overloads exported with trailing underscores, `div(scalar_:)`, distinguishable only by that.

Operators are methods rather than operators: `plus(other:)`, `minus(other:)`, `times(other:)`, `div(other:)`. `Money.div(other:)` returns a `Double` ratio, while `div(scalar:)` returns a `Money`.

## `Outcome` is not a Swift enum, so there is no exhaustive switch

The sealed class exports as three separate classes — `Outcome`, `OutcomeSuccess` and `OutcomeFailure` — and Swift cannot see that the set is closed. Test with `is` or `as?`, and write a default branch the compiler will not write for you:

```swift
let result = OutcomeKt.runOutcome { try? something() }
if let success = result as? OutcomeSuccess { use(success.data) }
else if let failure = result as? OutcomeFailure { report(failure.message) }
```

Adding a case to the sealed class in a future version will not produce a Swift compile error anywhere. That is the reason `Outcome` exists rather than `kotlin.Result` — a `Result` cannot cross into Swift as data at all — but it does not make the check exhaustive.

**Exhaustiveness is decided by the framework Swift sees, not by this library.** This library publishes no Swift framework of its own: Swift reaches it through the consuming project's Kotlin framework, so how a sealed class exports is that build's choice. A framework built with Touchlab's SKIE presents sealed classes, `Outcome` included, as Swift enums through `onEnum(of:)`, and a `switch` over that is exhaustive. This library neither requires nor configures SKIE; without it, the `as?` form above is the one that works.

The whole API arrives on the class, including the parts that are Kotlin extensions — `getOrElse(onFailure:)`, `getOrDefault(defaultValue:)` and `recover(transform:)` export as an Objective-C category, so they read as ordinary methods and need no `OutcomeKt`. Only `runOutcome(block:)` lives on `OutcomeKt`, being a top-level function.

`isSuccess` and `isFailure` are properties; `getOrNull()`, `getOrThrow()` and `exceptionOrNull()` are methods, with the parentheses Kotlin lets you omit. Every failure callback is handed a **`KotlinThrowable`**, not a Swift `Error`: `outcome.onFailure { throwable in … }`, `outcome.fold(onSuccess:onFailure:)`, `getOrElse(onFailure:)` and `recover(transform:)` all take one. Read `throwable.message`, which is optional, or branch to `OutcomeFailure` for the non-optional `message`.

## Nullable and generic numbers arrive boxed

A nullable or generic numeric becomes a boxed class: `accuracy` on `Coordinates` and `Distance` is `AOTKFloat?`, not `Float?`, and a `List<Double>` becomes `[AOTKDouble]`. Unbox before arithmetic.

`GeoPoint` has one initializer taking a raw `KotlinDoubleArray` and another taking `[AOTKDouble]` with a `bbox`; they are different initializers, not one with a default.

## Throwing calls are `throws`, with three exceptions that still crash

Every function and constructor here that can fail is annotated `@Throws`, so Swift sees it as `throws` and a failure arrives as an `NSError` you can catch. The Kotlin exception is in its `userInfo` under `KotlinException`.

```swift
do {
    let site = try UriParsingKt.url(value: userInput)
    let total = try price.plus(other: tax)          // throws across two currencies
} catch {
    report(error.localizedDescription)
}
```

That covers the parsers (`url`, `uri`, `geoUri`, `urn`), GeoJSON and quantity construction that breaks its rules, `Money` arithmetic across currencies, operators that would go below zero or divide by zero, `BigDecimal` and `BigInteger` parsing and division, `BitSet` indexing, and `Outcome.getOrThrow()`.

**Three things still terminate the process,** because Objective-C gives them nowhere to report an error:

- **`Locale.Companion.shared.current` and `Currency.Companion.shared.current`** are properties, and a property cannot throw. Use `currentOrNull`.
- **`Money.compareTo`** across two currencies. It implements `Comparable`, and an override cannot add `throws`. Check the currencies match before comparing.
- **`doCopy`** on a type that validates itself — `Coordinates`, `Distance`, `Speed`, `Altitude`, `Azimuth`, the GeoJSON geometries, `GeoFeature`, `Url`, `Urn`, `GeoUri` — when the copy breaks a rule. Build a new value with the throwing constructor instead.

Nothing that is unannotated is expected to throw. If one does, it terminates the process as before, and it is a bug here.

## The coordinate-order trap applies here too

`GeoPoint.init(longitude:latitude:)` and `Coordinates.init(latitude:longitude:)` take their arguments in opposite orders, exactly as in Kotlin. Swift's argument labels make this visible at the call site, which Kotlin's positional form does not — so the labels are worth reading rather than autocompleting.

Better, convert rather than retype: `coordinates.toGeoPoint()` arrives as an ordinary method on `Coordinates`, since the Kotlin extension exports as an Objective-C category, and it does the reordering for you. It drops `accuracy`, which GeoJSON has no member for.

## If you received this as an XCFramework

You are reading this file, so you have the sources jar. A Swift developer given only a binary XCFramework never receives it, and the only guidance that reaches them is the doc comment Kotlin writes into the framework header.

The two traps that exist nowhere else — the trailing-underscore overloads on `Money`, and the opposing argument orders of `GeoPoint` and `Coordinates` — are now in those doc comments as well, so they reach a binary consumer. The rest of this file is about the export itself and has no declaration to hang on.
