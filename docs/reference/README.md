# Reference Data

Pinned source snapshots for the data tables compiled into the published artifact. These are inputs, not documentation — they live here rather than under `knowledge/` because a documentation mirror carries markdown only, and a data file parked in the knowledge base is invisible to readers of it.

Keywords: where did the currency list come from, where did the locale display names come from, is the locale table CLDR, do we need the Unicode license, ISO 4217 source, regenerating the resource maps, why are some country names out of date, which CLDR version, why is some data not reproducible, platform-harvested names

## `list-one.xml` — ISO 4217 currency codes

Source: https://www.iso.org/iso-4217-currency-codes.html
Published: 2025-02-04 (from the file's own `Pblshd` attribute)
Compiled into: `io.github.aughtone.types.financial.currencyResourceMap` (`Currency.resource.kt`)

Provenance is settled. Attribution is in [THIRD-PARTY-NOTICES.md](../../THIRD-PARTY-NOTICES.md).

## Country to currency

Source: `java.util.Currency` on JDK 26, which tracks ISO 4217, read for every ISO 3166 region on 2026-11-01
Compiled into: `io.github.aughtone.types.financial.regionToCurrencyMap` (`RegionToCurrency.resource.kt`)

Two entries differ from that source, each marked in the file: `SV` is `USD`, where the JDK reports the colon, and `XK` is `EUR`, which the JDK does not report. `list-one.xml` cannot supply this table, because it names countries in prose rather than by code, and it predates the euro in Bulgaria and the Caribbean guilder.

## Three-letter language aliases

Source: Unicode CLDR 48.0 language aliases, read through ICU 78.2 in Node 24 by canonicalizing every three-letter code, 2026-11-07
Compiled into: `io.github.aughtone.types.locale.languageAliasMap` (`LanguageAlias.resource.kt`)

An alias is kept only when CLDR's replacement is a bare language code this library ships, so `eng` → `en` is kept and `cnr` → `sr-ME` is not. That covers ISO 639-2/T and 639-2/B codes, deprecated codes such as `mol`, and the member CLDR treats as its macrolanguage, such as `cmn` → `zh` and `cwd` → `cr`. Four codes are kept by hand in `legacyLanguageCodes` instead and left out of the generated file: `iku`, `ike` and `ikt`, which resolve to `iu` (#40), and `tgl`, which goes to `tl` because CLDR's replacement `fil` ships as a separate locale. To regenerate, canonicalize `aaa`…`zzz` with `Intl.getCanonicalLocales` and apply the same filter.

## `UNICODE-LICENSE-V3.txt` — localized display names

Source: Unicode CLDR, via the npm package `cldr-localenames-full`
Pinned: version **48.2.0**, integrity `sha512-4o0paYDz0UXhfhChAVFkmVl2CP0YTSEqE6WrC0fNafaSC5u5DAXphxH3SZ1Ujczarikr8DToKZfiok6EqWLdNQ==`
Compiled into: `LocaleDisplayNameSupplement` (all targets) and `LocaleDisplayNameTable` (Linux only)

The licence file is vendored here rather than the data, because the data is 1.9 MB and the package is reproducible from its version and integrity hash. Attribution is in [THIRD-PARTY-NOTICES.md](../../THIRD-PARTY-NOTICES.md).

Names are **composed**, not transcribed: language, script and territory are read from CLDR's own files and assembled with CLDR's own `localeDisplayPattern`. That is the same rule the platforms apply, which is why the output matches them — `fr` + `en-AU` gives "anglais (Australie)" from either.

### Part of this data is not from the pinned release

CLDR 48.2.0 cannot name 8,423 of the 28,728 pairs this library ships. Most of those are names rendered *in* one of the 25 Canadian Indigenous languages added in #44, for which CLDR has no locale data at all. The rest were taken from platform CLDR implementations where one could name them — JDK 26, Apple, and a Chromium browser, measured 2026-09-28 and again for the #44 pairs on 2026-11-07 — and **that portion is not reproducible from any pinned source**. Each platform carries its own CLDR version and, in places, vendor additions; Apple's handful of Inuktitut names, and its five names for Canadian Indigenous languages in themselves, exist in no CLDR release this project could find.

Of the names that ship, **19,581 come from the pinned release and 235 from platform implementations**. A platform answer is not taken when it only echoes the codes, such as `zh (Hans)`, or when it is the platform's English fallback for a display language it has too little data for, such as "Austrian German" offered as Tibetan. Everything neither source can name falls back to English.

This is a known weakness, deliberately accepted rather than overlooked, and it is the one part of this directory that does not meet the standard the rest of it sets. Replacing it with a citable source is tracked separately.

## `supported_languages.json` — locale display names

**Status: first-party curated data, adopted 2026-09-06.** Owned under this project's copyright, carrying no third-party license. It is maintained **as source, by hand** — not generated, and never to be regenerated from a CLDR or JDK run.

The table that actually ships is `Locale.resource.kt` (`localeResourceMap`), 253 entries. This JSON file is a copy of it kept for provenance, held to it entry for entry by `LocaleSnapshotParityTest`, and is not the source of truth — see *The snapshot had already drifted* below.

### It is not CLDR, and that was tested rather than assumed

The table was assumed to be Unicode CLDR data on the strength of its canonical `Language (Region)` shape. That assumption does not survive testing, which matters because it would have meant reproducing the Unicode License.

Running `java.util.Locale.forLanguageTag(tag).getDisplayName(Locale.ENGLISH)` over every tag reproduces most entries on JDK 17 and JDK 26 alike, under both the CLDR and the legacy locale providers. **That overlap proves nothing**: most English language names are identical across every source that publishes them, so a large match is expected between any two such tables. The mismatches are the whole signal, and several were forms CLDR has never used at any revision — `Farsi` rather than `Persian`, `Azeri` rather than `Azerbaijani`, the abbreviated `Arabic (U.A.E.)`.

The decisive tell is the **script folded into the parentheses** — `Serbian (Latin)`, `Chinese (Traditional, Hong Kong)`, `Norwegian (Bokmål, Norway)`. CLDR and ICU never compose a display name that way; they store language, script and region separately and compose only language and region. Those strings cannot have come from CLDR at any version.

The lineage is consistent with a pre-JDK-9 JRE locale table (Java's own data before JDK 9 was not CLDR) and/or hand curation. It was not pinned down further, because the conclusion does not depend on it.

### Deliberate differences from CLDR

These are conventions, not errors, and should survive any future edit:

- **Script is carried in the display name** — `Serbian (Latin)`, `Uzbek (Latin)`, `Chinese (Simplified, China)`, `Norwegian (Bokmål, Norway)`. CLDR drops it; this table keeps it because for these languages the script is the useful distinction.
- **The Canadian Indigenous languages added in #44 take CLDR's English name**, such as `Ojibwa`, `Dogrib` and `Mi'kmaw`, so the English name matches what every platform says. Five have no CLDR name and use their ISO 639-3 reference name instead: `Squamish`, `Nuu-chah-nulth`, `Shuswap`, `Oneida` and `Wyandot`.
- `Bengali` rather than CLDR's `Bangla`, `Kurdish` rather than `Kurmanji`, `Malay (Brunei Darussalam)` using the official long form. All current English usage.

### Names corrected on 2026-09-06

Eleven entries were factually stale — renamed countries, abbreviated or incomplete country names, dated language exonyms, and one missing region. Script annotations were left alone.

| Tag | Was | Now | Why |
|---|---|---|---|
| `ar-AE` | Arabic (U.A.E.) | Arabic (United Arab Emirates) | abbreviation |
| `az` | Azeri (Latin) | Azerbaijani (Latin) | dated exonym |
| `az-AZ` | Azerbaijani | Azerbaijani (Azerbaijan) | missing region — the only regional entry without one |
| `cs-CZ` | Czech (Czech Republic) | Czech (Czechia) | renamed 2016 |
| `en-TT` | English (Trinidad) | English (Trinidad & Tobago) | incomplete country name |
| `fa` | Farsi | Persian | dated exonym |
| `fa-IR` | Farsi (Iran) | Persian (Iran) | dated exonym |
| `ko-KR` | Korean (Korea) | Korean (South Korea) | ambiguous |
| `mk-MK` | Macedonian (Macedonia) | Macedonian (North Macedonia) | renamed 2019 |
| `tr-TR` | Turkish (Turkey) | Turkish (Türkiye) | renamed 2022 |
| `vi-VN` | Vietnamese (Viet Nam) | Vietnamese (Vietnam) | spelling |

### The snapshot had already drifted

`Locale.resource.kt` carried **202** entries when this JSON carried **168**. The 34 extra languages were added straight to the Kotlin table and never back to the snapshot, so the file documented here as the "source" had silently fallen 34 entries behind what ships. No entry conflicts between the two — the JSON is simply a subset.

That is the practical argument for maintaining the Kotlin table as source and treating this file as history: a second copy with no test binding it to the first drifts, and nothing reports it. **Either delete this file or bind it to the shipping table with a test; leaving it as an unenforced duplicate is what produced the drift.**
