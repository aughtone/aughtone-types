# Delivering Display Names Where the Platform Has No CLDR

RAD-0002 · 2026-09-27
Keywords: localized timezone names, Eastern Time in French, EST HNE abbreviations,
          does IANA tzdb contain translations, where do timezone display names belong,
          types or format, should we split types-timezone, separate artifact for locale
          data, linuxMain bundled tables, why does Linux show English, coverage versus
          consistency, how big is the CLDR matrix
Measured against: JDK 26.0.2.1 CLDR, the 202-entry `localeResourceMap` and the 604 zone
          ids from `ZoneId.getAvailableZoneIds()`, across the 89 display languages the
          resource map covers. gzip at maximum compression. macOS arm64, 2026-09-27.
          String-payload sizing only: no compiled artifact, bundler output or Kotlin/Native
          build was measured.

## Question

A downstream formatting library was asked for generic timezone display names by IANA id — "Eastern Time", "heure de l'Est" — from offline CLDR metazone tables, and proposed that this belongs in the shared types library on the grounds that it is identity-to-localized-name reference data, the same shape as the existing locale display names.

That is one question wearing another. Underneath it are three: whether timezone names are the same shape as locale names, where the data should live, and what it costs to ship it on a platform that has none.

The second and third are not specific to timezones. They are the same questions [RAD-0001](javascript-and-wasm-delivery-of-bundled-display-names.md) left open for locale names, arriving a second time in a different data domain — which is the reason to answer them properly rather than twice.

## Trail

### The proposed source does not contain the data

The IANA time zone database was named as where the data comes from. It is not. tzdb holds zone identifiers, UTC offsets, transition rules and English-only abbreviations in the zone files' `FORMAT` field. It carries no translations of anything.

Every localized name — long, short, generic or specific — comes from CLDR, a separate project on a separate release cycle. A tzdb release changes when clocks move, not what zones are called: release 2026d, 2026-09-11, moved Canada's Northwest Territories to permanent −06.

This matters beyond pedantry, because the two sources have different shapes, different sizes and different reasons to be bundled. Conflating them makes "ship the tzdb" sound like a small, well-defined task when the data actually wanted is the large, open-ended one.

The library also already depends on `kotlinx-datetime`, which supplies zone identifiers and rules. Nothing about zones-as-zones is missing.

### Two kinds of timezone name, and only one is keyed by zone

The proposal assumed a table keyed by IANA id, which implies no dependency on a date or time. That holds for **generic** names and fails for **specific** ones.

Measured for `America/Toronto`:

| locale | winter | summer | generic |
|---|---|---|---|
| en-US | EST | EDT | ET |
| fr-CA | HNE | HAE | HE |
| fr-FR | EST | EDT | ET |
| de-DE, ja-JP, ru-RU, es-ES, pt-BR | EST | EDT | ET |

`EST` against `EDT` depends on whether daylight time is in effect at a moment, so a specific abbreviation is a function of *(zone, instant, locale)*. No table keyed by zone alone can produce one. Generic names — "ET", "Eastern Time" — are a function of *(zone, locale)* and fit a reference-data library without reservation.

The split is therefore not arbitrary: **generic names belong wherever locale display names belong, and specific abbreviations belong wherever an instant already is**, which is the formatting library rather than the types library.

Two further observations from the same measurement. Localized abbreviations barely exist: outside `fr-CA` every locale sampled returns the English `EST`/`EDT`, because CLDR deliberately withholds short forms where they would mislead — `CST` is US Central, China Standard and Cuba Standard. And a preference for short forms, which is a reasonable one because they are what people recognise, therefore delivers a foreign abbreviation to most non-English readers rather than a translated one.

### What the platforms already provide, and where they stop

Display names are resolved today by delegating to each platform's own CLDR: `java.util.Locale` and `java.time.ZoneId` on JVM and Android, `NSLocale` and `NSTimeZone` on Apple, `Intl.DisplayNames` and `Intl.DateTimeFormat` on JS and Wasm. Linux native has no system CLDR and returns nothing, falling back to English.

The browser case is worth stating plainly because it is routinely assumed to be the weak one: **it is not**. `Intl` is complete and costs nothing to use. Shipping a table to JS means paying download size for data the runtime already holds, which is strictly worse than delegating. The weak target is Linux.

Platform CLDR is also incomplete in ways that are invisible until measured. Over the 202 shipped locales against 89 display languages — 17,978 pairs — roughly 5% have no localized data and silently render in English, concentrated in particular languages rather than spread thin. The same holes appear in the timezone data: asked for `America/New_York` in Inuktitut or Tibetan, the JDK returns "New York" rather than a localized zone name, and those are the same two languages that top the locale-name gaps.

That correspondence is the useful part. It is one CLDR coverage hole showing through two APIs, so it will not be fixed twice, and a decision about one data domain is really a decision about both.

### Coverage and consistency are different goals

Bundling tables was originally motivated by consistency: identical strings on every target and OS version, the property the library exists to provide elsewhere. Platform delegation cannot deliver that, because four CLDR versions drift independently.

But delegation plus a fallback for the platform that has nothing delivers something else, which is **coverage**: every platform shows something sensible in the reader's language, and none shows English to a French reader.

These read as the same goal and are not, and the distinction decides the design:

- **Consistency** requires tables in `commonMain`, shipped to every target including the browser, at a cost measured below.
- **Coverage** requires tables only where the platform has none.

Consistency was judged not worth its price: acceptable for an application on a desktop, not for a mobile application or a web page. Coverage was adopted.

The English display names already in the resource map are sometimes cited as precedent for bundling translations. They are a weaker precedent than they appear — they are identity data with a name attached, and a last-resort fallback, not a translation table.

### Putting the data only where it is needed

Because Kotlin Multiplatform compiles a source set into the targets it belongs to, a table placed in `linuxMain` is present in the Linux artifact and absent from the JVM, Android, Apple, JS and Wasm artifacts. The platform-delegating function is already an `expect`/`actual` pair whose Linux implementation returns null; consulting a bundled table instead is a change to one `actual` with no effect on the shared API.

This dissolves the objection that dominated RAD-0001 rather than reducing it. The cost figures there were the cost of bundling into `commonMain`. On this route the browser pays nothing, and the cost lands only on the target where it is affordable — which happens to be the same target that needs it, since the platform lacking CLDR is the desktop one.

RAD-0001 listed a Linux-only table among the things that would change its answer. It is recorded here as the chosen design rather than as an alternative, and the framing matters: presented as a choice against bundling everywhere it looks like a trade, and on this route there is nothing to trade.

### The dead end: a separate artifact

Publishing the data as its own library — `types-timezone`, and by symmetry `types-locale` — was examined twice and rejected twice, for different reasons each time. Both are worth recording, because the first set of reasons no longer applies and could be mistaken for still binding.

RAD-0001 rejected it because it reintroduces inconsistency by configuration, and because a multiplatform consumer must declare the dependency in `commonMain`, which ships it to every target anyway. **Both are now void**: consistency is no longer the goal, and on the `linuxMain` route the data does not reach other targets regardless of how artifacts are arranged.

Three fresh arguments were then offered for splitting, and each failed against how the project actually builds and publishes:

- **Independent release cadence**, so a table refresh does not force a version bump of unrelated types. Void: a single `versionName` in the version catalog drives every publication and they ship together, so any refresh triggers a full publish regardless.
- **Keeping a datetime dependency out of core types.** Void: `kotlinx-datetime` is already an `api` dependency of `commonMain`, so there is nothing to keep out.
- **Reducing what consumers download.** Void: source sets already do this, and artifact boundaries do not change what a target contains.

A fourth argument is specific to locale data and rules out splitting it on its own terms rather than on cost. `Locale` is load-bearing for `Currency`, which resolves through it, and `Money` carries a `Currency`. Extracting locale would make the core library depend on the extracted one, so every consumer would acquire it anyway — an additional artifact, version and release for no isolation.

What survives is the observation that a Linux consumer who never touches timezones still carries the table, because a runtime-keyed lookup cannot be eliminated. At desktop scale this is not worth doubling the published artifacts from nine to eighteen, each signed, validated and version-aligned.

### What the data costs, and the constraint that is not size

Sizing the full matrix, grouped where zones share a name vector:

| | raw | gzipped |
|---|---|---|
| Locale display names, 202 × 89 | 445 KB | 107 KB |
| Timezone long generic, 604 × 89, grouped | 1,060 KB | 195 KB |
| Timezone short generic, grouped | 84 KB | 9 KB |
| **Total** | **~1.6 MB** | **~311 KB** |

Ungrouped, the long timezone table is 1,437 KB raw and 246 KB gzipped, so collapsing zones that share a name vector saves about 20%. That grouping is a crude proxy for CLDR metazones — 604 zones collapse to 455 distinct name vectors, against roughly 180 real metazones — so proper metazone modelling should beat these figures.

Short generic names compress to almost nothing for a reason that is not a virtue: they are largely un-localized. `ET` is returned for English, French, Spanish, German, Japanese and Russian alike, so the table is one string repeated plus GMT offsets. A short-name table is cheap because it is nearly empty of translation, and it cannot be shipped alone on a platform with no CLDR, since there is nothing to fall back to for everything it omits.

**The binding constraint is not size but code generation.** The full matrix is roughly 72,000 strings. Emitted as ordinary Kotlin — one literal per name — that is 72,000 declarations compiled by Kotlin/Native, the slowest compiler in the toolchain, and a plausible build-time or codegen-limit problem entirely separate from how many bytes result. The shape that avoids it is a small number of large string constants with an offset index: identical bytes, three or four orders of magnitude fewer declarations.

RAD-0001 rejected resource files rather than generated code, on the grounds that browsers cannot load them synchronously. That objection is about JS and does not reach a table shipped only to Linux, which has ordinary file I/O. Kotlin/Native has no standard resource mechanism, so embedding is still the likely route, but the blob-and-index encoding is what makes embedding tractable.

### An aside that removes a recurring worry

The published JS artifact is 319 KB, which invites the conclusion that the library is heavy on the web. It is not evidence of that. A `.klib` is compiler input, and roughly 93% of it is intermediate representation — function bodies, signatures, string pool, and 109 KB of debug information that never reaches a bundle. JavaScript is emitted and dead-code eliminated at the application link step.

Declaration weight by package puts GeoJSON first and locale fifth, so locale data is not what makes the artifact large either.

The asymmetry is the point, and it is the same one that governs everything above: **the library's own types are eliminable and a translation table is not**. An application using only monetary types never references the geometry types and does not ship them. A table reached by the reader's display language keeps every entry live. That is why 319 KB of klib is not a cost and 107 KB of tables would be.

## Findings

**Measured.** IANA tzdb contains no translations; all localized names come from CLDR. Verified against the release notes for 2026d and against the fact that every name measured here came from the JDK's bundled CLDR rather than from zone data.

**Measured.** Specific timezone abbreviations are instant-dependent: `America/Toronto` renders HNE in January and HAE in July for fr-CA, EST and EDT for en-US. Generic names are not, and are keyed by zone and locale alone.

**Measured.** Localized short forms barely exist. Of the locales sampled only fr-CA differs from the English abbreviation; `ZoneId.getDisplayName(SHORT, …)` returns `ET` identically across en, fr, es, de, ja and ru.

**Measured.** Platform CLDR coverage is incomplete and the gaps coincide across data domains. About 5% of locale-name pairs have no data, worst in Inuktitut and Tibetan, and those same two languages return a city name rather than a zone name for `America/New_York`.

**Measured.** The full matrix is approximately 1.6 MB raw and 311 KB gzipped, of which the long timezone table is the largest part at 195 KB gzipped after grouping.

**Measured.** The published JS artifact is 93% intermediate representation, including 109 KB of debug information, and its declaration weight is dominated by geometry rather than locale data.

**Established by argument.** A table in `linuxMain` reaches only the Linux artifact, so the browser cost of bundling is avoidable without abandoning bundling. This follows from how Kotlin Multiplatform compiles source sets and was not separately verified against a published artifact.

**Established by argument.** Splitting the data into its own published library achieves nothing here: cadence is fixed by a single shared version, the datetime dependency is already exposed, source sets already control what each target carries, and locale cannot be extracted at all because the financial types depend on it.

**Assumed, not measured.** That 72,000 generated string literals would strain Kotlin/Native compilation. The count is arithmetic from the measured matrix; the compiler behaviour is inferred and should be checked with a generated sample before committing to an encoding.

**Assumed, not measured.** That grouping by CLDR metazones proper would beat the 195 KB achieved by grouping on identical name vectors. The metazone count is quoted from CLDR's own structure rather than derived here.

## Recommendation

**Deliver coverage by delegation, and bundle only where the platform has nothing.**

- Resolve display names through platform CLDR on JVM, Android, Apple, JS and Wasm, as the locale names already do. On the browser this is not a compromise but the better option, since `Intl` is complete and bundling would duplicate it.
- Fill the Linux gap with a table in `linuxMain`, reached from the existing `actual`. No shared API changes and no other target is affected.
- Keep generic timezone names with the locale display names. Put specific abbreviations in the formatting library, where an instant is already in hand.
- **Do not split into separate published libraries.** Revisit only if release cadence genuinely diverges, which a major version would be the natural moment for.
- **Do not bundle into `commonMain`.** That is the design whose cost was measured and declined.

Accept explicitly that this delivers coverage and not consistency: four CLDR versions will word things differently and drift apart. Nobody sees English who should not; nobody sees byte-identical output across platforms either.

Before generating anything, settle the encoding with a small experiment: emit a representative slice as individual literals and as a blob with an offset index, and compare Kotlin/Native compile time. The measured sizes above say the table is affordable; nothing here says it is compilable in the obvious shape.

What would change the answer:

- If Kotlin Multiplatform gains a resource mechanism that serves every target synchronously, the generated-code route stops being necessary and this reasoning is moot.
- If consistency becomes worth its price — a desktop-only consumer, say, where the browser cost never applies — `commonMain` bundling returns as the option, at the figures recorded above.
- If a target without CLDR is added that is not desktop-shaped, the affordability argument fails and the table needs a shared source set declared through the default hierarchy template rather than copying into a second leaf.

This supersedes RAD-0001's rejection of a Linux-scoped table and its rejection of a separate artifact, the latter now resting on different reasoning. It does not disturb that document's finding on composition, which remains measured and unchanged.
