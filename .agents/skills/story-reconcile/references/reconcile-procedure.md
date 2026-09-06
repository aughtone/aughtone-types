# Reconcile Procedure (detail)

## Phase 1 - local inventory

Search the repo for task-shaped content:

- `docs/**/gap*`, `**/GAP*` - the retired GAP system
- `## Acceptance Criteria`, `## AC`, checklist blocks (`- [ ]`) inside
  `docs/knowledge/**` (PRDs and similar)
- `TODO.md`, `BACKLOG.md`, `docs/todo*`

For each discrete item record: `source` (file + heading), `text`,
`status-hint` (checked/unchecked, "done" notes, git-log age if useful).
Skip: ADRs (decisions, not tasks), code TODOs (different lifecycle -
mention their count in the final report, don't migrate them).

## Phase 2 - server inventory

Run the snapshot command from the tracker binding
([tracker-youtrack.md](tracker-youtrack.md) for YouTrack). It produces one
markdown file per issue + `INDEX.md`. Generated files - never hand-edit;
re-run to refresh. Commit the snapshot if the team wants offline/PR-visible
reference, or gitignore it - team's choice, ask once.

## Phase 3 - mapping table (approval gate)

| # | Local item | Source | Disposition | Target / Draft | Notes |
|---|---|---|---|---|---|
| 1 | "Offline queue drains on reconnect" | prd/offline.md | MATCH | PROJ-112 | server AC missing the 30s bound - propose add |
| 2 | "Achievement scoring model" | gap/scoring.md | NEW (prose-only) | - | source is prose + open questions, no verifiable AC extractable - needs-triage, AC authored at triage |
| 2 | "Conflict merge dialog" | gap/GAP-07.md | NEW | draft below | |
| 3 | "Migrate to SDK 34" | TODO.md | OBSOLETE | - | shipped in v2.1 (git log) |
| 4 | "Retry semantics unclear" | gap/GAP-09.md | QUESTION | - | is this still wanted? |

Drafts for NEW stories use the canonical format - identical on every
tracker (bundled: [ac-format.md](ac-format.md)): `## Purpose` and
`## Specification` in prose, `## Acceptance Criteria` checklist,
`## References` pointing at the source ADR/PRD paths.

Conflict rule: where local and server disagree (item checked locally,
unchecked AC on the story, or vice versa), the table shows both and
recommends - the user decides. Never assume the newer one wins.

## Phase 4 - execution notes

- Create stories via the tracker binding (project key from
  `.agents/config/story-tools.json`).
- The binding's add-AC path is sanctioned here for adding user-approved
  missing AC to matched stories.
- PRD rewrite: replace embedded AC sections with

  ```markdown
  ## Stories

  | Story | Covers |
  |---|---|
  | PROJ-112 | R1, R2 |
  ```

- Archive, don't delete: `git mv docs/gap docs/_archive/gap` (history stays).

### Read-only variant

Write `docs/reconcile-report.md` instead: the approved mapping table, one
ready-to-paste story body per NEW item, per-file PRD edit instructions, and
the archive `git mv` commands. A human executes; the agent then verifies
with Phase 5 once the human confirms.

## Phase 5 - verification checklist

- [ ] Every APPROVED item has a story ID or an archive location
- [ ] Fresh snapshot (binding command) contains every created story
- [ ] `grep -r "Acceptance Criteria" docs/knowledge/` returns nothing live
- [ ] Archived files intact under `docs/_archive/`
- [ ] Final report: created N, matched M (K AC added), obsoleted X,
      questions resolved Q, deferred D
