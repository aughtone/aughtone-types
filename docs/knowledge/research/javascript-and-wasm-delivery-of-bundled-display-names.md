# Delivering Bundled Locale Display Names on JavaScript and Wasm

RAD-0001 · 2026-09-20
Keywords: localized locale names cost too much in the js bundle, why not bundle CLDR
          display names, 90 display languages bundle size, tree shaking cannot strip
          locale tables, lazy loading does not help the browser, fetch resource files
          from a library, splitting display names into a separate artifact, composing
          display names from language and region parts
Measured against: the 202-entry `localeResourceMap` as of 3.4.0, Kotlin 2.4.0,
          measured 2026-09-20. String-length arithmetic only; no compiled artifact
          or bundler output was measured.

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

## Findings

**Measured.** The extraction of parts from the shipped English table saves **45.5%** of raw string bytes (2790 → 1520). Per-language table sizes are ~2.8 KB latin, ~5.3 KB cyrillic/greek, ~7.2 KB CJK.

**Measured.** The full matrix is ~245–360 KB of raw string data across roughly 90 display languages.

**Assumed, not measured.** That the 45.5% ratio holds for non-English display languages. It should hold or improve, since the repetition is structural rather than a property of English, but no other language table exists to check against.

**Assumed, not measured.** The gzipped figure of 100–150 KB. Natural-language text typically compresses around 3:1; this was not measured, and gzip already exploits some of the same redundancy composition targets — so the two savings **overlap and are not additive**. Composition may yield considerably less than 45.5% after compression. This is the single most important unverified claim here.

**Established by argument.** Dead-code elimination cannot strip the tables, because the display language is a runtime value. Lazy loading does not reduce bundle size. A separate artifact reintroduces inconsistency by configuration. Remote loading is blocked by the browser constraints already recorded.

## Recommendation

**Do not schedule the bundled tables until the composition question is settled by a proof of concept**, and not before 4.0.0.

The proof of concept has to answer one thing: **how much of the 45.5% survives gzip?** Build one composed table and one full-string table for the same display language, bundle both for JavaScript, and compare transfer size. If composition saves little after compression, it is complexity for nothing and the honest choice is the full matrix at ~100–150 KB gzipped, taken deliberately as the price of consistency.

Pick a non-Latin display language for the test. The redundancy and the compression behaviour both differ, and Latin is the flattering case.

What would change the answer:

- If composition survives compression well, it becomes the delivery mechanism and the bundle cost roughly halves.
- If a future Kotlin Multiplatform resource mechanism delivers files to all targets synchronously, the resource-file route reopens and this whole line of reasoning is moot.
- If the audience narrows to Linux targets only, a target-specific table sidesteps the browser constraint entirely and none of this applies.

Proceeding in any form supersedes the decision on platform CLDR delegation, which chose delegation on a guessed size range and without the consistency position recorded. It should be superseded by a new record rather than edited.
