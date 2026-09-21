# Delivering Bundled Locale Display Names on JavaScript and Wasm

RAD-0001 · 2026-09-20
Keywords: localized locale names cost too much in the js bundle, why not bundle CLDR
          display names, 90 display languages bundle size, tree shaking cannot strip
          locale tables, lazy loading does not help the browser, fetch resource files
          from a library, splitting display names into a separate artifact, composing
          display names from language and region parts
Measured against: the 202-entry `localeResourceMap` as of 3.4.0, Kotlin 2.4.0,
          measured 2026-09-20. The first pass was string-length arithmetic only. The
          proof of concept added 2026-09-20 generated the full 202 x 89 matrix from
          JDK 26 CLDR and gzipped it at maximum compression; still no bundler output,
          so boilerplate and minification are excluded.

## Question

Bundling display-name tables for every display language would make locale names identical on every target, which is what the library promises everywhere else. The cost of that is concentrated almost entirely on JavaScript and Wasm, where the scarce resource is download size rather than heap.

The question is whether there is a delivery mechanism that gives consistent names without imposing the full matrix on every browser consumer. The direction — bundled names, in the main artifact, for consistency — is settled. This is only about how they reach a browser.

## Trail

### What the matrix costs

Derived from the 202 entries already in `localeResourceMap`, at an average display-name length of 13.8 characters:

| Script of the display language | One table of 202 names |
|---|---|
| Latin (en, fr, de) | ~2.8 KB |
| Cyrillic / Greek | ~5.3 KB |
| CJK | ~7.2 KB |

At roughly 90 display languages that is **~245–360 KB of raw string data** depending on the script mix, before map structure — the upper half of the 100–400 KB range estimated in the decision on platform CLDR delegation. Natural-language text compresses well, so perhaps 100–150 KB gzipped.

### Lazy loading solves the wrong problem here

The approach recorded on the issue loads one table per display language on first access, so heap holds a single table at a time. That works on JVM, Android and Native.

It does nothing for JavaScript or Wasm. Every table ships in the bundle whether or not it is touched, because laziness governs *construction*, not *inclusion*. The constraint that actually hurts on those targets is untouched by the mechanism chosen to address it.

### Dead code elimination cannot strip the tables

The obvious mitigation is per-language objects that a bundler can drop. It does not work, and the reason is structural rather than technical: the feature renders a locale name in the **end user's** language, which is not known at compile time. A runtime lookup keyed by display language must reach every table, so every table stays live.

Per-language objects would be strippable only if the consumer hardcoded which display languages they would ever need, which is the feature deleted rather than delivered.

### A separate artifact was considered and rejected

Publishing the tables as an opt-in artifact was examined and rejected on two grounds.

It defeats the purpose. If consistency is the reason to bundle, then two consumers of the same library getting different strings depending on whether they added a dependency is inconsistency by configuration — the failure being eliminated, reintroduced through the build file.

It also does not reduce the cost for anyone who uses it, for the dead-code reason above. And a Kotlin Multiplatform consumer must declare the dependency in `commonMain` to call it from shared code, which ships it to every target including JavaScript — so it cannot be scoped to the targets that actually need it.

### Remote loading remains ruled out

Fetching tables over the network was rejected in the decision on platform CLDR delegation, and that reasoning still holds: browsers have no synchronous file I/O, so fetching forces a `suspend` API onto what is a property access, and a published library cannot know the URL its own files were deployed to. It would also make a locale name a failable, network-dependent call in a types library that has no networking.

### Structural redundancy — the option that survived

Display names are highly repetitive. "English (Australia)", "English (Belize)" and "English (Canada)" each store the word *English* again, and every display language repeats every region name across its own table.

Storing the parts and composing at runtime was measured against the existing English table:

```
202 entries, full composed strings   : 2790 bytes
 87 distinct language names          :  608 bytes
105 distinct region qualifiers       :  912 bytes
parts total                          : 1520 bytes   → 45.5% smaller
```

Extrapolated to 90 display languages: **~245 KB of full strings versus ~133 KB of parts.**

This is the only mechanism examined that reduces the cost without weakening consistency, without a suspending API, and without asking the consumer to know the end user's language at build time.

It is not free. Composition needs a per-display-language pattern, because word order and punctuation are not universal — a language that renders the region before the language name, or without parentheses, cannot use a single hardcoded `"$language ($region)"` template. Storing one pattern per display language is cheap; getting the set right is the work.

### Proof of concept: how much of the saving survives gzip

The question the rest of this document could not settle by argument was answered by building it. Two throwaway Java programs generated the full matrix — 202 locales against 89 display languages, the display languages being the distinct language codes already in the shipped table — in both representations, taking display names from the JDK's own CLDR. Each representation was then gzipped at maximum compression and the totals compared. The programs lived in a scratch directory and have been deleted; they are a few dozen lines and reproducing them is faster than maintaining them.

| | raw | gzipped |
|---|---|---|
| Full strings | 444.5 KB | 107.3 KB |
| Composed parts | 298.6 KB | 86.7 KB |
| Composition saves | 32.8% | 19.2% |

The first attempt reported composition as 27.8% *larger* than full strings. That was a defect in the experiment, not a result: it encoded the part index once per display language and spent a locale tag and a hash code on every entry. The correction matters beyond the arithmetic, because it is the reason composition works at all — **which language part and which qualifiers a locale uses is the same in every display language**, so the index is structural, stored once for the whole matrix rather than 89 times. Any real implementation would have to exploit that; an implementation that did not would be larger than the thing it replaced.

## Findings

**Measured.** The extraction of parts from the shipped English table saves **45.5%** of raw string bytes (2790 → 1520). Per-language table sizes are ~2.8 KB latin, ~5.3 KB cyrillic/greek, ~7.2 KB CJK.

**Measured.** The full matrix is ~245–360 KB of raw string data across roughly 90 display languages.

**Measured, and the assumption was optimistic.** The 45.5% saving taken from the English table alone becomes **32.8%** across the full 89-language matrix. The expectation recorded here beforehand was that the ratio would hold or improve, on the grounds that the repetition is structural rather than a property of English. The repetition is indeed structural, but English is unusually well served by it: languages that inflect region names, or that write a qualifier differently depending on what it qualifies, share fewer whole strings.

**Measured, and it decides the question.** The full matrix is **107.3 KB gzipped**, at the bottom of the 100–150 KB estimated here beforehand. Composition brings that to **86.7 KB**, a saving of **19.2%** — against 32.8% on the raw bytes. The overlap this document flagged as its single most important unverified claim is real and it is most of the effect: gzip independently finds the repetition that composition is built to remove, so roughly two thirds of the structural saving is work the compressor was already doing. In absolute terms composition is worth about **20 KB** on a 107 KB payload.

**Established by argument.** Dead-code elimination cannot strip the tables, because the display language is a runtime value. Lazy loading does not reduce bundle size. A separate artifact reintroduces inconsistency by configuration. Remote loading is blocked by the browser constraints already recorded.

## Recommendation

### Before the proof of concept

**Do not schedule the bundled tables until the composition question is settled by a proof of concept**, and not before 4.0.0.

The proof of concept has to answer one thing: **how much of the 45.5% survives gzip?** Build one composed table and one full-string table for the same display language, bundle both for JavaScript, and compare transfer size. If composition saves little after compression, it is complexity for nothing and the honest choice is the full matrix at ~100–150 KB gzipped, taken deliberately as the price of consistency.

Pick a non-Latin display language for the test. The redundancy and the compression behaviour both differ, and Latin is the flattering case.

### After the proof of concept — 2026-09-20

Composition saves little after compression, so by the test this document set itself, **ship the full matrix and do not build the composition layer.** Roughly 20 KB on a 107 KB payload does not pay for a two-table indirection, a runtime compose step, and a structural index that has to stay in step with both halves. The test was run over the whole matrix rather than one display language, which is stronger than what was asked for and removes the worry about Latin being the flattering case.

Directionally the case against composition is understated here rather than overstated. Content delivery increasingly serves brotli rather than gzip, and brotli's larger window and built-in dictionary would exploit exactly the repetition composition targets, eroding the remaining 19.2% further. Unmeasured, and worth confirming before anyone revisits composition on size grounds.

The decision is therefore no longer technical. The remaining question is editorial and belongs to whoever owns the library's size budget: **is ~107 KB gzipped acceptable on every JavaScript and Wasm consumer, including those perfectly happy with platform names?** Nothing in the delivery mechanism hides that number any more, and no cleverness recovered here is going to move it much.

What would still change the answer is unchanged from the list below, with one removal: the composition branch is closed.

What would change the answer:

- ~~If composition survives compression well, it becomes the delivery mechanism and the bundle cost roughly halves.~~ Settled by measurement: it does not.
- If a future Kotlin Multiplatform resource mechanism delivers files to all targets synchronously, the resource-file route reopens and this whole line of reasoning is moot.
- If the audience narrows to Linux targets only, a target-specific table sidesteps the browser constraint entirely and none of this applies.

Proceeding in any form supersedes the decision on platform CLDR delegation, which chose delegation on a guessed size range and without the consistency position recorded. It should be superseded by a new record rather than edited.
