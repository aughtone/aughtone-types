---
id: "#19"
summary: "Benchmark BigInteger/BigDecimal against JDK and bignum"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/19
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #19: Benchmark BigInteger/BigDecimal against JDK and bignum

## Purpose

`BigInteger` and `BigDecimal` are pure-Kotlin implementations of algorithms
where a naive change can be correct and still an order of magnitude slower —
Knuth division and scaling especially. Correctness is well covered by
differential parity testing; performance is not covered at all, so a
regression would ship silently and only surface as someone else's slow app.

## Specification

The project can measure the cost of the arbitrary-precision operations that
matter — addition, multiplication, division (including the Knuth path),
scaling and rounding — and compare a change against the previous state.

Measurements are reproducible rather than ad hoc: a committed benchmark
definition, run the same way each time, with results that mean something
across runs. Comparison baselines are the same ones parity testing already
uses (the JDK types on JVM, ionspin bignum on common targets), so a "we are
2x slower here" statement is grounded.

Benchmark dependencies stay out of the published artifact, exactly as the
parity test dependencies do.

## Open Questions

- JetBrains `kotlinx.benchmark` was the named candidate — still the right
  choice, and on which targets?
- Do benchmarks run in CI (and gate anything), or on demand only?
- What counts as a regression worth failing over?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Full kotlinx.benchmark Integration for BigDecimal and BigInteger" (recorded as MEDIUM, target 3.3.0)
- `docs/knowledge/architecture-decision-records/differential-testing.md` (ADR-0001) — the dependency-isolation rule this must follow

