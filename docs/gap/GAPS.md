# Project GAPS

This document tracks identified technical debt, missing features, and cross-platform inconsistencies in `aughtone-types`.

## ⚙️ Engineering & Testing

### gap: Platform-Specific Serialization Discrepancies
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: `GeoJsonTest` and `GeoUriTest` fail in JS/Wasm environments because `Double` serialization differs (e.g., `100.0` on JVM becomes `100` in JS).
- **Resolution**: Refactor tests to use platform-agnostic comparison logic (e.g., `JsonElement` comparison or normalized string matching).
- **Target Release**: 2.0.3

### gap: Deprecated `toLanguageTag` Removal
- **Status**: OPEN
- **Priority**: LOW
- **Context**: `Locale.toLanguageTag()` was deprecated in 2.2.0 in favor of the `Locale.languageTag` property.
- **Resolution**: Remove the extension function in a future major release (v3.0.0).
- **Target Release**: 3.0.0

### gap: Full kotlinx.benchmark Integration for BigDecimal and BigInteger
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: Performance verification for pure-Kotlin arbitrary-precision math types (`BigDecimal` and `BigInteger`) compared to JDK and third-party libraries is currently missing.
- **Resolution**: Integrate JetBrains `kotlinx.benchmark` plugin into the Gradle build and create JMH benchmarks for critical operations (addition, division, Knuth division, scaling) to profile performance and avoid regression.
- **Target Release**: 2.3.0

### gap: Bundled Localized Display-Name Tables
- **Status**: OPEN
- **Priority**: LOW
- **Context**: ADR 0003 delegates locale display-name localization to platform CLDR data. Linux targets therefore only get the English fallback, and wording differs slightly across OSes. Byte-identical, all-platform names require bundling generated per-display-language Kotlin tables (~90 strings each, one lazily-initialized table per language via `lazyMapOf`), plus tooling to generate/refresh them from CLDR. AI-assisted maintenance makes the table upkeep tractable, but it is deliberately deferred.
- **Resolution**: Generate one Kotlin table file per display language plus a registry keyed by language code; load-on-first-access keeps RAM at one table (a few KB). Build an AI-assisted CLDR extraction/refresh workflow to maintain them.
- **Target Release**: TBD

