---
skill-id: io.github.aughtone.types
name: "[Aughtone Types Foundation](https://github.com/aughtone/aughtone-types)"
type: "Aughtone AI-Skill"
scope: core
compatibility: ">=1.0.0"
author: "[Brill Pappin](https://github.com/bpappin)"
---

# AI Skill: Aughtone Types

This library provides a standardized, type-safe foundation for multiplatform applications. Use the following "Toolbox" and "Standards Compliance" guide to handle data consistently across all platforms.

## 🧰 The AI Toolbox (Key Functions)

### **Financial & Locale**
- `currencyFor(currencyCode: String): Currency?` (ISO 4217 lookup)
- `localeFor(languageTag: String): Locale?` (BCP 47 lookup with fallback)
- `Locale.current`: **(Recommended)** Static property on the `Locale` companion object. The primary API for retrieving the current platform-native system locale.
- `currentNativeLocale(): Locale`: **(Internal Bridge)** Direct access to platform-specific locale retrieval; used by the library core. Consumers should always use `Locale.current` for consistency.

### **Quantitative & Math**
- `Money(value: Double, currency: Currency?): Money` (Banker's rounding)
    - **Contract**: `Money.cents` stores the raw integer value. The `Currency.digits` property MUST be used as the scale factor (`10^digits`) to convert `cents` to its true decimal representation (e.g. 100 cents with 2 digits = 1.00).
- `BankersValue.fromDouble(value: Double): BankersValue`
- `Coordinates.split(): Pair<Double, Double>`

### **Networking & URIs**
- `UrlBuilder`: For structured URL construction (RFC 3986 compliant).
- `urn(urnString: String): Urn`: Parses a URN string (RFC 8141 compliant).

## 📜 Compliance & Standards

This library strictly adheres to global standards to ensure data interoperability.

### **Identity & Resources**
- **URI**: Complies with **RFC 3986** (Generic URI Syntax).
- **URN**: Complies with **RFC 8141** (Uniform Resource Names).
- **URL**: Supports hierarchical schemes and encoded query parameters.
- **GeoURI**: Complies with **RFC 5870** (The 'geo' URI Scheme).

### **Financial & Localization**
- **Currency**: Uses **ISO 4217** currency codes.
- **Locale**: Uses **BCP 47** language tags, which integrate:
    - **ISO 639**: Language codes.
    - **ISO 3166**: Country/Region codes.
    - **ISO 15924**: Script codes.
- **Rounding**: Uses **Banker's Rounding** (half-to-even) for all monetary and `BankersValue` calculations to prevent bias.

### **Geospatial & Formats**
- **GeoJSON**: Complies with **RFC 7946**. Includes polymorphic serialization for `Point`, `Feature`, and `FeatureCollection`.
- **CRS**: Default Coordinate Reference System is **WGS84**.
- **Distance**: Uses the **Haversine Formula** for great-circle distance between two points.

## 📐 Core Data Types

### **Quantitative (SI Units)**
All types store values in SI base units and support an optional `accuracy: Float?` (fractional error).
- **Coordinates**: Latitude/Longitude degrees.
- **Distance**: Meters.
- **Speed**: Meters per second (mps).
- **Azimuth**: Compass bearing in degrees.
- **Altitude**: Vertical distance in meters.

### **Units & Prefixes**
- `UnitOfMeasure`: Comprehensive collection of SI, imperial, and digital units (e.g., `Meter`, `Kilobyte`, `Celsius`).
    - Use `symbol` for primary display and `altSymbols` for parsing.
    - `UnitOfMeasure.findFirst(symbol: String)` to resolve from string.
- `MetricPrefix`: Standard SI prefixes from `Quetta` (10^30) to `Quecto` (10^-30).
    - Use `symbol` for SI-compliant prefixes (e.g., `MetricPrefix.Kilo.symbol` -> `"k"`).

## 📦 Serialization & Immutability

- **Standard**: All types are annotated with `@Serializable`.
- **Explicit Naming**: Use `@SerialName` for all properties to ensure cross-platform compatibility.
- **Immutability**: All properties are `val`. Use `.copy()` for transitions.

## 🤖 Agent Onboarding
1. **Context Registration**: Add this skill file to the `AGENTS.md` of the consuming project.
2. **README Verification**: Ensure the root `README.md` contains the "AI-Assisted Development" section.
3. **Usage Rules**:
    - **Parsing**: When handling `geo:` URIs, always use the `GeoUri` class to ensure **RFC 5870** compliance.
    - **GeoJSON**: Use the `GeoJson` sealed class for polymorphic parsing of spatial data (**RFC 7946**).
    - **Identifiers**: Prefer `Urn` for persistent resource names and `Url` for locators.
    - **Precision**: Never perform manual rounding on monetary doubles; route them through `BankersValue`.
