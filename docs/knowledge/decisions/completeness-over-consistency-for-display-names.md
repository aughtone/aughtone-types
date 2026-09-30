# Completeness Over Consistency For Display Names

ADR-0005 · 2026-09-28 · Status: accepted
Keywords: why do locale names differ between platforms, display name says English
          on Linux, localizedDisplayName fallback is silent, why not bundle CLDR
          tables, coverage versus consistency, cross-platform merged name table,
          detecting a platform's own English fallback, per-platform coverage test,
          supersedes platform CLDR delegation
Measured against: JDK 26.0.2.1, Apple CLDR via iosSimulatorArm64, browser CLDR via
          headless Chrome for js and wasmJs. The 202-entry `localeResourceMap`
          against its 89 display languages, 17,978 pairs. macOS arm64, 2026-09-28.

## Context

[ADR-0003](localized-display-names-via-platform-cldr.md) resolved localized display names to the platform's own CLDR, recording that wording might vary between platforms and that Linux, having no system CLDR, would fall back to English. Both were recorded as acceptable and neither was measured.

Measuring them changed the decision. Three findings, in ascending order of importance.

**The fallback was invisible.** `localizedDisplayName` returns the English name when the platform cannot answer, and a caller had no way to tell that from a translation. Worse, the platforms themselves hide the miss: asked for a name in a language they do not carry, the JVM and Apple do not report failure — they render the English form, so a real English word comes back. The library's own miss detection looked for an untranslated subtag echoed back, which is a different and much rarer failure, so it almost never fired.

**One target was entirely broken.** On JS every lookup had been failing since the feature shipped, and returning English for all 17,978 pairs. Two compounding causes, each hidden by a `catch (Throwable)`: a `js(...)` snippet cannot reference captured locals, because the compiler renames them; and Kotlin/JS re-emits such a snippet rather than passing it through, losing the `new` operator, so construction failed with "Constructor Intl.DisplayNames requires 'new'". Nobody noticed for two releases, because a silent fallback to English looks exactly like a library that works.

**Consistency was never on offer.** Of the pairs all of JVM, Apple and the browser can name, they disagree on **40.8%** between Apple and the browser and **9.6%** between the JVM and Apple. Not edge cases: `nl-BE` is "Nederlands (België)" on two platforms and "Vlaams" on the third; Afrikaans renders Cyrillic as "Sirillies" or "Cyrillies" depending on who is asked. This is CLDR version skew, it is not closing, and no amount of delegation removes it.

Coverage itself is uneven but not catastrophic — each platform lacks 6–7% of pairs — and, critically, **the platforms lack different ones**.

## Decision

**Pursue completeness and abandon consistency**, explicitly, as two goals that were being conflated.

- **Completeness** — every platform can name every locale in the reader's language — is achievable, and is what a caller actually needs.
- **Consistency** — every platform produces the same string — is not achievable by delegation, and achieving it another way means bundling the full matrix everywhere, at a cost already measured and declined in [RAD-0001](../research/javascript-and-wasm-delivery-of-bundled-display-names.md).

Four consequences follow.

**Keep delegating to platform CLDR.** It is free, it is correct where it answers, and on the browser it is better than anything shippable: `Intl` is complete and bundling would duplicate it.

**Make the miss visible.** `localizedDisplayNameOrNull` performs the same lookup without the English fallback, so a caller can decide for itself whether to substitute, mark or omit. `localizedDisplayName` is unchanged for callers who want any readable name. Each platform's bridge now detects its own silent fallback by comparing against the English rendering, with English itself exempt because the comparison is meaningless there.

**Fill the gaps from one merged table, not per platform.** Each platform lacks 1,084 to 1,266 pairs of the 17,889 the library ships, and they lack largely different ones, so between them they can name all but **21**. A single table assembled from what the platforms collectively know therefore serves every target, and per-platform tables are unnecessary — one thing to keep in step rather than four.

**The table carries only genuine translations.** CLDR answers a great many untranslated entries with the English word, and 782 of the 2,467 candidate names were exactly that. Carrying them would have added bytes to produce precisely what the English fallback already produces, so they are dropped and the table holds **1,685** names. The visible result is identical either way; what differs is that `localizedDisplayNameOrNull` now reports `null` for them rather than returning English and implying a translation exists. That matches how the platform bridges already behave.

**Pin coverage with a test.** Platform CLDR changes under us with OS and JDK releases. A per-platform test records the measured gap count and fails when it moves, so a platform gaining or losing data is a reviewed change rather than a silent one.

## Consequences

**Callers get names everywhere, and still cannot rely on the exact string.** That is the trade, stated plainly: identical wording across platforms is not a property this library offers for display names, and code that depends on one — a snapshot test, a cache key, a string comparison — will break across targets. Compare locales, never their rendered names.

**Linux remains the outlier and needs the full matrix**, not the gap table, because it has no platform data at all. That is a larger artifact, it ships only in the Linux target, and it is tractable there in a way it is not in a browser bundle.

**Around 800 pairs have no translation anywhere, and that is not a hole.** Twenty-one can be named by no platform at all — every one of them naming Inuktitut, `iu-CA` or Tibetan — and a further 780 or so are named only in English by every platform that has them. All of these render in English, and every locale in the shipped map has an English name: `iu` is "Inuktitut", `iu-CA` is "Inuktitut (Canada)". English is not a wrong answer, only an untranslated one — it tells the reader something true in the default language rather than nothing, which is why `localizedDisplayName` falls back rather than returning `null`.

The count differs by target because it is the platform's own gaps intersected with what has no translation: 801 on JVM and Apple, 23 on JS and Wasm, whose holes fall in languages the native platforms do translate. The coverage tests record these, so they are visible rather than discovered later as a defect.

**These numbers were got wrong three times before the tests pinned them**, at 30, then 0, then 21, each from a different mistaken denominator — the union of nameable pairs rather than the full matrix, and unique tags rather than resource entries. The measurements are easy to take and easy to take wrongly, which is the argument for asserting them in a test rather than recording them in prose.

**The generator must run on every target.** No single platform can fill its own gaps — that is the whole reason the merged table works — so producing it means collecting from each and merging, rather than running a tool against one. That is more machinery than a JVM-side generator, and it is shared with the coverage test, which needs the same measurements.

**Regeneration is a documented procedure, not a single command** — see [regenerating the display-name tables](../guides/regenerating-display-name-tables.md). It collects from every target and merges, because no single platform can fill its own gaps.

**A generation step must emit a count and assert on it.** Not because the transport is unreliable — collecting through test stdout was measured as exact, once counted correctly — but because the first attempt appeared to lose about 85 rows per platform and did not. The rows were real: the shipped map holds two entries that build the same language tag, `zh` carrying a `Hans` script and `zh-Hans` itself, so 87 pairs legitimately occur twice and a parser keyed by tag collapses them. A declared count compared against a parsed one catches that in seconds; without it, a duplicate key is indistinguishable from a dropped row.

This supersedes [ADR-0003](localized-display-names-via-platform-cldr.md) on two points: that wording variance is acceptable because it is slight — it is not slight — and that the English fallback is an adequate answer for a platform with no data. Delegation as the primary mechanism, which that record chose, stands and is reaffirmed here.

## Amendment — 2026-09-29: the numbers in this record went stale, exactly as it warned

This record argues that measurements like these belong in a test rather than in prose, and then recorded several of them only in prose. Those are now wrong. The decision is unaffected; the figures are corrected here, and the ones that can be asserted now are.

**The supplement holds 1,565 names, not 1,685.** The arithmetic above is self-consistent — 2,467 candidates less 782 English-equal names is 1,685 — so 120 names were removed by some later trimming pass that nobody wrote down. Eleven of them were entries carrying raw subtags rather than names, such as `pa (Guru)` and `zh (Hans, CN)`, which were filtered deliberately. The remaining hundred-odd cannot be accounted for from here without regenerating, and are not worth regenerating to explain.

**Both generated files disagreed with themselves.** Each declared a hard-coded `size` while its own header comment declared a different count — the supplement said 1,565 and 2,467, the Linux table said 16,797 and 17,868 — and each comment claimed the constant existed "for the coverage test to assert against" when no test read it. Three numbers per file, no two of them agreeing, and nothing to catch it.

**Both `size` constants are now derived from the payload** with a `by lazy` count over the packed blobs, so a count cannot contradict what it counts, and `LocaleDisplayNameSupplementTest` pins the supplement's at 1,565 along with the 89 display languages. The Linux table's 16,797 remains unasserted: there is no `linuxTest` source set, and `linuxX64` cannot execute on an arm64 macOS host regardless.

**The untranslated counts are 823 on JVM and 910 on Apple**, as their coverage tests have asserted all along — not the "801 on JVM and Apple" recorded above, which is both wrong and wrong in shape, since the two platforms do not share a figure. The "23 on JS and Wasm" is unverified: no test pins it, and nothing here reproduces it.

**Mind the denominator, which is how the earlier counts went wrong three times.** `localeResourceMap` has 202 entries and 201 distinct language tags, because `zh` carrying a `Hans` script and `zh-Hans` build the same tag. The matrix is therefore 17,978 pairs by resource entry and 17,889 by tag, and the coverage tests deduplicate by tag. Quoting one denominator against the other is the specific error behind the 30 → 0 → 21 sequence recorded above.

The lesson is narrower than "write tests". A number in prose has no owner and no failure mode, so nothing stops a later change from invalidating it — which is what happened here, in the very document making that argument. A number worth recording is worth deriving from the thing it describes, and then asserting.
