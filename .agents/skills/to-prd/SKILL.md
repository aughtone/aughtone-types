---
name: to-prd
description: Synthesize conversation context and codebase understanding into a formal PRD, with verification living on tracker stories - then derive the product and commercial briefs the PRD's other audiences need. Use when the user wants to formalize a plan, feature idea, or requirement discussion into a PRD, or wants an existing PRD restated for product management or business development. Triggers - "write a PRD", "formalize this plan", "turn this into requirements", "brief for the PM", "what do we tell sales", "commercial brief", "what can we promise".
license: MIT
compatibility: Standalone for the PRD document; creating the verification stories uses the to-issues skill and the project's tracker.
metadata:
  author: bpappin
  version: "1.5"
---

# Product Requirements (to-prd)

Synthesize the current context into a **PRD document** in the **Product Requirements** section of
`docs/knowledge/`. The
PRD carries requirements and intent; **verification lives on tracker
stories** (their `## Acceptance Criteria` checklists), never in a companion
AC file — the tracker is the source of truth for anything with done-ness.

Works with the project-docs skill: this skill is the authoring workflow;
project-docs owns the conventions, the template, and the KB sync.


> **A PRD is a product decision, not a capture.** If you are recording
> something you noticed - a bug, a gap, an idea, a story that needs
> writing - file it as an issue and let triage route it. Only write a PRD
> when deciding what the product does is your call to make. Nobody should
> be pushed into authoring one because a story they picked up turned out
> to be thin.

## Process

### 1. Synthesis

From the conversation and the codebase: identify the problem, the actors,
and the major modules — look for opportunities to extract deep modules
(isolatable, testable logic). Use the project's domain glossary and respect
existing ADRs. If research fed this (the Research section of `docs/knowledge/`), reference it.

### 2. Write the PRD

Create `<slug>.md` in the Product Requirements section directory of
`docs/knowledge/` (find it by its README H1; create it per project-docs if
missing) from the project-docs template (`assets/templates/prd.md` in that
skill). No id/status frontmatter — the `# Title` heading names the KB
article, and the sync gives the file its ID-prefixed name. Sections:

- **Problem** — from the user's perspective, with the discovery trail
  (link Research records that led here).
- **Goals / Non-goals** — non-goals are the requirements-level scope guard.
- **Requirements** — numbered (R1, R2 …) narrative requirements, backed by
  an extensive list of user stories ("As an <actor>, I want <feature>, so
  that <benefit>").
- **Decisions** — architectural choices, API contracts, module boundaries.
  No file paths or code snippets, unless a snippet encodes a decision
  (state machine, schema, type shape) more precisely than prose.
- **Stories** — the table of tracker story IDs. Leave it with a
  placeholder note until step 3 fills it.

Run the project-docs sync after writing — the PRD becomes a KB article
immediately (and again after step 3 fills the Stories table).

### 3. Create the verification

Offer to run the **to-issues** skill to break the PRD into tracker stories
(vertical slices, each carrying its own `## Acceptance Criteria`). When the
stories are published, fill the PRD's `## Stories` table with their IDs and
which requirements each covers. Where a requirement has strict rules or
needs test automation, note it so the story gets the `needs-gherkin` tag.

### 4. Derive the audience briefs

A PRD is written for the people building the thing. The same decisions
matter to people who do not read module boundaries, and re-explaining it
verbally each time is how the versions drift apart.

**Separate documents, not renderings.** Each brief is authored in its own
audience tier and is a first-class document there — see the
audience-replication convention in project-docs' taxonomy. Do not write one
document with a section per reader; nobody reads past their own part.

**Cross-link all of them.** That is what stops them diverging silently, and
it is how a reader who needs more depth finds it. Where two state the same
fact, **the PRD owns it** — correct it there first, then carry the
correction outward.

**Each tier may hold what the others do not.** Competitive positioning was
never in the PRD and does not belong there. But if a brief needs something
the PRD *should* have said — success signals, a firm date, a segment — that
is a gap in the PRD. Fix it there, then write the brief. This is the most
useful thing about the exercise: it finds the holes.

**Product brief** (`assets/templates/pm-brief.md` in project-docs) - write
this whenever the PRD represents a real product decision. Outcomes, users,
non-goals in plain terms, how we will know it worked, sequencing, risks. No
module names; if the problem cannot be stated without them, the PRD's
Problem section is not finished.

**Commercial brief** (`assets/templates/bd-brief.md`) - **only when it makes
sense**, and often it does not. The test: *does this change what someone
outside the company can be told, sold, or promised?* A new capability, a
changed limit, a new integration - yes. Refactors, tech debt, internal
tooling, performance work nobody asked for - no, and producing one anyway
trains people to ignore them.

When you do write one, the section that earns its place is **what it does
NOT do**. Commercial harm comes from promises made in the gap between what
shipped and what someone assumed shipped. Mark availability as committed,
planned, or exploratory, because a reader assumes the strongest reading you
leave open. Never carry story IDs, module names, or internal codenames into
it.

Both live beside the PRD in the Product Requirements section unless the
project has a commercial or go-to-market section, in which case the
commercial brief belongs there. Sync after writing.

## Review checklist

- [ ] Are the user stories comprehensive?
- [ ] Is there a discovery trail (research/ADR links) in the Problem section?
- [ ] Are decisions decoupled from specific files?
- [ ] Are non-goals explicit?
- [ ] Is the Stories table filled (or explicitly deferred to to-issues)?
- [ ] No AC in the PRD - checklists belong to the stories.
- [ ] Does the PM brief read without a single module name?
- [ ] Does the PRD actually say how success is measured, or did the brief
      expose that it does not?
- [ ] If there is a commercial brief, does it pass the outside-the-company
      test - and does it state what the thing does *not* do?
- [ ] Does each brief cross-link the PRD and the other tiers, with a date?
