---
skill-id: io.github.aughtone.types
spec-version: "1.0"
type: "Aughtone AI-Skill"
scope: core
compatibility: ">=1.0.0"
---

# AI Skill: Aughtone Types

This library provides a standardized, type-safe foundation for multiplatform applications. Use the following "Toolbox" and "Standards Compliance" guide to handle data consistently across all platforms.

## 🧰 The AI Toolbox (API Index & Usage Guide)

**Core Philosophy**: Standard Kotlin library primitives are always preferred. This library exists strictly to fill multiplatform gaps (e.g., cross-platform `Locale`, `Currency`, or specific SI units). It is perfectly acceptable to mix library-provided types with standard primitives where it makes logical sense for the architecture. 

### **Financial & Locale**
*   **`Locale`**: A platform-independent locale representation.
    *   **Preference**: When working in `commonMain` with this library directly imported, prefer `io.github.aughtone.types.locale.Locale` over platform-specific or framework-specific (e.g. Compose) types to ensure cross-platform consistency.
    *   `Locale.current`: Primary API to get the platform's native system locale.
    *   `localeFor(languageTag: String)`: Strict lookup of a BCP 47 tag in the internal resource map.
    *   `resolveLocale(languageTag: String)`: Lookup with fallback (e.g., "en-US" -> "en").
    *   `parseLocale(languageTag: String)`: Parses a tag into a `Locale`, creating a new instance if not found in the map.
    *   `availableLocales()`: Get a list of all supported locales.
    *   `localesByName(name: String)`: Filter supported locales by their display name.
    *   `Locale.languageTag`: Property that provides the BCP 47 tag for a `Locale`.
    *   `Locale.toLanguageTag()`: (Deprecated) Use `Locale.languageTag` property instead.
*   **`Currency`**: Represents ISO 4217 currencies.
    *   `currencyFor(currencyCode: String)`: Lookup a currency by its 3-letter ISO code.
    *   `currencyFor(locale: Locale)`: Resolve the default currency for a given locale.
    *   `availableCurrencies()`: Get a list of all supported currencies.
    *   `currenciesByName(name: String)`: Filter supported currencies by their display name.
*   **`Money`**: Stores monetary values.
    *   **Contract**: `Money.cents` stores the raw integer value. The `Currency.digits` property MUST be used as the scale factor (`10^digits`) to convert `cents` to its true decimal representation (e.g. 100 cents with 2 digits = 1.00).
    *   `Double.toMoney(Currency?)`: Safely convert a double to `Money`.
    *   `Money.toDouble()`: Convert `Money` back to a Double.
*   **`BankersValue`**: Use this for all manual rounding of financial `Double` values. It implements Banker's Rounding (half-to-even) to prevent bias.

### **Geospatial & GeoJSON**
*   **`Telemetry`**: Stores geographical telemetry data, including positioning (`Coordinates`) and optional motion metrics (`altitude`, `speed`, `azimuth`, and `timestamp`) using the library's SI data types.
*   **`Coordinates`**: A basic Lat/Lon pair. Use `Coordinates.add(lat, lon)` or `Coordinates.split()`.
*   **`GeoJson`**: Use this sealed class for polymorphic parsing and serialization of spatial data (**RFC 7946**). Subtypes include: `Geometry`, `Point`, `MultiPoint`, `LineString`, `MultiLineString`, `Polygon`, `MultiPolygon`, `GeometryCollection`, `Feature`, `FeatureCollection`.

### **Networking & Identifiers (RFC Compliant)**
*   **`Uri` & `Url`**: Use these data classes for type-safe URI manipulation instead of plain strings.
*   **`UrlBuilder`**: Use for structured, RFC 3986 compliant URL construction.
*   **`UrlEncoder`**: Use for safe URL encoding.
*   **`Urn`**: Represents a Uniform Resource Name (RFC 8141). Use the `urn(urnString: String)` function to parse strings into persistent resource names.
*   **`GeoUri`**: Represents a `geo:` URI (RFC 5870). ALWAYS use this class when handling geographic URIs to ensure compliance.

### **Quantitative (SI Units)**
All types store values in SI base units and support an optional `accuracy: Float?` (fractional error).
*   **`Distance`**: Represents distance in meters.
*   **`Speed`**: Represents speed in meters per second (mps).
*   **`Azimuth`**: Represents compass bearing in degrees.
*   **`Altitude`**: Represents vertical distance in meters.

### **Units & Utilities**
*   **`UnitOfMeasure`**: Comprehensive collection of SI, imperial, and digital units (e.g., `Meter`, `Kilobyte`, `Celsius`). Use `UnitOfMeasure.findFirst(symbol: String)` to parse from strings.
*   **`MetricPrefix`**: Standard SI prefixes from `Quetta` (10^30) to `Quecto` (10^-30).
*   **`BitSet`**: Use `bitSet()` or `emptyBitSet()` for memory-efficient boolean arrays.
*   **`LazyMap`**: A map that lazily evaluates and caches its values.

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
