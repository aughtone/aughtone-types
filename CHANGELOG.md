# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [4.1.0] - 2026-09-30

A breaking release. `Outcome` now reads as `kotlin.Result`, `Currency` compares by its ISO 4217 code, `Gigabyte` and `Gigabit` gain the symbols their families already had, and locale display names are repaired on JS and bundled where a platform has no CLDR. Several changes alter behaviour **without a compile error** — read the first two sections before upgrading.

### ⚠️ Breaking Changes

- **`Outcome`'s failure callbacks now receive the `Throwable`, not the `Outcome.Failure`.** This applies to `onFailure`, `fold`'s second parameter, `recover` and `getOrElse`, matching `kotlin.Result`. **This is the one part of the change that does not fail the build, so check it first:** a body reading `it.message` keeps compiling, because `Throwable.message` exists, but it is `String?` where `Outcome.Failure.message` was a non-null `String` — inside a string template it silently renders the text `null` for an exception carrying no message. Use `it.message ?: it.toString()`, which is what `Failure.message` does. A body reading `it.exception` becomes just `it`; a read of `.exception` on a `Failure` you reached by a type check or smart cast needs no change, since `Failure.exception` is untouched. Grep your doc comments as well as your source — a KDoc sample compiles nowhere, so nothing flags it. (#35)
- **`Currency` equality is its ISO 4217 `code`, and nothing else.** `Currency` is a data class over seven fields, so equality used to compare all of them — meaning two instances describing one currency were unequal whenever their `name` or `symbol` differed, which happens routinely between the bundled resource map, a platform lookup and a decoded payload. ISO 4217 makes the alphabetic code the identity of a currency and the rest presentation, so equality now reads the code, matching how the URI types already defer to their own specifications. `hashCode` follows. Consequences, none of them a compile error: a `Set`, `distinct()` or a map keyed by `Currency` may now hold one entry where it held two; `usd.copy(symbol = "US$") == usd` is `true`; and any value persisted from a Kotlin hash of a `Currency` or a `Money` changes. Compare the individual properties where a presentation difference is what you care about. (#37)
- **Every `Money` operation now agrees about what the same currency is.** `plus`, `minus`, `times` and `div` matched on `code` while `equals` and `compareTo` matched on whole-`Currency` equality, so a pair of amounts assembled from different sources **added fine and then threw `IllegalArgumentException` from `sorted()`**, which reads as a data problem rather than an API one. All six guards now defer to `Currency` equality above, so the rule is defined once instead of restated six times. Relaxing `compareTo` alone would have been worse than the bug — it would have returned `0` for a pair `equals` called unequal, leaving a `TreeMap` and a `HashMap` disagreeing about how many keys they hold — so a test pins the `Comparable` contract, and another pins that no two entries in the shipped map share a code, since code-only equality depends on it. (#37)
- **`Outcome`'s accessors are renamed to match `kotlin.Result`.** `dataOrNull` is now `getOrNull`, `dataOrThrow` is `getOrThrow`, and `dataOrElse` is `getOrElse`. The old names are gone rather than deprecated, because the whole point of the change is that a caller reaching for `Outcome` is reaching for the type they cannot use across the language boundary and expects its vocabulary. Every one of these fails the build, so the compiler finds them for you. (#35)

  `Success.data` is deliberately **not** renamed to `value`: it is the accessor every existing `when` branch already reads. See [ADR-0004](docs/knowledge/decisions/outcome-over-kotlin-result.md) for why the rest of the vocabulary moved.

### ⚠️ Behavior Changes

- **`Gigabyte` and `Gigabit` gained the symbols every other prefix in their family already had.** `Gigabyte` was `GB` alone where `Kilobyte`, `Megabyte` and `Terabyte` lead with `KiB`, `MiB` and `TiB`; `Gigabit` was `Gb` alone where the others lead with `kbit`, `Mbit` and `Tbit`. Both now follow the rule: `Gigabyte` is `GiB` with `GB` as an alternative, `Gigabit` is `Gbit` with `Gb` as an alternative. `findAll("GiB")` and `findAll("Gbit")` returned an empty list before and now resolve, and `findAll("GB")` and `findAll("Gb")` still do, through the alternatives. **`Gigabyte.symbol` and `Gigabit.symbol` render differently**, which is not a compile error — anything printing a size sequence showed `1 KiB, 1 MiB, 1 GB, 1 TiB` and now shows `GiB`. Lookup is case-sensitive, which is what lets `GB` and `Gb` stay distinct. A test now states both families as a rule rather than entry by entry, so a prefix added later is held to it. (#36)

- **`localizedDisplayName` returns real translations on `js` and `wasmJs`, where it had returned English for every locale.** Every lookup on those targets had been failing since the feature shipped in 3.4.0, and 4.0.0 shipped the same defect. Anything rendering locale names on web changes output, with no compile error and no deprecation to warn you — a French UI that has been showing "German" starts showing "allemand". Snapshot tests over rendered names on those targets will fail, and that failure is the fix arriving. (#33)
- **A few names change on `jvm` and `android`**, because the JVM lookup was built from the language and region alone and silently discarded script subtags: `sr-Latn` was named as though it were `sr`. It is now built from the full language tag. (#33)
- **`localizedDisplayName` still falls back to English, and that is unchanged** — what changed is that the fallback is now reachable. See the new `localizedDisplayNameOrNull` below, and [ADR-0005](docs/knowledge/decisions/completeness-over-consistency-for-display-names.md) for why English is treated as an untranslated answer rather than a wrong one.

### Added

- **`Coordinates.toGeoPoint()`**, the conversion that cannot be got wrong. `Coordinates` takes latitude first, as the pair is spoken; a GeoJSON position takes longitude first, per RFC 7946 §3.1.1. Both values are `Double`, so hand-converting compiles whichever way round it is written and silently puts the point in the wrong hemisphere. `Coordinates.accuracy` is dropped, because GeoJSON defines no member for it — a position's optional third element is altitude, not an error estimate. A `GeoPoint(Coordinates)` constructor was considered and declined: GeoJSON's own order is correct for `GeoPoint`, and a constructor does not carry the reordering in its name. (#34)

- **`Outcome.isSuccess`, `Outcome.isFailure` and `Outcome.exceptionOrNull()`**, matching `kotlin.Result`, for callers that want to test or read a failure without a `when`. (#35)
- **`Outcome.getOrDefault(defaultValue)`**, the form of `getOrElse` that ignores why the operation failed. (#35)
- **`Locale.localizedDisplayNameOrNull(displayIn)`**, which performs the same lookup without the English fallback and returns `null` when no genuine translation exists. `localizedDisplayName` is unchanged and now routes through it. Use the nullable form wherever showing the wrong language is worse than showing nothing — a language picker, say — and the throwing-free original where any readable name will do. (#33)
- **`Locale.currentOrNull` and `Currency.currentOrNull`**, beside the existing `current`. Both `current` properties are `requireNotNull` over a platform lookup and throw `IllegalArgumentException` when the platform cannot answer, which makes them a poor default argument, a poor property initialiser and a poor thing to read during composition. Neither `current` changes behaviour; the nullable forms are additions. (#33)
- **Bundled display names, so a platform's gaps are filled rather than papered over.** Platform CLDR coverage is uneven and the platforms lack *different* pairs — each lacks 1,084 to 1,266 of the 17,889 pairs the library ships — so a table assembled from what they collectively know covers all but 21. A **1,565-name supplement** ships to every target and is consulted only when the platform returns nothing. Linux has no system CLDR at all, so it alone receives the **full 16,797-name matrix**. Both are packed as one string per display language and parsed lazily, per language, on first use. (#20)
- **Per-platform coverage tests that fail when a platform's CLDR moves.** Platform data changes with OS and toolchain releases, and it changes silently — a platform that loses a language renders English, which looks like a working library. `JvmCoverageTest` and `AppleCoverageTest` each pin the platform's own gap count and how many pairs remain untranslated after the supplement, so the change is reviewed rather than discovered. An `appleTest` source set was added to run the second of them. (#20)
- **A dependency skill, shipped inside the sources jar**, correcting what an agent is most likely to get wrong about this library — including that display names must never be compared across platforms. (#20)

### Fixed

- **`Intl.DisplayNames` is constructed correctly on `js`.** Two compounding faults, each hidden by a `catch (Throwable)` that made the failure indistinguishable from a working fallback: a `js(...)` snippet cannot reference a captured local, because the compiler renames it; and Kotlin/JS re-emits such a snippet rather than passing it through, losing the `new` operator, so construction failed with "Constructor Intl.DisplayNames requires 'new'". The call now goes through `Reflect.construct` — which is also CSP-safe, unlike a `Function` factory — and takes the tags as parameters. (#33)
- **The platforms' own silent English fallback is now detected on `jvm`, `android` and Apple targets.** Asked for a name in a language it does not carry, neither the JDK nor `NSLocale` reports failure — each renders the English form, so a real English word comes back and the library had no way to tell it from a translation. The previous miss detection looked for an untranslated subtag echoed back, a different and much rarer failure, so it almost never fired. Each bridge now compares against the English rendering, with English itself exempt because the comparison is meaningless there. (#33)
- **Two `@throws` tags named `IllegalStateException`** where `requireNotNull` throws `IllegalArgumentException`. (#33)
- **Documentation corrections found by auditing every KDoc block before release.** Four were wrong rather than merely thin, and each would have misled a caller reading it at the moment of writing the call: `localizedDisplayNameForNative` still said platforms without CLDR "like Linux" return `null`, which stopped being true when Linux gained the bundled matrix; `currentNativeLocale` claimed to delegate to a `getCurrentNativeLocaleImpl` that does not exist and pointed `@see` at a `Locale.Companion.getCurrent` that does not either; `GeoBoundingBox` told the reader to assign `toDoubleArray()` to a `bbox` property that is a `List<Double>?`, which does not compile — `toBbox()` is the one that fits; and `UnitOfMeasure` described its byte units with the pre-2.0.0 `KILOBYTE` spelling and asserted an IEC-symbol rule that `Gigabyte` does not follow. (#35)
- **`@throws` documentation for the arithmetic that can refuse.** `BigDecimal.divide` rejects a non-terminating quotient, `setScale` without a rounding mode rejects dropping a non-zero digit, `valueOfExact` rejects `NaN` and infinity, and `BigInteger.mod`, `modInverse`, `modPow`, `divideAndRemainder`, `testBit`, `parseString` and `fromByteArray` each have conditions they raise on. None of that was written down. `Money.times(Money)` was also missing the currency-mismatch `@throws` its siblings carry, and `Money.compareTo` now records that it matches on whole-`Currency` equality where the arithmetic operators match on ISO code (#37). (#35)
- **The generated display-name tables each disagreed with themselves about how many names they hold.** Both declared a hard-coded `size` while their own header comment gave a different count, and both comments claimed the constant existed for a test to assert against when no test read it. Both counts are now derived from the payload, so a count cannot contradict what it counts, and the supplement's is pinned by a test. Internal only — no published signature changes — but the wrong figures were visible in the sources jar, and they had also reached ADR-0005 and the regeneration guide, both now corrected. (#20)
- **`NOTICE.md` and `THIRD-PARTY-NOTICES.md` stated that no CLDR data was embedded**, which stopped being true the moment the tables shipped. Both are corrected, the Unicode License v3 is reproduced verbatim, and the provenance is recorded: Unicode CLDR 48.2.0, pinned by version and npm integrity hash, with the 613 pairs upstream CLDR lacks taken from platform CLDR implementations. (#20)

### Changed

- **The README explains why completeness is pursued and consistency is not.** Of the pairs every platform can name, Apple and a browser disagree on wording for 40.8% and the JVM and Apple for 9.6% — `nl-BE` is "Nederlands (België)" on two platforms and "Vlaams" on the third. Identical wording across targets is not a property this library offers: compare locales, never their rendered names, and keep rendered names out of cache keys and snapshot tests. (#20)
- **New records**: [ADR-0005](docs/knowledge/decisions/completeness-over-consistency-for-display-names.md) on choosing completeness over consistency, [RAD-0002](docs/knowledge/research/delivering-display-names-where-the-platform-has-no-cldr.md) on delivering names where the platform has no CLDR, and a [regeneration guide](docs/knowledge/guides/regenerating-display-name-tables.md) for the two generated tables. (#20)
- **The test workflow assembles the publications**, so a packaging break surfaces in CI rather than at release. (#20)

## [4.0.0] - 2026-09-21

A breaking release. It removes names deprecated across the 2.x and 3.x lines, corrects operators and symbol lookups that returned confident wrong answers, and brings the GeoJSON types into line with RFC 7946.

### ⚠️ Breaking Changes

- **`Outcome.Error` and `Outcome.error(...)` are removed.** Renamed to `Outcome.Failure` and `Outcome.failure(...)` in 3.4.0, where the old names survived as deprecated aliases. (#29)
- **`Locale.toLanguageTag()` is removed.** Deprecated since 2.2.0 in favour of the `Locale.languageTag` property. (#18)
- **`Money` equality is now numeric.** `Money(5.1, usd) == Money(5.10, usd)` is `true` where it was `false`. Scale is still preserved in storage and serialization — `5.0100000` is stored and serialized unchanged — but takes no part in equality, because no arithmetic in this library can make two spellings of one amount differ in value. Code relying on equality distinguishing `12.50` from `12.5` changes meaning **silently, with no compiler error**. A consequence: two equal amounts can serialize differently. Compare `value.scale` explicitly to ask whether two amounts were *written* the same way. `BigDecimal` is unchanged and keeps JDK-style scale-sensitive equality. (#21)
- **`Distance` and `Speed` throw instead of clamping to zero.** `Distance(3m) - Distance(5m)` returned `0m`; it now throws. The same applies to a negative divisor on `Distance`, and to negative scalars in `Speed.times` and `Speed.div`. The constructors already rejected negative values, so the operators now enforce the same invariant rather than inventing one. This reverses the change made in 3.4.0, which replaced the throwing behaviour with clamping: clamping turns an arithmetic mistake into a plausible measurement that nothing downstream can tell from a real one, which is the harder failure to find. (#25)
- **`Distance.times(Distance)` is removed.** Metres times metres is an area, and there is no area type; returning a `Distance` labelled with square metres type-checked and was wrong. (#25)
- **`Distance.div(Distance)` now returns `Double`.** A ratio of two lengths is dimensionless. This one breaks at call sites that did not change — anything storing the result in a `Distance` stops compiling. (#25)
- **`GeoBoundingBox` is no longer a `GeoGeometry`.** RFC 7946 makes a bounding box a `bbox` member, not a geometry, and the old modelling serialized to invalid GeoJSON while allowing a bounding box anywhere a geometry was expected. The type remains, with `toBbox()` for the RFC form. Exhaustive `when` expressions over `GeoGeometry` will fail to compile — at the `when`, not at the line that changed. (#23)
- **`GeoFeature.properties` is now `JsonObject?`**, was `Map<String, String>?`, and **`GeoFeature.id` is now `JsonPrimitive?`**, was `String?`. RFC 7946 permits any JSON value in properties and a string or number for id, so the library could not previously round-trip GeoJSON it did not produce itself. Read values with the new accessors: `feature.stringProperty("name")`. (#22)
- **Invalid GeoJSON geometries are rejected on construction and on deserialization.** RFC 7946 §3.1 structural rules are enforced: minimum position counts, positions of at least two elements, and linear rings closed and at least four positions long. Payloads that previously decoded into structurally invalid values now throw. Winding order is deliberately **not** enforced on parse — §3.1.6 instructs parsers not to reject polygons breaking the right-hand rule — so a clockwise exterior ring round-trips unchanged. (#24)
- **`UnitOfMeasure.FoodCalorie` is removed.** A food calorie *is* a kilocalorie; two enum entries modelled one unit. Use `UnitOfMeasure.Kilocalorie`. Symbol lookups are unaffected. (#28)
- **`UnitOfMeasure.findFirst` refuses ambiguous symbols.** `findFirst("gal")` returned `Gallon` by declaration order with nothing to signal that `GallonImperial` also claimed the symbol — a silent 20% error in an imperial market. It now returns `null`; use `findAll` to discover the candidates. `findAll` returns an empty list for an unknown symbol and more than one entry for an ambiguous one, so the two cases stay distinguishable. (#28)
- **`Gallon.symbol` is now `"US gal"`**, was `"gal"`. Anything rendering `.symbol` produces different output, and this is not a compile error. `Calorie.symbol` is now `"cal"` and `Kilocalorie.symbol` is `"kcal"`, matching the standard casings. (#28)
- **`LazyMap`'s `cache` constructor parameter is removed.** The cache is now internal and atomic. (#27)
- **`GeoUri` equality follows RFC 5870 §3.4.4 rather than comparing members.** The specification defines when two `geo` URIs identify the same location: an omitted `crs` and an explicit `"wgs84"` are the same CRS (§3.4.1), and the label is case-insensitive (§3.3). `GeoUri(48.2, 16.3, crs = null)` now equals `GeoUri(48.2, 16.3, crs = "WGS84")`, where both previously compared unequal — so sets, map keys and `distinct()` no longer treat one location written three ways as three locations. The value supplied is still preserved and still serialized; only the comparison is normalized. `effectiveCrs` reports the CRS actually in force, lowercased.

### Added

- **`Urn` equality follows RFC 8141 §3.1.** The r-, q- and f-components no longer take part, because the specification requires it: *"If an r-component, q-component, or f-component (or any combination thereof) is included in a URN, it MUST be ignored for purposes of determining URN-equivalence."* `urn:isbn:0451450523#page1` now equals `urn:isbn:0451450523`, where they previously compared unequal — those components locate or qualify a resource, they do not identify a different one. The NID remains case-insensitive and the NSS case-sensitive, as before. All five values are still preserved and serialized.
- **`Uri` and `Url` equality follows RFC 3986 §6.2.2.1.** Scheme and host are case-insensitive and are now compared as such; path, query, fragment and user information stay case-sensitive. Previously case normalization happened only in the parser, so a parsed URL and a directly constructed one for the same address compared unequal. The supplied case is still preserved and serialized.
- **URI parsing**: `url(...)`, `uri(...)`, `geoUri(...)` and `urnOrNull(...)`, each with an `OrNull` counterpart. Previously only `urn(...)` could read a string back, so half of RFC 3986 and RFC 5870 support was missing — and it was the half consumers meet first, since a URI almost always arrives as text from elsewhere. Malformed input throws **`UriParseException`**, which extends `IllegalArgumentException`, so existing `catch` blocks keep working while callers who want to name the failure now can. `urn(...)` is retro-fitted to throw it too. Scheme and host are lowercased on parse, and so is a `geo:` URI's `crs` label, all three being case-insensitive with lowercase preferred; nothing else is normalized — percent encoding is preserved and dot segments are left alone, since resolving them changes what a path denotes. Failure as data needs no second API: `runOutcome { url(text) }` yields an `Outcome<Url>` carrying the exception. (#26)
- **`Money` ordering**: `Money` implements `Comparable<Money>`. Comparing amounts in different currencies throws, since that ordering is not meaningful. (#21)
- **GeoJSON winding tools**: `Winding`, `GeoPolygon.windingOf(ringIndex)`, `GeoPolygon.rewound()`, and the `geoPolygon(...)` / `geoPolygonRewinding(...)` factories. The first rejects rings breaking the right-hand rule; the second accepts any winding and returns it corrected — the function to reach for after reading a foreign document. (#24)
- **`GeoFeature` property accessors**: `stringProperty`, `intProperty`, `doubleProperty`, `booleanProperty`, `objectProperty`, `arrayProperty`. None throws. `stringProperty` renders objects and arrays as their JSON text rather than returning `null`, so a property that is set never looks unset; it returns `null` only for an absent key or a JSON null, which `properties?.containsKey(key)` tells apart. (#22)
- **`GeoBoundingBox.toBbox()`**, giving the RFC 7946 flat-array form to assign to a geometry's or feature's `bbox`. (#23)
- **`BigInteger.bitLength()`**, matching `java.math.BigInteger.bitLength` including the two's-complement rule that makes a negative power of two one bit shorter than its magnitude. It answers "how large is this, roughly" in constant time, which is what lets comparisons avoid work. (#30)
- **Benchmarks**: a `:benchmarks` module measuring `BigInteger` and `BigDecimal` against `java.math`, run on demand with `./gradlew :benchmarks:benchmark`. It is a separate Gradle module so its JMH dependencies cannot reach the published artifact. Performance had no coverage at all before this, so a change could be correct and an order of magnitude slower and nothing would say so. (#19)

### Performance

Benchmarking found three costs, all fixed here. None changes an observable result — only the time taken to produce one. Figures are relative to `java.math`, where 1.0x is parity and below 1.0x is faster than the JDK. (#30)

- **Text conversion is no longer quadratic.** `BigInteger.toString` and string parsing peeled or accumulated one digit at a time, costing a full-width division or multiplication per digit; both now split the work in half recursively, and small values are handled in `Long`-sized chunks rather than one digit at a time. `toString` went from 16.1x to **0.8x** at 4096 bits, and parsing from 18.2x to **1.0x**. `BigDecimal.toString` and `toDouble` inherit the improvement, reaching **0.9x** at 400 digits from 9.0x and 7.2x.
- **Powers of ten are cached.** Every operation that brings two scales together multiplies through a power of ten, and each call rebuilt that value from scratch — across `compareTo`, `add`, `subtract`, both `divide` overloads and both `setScale` overloads. `setScale` improved from 7.9x to **1.0x** at 8 digits, and scaled division from 3.8x to **1.2x**.
- **`BigDecimal.compareTo` decides most comparisons without aligning scales.** It multiplied one side up to the other's scale before comparing, even when the sign or the sheer difference in magnitude already settled it. Where magnitudes are separable this went from 67.5x to **1.7x** at 400 digits; where they are within a digit of each other it still aligns, which is the only case that can need it.

### Fixed

- **`LazyMap` is safe for concurrent access.** Its cache was an unsynchronized `LinkedHashMap` reachable from `currencyFor(...)` and `localeFor(...)`, so two coroutines on a multi-threaded dispatcher could drop an entry or catch the map mid-resize — after which a later read returned a wrong value on a key that had been written correctly. The cache is now an immutable map in an atomic reference, replaced by compare-and-set. A cache hit costs one atomic read. Under contention a value function may run more than once, so value functions must be pure; every caller still observes the same value. (#27)
- **GeoJSON serialization tests compare structure rather than text.** A `Double` renders as `100.0` on the JVM and `100` on JS and Wasm, so the same correct payload passed on one target and failed on another. (#17)

### Changed

- **`LazyMap` uses the standard library's atomics, and the dependency list is unchanged.** The cache is held in a `kotlin.concurrent.atomics.AtomicReference`, so every POM still lists only `kotlinx-datetime`, `kotlinx-serialization-json` and the Kotlin standard library, on every target. The atomic is a private field and the experimental opt-in does not reach callers. An earlier approach used the `kotlinx-atomicfu` compiler plugin, which achieved the same clean POM by transforming the atomics away; it was dropped because the plugin has to be kept in step with both the Kotlin version and the Android Gradle plugin, and a published library gains nothing from that coupling.

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
