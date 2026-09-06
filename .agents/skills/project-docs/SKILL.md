---
name: project-docs
description: Decide where a document belongs, and keep the repo's docs/knowledge/ tree in two-way sync with the tracker knowledge base (YouTrack articles or the GitHub repo wiki; tracker-agnostic via bindings). Owns filing, sections, templates, and the sync - NOT the authoring of specific document types, which have their own skills (to-adr for decisions, to-prd for requirements, to-rad for investigations and proofs of concept, to-wiring for wiring rules). Use when placing a document, creating a section, syncing or publishing docs, resolving a sync conflict, or adopting the docs system. Triggers - "where should this doc go", "file this", "what section", "update docs", "sync docs", "publish the docs", "docs conflict", "set up the docs".
license: MIT
compatibility: Standalone for filing/creating docs. Syncing requires git on PATH plus the binding's connection - YouTrack REST (story-tools connection or YOUTRACK_URL/TOKEN env), or a GitHub token with Contents RW and an initialized wiki.
metadata:
  author: bpappin
  version: "1.24"
---

# Project Docs

Everything lives in exactly one of four kinds of place:

1. **Work** (status, done-ness, task lists) → tracker issues, never files.
   Use the story-workflow skill.
2. **Knowledge** (decisions, specs, research, guides, mandates - anything
   a person would look up) → the tracker's knowledge base, mirrored
   two-way into `docs/knowledge/`.
3. **Visual work** (design records and the samples that go with them) →
   `docs/design/`, a **companion tree to `docs/knowledge/`**. Git-native
   and never synced: it is mostly not prose - mockups, exports, a
   directory per iteration - and what a reader looks up is the direction
   it settled on, not the pile it came from. The to-ux skill owns it.
4. **Machinery** (agent instructions, wiring rules, indexes, tooling
   state) → plain git files. `AGENTS.md` and `WIRING.md` at the repo
   root; doc-system notes and indexes at the `docs/` root. Never synced.
5. **Experiments** (spikes, prototypes, proofs of concept - code written
   to answer a question, expected to be thrown away) → `poc/<name>/`. Not
   knowledge and not product; what it *proved* becomes a RAD. See Proofs
   of concept below.

`docs/stories/` is the generated issue snapshot (story-reconcile skill) -
unchanged by this skill, never edited, never synced as articles.

**Authoring lives elsewhere.** This skill decides *where* a document goes
and keeps it in sync. Writing one is a different job with its own skill:

| Document | Skill |
|---|---|
| A decision, and why the alternatives lost | `to-adr` |
| Requirements | `to-prd` |
| An investigation, a design worked out by discussion, or what a proof of concept proved | `to-rad` |
| UX, AX and visual design | `to-ux` |
| Feature wiring rules | `to-wiring` |

Reach for those when the task is "record this decision" or "write this up",
and this one when the task is "where does it go" or "get it synced". If a
document type has no authoring skill, write it here from its template.

Bundled resources:

- [references/taxonomy.md](references/taxonomy.md) - filing conventions
  and suggested starting sections.
- [references/stability.md](references/stability.md) - the four stability
  levels (spike, proof of concept, experimental, supported), what each
  promises, and where the word gets written.
- `assets/templates/` - starting points: `prd.md`, `adr.md`,
  `research.md`, `qa-plan.md`, `prospect.md`, `pm-brief.md` and
  `bd-brief.md` (audience renderings derived from a PRD - see to-prd),
  plus `readme.md` (section filing guide) and `docs-guide.md` (the
  `docs/README.md` front door).
- Tracker bindings - `tracker.type` in `.agents/config/story-tools.json`
  selects one (absent → youtrack):
  - [references/tracker-youtrack.md](references/tracker-youtrack.md) -
    YouTrack Knowledge Base (`scripts/yt-sync.sh`)
  - [references/tracker-github.md](references/tracker-github.md) -
    GitHub repo wiki (`scripts/gh-wiki-sync.sh`), capability-detected:
    no wiki → `docs/knowledge/` stays git-native, no mirror.

## Titles

The tracker stores a document's title in its own field and renders it above
the body. A heading in the body therefore appears **twice**. So:

- The local file opens with `# Title` — a file needs a title, and it is
  what the sync reads to name the article.
- The sync **strips that heading when pushing** and **restores it when
  pulling**. Only the sync knows the difference; you always see the H1.
- The recorded base keeps the local form, so merges compare like with like.

A rename on the tracker side rewrites the local heading on the next pull —
titles follow the same ownership rule as structure.

Articles created before this carried the heading into the body and show a
doubled title. They fix themselves: the next pull collapses it to one, the
next push removes it from the stored content.

## The sync model

Per-article three-way merge against a recorded base (the state dir the
binding names - commit it, never hand-edit it). **Content flows both
ways** everywhere: a local edit pushes; a KB/wiki edit pulls; both at
once merge. True collisions get git conflict markers, are NEVER pushed,
and wait for you to resolve them with normal git tooling - then the
next sync pushes the resolution.

**Structure ownership differs per tracker** - see the binding:

- **YouTrack: structure flows down.** The KB is the organizing surface -
  humans arrange, rename, and edit articles there and the tree follows.
  Never `git mv` inside `docs/knowledge/` to reorganize - the sync moves
  files back and says so. The one exception: a NEW file's location
  chooses its parent section at birth. Names are ID-prefixed like
  stories: `PROJ-A-12_title-slug.md`, section dirs `PROJ-A-7_section-name/`.
- **GitHub: structure flows up.** The wiki has no hierarchy UI, so the
  local tree owns the layout - reorganize by moving files locally and
  the wiki's page names and generated sidebar follow. Wiki UI edits are
  content edits; a page born in the wiki UI lands at the KB root for
  filing.

## The sync ritual

- **Session start:** run the binding's sync script so the agent works
  from a fresh mirror. `--dry-run` previews; `--pull-only` refreshes
  without pushing anything.
- **After creating or editing docs:** sync again - edits reach the KB
  immediately, article by article. Do not sit on local doc edits.
- **Exit 2 = conflicts.** Open each listed file, resolve the markers,
  sync again. A file with markers is never pushed.
- **When the report lists Moved or Renamed entries:** update any
  `docs/`-root indexes or instruction files that referenced the old
  paths. That's the agent's job, every time.
- **Deletes are deliberate.** Deleting an article/page on the tracker
  side prunes the file on the next sync (a locally-edited file survives
  as a reported conflict). Deleting a local file does nothing until
  `--allow-delete`.

## Filing a document

1. **Does it track work?** → it's an issue, not a file.
2. **Otherwise, pick the section.** Each section directory's `README.md`
   IS the section article's body - read it; it says what belongs there.
   Create the file in that directory with a `# Title` heading (that
   heading becomes the article title), then sync. **The heading is the
   title alone** - no type prefix, no identifier. `# Signal Enrichment`,
   never `# RAD-0023: Signal Enrichment`. The section already says what
   type it is, and a KB or wiki index full of prefixed titles is
   unreadable.

   **Every synced document opens with the same two lines**, directly under
   the heading — an identifier line and a keywords line:

       # Signal Enrichment

       RAD-0023 · 2026-08-05 · status: recommended
       Keywords: geocoding fallback, provider outage, cost per lookup,
                 why not Mapbox

   They live in the body, not frontmatter: the sync pushes the file
   verbatim, so YAML would render as literal text in the article. This is
   also why frontmatter stays minimal (below).

   **Keywords earn their place by being the searcher's words, not the
   document's.** Write the problem as someone would phrase it before they
   knew the answer, and include the options that were rejected — the most
   common search is for the thing you did not choose. A keyword line that
   restates the title helps nobody.
 No matching section?
   Create the directory with a `README.md` describing what belongs in it
   (template: `assets/templates/readme.md`) - the sync births the
   section article too. Suggested starting sections:
   [references/taxonomy.md](references/taxonomy.md).
3. **Creating several docs at once and the order matters?** Prefix the
   new filenames with numbers (`1-`, `2-`, ... `10-`) - they are pushed
   in natural numeric order, so the articles get IDs in that sequence.
   The prefix is consumed at birth: the sync renames each file to its
   ID once the article exists.

Either side can author: a human can just as well create the article in
YouTrack (it appears in the tree on the next sync) or a page in the
GitHub wiki (it lands at the KB root, then gets filed).
When a doc mixes knowledge and a task list, split it - knowledge stays
in the file, tasks become stories; propose the split first.

## Creating a document

Start from the matching template in `assets/templates/`. Keep
frontmatter to title/date only - never status/id fields; lifecycle
state is the tracker's job. PRDs list their stories as tracker IDs
(`PROJ-123`, `#123`) and never contain acceptance criteria.

**`docs/README.md` is the front door** - a git-native guide to the
whole system (template: `assets/templates/docs-guide.md`; create it
during adoption, keep it current when the model changes). It is NOT
synced - it describes the system rather than living inside it.

## After a tracker move (server migrated or project rebound)

When a project is rebound to a different server/connection, the sync
*script* follows the pointer immediately - but the sync *state* does
not: the recorded base (`.yt-sync/` / `.gh-wiki-sync/`), ID-prefixed
filenames, and the `docs/stories/` snapshot all still reference the old
server. Never push blind after a move. The ritual:

1. **Trust exactly one config.** `.agents/config/story-tools.json` is
   the only pointer. Any other file naming a tracker server (legacy
   `.agents/youtrack.json`, `.agents/config/youtrack.json`, configs
   from earlier tooling) is stale - surface it to the user and get it
   removed before syncing anywhere. Two configs disagreeing is a STOP,
   not a coin flip.
2. **Dry-run first.** Run the binding's sync with `--dry-run`. A sane
   plan (your recent local edits, nothing else) means the article/page
   identities survived the migration - sync for real, done.
3. **A wild plan means the identities didn't survive** (mass deletes,
   moves, or creates you didn't make). Re-bootstrap: move the KB dir
   aside (keep it - it holds local-only edits), let an empty-dir sync
   pull the migrated KB fresh from the new server, then port the
   local-only documents into the pulled tree and sync again.
4. **Refresh the snapshots.** `docs/stories/` and `.agents/config/dimensions.md`
   are old-server data until the binding's pull is re-run.

The installer warns about all of this when it detects a rebind; this
section is what to do about the warnings.

## Proofs of concept

Code written to find something out, not to ship. **A spike is the same
thing** - so are "prototype", "experiment" and "throwaway" in this sense -
and all of them land here. The difference between a spike and a proof of
concept is only whether anyone means to keep it (see
[references/stability.md](references/stability.md)); the place and the
rules are identical, so nothing turns on which word gets used.

It lives in `poc/<name>/` with a `README.md` opening on the question it
exists to answer, and it is the one part of the repo where the usual
discipline is deliberately off. A project already using `spikes/` or
`experiments/` should keep it - one such directory, whatever it is called,
and do not create a second.

**Inside `poc/`**, do not apply the project's standards. No acceptance
criteria, no wiring conformance, no test coverage bar, no ADR compliance
check. A proof of concept that is held to production standards stops being
cheap, which removes the only reason to write one. Shortcuts, hard-coded
values and dead ends are all fine and should not be tidied.

**Nothing leaves `poc/` by being copied.** Code moves out only as a story
someone picked up, written against the project's standards, with the
proof of concept as a reference rather than a source. An agent that lifts
a shortcut out of `poc/` into `src/` has done the one genuinely damaging
thing available here. Both halves of this fence get broken in practice -
agents gold-plate spikes, and agents promote them silently.

The README carries the question, the answer once there is one, the
stability level (see [references/stability.md](references/stability.md)),
and what it was measured against. **Findings go to a RAD** - `to-rad`,
filed in the knowledge base - because a finding nobody wrote up is a
proof of concept you will run twice.

**Measured findings carry their environment or they rot.** Anything
established by experiment is true against particular versions, and the
versions move. Record them on the RAD's `Measured against:` line, and say
whether the proof of concept still runs; re-deriving a finding is far
cheaper than trusting a stale one.

Retire them. A `poc/` directory that only grows is a second codebase
nobody maintains: once the RAD is written, the proof of concept has done
its job and can go.

## Reorganizing

Structure changes happen on the side that owns structure - YouTrack:
move and rename articles there and sync; GitHub: move files locally
and sync. Either way, fix references the Moved/Renamed report surfaces.
For adopting this system over a legacy docs tree, see the migration
notes in [references/taxonomy.md](references/taxonomy.md).
