# Project GAPS

This document tracks identified technical debt, missing features, and cross-platform inconsistencies in `aughtone-types`.

## ⚙️ Engineering & Testing

### gap: Platform-Specific Serialization Discrepancies
- **Status**: OPEN
- **Priority**: MEDIUM
- **Context**: `GeoJsonTest` and `GeoUriTest` fail in JS/Wasm environments because `Double` serialization differs (e.g., `100.0` on JVM becomes `100` in JS).
- **Resolution**: Refactor tests to use platform-agnostic comparison logic (e.g., `JsonElement` comparison or normalized string matching).
- **Target Release**: 2.0.3
