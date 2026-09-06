---
id: "#18"
summary: "Remove the deprecated Locale.toLanguageTag() extension"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/18
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #18: Remove the deprecated Locale.toLanguageTag() extension

## Purpose

`Locale.toLanguageTag()` was deprecated in 2.2.0 in favour of the
`Locale.languageTag` property. Two ways to ask the same question is a small
tax on every reader of the API, and the deprecation has been carried long
enough to do its job.

## Specification

The deprecated extension function is removed. Removal is a source-breaking
change and therefore belongs in a major release, not a minor one.

Anything inside this repo still calling it moves to `languageTag`, and the
CHANGELOG records the removal under breaking changes so consumers meet it
before their build does.

## Open Questions

- Is the deprecation warning currently at `WARNING` level, and should it be
  raised to `ERROR` for a release before removal?

## References

- Source: `docs/_archive/GAPS.md` — "gap: Deprecated `toLanguageTag` Removal" (recorded as LOW, target 4.0.0)

