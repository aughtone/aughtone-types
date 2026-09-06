---
id: "#26"
summary: "Parse Url and GeoUri from strings"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/26
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #26: Parse Url and GeoUri from strings

## Purpose

The `uri` package can build and render `Url` and `GeoUri` values but cannot
read them back from strings — only `urn(...)` has a parser. Half of RFC 3986
and RFC 5870 compliance is therefore absent, and the half that is missing is
the one consumers hit first: a URI almost always arrives as a string from
somewhere else.

Without parsers, anyone receiving a URL or geo URI has to either hand-roll
the parsing or drop out of these types entirely, which defeats having them.

## Specification

A well-formed URI string can be turned into the corresponding value, and that
value renders back to an equivalent string — round-trip is the property that
matters, and it must hold for the forms the RFCs actually permit, not only
the tidy ones: percent-encoded components, absent optional parts, IPv6 hosts
in brackets, ports, empty paths, query and fragment edge cases, and for
`GeoUri` the coordinate reference system and uncertainty parameters.

Malformed input fails in a way the caller can handle and understand, rather
than producing a half-populated value. Construction already rejects invalid
input for `Urn` and `GeoUri`; parsing is consistent with that.

Round-trip is not always byte-identical — percent-encoding and case
normalization legitimately differ — so what "equivalent" means is part of the
contract, not left to the reader.

## Open Questions

- What is the failure signature — nullable return, result type, or throw?
  `urn(...)` sets a precedent worth following if it is the right one.
- Is a partial or lenient parse ever wanted, or is strict the only mode?
- Does normalization happen on parse (lowercased scheme and host, resolved
  dot segments) or only on demand?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Url/GeoUri String Parsers" (recorded as MEDIUM, target 3.2.0)
- Related, shipped: #8 Add Uri Type for building and parsing URLs

