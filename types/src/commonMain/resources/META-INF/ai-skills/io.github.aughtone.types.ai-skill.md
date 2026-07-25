---
skill-id: io.github.aughtone.types
spec-version: "1.0"
type: "Library AI-Skill"
scope: core
compatibility: ">=3.0.0"
---

# AI Skill: types

This library provides machine-readable instructions for AI coding assistants to ensure standardized, type-safe development across multiplatform applications.

## 🧰 The AI Toolbox (API Index & Usage Guide)

**Core Philosophy**: Standard Kotlin library primitives are always preferred. This library exists strictly to fill multiplatform gaps (e.g., cross-platform `Locale`, `Currency`, or specific SI units). It is perfectly acceptable to mix library-provided types with standard primitives where it makes logical sense for the architecture. 

### **Financial & Locale**
*   **`Locale`**: A platform-independent locale representation.
    *   **Preference**: When working in `commonMain` with this library directly imported, prefer `io.github.aughtone.types.locale.Locale` over platform-specific or framework-specific (e.g. Compose) types.
    *   `Locale.current`: Primary API to get the platform's native system locale.
    *   `localeFor(languageTag: String)`: Strict lookup of a BCP 47 tag in the internal resource map.
    *   `resolveLocale(languageTag: String)`: Lookup with fallback (e.g., "en-US" -> "en").
    *   `Locale.localizedDisplayName(displayIn)`: The locale's name rendered in the language of `displayIn`, via each platform's native CLDR data (JVM/Android `java.util.Locale`, Apple `NSLocale`, JS/Wasm `Intl.DisplayNames`). Falls back to the English `displayName`; Linux and unknown language codes always yield that fallback.
*   **`Currency`**: Represents ISO 4217 currencies.
    *   `Currency.current`: Retrieve the currency for the system's current locale.
    *   `availableCurrencies()`: Get a list of all supported currencies.
*   **`Money`**: Stores monetary values using arbitrary precision.
    *   **Contract**: `Money.value` is a `BigDecimal` storing the precise amount. `Money.minorUnits` returns the value rounded to the currency's standard digits using Banker's Rounding.
    *   `Double.toMoney(Currency)`: Safely convert a double to `Money`.
    *   `BigDecimal.toMoney(Currency)`: Safely convert a big decimal to `Money`.
    *   **Division**: `/` stays exact for terminating quotients and rounds HALF_EVEN otherwise — it never throws for ordinary divisors like 3.
    *   **⚠️ Long operator asymmetry**: `+ Long` / `- Long` treat the operand as **minor units** (cents); `* Long` / `/ Long` treat it as a **dimensionless multiplier**. Prefer `plusMinorUnits` / `minusMinorUnits` for clarity.
    *   **Currency matching**: Arithmetic guards compare currencies by ISO `code`, not full object equality.

### **Mathematics (Arbitrary Precision)**
All types are `data class` and `@Serializable`.
*   **`BigInteger`**: Platform-independent arbitrary-precision integers.
    *   `BigInteger(String)` / `BigInteger(Long)`: Primary constructors.
    *   `toInt()` / `toLong()`: Converters (return low-order bits on overflow).
*   **`BigDecimal`**: Platform-independent arbitrary-precision decimals.
    *   `BigDecimal(String)` / `BigDecimal(Double)` / `BigDecimal(Long)`: Primary constructors.
    *   `toDouble()`: Conversion back to platform primitives.
    *   `setScale(newScale: Int, roundingMode: RoundingMode)`: Precision control.
    *   `divide(other)`: Exact division; throws `ArithmeticException` only for genuinely non-terminating quotients.
    *   `divide(other, scale, roundingMode)`: Rounding division that always succeeds — prefer this when the divisor is arbitrary.

### **Geospatial & GeoJSON (RFC 7946)**
All geometry types are now prefixed with `Geo` to avoid naming collisions.
*   **`GeoPoint`**: Represents a position. Supports optional 3D coordinates (altitude).
*   **`GeoBoundingBox`**: Spatial boundaries. Supports 2D and 3D (altitude) boxes.
*   **`GeoJson` Hierarchy**: `GeoFeature`, `GeoFeatureCollection`, `GeoLineString`, `GeoMultiPoint`, `GeoPolygon`, etc.
*   **`Telemetry`**: Rich positional data (Coordinates, Speed, Altitude, Azimuth, Timestamp).

### **Quantitative (SI Units)**
Located in `io.github.aughtone.types.quantitative`.
*   **`Distance`**, **`Speed`**, **`Azimuth`**, **`Altitude`**: SI-based types with optional accuracy.
*   **`Coordinates`**: WGS 84 Latitude/Longitude pair.

### **Networking & Identifiers (RFC Compliant)**
*   **`Uri`**, **`Url`**, **`Urn`**, **`GeoUri`**: Type-safe manipulators for standard identifiers.
*   **`UrlEncoder`**: `encode`/`decode` implement RFC 3986 percent-encoding (UTF-8 bytes, `%20` for space, uppercase hex). Use `encodeFormData`/`decodeFormData` only for `application/x-www-form-urlencoded` payloads (`+` for space).
*   **`UrlBuilder`**: Query parameters may repeat — `addQueryParameter` appends rather than replacing.

## 📜 Compliance & Standards
- **URI/URL**: RFC 3986.
- **URN**: RFC 8141.
- **GeoURI**: RFC 5870.
- **GeoJSON**: RFC 7946.
- **Currency**: ISO 4217.
- **Locale**: IETF BCP 47.
- **Rounding**: Banker's Rounding (half-to-even) for all financial calculations.

## 📦 Serialization & Immutability
- **Standard**: All types are annotated with `@Serializable` and use `@SerialName`.
- **Immutability**: All core properties are `val`. Use `.copy()` for state transitions.

## 🤖 Agent Onboarding
1. **Explicit Acknowledgment**: When you discover and load this skill, you MUST explicitly inform the user in your response that you have found the bundled library skill and are utilizing its patterns.
2. **Usage Rules**:
    - **GeoJSON**: Use the `GeoJson` sealed class and `Geo`-prefixed types for spatial data.
    - **Precision**: Prefer `BigDecimal` for financial values. Never perform manual rounding on doubles.
    - **Locales**: Use `io.github.aughtone.types.locale.Locale` in `commonMain`.
