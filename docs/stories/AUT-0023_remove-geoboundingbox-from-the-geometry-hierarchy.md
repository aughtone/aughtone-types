---
id: "#23"
summary: "Remove GeoBoundingBox from the geometry hierarchy"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/23
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #23: Remove GeoBoundingBox from the geometry hierarchy

## Purpose

`GeoBoundingBox` extends `GeoGeometry`, but a bounding box is not a geometry
in RFC 7946 — it is a `bbox` member, an array of coordinates attached to a
geometry or feature. Two things follow from the mismodelling: serializing a
bounding box polymorphically emits JSON that is not valid GeoJSON, and the
type can be passed anywhere a geometry is expected, so the compiler accepts
code that cannot produce correct output.

## Specification

A bounding box cannot be used as a geometry, and cannot be serialized as one.
It serializes in the RFC form — a flat array of 2n numbers, southwest corner
first, matching the dimensionality of what it bounds.

Geometries and features can carry a bounding box in the position the RFC puts
it, so the capability is not lost, only correctly placed.

Removing a type from a sealed hierarchy is breaking for exhaustive `when`
expressions over `GeoGeometry` in consumer code — expected for a major
release, and worth calling out in the CHANGELOG because the compiler error
appears somewhere other than the line that changed.

## Open Questions

- Does `bbox` become a member on the geometry types, and is it computed on
  demand or stored?
- What happens to existing serialized data that contains a polymorphic
  bounding box — is there anything to migrate, or was that output never
  valid enough to have been persisted?

## References

- Source: `docs/_archive/GAPS.md` — "gap: GeoBoundingBox in the GeoGeometry Hierarchy" (recorded as MEDIUM, target 4.0.0)
- RFC 7946 §5 (Bounding Box)

