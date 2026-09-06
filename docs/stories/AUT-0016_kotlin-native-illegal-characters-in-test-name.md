---
id: "#16"
summary: "Kotlin Native Illegal Characters in Test Name"
state: "closed"
resolved: true
labels: "bug"
milestone: ""
assignee: "bpappin"
url: https://github.com/aughtone/aughtone-types/issues/16
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #16: Kotlin Native Illegal Characters in Test Name

The Kotlin Native compiler (compileTestKotlinIosX64, etc.) crashed because of the parentheses () inside the test names.

Line 126: money with zero digits (JPY)
Line 140: money with three digits (KWD) (You'll need to remove the parentheses from those test function names for Apple/Linux native targets to compile).
