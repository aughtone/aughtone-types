# Aughtone Types

> [!IMPORTANT]
> **v2.0.0 Breaking Change**: All enum constants in `MetricPrefix` and `UnitOfMeasure` have been renamed from `UPPER_SNAKE_CASE` to `PascalCase` (CamelCase) to improve readability and consistency across the ecosystem.

This project follows a specialized 5-sector documentation hierarchy.

## 📚 Documentation Sectors
- 📐 [Architecture Guidelines](docs/mandates/ARCH.md): Engineering rules and design patterns.
- 🧠 [Functional Specifications](docs/mandates/SPEC.md): Business logic and domain constraints.
- 🎨 [Design & UI](DESIGN.md): Presentation layer and user stories.
- 📋 [Acceptance Criteria](docs/ac/README.md): Success outcomes and verification.
- 📖 [Developer Guide](docs/guides/DEVELOPER.md): Environment setup and onboarding.
- 📜 [Changelog](CHANGELOG.md): History of changes and release notes.

## 📦 Core Data Types

| Category | Type | Standard / Compliance | Description |
| :--- | :--- | :--- | :--- |
| **Financial** | `Money` | Banker's Rounding | Type-safe monetary values with raw integer `cents`. |
| | `Currency` | **ISO 4217** | Global currency definitions with scale factors. |
| **Localization** | `Locale` | **BCP 47** | Universal language, region, and script identifiers. |
| **Quantitative** | `Coordinates` | WGS84 | Geodetic latitude and longitude degrees. |
| | `Distance` | SI (Meters) | Linear distance with accuracy support. |
| | `Speed` | SI (mps) | Rate of motion in meters per second. |
| | `Altitude` | SI (Meters) | Vertical distance above/below reference. |
| | `Azimuth` | Degrees | Compass bearing (0-360°). |
| **Geospatial** | `Location` | Unified Domain | Comprehensive model with coordinates, azimuth, speed, and altitude. |
| | `GeoJson` | **RFC 7946** | `Point`, `Feature`, and `FeatureCollection` models. |
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
- **Money**: `Money(1250, Currency.Usd)` or `12.50.toMoney(Currency.Eur)`.
- **Navigation**: `Location(coords, speed = 2.5.mps, azimuth = 90.degrees)`.

### 📏 Quantitative & SI
- **Distance**: `100.meters` or `5.kilometers`.
- **Units**: `UnitOfMeasure.Litre.symbol` ("L"), `MetricPrefix.Kilo`.

### 🔗 RFC Compliant Identifiers
- **Identifiers**: `Url("https://pkg.dev")`, `Urn("urn:uuid:...")`.
- **Geo**: `GeoUri(45.5, -122.6)` (**RFC 5870**).

### 🔢 Precision Math
- **Arbitrary Precision**: `BigInteger("999999999999999999999999")` or `BigDecimal("123.456")`.
- **Rounding**: `BankersValue(1.255).round(2)` -> `1.26`.

### 🗺️ GeoJSON
- **GeoJSON**: `Point(45.5, -122.6).toGeoJson()` (**RFC 7946**).

---
## 🧪 Verification & Parity

To ensure mathematical precision and behavior consistency, this library employs rigorous **Differential Parity Testing** against standard baseline libraries:

- **JVM Baseline**: Parity verified against standard JDK types (`java.math.BigInteger` and `java.math.BigDecimal`).
- **KMP Baseline**: Parity verified against the official Multiplatform [Ionspin BigNum](https://github.com/ionspin/kotlin-multiplatform-bignum) library (`com.ionspin.kotlin:bignum`).

### Parity Test Status

| Target / Operation Area | Aughtone Types | Java (JDK) | Ionspin BigNum | Status / Notes |
| :--- | :---: | :---: | :---: | :--- |
| **BigInteger Arithmetic** | ✅ Pass | ✅ Pass | ⚠️ Partial | Ionspin `%` (remainder) and `divrem` sign bugs require skipping negative dividend/divisor assertions in tests. |
| **BigInteger Modulo** | ✅ Pass | ✅ Pass | ✅ Pass | Both match on positive divisors (Modulo requires positive divisor). |
| **BigInteger Shifts** | ✅ Pass | ✅ Pass | ⚠️ Partial | Ionspin `shl` / `shr` do not support negative shift counts (throws `IllegalArgumentException`); mapped natively in tests. |
| **BigInteger Bitwise** | ✅ Pass | ✅ Pass | ❌ Skipped | Ionspin crashes with a sign-constructor bug when result magnitude is zero (e.g. `x.not()` or bit toggling). |
| **BigDecimal Arithmetic** | ✅ Pass | ✅ Pass | ✅ Pass | Matches on addition, subtraction, and multiplication. |
| **BigDecimal Division (Exact)** | ✅ Pass | ✅ Pass | ✅ Pass | Matches on exact fractions (e.g. `1/2`, `1/5`, `10/8`). |
| **BigDecimal Division (Rounded)**| ✅ Pass | ✅ Pass | ✅ Pass | Verified across multiple scales (0, 1, 2, 5) and rounding modes. |
| **BigDecimal Scaling** | ✅ Pass | ✅ Pass | N/A | Only verified against JDK baseline (`setScale` parity). |
| **BigDecimal Trailing Zeros** | ✅ Pass | ✅ Pass | ✅ Pass | `stripTrailingZeros` verified; matches on canonical numerical value. |


The JVM baseline parity suite runs on the JVM target, while the KMP parity suite runs automatically on all targets (JVM, JS, Wasm, iOS, Linux) during:
```bash
./gradlew check
```

---
## 🛠️ Governance Standards
Access the [Architecture Guidelines](docs/mandates/ARCH.md) for specialized development rules and VM/UDF governance.

---
## 🤖 AI-Assisted Development
This library includes embedded, machine-readable "skills" to help AI assistants understand its APIs and best practices.

- **Discovery**: Look for `META-INF/ai-skills/*.ai-skill.md`

### 🪄 Magic Prompt for AI Assistants
If you are using an AI assistant (like Claude, Gemini, or ChatGPT) to write code with this library, paste this prompt first:

> "Scan all project dependencies for AI Skill files in `META-INF/ai-skills/` with the prefix `io.github.aughtone`. Use these to understand the API patterns, types, and governance for this library. If they are not found in the local classpath, refer to https://github.com/aughtone/aughtone-types for the source definitions."
