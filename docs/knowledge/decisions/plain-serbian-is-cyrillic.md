# Plain Serbian Is Cyrillic

ADR-0008 · 2026-11-24 · Status: accepted
Keywords: sr is Cyrillic, sr-RS script, Serbian Latin sr-Latn, sh hbs Serbo-Croatian alias,
          sr-ME Montenegro Latin, why did sr change script, CLDR default script for Serbian

## Context

`sr` and `sr-RS` shipped written in Latin. CLDR's likely subtags give `sr` → `sr-Cyrl-RS`, and every other part of this library follows CLDR: the default-script table, and the region-script table, which lists only the regions where Serbian is written in Latin (`sr-ME`, `sr-RO`, `sr-TR`). The platforms follow CLDR too. So `sr` was the one shipped locale whose script disagreed with the rules around it, and code following CLDR disagreed with this library about what `sr` meant (#51). The region-script check against ICU had to exclude Serbian to pass.

## Decision

**Plain `sr` and `sr-RS` are Serbian in Cyrillic.** Latin Serbian ships as `sr-Latn` and `sr-Latn-RS`. The Serbo-Croatian codes `sh` and `hbs` resolve to `sr-Latn`, as in CLDR, and a script named in the tag is kept rather than doubled. `sr-ME` resolves to Latin through the region-script table.

## Alternatives that lost

- **Keep `sr` Latin and document the departure from CLDR.** No break, but `sr` would go on meaning one thing here and another everywhere else, and a Cyrillic request still found nothing shipped.
- **Ship both scripts and leave plain `sr` Latin.** Fixes Cyrillic requests but keeps the disagreement over the bare tag, which is the one most callers pass.

## Consequences

**Harder.** It is a behaviour change: a tag stored as `sr` or `sr-RS` meaning Latin now finds Cyrillic, and must be stored as `sr-Latn` or `sr-Latn-RS`.

**Easier.** `sr` means what CLDR and the platforms say, and Serbian is back in the region-script check.

**Names were composed rather than harvested.** The Linux and Windows table gained 166 Cyrillic names composed directly from CLDR 48.2.0 — language and region names joined with each display language's own pattern — after the same composition reproduced all 166 of the table's existing Latin Serbian names exactly. The bundled Serbian display-language names were already Cyrillic, CLDR's default, so a `sr-Latn` reader skips them under [ADR-0006](display-names-in-the-readers-language.md)'s script rule.

## Corrections
