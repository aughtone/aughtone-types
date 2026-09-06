---
id: "#27"
summary: "Offer a thread-safe LazyMap variant"
state: "Backlog"
resolved: false
labels: "needs-triage"
milestone: ""
assignee: ""
url: https://github.com/aughtone/aughtone-types/issues/27
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #27: Offer a thread-safe LazyMap variant

## Purpose

`LazyMap`'s memoization cache is unsynchronized. Concurrent access on JVM or
Native can run the same supplier twice or corrupt the cache outright. It is
documented as not thread-safe, which is honest, but it leaves anyone wanting
a lazily-populated map in a concurrent context with nothing to use.

`lazyMapOf` is the mechanism the deferred display-name tables would rely on,
where per-language tables are populated on first access — a concurrent
first-touch is exactly the scenario that would bite.

## Specification

A caller who needs concurrent access has a supported way to get it, where a
supplier runs at most once per key and the cache cannot be corrupted by
simultaneous access. Whether that is a separate thread-safe variant or a
documented wrapping strategy is open; what is not open is the current state,
where the only options are unsafe or hand-rolled.

Multiplatform matters here — the answer has to hold on Native, JS (where the
concern is different but the API should not fork) and JVM alike.

Whatever is chosen must not slow down the single-threaded case, which is the
common one; a thread-safe default that taxes everyone is the wrong trade.

## Open Questions

- Separate `ConcurrentLazyMap` type, or a flag on the existing one?
- Atomics or a lock? What does each cost on Native?
- Is "supplier runs at most once" a guarantee, or is "runs at least once and
  the result is consistent" enough?

## References

- Source: `docs/_archive/GAPS.md` — "gap: LazyMap Thread Safety" (recorded as LOW, target 3.2.0)

