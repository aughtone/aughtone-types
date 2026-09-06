---
id: "#22"
summary: "GeoFeature.properties must accept full JSON values"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/22
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #22: GeoFeature.properties must accept full JSON values

## Purpose

`GeoFeature.properties` is typed `Map<String, String>?`, but RFC 7946 puts an
arbitrary JSON object there — numbers, booleans, nulls, nested objects and
arrays are all legal. Under a strict `Json` instance, real-world GeoJSON from
any external source fails to deserialize. The same applies to `id`, typed
`String?`, where the RFC permits a number.

The practical effect is that the library cannot round-trip GeoJSON it did not
produce itself, which is most of the GeoJSON anyone encounters.

## Specification

A `GeoFeature` accepts and preserves any RFC 7946-legal properties object,
including nested structures and non-string scalars, and round-trips it
without loss or reinterpretation — a number stays a number.

`id` accepts both permitted forms, string and number, and serializes back as
whatever it was.

The recorded approach is `properties: JsonObject?` and a string-or-number
representation for `id`. Both are breaking changes for existing callers, so
this is major-release work, and the migration path for code doing
`properties["name"]` today needs to be stated rather than discovered.

## Open Questions

- Does a typed-accessor convenience layer come with this, or do consumers
  work with `JsonObject` directly?
- Does exposing `JsonObject` in the public API constrain the serialization
  dependency in a way the library should avoid?

## References

- Source: `docs/_archive/GAPS.md` — "gap: GeoFeature Properties Cannot Hold Real GeoJSON" (recorded as HIGH, target 4.0.0)
- `docs/knowledge/architecture-decision-records/geo-prefixing-to-avoid-shadowing.md` (ADR-0002) — the RFC-compliance commitment behind this

