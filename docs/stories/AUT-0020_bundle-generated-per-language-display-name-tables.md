---
id: "#20"
summary: "Bundle generated per-language display-name tables"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/20
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #20: Bundle generated per-language display-name tables

## Purpose

`Locale.localizedDisplayName()` delegates to whatever CLDR data the platform
already ships, which costs nothing and works well on JVM, Android, Apple and
modern browsers. It leaves two gaps: Linux targets have no system CLDR at all
and always fall back to English, and wording differs slightly between
platforms and OS versions ("Chinese (Simplified)" vs "Simplified Chinese").

Consumers who need byte-identical names on every platform, or real Linux
support, currently cannot get them. ADR-0003 deliberately deferred the fix
rather than rejecting it.

## Specification

Display names come from data the library ships, so the same input produces
the same string on every target including Linux.

The recorded approach: generate one Kotlin table file per display language
(roughly 90 strings each) plus a registry keyed by language code, loaded on
first access so memory holds one table at a time — a few KB — rather than the
full matrix. Generated code rather than resource files, because resource
loading is what ADR-0003 found unworkable across targets.

Generated data is only viable if it can be refreshed: the work includes the
extraction pipeline that produces the tables from CLDR and can re-run when
CLDR moves, not just one hand-checked snapshot.

The platform-delegating path stays as the fallback wherever a bundled table
is absent.

## Open Questions

- Which display languages ship? All ~90, or a subset with platform
  delegation covering the rest?
- What does the full matrix cost in binary and JS bundle size — the number
  that decides whether this is worth doing at all?
- Is this opt-in (a separate artifact) so consumers who are happy with
  platform names pay nothing?
- What triggers a refresh — a CLDR release, or a project release?

## References

- `docs/knowledge/architecture-decision-records/localized-display-names-via-platform-cldr.md` (ADR-0003) — the decision that deferred this, and why resource files were rejected
- Source: `docs/_archive/GAPS.md` — "gap: Bundled Localized Display-Name Tables" (recorded as LOW, target TBD)

