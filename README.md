# Aughtone Types

> [!IMPORTANT]
> **v2.0.0 Breaking Change**: All enum constants in `MetricPrefix` and `UnitOfMeasure` have been renamed from `UPPER_SNAKE_CASE` to `PascalCase` (CamelCase) to improve readability and consistency across the ecosystem.

This project follows a specialized 5-sector documentation hierarchy.

## 📚 Documentation Sectors
- 📐 [Architecture](docs/ARCH.md): Engineering rules and design patterns.
- 🧠 [Functional Specifications](docs/SPEC.md): Business logic and domain constraints.
- 🎨 [Design & UI](docs/DESIGN.md): Presentation layer and user stories.
- 📋 [Acceptance Criteria](docs/ACs/README.md): Success outcomes and verification.
- 📖 [Developer Guide](docs/DEVELOPER.md): Environment setup and onboarding.

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

### 🔢 Precision Math & GeoJSON
- **Rounding**: `BankersValue(1.255).round(2)` -> `1.26`.
- **GeoJSON**: `Point(45.5, -122.6).toGeoJson()` (**RFC 7946**).

---
## 🛠️ Governance Standards
Access the [Governance Skills](docs/standards/) for specialized development rules.

---
## 🤖 AI-Assisted Development
This library includes embedded, machine-readable "skills" to enhance the experience of developers using AI code assistants. These skills help the AI understand our library's APIs and best practices, leading to more accurate and idiomatic code suggestions.

- **AI Skill Discovery**: Look for `META-INF/ai-skills/*.ai-skill.md`

To learn more about this pattern and how to adopt it for your own libraries, please see the [AI Skill Publishing Standard](docs/standards/ai-skill-publishing.md).
