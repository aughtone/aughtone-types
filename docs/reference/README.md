# Reference Data

Pinned source snapshots for the data tables compiled into the published artifact. These are inputs, not documentation — they live here rather than under `knowledge/` because a documentation mirror carries markdown only, and a data file parked in the knowledge base is invisible to readers of it.

Keywords: where did the currency list come from, where did the locale display names come from, is the locale table CLDR, do we need the Unicode license, ISO 4217 source, regenerating the resource maps, why are some country names out of date

## `list-one.xml` — ISO 4217 currency codes

Source: https://www.iso.org/iso-4217-currency-codes.html
Published: 2025-02-04 (from the file's own `Pblshd` attribute)
Compiled into: `io.github.aughtone.types.financial.currencyResourceMap` (`Currency.resource.kt`)

Provenance is settled. Attribution is in [THIRD-PARTY-NOTICES.md](../../THIRD-PARTY-NOTICES.md).

## `supported_languages.json` — locale display names

**Status: first-party curated data, adopted 2026-09-06.** Owned under this project's copyright, carrying no third-party license. It is maintained **as source, by hand** — not generated, and never to be regenerated from a CLDR or JDK run.

The table that actually ships is `Locale.resource.kt` (`localeResourceMap`), 202 entries. This JSON file is a **historical snapshot of 168 of them** and is not the source of truth — see *The snapshot had already drifted* below.

### It is not CLDR, and that was tested rather than assumed

The table was assumed to be Unicode CLDR data on the strength of its canonical `Language (Region)` shape. That assumption does not survive testing, which matters because it would have meant reproducing the Unicode License.

Running `java.util.Locale.forLanguageTag(tag).getDisplayName(Locale.ENGLISH)` over every tag reproduces most entries on JDK 17 and JDK 26 alike, under both the CLDR and the legacy locale providers. **That overlap proves nothing**: most English language names are identical across every source that publishes them, so a large match is expected between any two such tables. The mismatches are the whole signal, and several were forms CLDR has never used at any revision — `Farsi` rather than `Persian`, `Azeri` rather than `Azerbaijani`, the abbreviated `Arabic (U.A.E.)`.

The decisive tell is the **script folded into the parentheses** — `Serbian (Latin)`, `Chinese (Traditional, Hong Kong)`, `Norwegian (Bokmål, Norway)`. CLDR and ICU never compose a display name that way; they store language, script and region separately and compose only language and region. Those strings cannot have come from CLDR at any version.

The lineage is consistent with a pre-JDK-9 JRE locale table (Java's own data before JDK 9 was not CLDR) and/or hand curation. It was not pinned down further, because the conclusion does not depend on it.

### Deliberate differences from CLDR

These are conventions, not errors, and should survive any future edit:

- **Script is carried in the display name** — `Serbian (Latin)`, `Uzbek (Latin)`, `Chinese (Simplified, China)`, `Norwegian (Bokmål, Norway)`. CLDR drops it; this table keeps it because for these languages the script is the useful distinction.
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

`Locale.resource.kt` carries **202** entries; this JSON carries **168**. The 34 extra languages were added straight to the Kotlin table and never back to the snapshot, so the file documented here as the "source" had silently fallen 34 entries behind what ships. No entry conflicts between the two — the JSON is simply a subset.

That is the practical argument for maintaining the Kotlin table as source and treating this file as history: a second copy with no test binding it to the first drifts, and nothing reports it. **Either delete this file or bind it to the shipping table with a test; leaving it as an unenforced duplicate is what produced the drift.**
