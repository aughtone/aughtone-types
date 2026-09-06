---
name: story-workflow
description: Work a tracker story with strict scope discipline - focus one story, treat its acceptance-criteria checklist as the scope, route discovered work to new linked issues, and gate completion. Tracker-agnostic via per-tracker bindings (YouTrack and GitHub today; Jira later). Use whenever starting development work, picking up a ticket, resuming a task, checking off acceptance criteria, completing a story, or when new work is discovered mid-task. Triggers - "work on", "pick up", "resume", "what am I working on", "start the next story", "is this done", issue IDs like PROJ-123 or #123.
license: MIT
compatibility: Requires a connection to the project's issue tracker (see the tracker binding for specifics; YouTrack needs MCP, Cloud or Server 2025.3+)
metadata:
  author: bpappin
  version: "1.24"
---

# Story Workflow

The tracker owns stories and acceptance criteria; ADRs/PRDs are repo docs.
The AC list of the focused story IS the scope of the current session. This
file defines the workflow in tracker-neutral operations; a binding file
maps each operation to the concrete tools of the tracker in use.

## Tracker dispatch — do this first

Read `.agents/config/story-tools.json` in the project. `tracker.type`
selects the binding (absent → `youtrack`); the rest of the `tracker` object
carries connection facts (server name, URL, project key). Load the matching
binding and use ONLY its tools:

- `youtrack` → [references/tracker-youtrack.md](references/tracker-youtrack.md)
- `github` → [references/tracker-github.md](references/tracker-github.md)

A developer may have several trackers/instances configured — always use the
one this project's config names, never another. `story-tools.json` is the
ONLY authoritative pointer: if some other config file names a different
tracker server (legacy `.agents/youtrack.json`, `.agents/config/youtrack.json`,
leftovers from earlier tooling), that file is stale — flag the
disagreement to the user and stop; never pick a server by guessing.
After a server move, local mirrors (`docs/stories/`, `.agents/config/dimensions.md`,
docs sync state) reference the OLD server until refreshed — the
project-docs skill has the recovery ritual.

If the binding's tools are unavailable (no MCP connection on this machine,
server unreachable) or the user asks to work disconnected, fall back to
**offline mode** — [references/offline.md](references/offline.md): the same
operations, recorded in a local worklog and replayed later via
story-reconcile. Point the user at the installer once, confirm, then work.

## The operations

| Operation | Meaning |
|---|---|
| `focus.get` / `focus.set` | Which story is this session working on |
| `story.context` | Full briefing: Purpose, Specification, AC list + state, links, references |
| `ac.toggle` | Check/uncheck one AC item (verifiably complete only) |
| `ac.add` | Expand scope — explicit user approval only |
| `work.discovered` | Log out-of-scope work as a NEW linked issue |
| `story.completeCheck` | Verdict: all AC done? QA required and present? |
| `effort.log` | Record human-approved EFFORT on the focused issue (was `work.logTime`). The developer's working day is a separate record - see the `worklog` skill |
| `story.next` | Pick the next story: highest priority, ready first |

The story format is the same everywhere (see
[references/ac-format.md](references/ac-format.md)): `## Purpose` and
`## Specification` in prose, a `## Acceptance Criteria` markdown task list,
optional `## References` (ADR/PRD paths) and `## QA` (Gherkin). Purpose and
Specification are the story's intent - read them before the AC, and never
toggle or edit them as if they were checklist items.

## Session start

1. Note the current time — this is the session's start for time logging.
2. `focus.get` — if a story is focused, confirm it with the user; if not,
   ask which story to work ("start the next story" → `story.next`: the
   highest-priority ready story), then `focus.set`.
3. `story.context` — read `## Purpose` and `## Specification`, then the AC
   list, and open the `## References` docs before writing any code. The AC
   is the scope; Purpose and Specification are why and what-exactly, and
   the edge cases live there rather than in the checklist. The context
   includes priority and tags — the tags tell you what this story groups
   with.
4. Restate scope to the user in one line: the unchecked AC items.
5. Move the story onto the board: set Stage to the project's in-progress
   column (e.g. "Develop") using the binding's state tool — announce it in
   the scope line, don't ask. Read the actual column names from the
   project's dimensions; never invent one. Already in progress → no-op.
   Leave State alone — it records how the story resolves, not where it is.

## While working

- Work only toward unchecked AC items.
- When an item is verifiably complete (tests pass, behavior confirmed),
  immediately `ac.toggle` it. Never batch-check items at session end.
- **Discovered work reflex**: any bug, refactor need, idea, or missing
  feature that does not serve an unchecked AC item → `work.discovered`
  (summary + description) → tell the user in one sentence → continue the
  focused story. Do not fix it inline, do not expand scope silently. This
  includes "quick wins" and "while we're here" fixes.
- `ac.add` is allowed only when the user explicitly asks to widen this
  story's scope. When in doubt, offer `work.discovered` first.

## Effort on the focused story

There are TWO kinds of time record and this skill owns only one of them.

**Effort** is time spent on *this issue*, recorded on the issue. That is
`effort.log`, and it is what this section is about.

**A work log** is the developer's working time, attributed to a *project*
and destined for a timesheet or an invoicing tool. It is a separate record
with a separate owner — the `worklog` skill — and this skill never writes
it. A day contains meetings, several projects, and work no issue covers;
none of that is effort on a story.

Mixing them produces two specific failures, both seen in the wild:

- Recording a whole working day as effort on one issue. Eleven hours never
  belongs to a story - that is a work log, and the `worklog` skill owns it.
- Hunting for "the best home" for a number when no single story owns it.
  **If no story is focused, there is no effort to record** — that time is
  work-log time, and the answer is to say so, not to pick an issue.

Effort goes on the focused story and nowhere else. Never search for a
story to carry a number, never split a number across several.

At session close (completion, handoff, housekeeping, or "I'm done"), if a
story was focused, compute end minus session start, round to the nearest 15
minutes, and propose ONE entry: "About 2h on PROJ-123 — log it as effort?"
On approval, `effort.log`.

- **Never record effort silently** - every entry is a number the user
  approved.
- **The duration is the user's to state, never inferred.** Do not derive it
  from commits, tracker activity or elapsed tool calls, and never adjust it
  to hit a target. "It says 3h but I worked 5" is a correction, not a
  conflict - take the number given.
- Gaps inside a session are work (thinking counts); don't subtract them.
- An absurd computed number (unclosed session overnight) → ask what the
  session actually took, and consider whether it is effort at all or the
  developer's day, which is the work log's business.
- **"log time", "log 3h", "log my day" are NOT this.** Those phrases mean
  the developer's work log — hand them to the `worklog` skill (à la carte;
  if it is not installed, say so rather than logging anywhere else), which
  attributes to a project and feeds timesheets. Only an explicit "log 30m
  ON PROJ-123" is effort, and only because they named the issue.
- Effort should be near-automatic: the session had a focused story and a
  duration, so propose it at close and take a one-word yes. It never
  competes for the phrase "log time".
- A comment on the entry is one short line, not a play-by-play.

## Priority and tags

- Priority is read from context, set by triage/planning. Never change a
  story's priority on your own; suggest a change to the user instead.
- Topical tags are Title Case, human-readable ("Trust Insights", not
  "trust-insights") and mark feature-level grouping. Component ownership
  belongs in the Subsystem field (shown in context) - never duplicate
  subsystem names as tags.
- **Human-added tags are data.** A tag you don't recognize is someone's
  grouping, not clutter - never remove or rename tags on your own
  initiative. Adding, removing, or merging tags at the user's direction
  is fine, and you may propose a tidy-up you've noticed; execute only
  what they approve.
  Release membership is the Fix versions field (shown in context), never a
  tag. Reserved workflow tags (`ready-for-agent`, `needs-gherkin`,
  `discovered`, `triaged`, triage roles) are machinery - never repurpose
  them.
- Discovered work inherits the story's topical tags automatically and lands
  at default priority - urgency is a triage decision, never copied from the
  current story.

## Completing

1. `story.completeCheck` — if not ready, work through what it reports; do
   not declare the story done.
2. If QA is required and missing, write Gherkin scenarios for the verified
   behavior into a `## QA` section
   ([references/ac-format.md](references/ac-format.md)).
3. When ready: confirm with the user, then move the story using the
   binding's state tool. Where the board has a testing/review column
   (e.g. "Testing", "Review"), Stage goes THERE, not to done — completion
   by the implementer means ready-for-verification; a human (or the QA
   pass) moves it to done. Only boards without a review column go
   straight to the done column. Where the project separates flow from
   resolution (a Stage field AND a State field), also set State to how it
   concluded (usually Fixed) — the story can sit in Testing with State
   Fixed; that is the two fields doing their jobs. Mention any open
   discovered-work issues.
4. Offer the session time entry (see Session time) if not yet logged.

## Coaching the human

This workflow is new to most people - assume the user forgets the ritual
and remind them at natural moments, one light line at a time, never
nagging:

- **Session start**: if the untriaged query has items, mention it once:
  "3 captures are waiting - say 'show me what needs attention' whenever
  you want to triage."
- **Mid-session**: when the user muses about work that is not the focused
  story ("we should also...", "someday it'd be nice..."), offer the
  capture: "want me to record that for later?" - then do it and return
  to the story. Never let a good idea evaporate OR derail the session.
- **Session close**: walk the ritual unprompted - completion check, stage
  move, the time proposal, docs sync where the project has one. The user
  should never have to remember the checklist; that is what you are for.
- **"How does this work again?"** - answer from `WORKFLOW.md` (the
  project-local guide the installer maintains); keep the answer to the
  piece they asked about.

## Read-only mode

The project is read-only when the config has `"readOnly": true`, or when a
write operation is refused server-side. In that mode: never call write
operations; track AC progress and discovered work in session notes; at
checkpoints hand the user a concise change list (AC items to check,
discovered issues to file in canonical format). Reads — and focus, where
the binding marks it safe — remain allowed. (No connection at all is the
related case — see [references/offline.md](references/offline.md).)

## Guardrails

- **If this developer has not run setup, say so before anything else.**
  Per-developer state lives in `~/.agents/story-tools/` - the credential
  and the role. If there is no entry for this project in
  `~/.agents/story-tools/developer.json`, or tracker tools are missing or
  unauthenticated, stop and tell them to run `.agents/setup.sh` in the
  project (or `install.sh` from the skills repo if the project ships no
  copy). Nothing in the repo carries it: skills and workflow docs are
  committed and shared, so a fresh clone looks fully set up when the
  person is not.
- **Respect the roles recorded there.** `developer` works ready stories,
  files issues and bugs, and routes anything unclear back to triage.
  `lead` does triage routing: priority, subsystem, deciding a story is
  ready. `architect` makes the technical calls - architecture decisions,
  ADRs, research records. `product` decides what the product does: PRDs
  and requirements. Someone can hold several; all of them is the ordinary
  answer for a person working solo. If a role is missing, do not steer
  them into that work - file it or hand it back instead. This is a hint
  about whose call something is, not a permission: the tracker enforces.

- One focused story per session; changing focus requires the user's
  explicit request.
- If `ac.toggle` is refused for drift, re-read `story.context` and retry
  against the current list.
- **A story with no `## Acceptance Criteria` is not ready to be worked.**
  Stop. Do not draft them here and do not write code. Tag it `needs-triage`,
  say what is missing, and hand it back - a story arrives with its scope
  already decided, or it is not a story yet.
  - This is not a permissions rule, it applies to everyone. Requirements
    invented inside a work session are invented by whoever happens to be
    holding the ticket, under time pressure, with no one reviewing them.
    That is the scope hole this skill exists to close.
  - If you own this project's requirements and want to fix it now, that is
    a **separate, deliberate step**: leave the work session, triage the
    story properly, then pick it up. Not a detour mid-implementation.
  - Filing the gap is always in scope. Anyone can create an issue or a bug
    and let triage decide - that is the off-ramp, and it is never blocked.
- **Never ask for or accept tokens, credentials, or connection secrets in
  conversation** — many organizations rightly prohibit it. All
  configuration, especially secrets, is entered only through the
  story-tools installer. Missing/invalid credentials → "run
  `.agents/setup.sh`" when the project ships it, else "run `install.sh`"
  from the skills repo - never "paste your token here".
- **Never edit an installed skill in place.** Skills listed in
  `.agents/skills/MANAGED.md` are installer-managed copies, overwritten
  on every refresh - an edit there is lost silently and never reaches
  other projects. Improving one is discovered work: `work.discovered`
  (or tell the user) so it lands in the story-tools source repo.
  Unlisted skill directories are the project's own; leave them alone
  unless the user asks.
- Background reading: [references/methodology.md](references/methodology.md)
  — the lifecycle, why the off-ramp matters, where BDD fits, writing good AC.
