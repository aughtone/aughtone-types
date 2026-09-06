---
name: to-adr
description: Record a decision as an Architecture Decision Record - the forces, the choice, what was rejected and why. Use the moment a hard-to-reverse choice is made, not later - a library or technology picked over alternatives, an approach settled after weighing trade-offs, a constraint accepted, an earlier decision superseded, or an approach abandoned after it failed. Triggers - "we decided", "we're going with", "let's use X instead of Y", "chose X over Y", "record this decision", "new ADR", "document this trade-off", "why did we do it this way", "supersede that decision", "that approach didn't work".
license: MIT
compatibility: Standalone for authoring. The project-docs skill owns where docs live and the tracker KB sync; run its sync after writing if the project has one.
metadata:
  author: bpappin
  version: "1.3"
---

# Architecture Decision Records (to-adr)

An ADR captures **why** a choice was made, so the next person - or the next
agent - does not relitigate it, and can tell when the reasons have expired.

Works with the project-docs skill: this skill is the authoring workflow;
project-docs owns the conventions, the section, and the KB sync.


> **An ADR is a technical decision, not a capture.** If you are recording
> something you noticed rather than making the call, file it as an issue
> and let triage route it. Nobody should be pushed into writing one because
> the story they picked up turned out to be thin.

## When it is an ADR

Write one when a choice is **hard to reverse** and someone could reasonably
have chosen otherwise. A decision with no alternatives was not a decision.

- **ADR** - why a choice was made. The decision is frozen once accepted; new knowledge means a new ADR that supersedes it, not a rewrite.
- **Spec** - how a thing IS. Updated in place. If you are describing current
  behaviour, that is a spec, not an ADR.
- **Code comment** - why this line is odd. Local, no alternatives weighed.

Do not write one for a reversible or obvious choice. A repo full of
ceremonial ADRs is worse than none, because the real ones stop standing out.

**Write it when the decision lands**, not at the end of the work. The
reasoning is available for about an hour and then it is reconstruction.

## Before writing

1. **Find where ADRs live.** Do not assume. Look for `docs/adr/`,
   `docs/decisions/`, or an "Architecture Decision Records" section under
   `docs/knowledge/`. Match whatever is already there - numbering, naming,
   frontmatter. A new ADR that does not match its neighbours is a smell.
2. **Read the last two.** They show the house style and may already cover
   this ground.
3. **Check for one to supersede.** If this reverses or narrows an earlier
   decision, that is a relationship to record in both directions.

## Writing it

Start from the project-docs template
(`assets/templates/adr.md` in that skill) and keep to its four sections.
Number sequentially, zero-padded, with a slug in the filename:
`0007-postgres-over-dynamo.md`.

**Keep the number out of the heading.** The heading is the title alone —
`# Postgres over DynamoDB`, not `# ADR-0007: Postgres over DynamoDB`. It
becomes the article title when synced, and an index of prefixed titles is
unreadable. The identifier belongs on the metadata line:

    # Postgres over DynamoDB

    ADR-0007 · 2026-08-05 · Status: accepted

**Context** - the forces, honestly. What pressure produced this decision.
Include the constraints that were not negotiable, because those are what
change later and make the decision revisitable.

**Decision** - actively phrased, in one or two sentences. "We use X."

**Consequences** - what gets easier, what gets harder, what was given up.
The "harder" list is the one people skip and the one that pays off.

Two things that make the difference between an ADR worth reading and a
formality:

- **Name the alternatives and why each lost.** A decision recorded without
  its rejected options cannot be re-evaluated later; the reader has no idea
  whether the reasons still hold.
- **Record what was tried and failed.** If an approach was attempted and
  abandoned, that is the most valuable content in the document - it is the
  thing a future reader is most likely to try again. Failures are evidence,
  not embarrassment.

State uncertainty where it exists. "We believe X, unverified" ages far
better than false confidence, and tells a reader exactly what to re-check.

## After writing

- **Context and Decision are frozen once accepted; everything else
  accrues.** A changed decision is a new ADR, never an edit to the old
  one - write it, cross-link both ways, and the old gets
  `Status: superseded by ADR-0012`. Editing the original instead leaves a
  record of reasoning nobody ever applied, and quietly repoints every code
  comment and story that cites the old ID at a different decision.
- **Consequences are the exception, and they are the part worth having.**
  What a decision actually cost is learned months later, not on the day it
  was made. Add to Consequences as it becomes known, dated, and leave what
  was already there - a cost that was predicted and a cost that arrived are
  two different facts, and the distance between them is how anyone
  calibrates the next decision. Correcting a broken link, a typo or a
  mis-stated fact is fine on the same grounds: the *decision* is frozen,
  the document is not.
- Point at it from the work it governs - the PRD, the spec, the story's
  `## References`.
- Where a decision constrains a library's public surface, summarise the rule
  in that library's shipped skill and leave the reasoning here. Do not
  re-argue it there.
- Run the project-docs sync if the project has one; the ADR becomes a KB
  article.

## Adopting mid-project

Decisions already made and undocumented are worth recording only where they
are still live and still contested. Write those; do not backfill history for
its own sake. Mark them plainly as reconstructed after the fact, since the
context is remembered rather than captured.
