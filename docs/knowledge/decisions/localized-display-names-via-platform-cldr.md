# Localized Display Names via Platform CLDR Delegation

ADR-0003 · 2026-07-23 · Status: accepted
Keywords: locale name in the user's own language, displayName is English only,
          why not bundle CLDR tables, why not ship resource files, klib
          resources on iOS, no synchronous file I/O in the browser, Linux has
          no CLDR, Intl.DisplayNames, NSLocale displayNameForKey, generated
          Kotlin name tables

## Context
Every `displayName` in `localeResourceMap` is English-only. Consumers need a locale's name rendered in the end user's own language (e.g. a French user should see "bengali", a Japanese user "ベンガル語"). Providing this ourselves means shipping a name matrix of roughly 90 languages × 90 display languages ≈ 8,000+ strings (~100–400 KB), and keeping it synchronized with CLDR over time.

Bundling that data as loadable resource files was considered and rejected because several targets simply cannot handle it — the recurring cross-platform capability problem in this library:

- **Browsers (JS/WasmJS)** have no synchronous file I/O. Resource files can only be `fetch`ed, which forces a `suspend` API, and a published library cannot know the URL its files were deployed to.
- **Apple targets**: a klib cannot deliver resource files into the consumer's app bundle. The only established workaround is the Compose Multiplatform resources plugin, which drags a Compose dependency and an async API into a zero-dependency types library.
- **Linux/MinGW** have no application bundle concept, so there is no reliable location to install or locate data files from a library.

Bundling the matrix as generated Kotlin code instead (one lazily-initialized table per display language) keeps RAM small but ships the full string matrix in every binary and JS bundle, and requires a CLDR extraction/refresh pipeline to maintain.

## Decision
Delegate display-name localization to the CLDR data each platform already ships, via the library's established expect/actual pattern:

- New `expect fun localizedDisplayNameForNative(locale: Locale, displayIn: Locale): String?` with actuals per target: `java.util.Locale.getDisplayName` (JVM/Android), `NSLocale.displayNameForKey` (Apple — the same API `localeForNative` already uses), and `Intl.DisplayNames` (JS/WasmJS). Linux returns `null`.
- Public API `Locale.localizedDisplayName(displayIn: Locale = Locale.current): String` never returns `null` — when the platform has no answer it falls back to the English `displayName` from the resource map.

Bundled, generated per-display-language name tables are **deferred**, tracked as issue [#20](https://github.com/aughtone/aughtone-types/issues/20). AI-assisted table maintenance makes that option tractable later if byte-identical cross-platform names or first-class Linux support become requirements.

## Consequences
- **Positive:** Zero binary/bundle growth, zero additional RAM, zero translation-data maintenance; names come from OS-grade CLDR data that updates with the platform.
- **Negative:** Wording may differ slightly across platforms and OS versions (e.g. "Chinese (Simplified)" vs "Simplified Chinese"); Linux targets only ever produce the English fallback; browsers require `Intl.DisplayNames` (widely available since ~2020).
- **Mitigation:** The universal English fallback guarantees a usable, non-null name everywhere; issue #20 preserves a concrete path to bundled tables for platforms without native data.
