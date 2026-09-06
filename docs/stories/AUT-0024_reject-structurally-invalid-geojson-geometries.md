---
id: "#24"
summary: "Reject structurally invalid GeoJSON geometries"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/24
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #24: Reject structurally invalid GeoJSON geometries

## Purpose

The GeoJSON geometry types accept structurally invalid data without
complaint: a `GeoLineString` with fewer than two positions, a `GeoPolygon`
with unclosed or too-short rings or the wrong winding order, a position array
of length under two. The type system says the value is a geometry; the RFC
says it is not.

Invalid values constructed here serialize into output that other tools
reject, and the error surfaces far from where it was created — usually in
someone else's system.

## Specification

Constructing a geometry that RFC 7946 §3.1 forbids fails at construction,
with a message naming the specific rule broken rather than a generic failure.
The recorded approach is init-block validation on the geometry types.

The rules to enforce are the RFC's: minimum position counts per geometry
type, polygon rings closed (first position equals last) and of sufficient
length, positions of at least two elements.

Strictness has a cost on ingestion — real-world GeoJSON is often slightly
wrong, and a hard failure can make otherwise-usable data unreadable. A
lenient path for ingestion is worth providing, but it must be an explicit
choice at the call site, never the default.

## Open Questions

- Is winding order enforced, or only warned about? The RFC's right-hand rule
  is a "should" for interoperability, and a lot of valid-in-practice data
  gets it wrong.
- What shape does the lenient path take — a separate constructor, a parse
  flag, a validating factory returning a result type?
- Does validation apply on deserialization as well as construction?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Geo Structural Validation" (recorded as MEDIUM, target 4.0.0)
- RFC 7946 §3.1 (Geometry Object)

