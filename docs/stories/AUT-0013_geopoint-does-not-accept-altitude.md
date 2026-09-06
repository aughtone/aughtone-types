---
id: "#13"
summary: "GeoPoint does not accept altitude"
state: "closed"
resolved: true
labels: "bug"
milestone: ""
assignee: "bpappin"
url: https://github.com/aughtone/aughtone-types/issues/13
---
<!-- GENERATED snapshot (2026-08-14): do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. -->

# #13: GeoPoint does not accept altitude

According to the GeoJSON Specification (RFC 7946, Section 3.1.1), a position is defined as:

> "The position MUST be an array of numbers. There MUST be two or more elements. The first two elements are longitude and latitude... A third element MAY be used to represent altitude or elevation."

However Point requires exactly 2 values lat & long and bails when it sees the third.


#### The Fix

We'll allow at least 2, and we will only use three. 

It says noting about limiting it though, so we wont enforce a max 3 rule.
