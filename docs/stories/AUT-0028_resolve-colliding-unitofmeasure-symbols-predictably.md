---
id: "#28"
summary: "Resolve colliding UnitOfMeasure symbols predictably"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/28
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #28: Resolve colliding UnitOfMeasure symbols predictably

## Purpose

`UnitOfMeasure.findFirst` resolves colliding symbols by declaration order, so
`"gal"` always returns `Gallon` and never `GallonImperial`, and `"a"` returns
`Year` rather than `YearJulian`. The caller gets a confident answer that may
be the wrong unit, with nothing to indicate a collision occurred — a US
gallon silently standing in for an imperial one is a 20% error.

Separately, `Kilocalorie("kCal")` and `Calorie("Cal")` use casings that are
not the standard symbols (`kcal`, `cal`), so lookups with correct input fail.

## Specification

Symbol lookup either returns an unambiguous answer or makes the ambiguity
visible to the caller. Where a symbol genuinely maps to more than one unit,
the caller can discover all candidates and choose, and the precedence used by
the convenience lookup is documented rather than being an artefact of
declaration order in a file.

Symbols match the standards they claim to follow. Correcting a symbol changes
lookup results for existing callers, so the casing fixes are breaking and
belong in a major release with a CHANGELOG note.

## Open Questions

- Does `findAll` exist already, or is it part of this?
- Should the ambiguous convenience lookup keep returning a value at all, or
  refuse and force the caller to disambiguate?
- Is there a locale or system dimension here (US vs imperial defaults), or is
  explicit selection the only answer?

## References

- Source: `docs/_archive/GAPS.md` — "gap: UnitOfMeasure Symbol Collisions" (recorded as LOW, target 4.0.0)

