---
id: "#17"
summary: "Compare GeoJSON test output as JsonElement, not strings"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/17
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #17: Compare GeoJSON test output as JsonElement, not strings

## Purpose

`GeoJsonTest` compares serialized output by string matching, which is not
stable across platforms: `Double` renders as `100.0` on JVM and `100` in
JS/Wasm, so the same correct payload passes on one target and fails on
another. The failure is in the test, not the library — but it costs a red
build on every JS/Wasm run and trains people to ignore it.

The sibling half of this problem is already fixed: `GeoUri` now formats
coordinates with a platform-stable plain-decimal formatter, so its output no
longer diverges. GeoJSON numeric payloads still go through platform `Double`
serialization in kotlinx.serialization, which is legitimate — the assertion
is what needs to change.

## Specification

GeoJSON serialization tests compare parsed JSON structure rather than raw
text: a `JsonElement` tree comparison, so numeric equality is decided
semantically and formatting differences between targets stop mattering.
Coverage must not shrink in the process — the same payloads are still
asserted, only the comparison changes.

Where a test genuinely cares about the literal text (RFC-mandated key order
or type strings, for instance), that intent is stated explicitly rather than
falling out of a string compare.

## Open Questions

- Does anything rely on the string form of GeoJSON output beyond these tests?
- Should the same treatment be applied pre-emptively to any other
  serialization test that compares raw strings?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Platform-Specific Serialization Discrepancies" (recorded as MEDIUM, target 3.2.0)
- Related, already resolved: #15 WasmJS Test Failure

