# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [4.0.0] - 2026-09-20

A breaking release. It removes names deprecated across the 2.x and 3.x lines, corrects operators and symbol lookups that returned confident wrong answers, and brings the GeoJSON types into line with RFC 7946.

### ⚠️ Breaking Changes

- **`Outcome.Error` and `Outcome.error(...)` are removed.** Renamed to `Outcome.Failure` and `Outcome.failure(...)` in 3.4.0, where the old names survived as deprecated aliases. (#29)
- **`Locale.toLanguageTag()` is removed.** Deprecated since 2.2.0 in favour of the `Locale.languageTag` property. (#18)
- **`Money` equality is now numeric.** `Money(5.1, usd) == Money(5.10, usd)` is `true` where it was `false`. Scale is still preserved in storage and serialization — `5.0100000` is stored and serialized unchanged — but takes no part in equality, because no arithmetic in this library can make two spellings of one amount differ in value. Code relying on equality distinguishing `12.50` from `12.5` changes meaning **silently, with no compiler error**. A consequence: two equal amounts can serialize differently. Compare `value.scale` explicitly to ask whether two amounts were *written* the same way. `BigDecimal` is unchanged and keeps JDK-style scale-sensitive equality. (#21)
- **`Distance` and `Speed` throw instead of clamping to zero.** `Distance(3m) - Distance(5m)` returned `0m`; it now throws. The same applies to a negative divisor on `Distance`, and to negative scalars in `Speed.times` and `Speed.div`. The constructors already rejected negative values, so the operators now enforce the same invariant rather than inventing one. (#25)
- **`Distance.times(Distance)` is removed.** Metres times metres is an area, and there is no area type; returning a `Distance` labelled with square metres type-checked and was wrong. (#25)
- **`Distance.div(Distance)` now returns `Double`.** A ratio of two lengths is dimensionless. This one breaks at call sites that did not change — anything storing the result in a `Distance` stops compiling. (#25)
- **`GeoBoundingBox` is no longer a `GeoGeometry`.** RFC 7946 makes a bounding box a `bbox` member, not a geometry, and the old modelling serialized to invalid GeoJSON while allowing a bounding box anywhere a geometry was expected. The type remains, with `toBbox()` for the RFC form. Exhaustive `when` expressions over `GeoGeometry` will fail to compile — at the `when`, not at the line that changed. (#23)
- **`GeoFeature.properties` is now `JsonObject?`**, was `Map<String, String>?`, and **`GeoFeature.id` is now `JsonPrimitive?`**, was `String?`. RFC 7946 permits any JSON value in properties and a string or number for id, so the library could not previously round-trip GeoJSON it did not produce itself. Read values with the new accessors: `feature.stringProperty("name")`. (#22)
- **Invalid GeoJSON geometries are rejected on construction and on deserialization.** RFC 7946 §3.1 structural rules are enforced: minimum position counts, positions of at least two elements, and linear rings closed and at least four positions long. Payloads that previously decoded into structurally invalid values now throw. Winding order is deliberately **not** enforced on parse — §3.1.6 instructs parsers not to reject polygons breaking the right-hand rule — so a clockwise exterior ring round-trips unchanged. (#24)
- **`UnitOfMeasure.FoodCalorie` is removed.** A food calorie *is* a kilocalorie; two enum entries modelled one unit. Use `UnitOfMeasure.Kilocalorie`. Symbol lookups are unaffected. (#28)
- **`UnitOfMeasure.findFirst` refuses ambiguous symbols.** `findFirst("gal")` returned `Gallon` by declaration order with nothing to signal that `GallonImperial` also claimed the symbol — a silent 20% error in an imperial market. It now returns `null`; use `findAll` to discover the candidates. `findAll` returns an empty list for an unknown symbol and more than one entry for an ambiguous one, so the two cases stay distinguishable. (#28)
- **`Gallon.symbol` is now `"US gal"`**, was `"gal"`. Anything rendering `.symbol` produces different output, and this is not a compile error. `Calorie.symbol` is now `"cal"` and `Kilocalorie.symbol` is `"kcal"`, matching the standard casings. (#28)
- **`LazyMap`'s `cache` constructor parameter is removed.** The cache is now internal and atomic. (#27)

### Added

- **`Money` ordering**: `Money` implements `Comparable<Money>`. Comparing amounts in different currencies throws, since that ordering is not meaningful. (#21)
- **GeoJSON winding tools**: `Winding`, `GeoPolygon.windingOf(ringIndex)`, `GeoPolygon.rewound()`, and the `geoPolygon(...)` / `geoPolygonRewinding(...)` factories. The first rejects rings breaking the right-hand rule; the second accepts any winding and returns it corrected — the function to reach for after reading a foreign document. (#24)
- **`GeoFeature` property accessors**: `stringProperty`, `intProperty`, `doubleProperty`, `booleanProperty`, `objectProperty`, `arrayProperty`. None throws. `stringProperty` renders objects and arrays as their JSON text rather than returning `null`, so a property that is set never looks unset; it returns `null` only for an absent key or a JSON null, which `properties?.containsKey(key)` tells apart. (#22)
- **`GeoBoundingBox.toBbox()`**, giving the RFC 7946 flat-array form to assign to a geometry's or feature's `bbox`. (#23)

### Fixed

- **`LazyMap` is safe for concurrent access.** Its cache was an unsynchronized `LinkedHashMap` reachable from `currencyFor(...)` and `localeFor(...)`, so two coroutines on a multi-threaded dispatcher could drop an entry or catch the map mid-resize — after which a later read returned a wrong value on a key that had been written correctly. The cache is now an immutable map in an atomic reference, replaced by compare-and-set. A cache hit costs one atomic read. Under contention a value function may run more than once, so value functions must be pure; every caller still observes the same value. (#27)
- **GeoJSON serialization tests compare structure rather than text.** A `Double` renders as `100.0` on the JVM and `100` on JS and Wasm, so the same correct payload passed on one target and failed on another. (#17)

### Changed

- **`kotlinx-atomicfu` is used internally by `LazyMap`, and adds nothing to the published JVM artifact.** Its Gradle plugin transforms the atomics into `AtomicReferenceFieldUpdater` calls and strips itself from the class metadata, so the JVM POM is unchanged — still only `kotlinx-datetime`, `kotlinx-serialization-json` and the Kotlin standard library — and no atomicfu reference survives in the shipped bytecode. Native targets link it as an ordinary klib dependency, as they must.

## [3.4.0] - 2026-09-07

### ⚠️ Behavior Changes

- **`Outcome.Error` is renamed to `Outcome.Failure`**, and the `Outcome.error(...)` factory to `Outcome.failure(...)`. Both old names remain as **deprecated** aliases, so code written against 3.3.0 still compiles — including `is Outcome.Error` in an exhaustive `when` — and now warns with the replacement. The old names will be removed in 4.0.0.

  The name was wrong on two counts: every callback in the API already said *failure* (`onFailure`, and `fold`'s second parameter), so the type disagreed with its own callbacks; and `Outcome.Error` reads as a relative of `kotlin.Error`, a specific severe-throwable type it has nothing to do with.

  This is a binary break — `Outcome$Error` no longer exists as a class — so a consumer upgrading from 3.3.0 needs a clean and rebuild rather than a code change. It ships as a minor because 3.3.0 was hours old and nothing had compiled against it. See [ADR-0004](docs/knowledge/decisions/outcome-over-kotlin-result.md).

## [3.3.0] - 2026-09-06

### ⚠️ Behavior Changes

No APIs were removed or renamed, but eleven locale display names change. Code that renders `Locale.displayName` (or the `displayName` of a `Locale` obtained from `localeFor`/`localeResourceMap`) will show different strings for these tags. Nothing throws and no type changes — but golden-file tests, cached UI strings and snapshot assertions containing the old names will need updating.

The corrections cover countries renamed since the table was written, abbreviated or incomplete country names, dated language exonyms, and one entry that was missing its region entirely.

| Tag | Was | Now |
| :--- | :--- | :--- |
| `ar-AE` | Arabic (U.A.E.) | Arabic (United Arab Emirates) |
| `az` | Azeri (Latin) | Azerbaijani (Latin) |
| `az-AZ` | Azerbaijani | Azerbaijani (Azerbaijan) |
| `cs-CZ` | Czech (Czech Republic) | Czech (Czechia) |
| `en-TT` | English (Trinidad) | English (Trinidad & Tobago) |
| `fa` | Farsi | Persian |
| `fa-IR` | Farsi (Iran) | Persian (Iran) |
| `ko-KR` | Korean (Korea) | Korean (South Korea) |
| `mk-MK` | Macedonian (Macedonia) | Macedonian (North Macedonia) |
| `tr-TR` | Turkish (Turkey) | Turkish (Türkiye) |
| `vi-VN` | Vietnamese (Viet Nam) | Vietnamese (Vietnam) |

Display names that deliberately differ from CLDR are **unchanged** — `Serbian (Latin)`, `Uzbek (Latin)`, `Chinese (Simplified, China)`, `Norwegian (Bokmål, Norway)` and similar carry the script on purpose, which CLDR drops. See `docs/reference/README.md` before altering them.

### Added
- **`Outcome`** (`io.github.aughtone.types.outcome`): a sealed success-or-failure type covering the same ground as `kotlin.Result`, but usable from Swift, JavaScript and Dart. `Result` is a `value class` over `Any?` and has no representation those languages can take apart; a sealed class compiles to an ordinary hierarchy everywhere, so failures cross the language boundary as data instead of as thrown exceptions. Ships with `onSuccess`, `onFailure`, `dataOrNull`, `dataOrThrow`, `dataOrElse`, `fold`, `map`, `mapCatching` and `recover`.
- **`runOutcome { }`**: the `runCatching` of that package, with the trap fixed — a `CancellationException` is re-thrown rather than captured, so wrapping suspending work never swallows coroutine cancellation. It is `inline` and not `suspend`, so the library still has no coroutines dependency. See [ADR-0004](docs/knowledge/decisions/outcome-over-kotlin-result.md) for why the type is deliberately not `@Serializable`.
- **`NOTICE.md` and `THIRD-PARTY-NOTICES.md`**: the repository now carries an explicit copyright notice and records the terms of the reference data compiled into the artifact. `docs/reference/README.md` documents each dataset's source and version.

### Changed
- **iOS framework name**: the Kotlin/Native framework produced for the iOS targets is now `AOTypesKit` (was `AughtoneTypesKit`). This is **not** a breaking change: the name has never been distributed. The library publishes only to Maven Central — there is no Package.swift, podspec or XCFramework — and a downstream Kotlin Multiplatform app links the klib and builds its *own* framework under its *own* `baseName`, so no consumer has ever imported this module name. No shim or deprecation window is needed. If SPM or XCFramework distribution is ever added, that is the point at which the name becomes a public contract.

### Removed
- **Embedded AI-skill file**: the library no longer publishes `META-INF/ai-skills/io.github.aughtone.types.ai-skill.md` inside its artifact, and the "Magic Prompt" section that advertised it is gone from the README. Tooling that scanned dependency classpaths for that file will find nothing; refer to the repository documentation instead.

## [3.2.0] - 2026-07-23

### ⚠️ Behavior Changes

No APIs were removed or renamed, but the following correct previously non-compliant behavior and will change results for code written against 3.1.x. Review these before upgrading.

- **UrlEncoder.encode output** (space/`~`/`*`/non-ASCII) is now RFC 3986 rather than form encoding — use `encodeFormData` for the previous behavior. ⚠️ This changes output **silently**: callers building `application/x-www-form-urlencoded` payloads must switch to `encodeFormData`.
- **Url/Uri/Urn/GeoUri toString** shapes are now well-formed; code depending on the previous malformed output will see different strings.
- **Urn construction** now rejects invalid NIDs — throws where malformed input was previously accepted.
- **GeoUri construction** now rejects out-of-range coordinates and negative uncertainty — throws where invalid input was previously accepted.
- **UrlBuilder repeated query parameters**: `addQueryParameter` appends instead of silently replacing, so repeated keys now emit multiple parameters.

### Fixed
- **BigInteger Division (CRITICAL)**: Both division paths (single-word and multi-word Knuth) used signed 64-bit arithmetic where unsigned was required, silently producing wrong quotients/remainders whenever a quotient digit or intermediate value reached 2³¹ (e.g. `16116354936157110357 / 3752381294`). Affected `divide`, `remainder`, `mod`, `modPow`, `modInverse`, and all `BigDecimal` division/scaling. Verified with a 7,000-case seeded differential fuzz suite against `java.math`.
- **BigInteger.shiftRight**: Shifting a positive value down to zero produced a corrupted instance (`signum=1`, empty magnitude) that was not equal to `ZERO`. The class `init` block now enforces the full invariant on every construction path, so invalid serialized payloads are also rejected on deserialization.
- **BigDecimal.divide (exact)**: Replaced the arbitrary 200-digit iteration cap with an exact termination test (2/5 factorization of the reduced divisor); exactly-representable quotients of any length now succeed (e.g. `1 / 2²⁰¹`).
- **BankersValue**: `times` now computes exactly via integer math (was rounding through `Double`, off by a cent for large products); `div` rescales correctly so `(a*b)/b == a`; `fromDouble` detects half-cent ties exactly (`0.575` → 58¢, was 57¢); NaN/Infinity operands now throw instead of silently producing 0.
- **Currency lookup for script-bearing locales (CRITICAL)**: `Currency.current`/`getCurrency` now resolve through BCP 47 stepdown, fixing permanently-broken lookups for `zh-CN`, `zh-HK`, `zh-SG`, `zh-TW`, `sr-RS`, and `uz-UZ` (whose `languageTag` carries a script subtag). Previously `Currency.current` threw for all Chinese-locale devices.
- **Apple currencyForNative**: No longer crashes with `NoSuchElementException` for codes outside the user's preferred languages; `NSNumberFormatter` is now configured with currency style before fraction digits are read (previously reported `digits=0` for every currency).
- **JVM/Android currencyForNative**: Returns `null` for unknown codes per the expect contract instead of leaking `IllegalArgumentException`.
- **Legacy ISO language codes**: `iw`/`in`/`ji` (returned by JVM/Android platform APIs) now normalize to `he`/`id`/`yi`, fixing `Locale.current`/`Currency.current` for Hebrew, Indonesian, and Yiddish users.
- **Money division**: `/ 3L` no longer throws for non-terminating quotients — division stays exact when terminating and otherwise rounds HALF_EVEN with two guard digits of sub-minor precision.
- **Money currency guards**: Arithmetic now compares currencies by ISO code, so semantically-identical `Currency` instances from different sources (native vs resource) no longer raise a mismatch.
- **UrlEncoder (CRITICAL)**: `encode` is now genuinely RFC 3986 — non-ASCII characters are percent-encoded as UTF-8 bytes (previously passed through raw), astral characters encode as real 4-byte UTF-8 (was invalid CESU-8), space → `%20`, `~` unreserved, uppercase hex. New `decode` added. Legacy form semantics preserved as `encodeFormData`/`decodeFormData`.
- **Url**: `authority`/`identity`/`toString` no longer emit `host:null`, `?#` residue, or a doubled slash, and now include `userInfo`. `Uri.toUrl` is IPv6-bracket-aware, leaves absent ports `null`, and throws `IllegalArgumentException` on malformed ports.
- **Urn**: RFC 8141 NID validation, case-insensitive `urn:` prefix and NID equality, and `r`/`q`/`f` component parsing (previously swallowed into the NSS).
- **GeoUri**: Coordinate/uncertainty range validation, platform-stable plain-decimal formatting (no JVM scientific notation, no JS `.0` divergence — closes the platform-serialization gap for GeoUri), and structurally-correct `toUri` output (no `//` authority on `geo:`/`urn:` URIs).
- **Coordinates.plus**: Longitude now wraps across the antimeridian instead of throwing.
- **Azimuth**: Negative scalar `times`/`div` wrap via floor-mod (`90° × -1` → `270°`); `360.0` normalizes to `0.0` at construction so equality is consistent.
- **Distance/Speed**: `minus` (and negative-scalar operations) floor at zero instead of throwing.
- **UnitOfMeasure**: `LiterPer100Kilometers` symbol fixed — `"L/1OOkm"` (letter O's) → `"L/100km"`.
- **LazyMap**: Null values now cache correctly and no longer crash `entries`/`values` iteration; suppliers invoked at most once per key; structural `equals`/`hashCode` per the Map contract; documentation corrected.
- **BitSet**: `emptyBitSet()`/`bitSet()` now return a size-0 set as documented.
- **GeoBoundingBox.toDoubleArray**: Single-sided altitude bounds are preserved instead of silently dropped.
- **KMF**: Trailing space removed from the Comorian Franc display name.

### Added
- **Locales**: Added 34 base-language locales to the locale resource map (Amharic, Assamese, Bengali, Burmese, Filipino, Gujarati, Hausa, Igbo, Javanese, Kannada, Khmer, Kurdish, Kyrgyz, Lao, Malayalam, Marathi, Mongolian, Nepali, Odia, Pashto, Punjabi, Sindhi, Sinhala, Somali, Sundanese, Tagalog, Tajik, Tamil, Telugu, Tibetan, Urdu, Uyghur, Yoruba, Zulu), with ISO 15924 script codes for non-Latin scripts.
- **Localized Display Names**: New `Locale.localizedDisplayName(displayIn)` resolving names via platform CLDR data (JVM/Android `java.util.Locale`, Apple `NSLocale`, JS/Wasm `Intl.DisplayNames`), falling back to the English `displayName` (see ADR 0003).
- **28 missing active ISO 4217 currencies**: RWF, SBD, SCR, SDG, SHP, SLE, SOS, SRD, SSP, STN, SVC, SZL, TJS, TMT, TOP, TZS, UGX, UYI, UYW, VED, VUV, WST, XAF, XCD, XOF, XPF, ZMW, ZWG.
- **BigDecimal.divide(other, scale, roundingMode)**: Public rounding division that always succeeds.
- **UrlEncoder.decode / encodeFormData / decodeFormData**.
- **Urn r/q/f components** (`rComponent`, `qComponent`, `fComponent`).
- **Money.plusMinorUnits / minusMinorUnits**: Self-describing alternatives to the `Long` operators (whose minor-unit vs multiplier asymmetry is now loudly documented).
- **Test infrastructure**: Seeded differential division/shift fuzz suite vs `java.math` (jvmTest); serialization round-trip and invalid-payload rejection tests for the number types; strict-Json round-trips for all GeoJSON types; `CurrencyMismatchTest` rewritten with real assertions covering every resource-map locale's generated `languageTag`.

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
