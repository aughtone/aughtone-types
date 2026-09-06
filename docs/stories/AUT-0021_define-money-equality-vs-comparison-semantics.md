---
id: "#21"
summary: "Define Money equality vs comparison semantics"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/21
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #21: Define Money equality vs comparison semantics

## Purpose

`Money(1250L, usd)` and `Money(12.5, usd)` are the same amount of money and
compare as unequal, with different hash codes and different serialized forms,
because `BigDecimal.equals` compares unscaled value and scale strictly. Money
that is equal but does not compare equal breaks the obvious things: set
membership, map keys, test assertions, deduplication.

`Money` also implements no `Comparable`, so the natural question — is this
amount larger than that one — has no answer in the API at all.

## Specification

The library states, and enforces, one coherent answer to "when are two
monetary amounts the same" and "which is larger".

The recorded candidate follows the JDK: `equals` stays scale-sensitive
(1.50 and 1.5 are distinguishable values), `compareTo` is numeric, and
`Comparable` is implemented for both `Money` and `BigDecimal` so ordering is
available without going through the internals. Whatever is chosen, `equals`
and `hashCode` agree, and the serialized form does not make two equal amounts
look different.

Cross-currency comparison needs a defined outcome rather than an accident —
comparing USD to EUR is not a meaningful ordering.

Normalizing `Money`'s stored scale at construction is a related option worth
weighing: it removes the surprise at the cost of losing the distinction
between 1.50 and 1.5.

Whichever way this lands it changes observable behaviour for existing code,
so it is a major-release change and the CHANGELOG must be explicit about it.

## Open Questions

- JDK semantics, or scale-normalizing `Money` so the surprise cannot occur?
- What does comparing two different currencies do — throw, or refuse to
  compile?
- Does `Money`'s serialized form change as a result?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Money/BigDecimal Scale-Sensitive Equality" (recorded as HIGH, target 4.0.0)
- `docs/knowledge/quality-assurance/arbitrary-precision-math-parity.md` — the scale-sensitivity scenarios this must keep true

