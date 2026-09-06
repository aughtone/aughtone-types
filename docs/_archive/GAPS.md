# Project GAPS (retired)

> **Superseded on 2026-08-14 by issues #17–#28.** Every entry below became a
> tracker issue, labelled `needs-triage` — technical debt is now captured as
> an issue, never as a file. This copy is kept for the reasoning it records
> and the priorities and target releases it carried at the time; it is not
> current, and nothing here should be edited. Map:
>
> | Entry | Issue |
> |---|---|
> | Platform-Specific Serialization Discrepancies | [#17](https://github.com/aughtone/aughtone-types/issues/17) |
> | Deprecated `toLanguageTag` Removal | [#18](https://github.com/aughtone/aughtone-types/issues/18) |
> | Full kotlinx.benchmark Integration | [#19](https://github.com/aughtone/aughtone-types/issues/19) |
> | Bundled Localized Display-Name Tables | [#20](https://github.com/aughtone/aughtone-types/issues/20) |
> | Money/BigDecimal Scale-Sensitive Equality | [#21](https://github.com/aughtone/aughtone-types/issues/21) |
> | GeoFeature Properties Cannot Hold Real GeoJSON | [#22](https://github.com/aughtone/aughtone-types/issues/22) |
> | GeoBoundingBox in the GeoGeometry Hierarchy | [#23](https://github.com/aughtone/aughtone-types/issues/23) |
> | Geo Structural Validation | [#24](https://github.com/aughtone/aughtone-types/issues/24) |
> | Dimensional Semantics for Quantitative Operators | [#25](https://github.com/aughtone/aughtone-types/issues/25) |
> | Url/GeoUri String Parsers | [#26](https://github.com/aughtone/aughtone-types/issues/26) |
> | LazyMap Thread Safety | [#27](https://github.com/aughtone/aughtone-types/issues/27) |
> | UnitOfMeasure Symbol Collisions | [#28](https://github.com/aughtone/aughtone-types/issues/28) |

This document tracks identified technical debt, missing features, and cross-platform inconsistencies in `aughtone-types`.

## ⚙️ Engineering & Testing

### gap: Platform-Specific Serialization Discrepancies
- **Status**: PARTIALLY RESOLVED
- **Priority**: MEDIUM
- **Context**: `GeoJsonTest` and `GeoUriTest` fail in JS/Wasm environments because `Double` serialization differs (e.g., `100.0` on JVM becomes `100` in JS).
- **Progress**: `GeoUri` now formats coordinates with a platform-stable plain-decimal formatter, eliminating its divergence. GeoJSON numeric payloads still rely on platform `Double` serialization in kotlinx.serialization.
- **Resolution (remaining)**: Refactor GeoJSON tests to use `JsonElement` comparison rather than string matching.
- **Target Release**: 3.2.0

### gap: Deprecated `toLanguageTag` Removal
- **Status**: OPEN
- **Priority**: LOW
- **Context**: `Locale.toLanguageTag()` was deprecated in 2.2.0 in favor of the `Locale.languageTag` property.
- **Resolution**: Remove the extension function in the next major release (v4.0.0).
- **Target Release**: 4.0.0

### gap: Full kotlinx.benchmark Integration for BigDecimal and BigInteger
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: Performance verification for pure-Kotlin arbitrary-precision math types (`BigDecimal` and `BigInteger`) compared to JDK and third-party libraries is currently missing.
- **Resolution**: Integrate JetBrains `kotlinx.benchmark` plugin into the Gradle build and create JMH benchmarks for critical operations (addition, division, Knuth division, scaling) to profile performance and avoid regression.
- **Target Release**: 3.3.0

### gap: Bundled Localized Display-Name Tables
- **Status**: OPEN
- **Priority**: LOW
- **Context**: ADR 0003 delegates locale display-name localization to platform CLDR data. Linux targets therefore only get the English fallback, and wording differs slightly across OSes. Byte-identical, all-platform names require bundling generated per-display-language Kotlin tables (~90 strings each, one lazily-initialized table per language via `lazyMapOf`), plus tooling to generate/refresh them from CLDR. AI-assisted maintenance makes the table upkeep tractable, but it is deliberately deferred.
- **Resolution**: Generate one Kotlin table file per display language plus a registry keyed by language code; load-on-first-access keeps RAM at one table (a few KB). Build an AI-assisted CLDR extraction/refresh workflow to maintain them.
- **Target Release**: TBD

## 🧭 API Design (deferred — breaking, needs 4.0 planning)

### gap: Money/BigDecimal Scale-Sensitive Equality
- **Status**: OPEN
- **Priority**: HIGH
- **Context**: `Money(1250L, usd)` (scale 2) and `Money(12.5, usd)` (scale 1) represent the same amount but are unequal with different hash codes and serialized forms, because `BigDecimal.equals` compares `unscaledValue`+`scale` strictly. `Money` also implements no `Comparable`.
- **Resolution**: Decide compareTo-vs-equals semantics (JDK-style: `equals` scale-sensitive, `compareTo` numeric) and implement `Comparable<Money>`/`Comparable<BigDecimal>`; consider normalizing Money storage.
- **Target Release**: 4.0.0

### gap: GeoFeature Properties Cannot Hold Real GeoJSON
- **Status**: OPEN
- **Priority**: HIGH
- **Context**: `GeoFeature.properties: Map<String, String>?` rejects numeric/boolean/nested JSON property values under a strict `Json` instance, and `id: String?` rejects RFC-permitted numeric ids.
- **Resolution**: Migrate `properties` to `JsonObject?` and `id` to a string-or-number representation.
- **Target Release**: 4.0.0

### gap: GeoBoundingBox in the GeoGeometry Hierarchy
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: `GeoBoundingBox` extends `GeoGeometry` but a bbox is not an RFC 7946 geometry; serializing it polymorphically emits invalid GeoJSON, and it can be placed anywhere a geometry is expected.
- **Resolution**: Remove it from the sealed hierarchy and serialize as the RFC bbox array form.
- **Target Release**: 4.0.0

### gap: Geo Structural Validation
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: GeoJSON geometry classes accept structurally invalid data: `GeoLineString` with fewer than 2 positions, `GeoPolygon` with unclosed/short rings or wrong winding, position arrays of length < 2.
- **Resolution**: Add init-block validation (with an opt-out for lenient ingestion if needed) per RFC 7946 §3.1.
- **Target Release**: 4.0.0

### gap: Dimensional Semantics for Quantitative Operators
- **Status**: OPEN
- **Priority**: LOW
- **Context**: `Distance * Distance` returns square meters labelled as a `Distance`; `Distance / Distance` returns a dimensionless ratio labelled `Distance`. Signed quantities (negative distance/speed deltas) are unrepresentable; `minus` now floors at zero.
- **Resolution**: Introduce distinct result types (`Area`, `Ratio`) or remove the operators; consider signed delta types.
- **Target Release**: 4.0.0

### gap: Url/GeoUri String Parsers
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: The `uri` package can build and render `Url`/`GeoUri` values but cannot parse them from strings (only `urn(...)` exists), so the "parsing" half of RFC 3986/5870 compliance is absent.
- **Resolution**: Add `url(String)` and `geoUri(String)` parser functions with round-trip guarantees.
- **Target Release**: 3.2.0

### gap: LazyMap Thread Safety
- **Status**: OPEN
- **Priority**: LOW
- **Context**: `LazyMap`'s memoization cache is unsynchronized (now documented as not thread-safe); concurrent access on JVM/Native can evaluate a supplier twice or corrupt the cache.
- **Resolution**: Offer a thread-safe variant (atomics or synchronized) or document a wrapping strategy.
- **Target Release**: 3.2.0

### gap: UnitOfMeasure Symbol Collisions
- **Status**: OPEN
- **Priority**: LOW
- **Context**: `findFirst` resolves colliding symbols by declaration order (e.g. `"gal"` → `Gallon`, never `GallonImperial`; `"a"` → `Year` vs `YearJulian`), and `Kilocalorie("kCal")`/`Calorie("Cal")` use nonstandard casing.
- **Resolution**: Document collision precedence, add `findAll` guidance, and review nonstandard symbol casings for 4.0.
- **Target Release**: 4.0.0
