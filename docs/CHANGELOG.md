# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
