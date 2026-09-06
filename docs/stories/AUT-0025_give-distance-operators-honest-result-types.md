---
id: "#25"
summary: "Give Distance operators honest result types"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/25
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #25: Give Distance operators honest result types

## Purpose

The quantitative operators return types that misdescribe their own results.
`Distance * Distance` produces square metres labelled `Distance`;
`Distance / Distance` produces a dimensionless ratio, also labelled
`Distance`. A value carrying the wrong unit is worse than no unit at all,
because it type-checks.

Separately, signed quantities cannot be represented — `minus` floors at zero,
so a negative distance or speed delta is silently unrepresentable, and code
computing "how much did this change" gets a wrong answer rather than an
error.

## Specification

An operation on quantitative types yields a type that honestly describes what
came out, or does not exist. Multiplying two distances gives an area or is
not offered; dividing them gives a ratio or is not offered.

Differences between quantities are representable, including negative ones, so
"how far did this move, and in which direction" is expressible. This may be a
distinct delta type rather than making the base quantities signed — a
negative absolute distance is as meaningless as a positive area labelled as a
length.

The floor-at-zero behaviour in `minus` is removed or made explicit; silently
clamping a subtraction is the specific failure mode here.

## Open Questions

- Introduce `Area` and `Ratio` types, or drop the operators entirely?
  Dropping is smaller and gives up less than it looks like, given the results
  are currently wrong.
- Signed deltas as separate types, or signedness on the existing quantities?
- Which of these are load-bearing in consumer code today?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Dimensional Semantics for Quantitative Operators" (recorded as LOW, target 4.0.0)

