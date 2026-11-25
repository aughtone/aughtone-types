# Display Names In The Reader's Language, Bundled First

ADR-0006 · 2026-11-24 · Status: accepted
Keywords: why is displayName not English, displayName changes with the device language, where
          does a locale name come from, bundled names before platform names, kok-IN shows codes,
          englishName, localizedDisplayName order, names in the wrong script, zh-Hant gets English
          on Linux, cost of reading displayName, current locale memo
Measured against: JDK 17 toolchain through kotlinx-benchmark 1.37 JMH, macOS arm64, 2026-11-24.

## Context

`Locale.displayName` was a stored string: the English name a locale was built with. A name in the reader's language came only from `localizedDisplayName(displayIn)`, which asked the platform first and fell back to the bundled supplement and then to English ([ADR-0005](completeness-over-consistency-for-display-names.md)).

#48 exposed what that meant in practice. A device set to a locale this library does not ship got a `Locale.current` built by the platform, whose stored name was in the device's own language and different on every target — Konkani in India was "कोंकणी (भारत)" on the JVM and "kok-IN" on JavaScript, Wasm and Linux. Every shipped locale's stored name was English, so the one unshipped locale was the odd one out, and data-class equality compared that name, so two locales for one tag could be unequal.

The forces:

- **What a caller expects of `displayName`.** On a locale class it conventionally means the name in the user's language — `java.util.Locale.getDisplayName()` with no argument does exactly that — and a UI reaching for it wants what the user can read.
- **Codes are the worst answer.** A name made of subtags, "kok (India)" or "kok-IN", is shown to users verbatim; anything that can name the locale should win.
- **Consistency where it is cheap.** Bundled names are the same on every target; platform names disagree on roughly 40% of shared pairs (ADR-0005). Where a bundled name exists, preferring it buys consistency for free.
- **No new public surface without need.**

## Decision

**`displayName` is the locale's name in the current locale's language, resolved on each read**, from the first of: this library's bundled names (the shipped table's own name for an English reader, the bundled translations otherwise), the platform's name, an English name, and the codes only for a subtag nothing can name. `localizedDisplayName(displayIn)` and `localizedDisplayNameOrNull` use the same order for a language of the caller's choosing.

The name a locale is built with stays stored, internally, as the English fallback and as the only name serialized. There is no `englishName` property; `localizedDisplayName(displayIn = english)` returns it.

Bundled names for a language are written in that language's default script, so a reader in another script — `zh-Hant`, `sr-Latn` — skips them rather than receiving names in a script it did not ask for.

## Alternatives that lost

- **Keep `displayName` as the stored English name** and point callers at `localizedDisplayName()`, which already defaulted to the current locale. Rejected: it leaves the property meaning something other than what a caller reaching for it expects.
- **Name unshipped locales from library data only**, never the platform: "kok (India)" on every target. Built and tested, then rejected: it showed codes where every platform but Linux knew the real name.
- **Add a public `englishName`** alongside a computed `displayName`. Rejected as surface nobody needs once `localizedDisplayName` takes an English reader.
- **Platform first, bundled second**, as before. Rejected: it gives up consistency exactly where it costs nothing.

## Consequences

**Easier.** A UI gets names its user can read without passing a language. A locale the library does not ship still gets a real name: a bundled table of CLDR 48.2.0's English language, script and region names makes `kok-IN` "Konkani (India)" on every target, Linux included. Where bundled data names a pair, every target says the same thing.

**Harder.** `displayName` now depends on the device, so a test, log line, cache key or snapshot that reads it varies by machine; the library's own tests assert against the stored name or an explicit `displayIn`. Code that read `displayName` expecting English changes behaviour on non-English devices — a breaking behaviour change in 4.3.0.

**Platform `localeForNative` stores English**, on the JVM, Android, Apple and Windows, since the reader's-language name is now computed rather than stored.

**A reader in a non-default script loses bundled names.** On Linux and Windows, where the bundled table is the only source, a Traditional Chinese reader gets English until the table carries Traditional names (#55). That is preferred over Simplified, which is the right language in a script the reader did not ask for.

**Every read asks for the current locale, and that was measured.** Before memoization a cached read cost 0.12 µs, and sorting the 258 shipped locales by `displayName` — which reads the name on every comparison — took 400 µs, because each read rebuilt the device tag (by reflection on the JVM and Android) and re-ran the lookup. The resolved current locale is now remembered for as long as the device reports the same tag: a read costs about 0.009 µs and the sort 33 µs. Those figures are host-specific; the benchmark is `LocaleDisplayNameBenchmark`.

**Consistency is still not guaranteed.** Pairs every platform can name are not in the supplement, so off Linux and Windows they keep the platform's wording. [ADR-0005](completeness-over-consistency-for-display-names.md)'s conclusion — no cross-target wording guarantee — stands; full consistency is #31 and #55.

This supersedes ADR-0005 on one point: the platform is no longer the first source of a name. Its decision to pursue completeness over consistency is unaffected.

## Corrections
