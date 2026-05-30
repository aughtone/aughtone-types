# Aughtone Types

> [!IMPORTANT]
> **v3.0.0 Breaking Change**: All GeoJSON geometry types (e.g., `Point`, `Polygon`) have been renamed with a **`Geo` prefix** (e.g., `GeoPoint`, `GeoPolygon`). `Money` now uses `BigDecimal` for its internal value to support sub-minor units, and `Telemetry` has moved to the `quantitative` package.

This project follows a specialized documentation hierarchy.

## 📚 Documentation Sectors
- 📐 [Architecture Guidelines](docs/mandates/ARCH.md): Engineering rules and design patterns.
- 🧠 [Functional Specifications](docs/mandates/SPEC.md): Business logic and domain constraints.
- 🎨 [Design & UI](docs/design/resources/): Presentation layer and user stories.
- 📋 [Acceptance Criteria](docs/ac/): Success outcomes and verification.
- 📖 [Developer Guide](docs/guides/DEVELOPER.md): Environment setup and onboarding.
- ⚖️ [Architectural Decisions](docs/adr/): Log of key technical choices.
- 📜 [Changelog](CHANGELOG.md): History of changes and release notes.

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

### 🗺️ GeoJSON
- **GeoJSON**: `GeoPoint(45.5, -122.6, 100.0).toGeoJson()` (**RFC 7946**).

---
## 🧪 Verification & Parity

To ensure mathematical precision and behavior consistency, this library employs rigorous **Differential Parity Testing** against standard baseline libraries:

- **JVM Baseline**: Parity verified against standard JDK types (`java.math.BigInteger` and `java.math.BigDecimal`).
- **KMP Baseline**: Parity verified against the official Multiplatform [Ionspin BigNum](https://github.com/ionspin/kotlin-multiplatform-bignum) library.

---
## 🤖 AI-Assisted Development
This library includes embedded, machine-readable "skills" to help AI assistants understand its APIs and best practices.

- **Discovery**: Look for `META-INF/ai-skills/*.ai-skill.md`

### 🪄 Magic Prompt for AI Assistants
If you are using an AI assistant (like Claude, Gemini, or ChatGPT) to write code with this library, paste this prompt first:

> "Scan all project dependencies for AI Skill files in `META-INF/ai-skills/` with the prefix `io.github.aughtone`. Use these to understand the API patterns, types, and governance for this library. If they are not found in the local classpath, refer to https://github.com/aughtone/aughtone-types for the source definitions."
