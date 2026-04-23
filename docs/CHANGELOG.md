# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.1] - 2026-04-23

### Added
- **New Locales**: Expanded `Locale` resource map to include **Inuktitut** (`iu`, `iu-CA`) and **Norwegian** (`no`), supporting broader language coverage in the formatting ecosystem.
- **`Locale.current`**: Added a convenience static property to the `Locale` companion object to retrieve the current system locale via `currentNativeLocale()`.

### Changed
- **Build System**: Incremented patch version to `2.0.1`.

## [2.0.0] - 2026-04-22

### ⚠️ BREAKING CHANGES
- **Enum Standardization**: All constants in `UnitOfMeasure` and `MetricPrefix` have been renamed from `UPPER_SNAKE_CASE` to **`PascalCase`** (e.g., `KILOBYTE` becomes `Kilobyte`). This aligns the API with idiomatic Kotlin Multiplatform conventions and the broader AughtOne ecosystem.

### Added
- **Metric Scaling Factor**: Added an `exponent: Int` property to the `MetricPrefix` enum. This enables automated mathematical scaling (e.g., `10^exponent`) for use in numeric abbreviation and formatting logic.
- **AughtOne AI-Skill**: Integrated the **AughtOne AI-Skill Publishing Standard**. The library now embeds machine-readable intelligence in `META-INF/ai-skills/` to enhance discovery and usage by AI coding assistants.
- **Metadata Integration**: Added structured frontmatter to the AI-Skill file including `skill-id`, `name` (linked to source), `type`, and `author` (linked to GitHub).

### Changed
- **Documentation**: Updated the root `README.md` with a prominent v2.0.0 breaking change notice and a new section on **AI-Assisted Development**.
- **Build System**: Incremented major version to `2.0.0` and stabilized the KMP build environment (Yarn lock updates).
- **Test Suite**: Fully updated all unit tests to reflect the new PascalCase naming and to verify the new mathematical `exponent` property in `MetricPrefix`.
