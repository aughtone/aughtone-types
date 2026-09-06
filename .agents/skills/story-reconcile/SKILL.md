---
name: story-reconcile
description: Adopt the story workflow in an existing project - reconcile local documentation (GAP files, AC embedded in PRDs, TODO lists) and offline-session pending logs with the issue tracker, and pull existing tracker stories into a local snapshot. Tracker-agnostic via bindings (YouTrack and GitHub today). Use when a team starts using story-tools mid-project, or when local docs and the tracker have drifted. Triggers - "reconcile", "replay the pending log", "replay the worklog", "we worked offline", "adopt the story workflow", "migrate gaps", "import our docs into the tracker", "import into youtrack", "pull stories local", "sync up with youtrack".
license: MIT
compatibility: Requires a connection to the project's issue tracker (read access minimum; see the tracker binding). Writes optional - produces a report instead when read-only.
metadata:
  author: bpappin
  version: "0.30"
---

# Story Reconcile

One-time (or occasional) adoption pass that makes the tracker the single
source of truth for task-level work. After it completes: stories + AC live
in the tracker, PRDs reference story IDs, GAP-style files are archived, and
a local read-only snapshot of the server exists for offline reference.

**Tracker dispatch:** read `.agents/config/story-tools.json` →
`tracker.type` (absent → `youtrack`) and use that binding's commands:

- `youtrack` → [references/tracker-youtrack.md](references/tracker-youtrack.md)
- `github` → [references/tracker-github.md](references/tracker-github.md)

Never write to the tracker before the user approves the mapping (Phase 3).

Bundled resources:

- [references/reconcile-procedure.md](references/reconcile-procedure.md) —
  full procedure, mapping-table and report templates.
- [references/ac-format.md](references/ac-format.md) — the canonical story
  format for drafting NEW stories (same on every tracker).

## Phases

1. **Inventory local.** Find task-shaped local docs: GAP files,
   `## Acceptance Criteria` (or AC/checklist sections) inside PRDs,
   TODO/backlog files, and the **offline pending log**
   (`.agents/offline/pending.md` - sessions recorded by story-workflow's
   offline mode: AC toggles, discovered work, time entries). Extract each
   discrete item: text, source file, apparent status. Skip worklog sessions
   already marked `Reconciled:`. ADRs are not tasks - leave them alone.

2. **Inventory server.** Run the binding's snapshot command to mirror
   existing stories into `docs/<tracker>/`. Read the snapshot INDEX.

3. **Propose the mapping - then STOP for approval.** One table, every local
   item accounted for: MATCHES existing story (cite ID; note AC diffs),
   NEW story needed (draft summary + AC in canonical format), or
   OBSOLETE (done/abandoned - say why). Flag conflicts where local and
   server disagree; recommend a resolution but let the user decide.
   Present the table and wait for explicit approval.

   **AC are authored, never extracted.** Source text (gap prose, notes,
   checklist fragments) is raw material for the story's `## Purpose` and
   `## Specification` - it does not become AC by slicing it into
   checkboxes. Every AC item you propose must independently pass the
   ac-format bar: a verifiable outcome, not a fragment, an activity, or a
   question. When the source doesn't support verifiable AC (most gap files
   won't):
   - Create the story **prose-only**: `## Purpose` and `## Specification`
     from the source, an `## Open Questions` section for the questions the
     source raises, NO fabricated `## Acceptance Criteria` section, tagged
     `needs-triage`.
   - AC get authored lazily: the triage skill grills the story into
     `ready-for-agent` shape when it approaches work, and story-workflow
     refuses to start a story without AC - that gate is the safety net.
   - Only author AC during reconciliation for stories the user wants
     ready NOW; that authoring effort is real - don't spend it on 200
     backlog items nobody is about to pick up.

4. **Execute.** Only after approval, and only what was approved:
   - Create NEW stories via the binding, `## References` pointing back at
     the source ADR/PRD paths.
   - Update MATCHED stories only where the user approved a change (the
     binding's sanctioned add-AC path).
   - Rewrite each PRD's task content into a `## Stories` table of IDs;
     remove embedded AC.
   - Replay approved worklog entries: toggle the AC items on their
     stories, create the discovered-work issues, record the effort
     entries (`effort.log` - the numbers were already user-approved when
     recorded). Mark each applied session block `Reconciled: <date>`;
     when every session is applied, offer to delete the pending log.
   - Move GAP-style files into `docs/_archive/` (do not delete).
   - **Read-only mode** (config `readOnly: true` or writes refused): skip
     all writes; emit `docs/reconcile-report.md` with ready-to-paste story
     bodies and per-file edit instructions for a human.

5. **Verify.** Re-run the snapshot; confirm every approved item maps to a
   live story, no PRD still contains live AC, and the archive holds the
   old files. Report counts: created / matched / obsoleted / deferred.

## Guardrails

- Never ask for or accept tokens/credentials in conversation. If the
  snapshot script or tracker tools can't authenticate, direct the user to
  `.agents/setup.sh` when the project has it, else the story-tools
  installer (`install.sh`) - secrets are entered only there.
- This skill migrates *documents*; it never changes issue states.
- If an item is ambiguous, put it in the mapping table as a question - do
  not guess silently.
- Large backlogs: process in batches of ~20 items per approval round.

## Conflicts in generated files

**Never hand-merge one.** But "take either side" is only safe for some of
them, and getting that wrong destroys work - so check which kind you have.

**The snapshot** - `docs/stories/*`, `INDEX.md`,
`.agents/config/dimensions.md`. Derived from the tracker on every pull.

- If neither side holds a change the tracker does not already have: take
  either side, re-run the pull, done.
- If one side does - somebody edited a story file locally, or edited it in
  the tracker while the other pulled - **the change goes to the tracker
  first**, then re-pull. That is this skill's job: reconcile the local
  document, push it, regenerate. Never resolve it by editing the file,
  because the file is about to be overwritten and the tracker will not have
  learned anything.
- The files say "do not edit" for this reason. When one has been edited
  anyway, treat the edit as real work to be reconciled, not as noise to
  discard.

**The pending log** - `.agents/offline/pending.md`. This one is NOT derived
and NOT overwritten: it is append-only, and two people working offline
produce different `## Session` blocks. **Keep both sides.** A union of the
blocks is the correct resolution and taking either side silently loses
somebody's session. Replay afterwards.

**The setting, if this keeps happening.** The project chooses whether the
snapshot is committed or local (`snapshot` in the pointer). **Committed**
travels with the repo and is readable with no credential - valuable when
the tracker is unreachable - but every pull rewrites it, so a team
collides. **Synced** is gitignored and regenerated per developer: no
conflicts, and a fresh clone has none until someone pulls. Frequent
conflicts mean the setting is wrong, not that people should resolve harder.
