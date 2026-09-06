---
id: "#15"
summary: "WasmJS Test Failure"
state: "closed"
resolved: true
labels: "bug"
milestone: ""
assignee: "bpappin"
url: https://github.com/aughtone/aughtone-types/issues/15
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #15: WasmJS Test Failure

The test floating point inaccuracy is resolved failed on wasmJsBrowserTest with an IllegalArgumentException. 

JavaScript math is notorious for decimal precision problems, 0.1 + 0.2 might be causing an underlying assertion check or BigDecimal initialization to throw.
