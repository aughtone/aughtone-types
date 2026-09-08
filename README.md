# Aughtone Types

A Kotlin Multiplatform library of strongly-typed, shareable data types — money and currency, locales, coordinates and telemetry, GeoJSON geometry, RFC-compliant identifiers, and arbitrary-precision math. It exists because KMP projects kept redefining the same types incompatibly; these are the shared ones, with no platform-specific API leaking through.

Targets: Android, JVM, iOS, JS, Wasm and Linux.

## 📥 Installation

Published to Maven Central as `io.github.aughtone:types`.

```kotlin
// build.gradle.kts
implementation("io.github.aughtone:types:3.4.0")
```

Or through a version catalog:

```toml
# gradle/libs.versions.toml
[versions]
aughtone-types = "3.4.0"

[libraries]
aughtone-types = { module = "io.github.aughtone:types", version.ref = "aughtone-types" }
```

```kotlin
// build.gradle.kts
implementation(libs.aughtone.types)
```

> [!IMPORTANT]
> **v3.4.0 `Outcome.Error` renamed**: The failure case of `Outcome` is now `Outcome.Failure`, and the factory is `Outcome.failure(...)`. The old names still compile as deprecated aliases and will be removed in 4.0.0. `Outcome$Error` no longer exists as a class, so upgrading from 3.3.0 needs a clean and rebuild rather than a code change.
>
> **v3.3.0 Locale Display Names**: Eleven locale `displayName` values are corrected — renamed countries (`Czechia`, `North Macedonia`, `Türkiye`), incomplete or abbreviated country names, and dated language exonyms (`Farsi` → `Persian`, `Azeri` → `Azerbaijani`). No API changed, but snapshot tests and cached UI strings holding the old names will need updating. See the [changelog](CHANGELOG.md) for the full table.
>
> **v3.2.0 Behavioral Fixes**: Still worth reading if you are coming from 3.1.x — that release corrected long-standing bugs whose output or validation changes for existing code. `UrlEncoder.encode` is now true RFC 3986 percent-encoding — use `encodeFormData` for the previous `application/x-www-form-urlencoded` behavior. `Url`/`Uri`/`Urn`/`GeoUri` string output is now well-formed, and `Urn`/`GeoUri` construction now rejects invalid input. Critical arbitrary-precision fixes also land in `BigInteger`/`BigDecimal` division and `BankersValue`. See the [changelog](CHANGELOG.md) for the full list.
>
> **v3.0.0 Breaking Change**: All GeoJSON geometry types (e.g., `Point`, `Polygon`) have been renamed with a **`Geo` prefix** (e.g., `GeoPoint`, `GeoPolygon`). `Money` now uses `BigDecimal` for its internal value to support sub-minor units, and `Telemetry` has moved to the `quantitative` package.

## 📚 Documentation
- 📖 [Developer Guide](docs/knowledge/guides/developer-guide.md): Building, testing, publishing.
- ⚖️ [Architecture Decision Records](docs/knowledge/decisions/): Why the library is shaped this way, and what was rejected.
- 📜 [Changelog](CHANGELOG.md): History of changes and release notes.
- 🗺️ [How the docs work](docs/README.md): The whole system — knowledge in `docs/knowledge/`, work in [Issues](https://github.com/aughtone/aughtone-types/issues).

## 📦 Core Data Types

| Category | Type | Standard / Compliance | Description |
| :--- | :--- | :--- | :--- |
| **Financial** | `Money` | Banker's Rounding | Arbitrary-precision monetary values with `BigDecimal` storage. |
| | `Currency` | **ISO 4217** | Global currency definitions with scale factors. |
| **Localization** | `Locale` | **BCP 47** | Universal language, region, and script identifiers. |
| **Quantitative** | `Coordinates` | WGS84 | Geodetic latitude and longitude degrees. |
| | `Distance` | SI (Meters) | Linear distance with accuracy support. |
| | `Speed` | SI (mps) | Rate of motion in meters per second. |
| | `Altitude` | SI (Meters) | Vertical distance above/below reference. |
| | `Azimuth` | Degrees | Compass bearing (0-360°). |
| | `Telemetry` | Unified Domain | Comprehensive model with coordinates, azimuth, speed, and altitude. |
| **Geospatial** | `GeoJson` | **RFC 7946** | `GeoPoint`, `GeoFeature`, and `GeoFeatureCollection` models. |
| **SI Units** | `UnitOfMeasure` | SI / Imperial | Definitions for meters, liters, bytes, etc. |
| | `MetricPrefix` | SI Prefixes | Scaling factors from `Quetta` to `Quecto`. |
| **Identifiers** | `Url` | **RFC 3986** | Uniform Resource Locators (Web). |
| | `Urn` | **RFC 8141** | Uniform Resource Names (Persistent IDs). |
| | `GeoUri` | **RFC 5870** | Geographic 'geo' URI scheme. |
| **Mathematics** | `BigInteger` | Pure Kotlin | Arbitrary-precision integer math support. |
| | `BigDecimal` | Pure Kotlin | Arbitrary-precision decimal math with rounding support. |
| **Utilities** | `BitSet` | Multiplatform | Space-efficient storage for bit-level flags. |
| | `BankersValue` | Half-to-Even | Precision math with bias-free rounding rules. |
| **Control Flow** | `Outcome` | Sealed (KMP-safe) | Success-or-failure result that survives the Swift/JS boundary, unlike `kotlin.Result`. |

## 🚀 Quick Usage

### 🌎 Domain Fundamentals
- **Locales**: `Locale.current` or `localeFor("fr-CH")`.
- **Money**: `Money(12.50, Currency.Usd)` or `Money(BigDecimal("1.23456"), Currency.Eur)`.
- **Telemetry**: `Telemetry(coords, speed = 2.5.mps, azimuth = 90.degrees)`.

### 📏 Quantitative & SI
- **Distance**: `100.meters` or `5.kilometers`.
- **Units**: `UnitOfMeasure.Litre.symbol` ("L"), `MetricPrefix.Kilo`.

### 🔗 RFC Compliant Identifiers
- **Identifiers**: `Url("https://pkg.dev")`, `Urn("urn:uuid:...")`.
- **Geo**: `GeoUri(45.5, -122.6)` (**RFC 5870**).

### 🔢 Precision Math
- **Arbitrary Precision**: `BigInteger("999999999999999999999999")` or `BigDecimal("123.456")`.
- **Rounding**: `BigDecimal("1.255").setScale(2, RoundingMode.HALF_EVEN)` -> `1.26`.

### ✅ Success or Failure
- **Outcome**: `runOutcome { parse(input) }` returns `Outcome.Success` or `Outcome.Failure`; `when` over the two, or use `fold`, `map`, `recover`, `dataOrElse`.
- Unlike `kotlin.Result` it is a sealed class, so Swift and JavaScript callers can read the failure as data. See [ADR-0004](docs/knowledge/decisions/outcome-over-kotlin-result.md).

### 🗺️ GeoJSON
- **GeoJSON**: `GeoPoint(45.5, -122.6, 100.0).toGeoJson()` (**RFC 7946**).

---
## 🌐 Localized Display Names

`Locale.displayName` from the bundled resource data is always **English**. To show a locale's name in the end user's own language:

```kotlin
localeFor("bn")?.localizedDisplayName() // "bengali" for a French user, "ベンガル語" for a Japanese user
```

Rather than bundling a full translation matrix (~90 × 90 names) into every app, this delegates to the CLDR data each platform already ships — `java.util.Locale` (JVM/Android), `NSLocale` (Apple), `Intl.DisplayNames` (JS/Wasm). Bundled resource files aren't viable everywhere: browsers can't read files synchronously, and klibs can't deliver resources into an iOS app bundle.

**Tradeoffs to be aware of:**
- Names come from the OS, so wording may differ slightly between platforms and OS versions (e.g. "Chinese (Simplified)" vs "Simplified Chinese").
- Linux targets have no system CLDR data — they always return the English fallback.
- Browsers need `Intl.DisplayNames` (widely available since ~2020); older environments fall back to English.
- The function never returns `null` — the worst case is the English `displayName`.

See [ADR-0003](docs/knowledge/decisions/localized-display-names-via-platform-cldr.md) for the full rationale, and issue [#20](https://github.com/aughtone/aughtone-types/issues/20) for the deferred bundled-tables alternative.

---
## 🧪 Verification & Parity

To ensure mathematical precision and behavior consistency, this library employs rigorous **Differential Parity Testing** against standard baseline libraries:

- **JVM Baseline**: Parity verified against standard JDK types (`java.math.BigInteger` and `java.math.BigDecimal`).
- **KMP Baseline**: Parity verified against the official Multiplatform [Ionspin BigNum](https://github.com/ionspin/kotlin-multiplatform-bignum) library.

