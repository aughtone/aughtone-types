# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.1.0] - 2026-06-21

### Added
- **GitHub Actions CI/CD**: Added a test workflow (`.github/workflows/test.yml`) running on macOS.
- **Test Reporting**: Integrated visual JUnit test reporting via `mikepenz/action-junit-report`.
- **Discord Build Alerts**: Added build status notifications via `sarisia/actions-status-discord` to the `#builds` channel.
- **Currencies**: Defined 19 missing currencies inside the resource map.

### Changed
- **LazyMap Refactor**: Redesigned `LazyMap` to implement the `Map` interface directly (removing cache delegation). Fully lazy evaluation for `entries`, `values`, and short-circuiting `containsValue`.
- **Locale Resolution**: Changed `resolveLocale` to execute the BCP 47 stepdown lookup on the resource map first, prior to native lookups.
- **Android Compatibility (API 17+)**:
  - Lowered `minSdk` to `17`.
  - Replaced JDK 7+ `toLanguageTag()` and `forLanguageTag()` with manual tag-building/splitting.
  - Utilized reflection for `getScript()` to prevent linkage errors on API < 21.
  - Enforced JVM 17 target compiler options for Android.
- **Yarn Lockfile Mismatch Handling**: Configured Yarn mismatch reporting to `WARNING` and auto-replace to prevent CI failures.
- **WasmJS/JS Native Locale**: Made navigator language retrieval robust to non-browser environments and removed default `"en-US"` fallback.

### Fixed
- **BitSet.all()**: Corrected bitwise masking logic to handle signed values and partially filled words correctly.
- **Warnings**: Cleaned up redundant Gradle property `toString()` calls and safe compiler warnings.

## [3.0.0] - 2026-05-16

### ⚠️ BREAKING CHANGES
- **GeoJSON Renaming**: All geometry and feature types in `io.github.aughtone.types.geo` have been renamed with a `Geo` prefix (e.g., `Point` -> `GeoPoint`, `Polygon` -> `GeoPolygon`, `Feature` -> `GeoFeature`) to prevent naming collisions and improve clarity.
- **Money Model Refactor**:
    - Internal storage changed from `Long` (cents) to `BigDecimal` (`value`).
    - Supports arbitrary precision (fractions of a cent).
    - `currency` parameter in `Money` constructor is now mandatory (defaults to `Currency.current`).
- **Telemetry Movement**: `Telemetry` and `Location` (deprecated) have moved from the `geo` package to `io.github.aughtone.types.quantitative`.
- **Project Structure**: Moved the `kotlin-js-store` directory to `gradle/kotlin-js-store` and updated the root build configuration.

### Added
- **GeoJSON Altitude Support**: `GeoPoint` and `GeoBoundingBox` now support an optional third dimension for altitude (elevation) as per RFC 7946.
- **Precision Types Enhancement**: `BigDecimal` and `BigInteger` are now `data class` types and fully `@Serializable`.
- **New Constructors**:
    - `BigDecimal`: Added `String`, `Double`, and `Long` constructors.
    - `BigInteger`: Added `String` and `Long` constructors.
- **Converters**: Added `BigDecimal.toDouble()`, `BigInteger.toInt()`, and `BigInteger.toLong()`.
- **Currency factor**: Added `Currency.factor` for automated mathematical scaling based on digits.
- **Agent Infrastructure**: Migrated project governance and automation skills to the new `.agents/` directory standard.
- **Claude Guardrails**: Integrated `git-guardrails-claude-code` and established `.claude/settings.json` with a `PreToolUse` hook to prevent destructive Git operations.
- **Claude Configuration**: Added `CLAUDE.md` and `.clauderc` to explicitly mandate reading `AGENTS.md` at session start.

### Fixed
- **Locale Throwing Behavior**: Updated documentation to correctly reflect that `Locale.current` and `Currency.current` throw `IllegalStateException` instead of falling back to English if the system locale cannot be resolved.
- **BigDecimal Default Scale**: The primary constructor for `BigDecimal` now has a default scale of `0`, allowing it to act as an integer by default.

## [2.2.0] - 2026-05-15

### ⚠️ BREAKING CHANGES
- **`localeFor` Behavior**: Changed from a BCP 47 lookup (with fallback) to a **strict lookup**. It now only returns a `Locale` if there is an exact match in the internal resource map. Use `resolveLocale` for the previous fallback behavior.

### Added
- **`Locale.languageTag`**: Added a dedicated property to the `Locale` class to provide the standard BCP 47 string representation.
- **`resolveLocale(tag)`**: New function implementing the BCP 47 lookup algorithm with fallback (e.g., "en-US" -> "en").
- **`parseLocale(tag)`**: New function that decomposes any IETF BCP 47 string into its components, creating a new `Locale` instance if no match is found in the resource map.
- **Intelligent Native Resolution**: `Locale.current` now dynamically parses native platform tags (e.g., `fr-MC`) if they aren't explicitly in the library's resource map, preventing unnecessary fallbacks to English.
- **Azerbaijani Locale**: Added `Azerbaijani` (`az-AZ`) as a top-level constant and resource map entry.

### Changed
- **AI Governance**: Updated `AGENTS.md` and `ai-skill.md` to provide clearer guidance on preferring the library's `Locale` type in `commonMain` while remaining compatible with platform-specific types.

### Deprecated
- **`Locale.toLanguageTag()`**: Deprecated in favor of the new `Locale.languageTag` property.

## [2.1.0] - 2026-05-09

### Added
- **Financial Utility**: Added `currencyFor(locale)` to retrieve the default currency for a given `Locale`.
- **Resource Maps**: Updated `localeToCurrencyMap` to support broader automated currency resolution.

## [2.0.3] - 2026-04-24

### Changed
- **Branding & Standardization**:
    - Renamed "AughtOne" to "Aughtone" across the project.
    - Unified iOS Kit naming to `AughtoneTypesKit`.
    - Standardized `namespace` to `io.github.aughtone.types` in version catalog.

### Added
- **Serialization Standards**: Enforced `@SerialName` and `@Serializable` across all core types (`Currency`, `Locale`, `Money`) to ensure stable cross-platform data exchange.
- **Financial Model Stability**: Documented the `Money` contract where `cents` storage interacts with `Currency.digits` as a scale factor.

## [2.0.2] - 2026-04-23

### Added
- **New Locales**: Expanded the `Locale` resource map to include **Inuktitut** (`iu`, `iu-CA`) and **Norwegian** (`no`), supporting broader language coverage in the formatting ecosystem.
- **`Locale.current`**: Added a convenience static property to the `Locale` companion object to retrieve the current system locale via `currentNativeLocale()`.

### Changed
- **Build System**: Incremented patch version to `2.0.2`.

## [2.0.1] - 2026-04-23

### Changed
- **Build System**: Stabilized build for patch release.

## [2.0.0] - 2026-04-22

### ⚠️ BREAKING CHANGES
- **Enum Standardization**: All constants in `UnitOfMeasure` and `MetricPrefix` have been renamed from `UPPER_SNAKE_CASE` to **`PascalCase`** (e.g., `KILOBYTE` becomes `Kilobyte`). This aligns the API with idiomatic Kotlin Multiplatform conventions and the broader Aughtone ecosystem.

### Added
- **Metric Scaling Factor**: Added an `exponent: Int` property to the `MetricPrefix` enum, enabling automated mathematical scaling (e.g., `10^exponent`) for use in numeric abbreviation and formatting logic.
- **Aughtone AI-Skill**: Integrated the **Aughtone AI-Skill Publishing Standard**. The library now embeds machine-readable intelligence in `META-INF/ai-skills/` to enhance discovery and usage by AI coding assistants.
- **Metadata Integration**: Added structured frontmatter to the AI-Skill file including `skill-id`, `name`, `type`, and `author`.

### Changed
- **Documentation**: Updated the root `README.md` with a prominent v2.0.0 breaking change notice and a new section on **AI-Assisted Development**.
- **Build System**: Incremented major version to `2.0.0` and stabilized the KMP build environment (Yarn lock updates).
- **Test Suite**: Fully updated all unit tests to reflect the new PascalCase naming and to verify the new mathematical `exponent` property in `MetricPrefix`.
