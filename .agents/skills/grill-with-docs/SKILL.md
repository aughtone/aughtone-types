---
name: grill-with-docs
description: Interview the user relentlessly about a plan or design until reaching shared understanding, resolving each branch of the decision tree - and, where the project has a docs system, challenge the plan against the domain glossary and record decisions as they crystallise. Use when the user wants to stress-test a plan, sharpen terminology, or get grilled on their design. Triggers - "grill me", "stress-test this plan", "poke holes in this", "challenge my design", "am I missing anything".
license: MIT
metadata:
  author: bpappin
  version: "2.1"
---

<what-to-do>

Interview me relentlessly about every aspect of this plan until we reach a
shared understanding. Walk down each branch of the design tree, resolving
dependencies between decisions one-by-one. For each question, provide your
recommended answer.

Ask the questions one at a time, waiting for feedback on each question
before continuing.

If a question can be answered by exploring the codebase, explore the
codebase instead.

</what-to-do>

<supporting-info>

## First: what does this project have?

The interview above is the whole skill. Everything below is what to do
with what the interview produces, and it scales to the project:

| Present | Then |
|---|---|
| A domain glossary (see below) | Challenge terminology against it; update it inline |
| `docs/knowledge/` (project-docs) | Offer ADRs there; sync after writing |
| A tracker (`.agents/config/story-tools.json`) | Route surfaced *work* to issues |
| None of the above | Just grill. Do not create a docs tree, a TODO file, or scratch notes to hold the output - report the conclusions in the conversation and offer to set up the docs system if the user wants decisions to persist |

Never invent a home for output. A decision with nowhere to go is a
sentence in the conversation, not a new file convention.

## Finding the glossary

The domain glossary is **knowledge**, so it lives in `docs/knowledge/`
like every other document - typically a "Domain Glossary" document in
the Reference section (or, for a multi-context repo, one per context).
`AGENTS.md` at the repo root carries the pointer: a line naming where
the glossary lives, so agents find it without guessing at paths (the
sync gives files ID-prefixed names, so the path is not stable - the
pointer and the title are).

- No pointer in `AGENTS.md`? Look for a document titled "Domain
  Glossary" in the knowledge tree before concluding there isn't one.
- Found a legacy root `CONTEXT.md`? That is the old location. Offer to
  move it into the knowledge tree and add the `AGENTS.md` pointer -
  then it syncs to the KB/wiki with everything else.
- No glossary at all? Create one lazily - only when the first term is
  actually resolved - and add the `AGENTS.md` pointer at the same time.
  Format: [references/GLOSSARY-FORMAT.md](references/GLOSSARY-FORMAT.md).

## During the session

### Challenge against the glossary

When the user uses a term that conflicts with the existing language,
call it out immediately. "Your glossary defines 'cancellation' as X, but
you seem to mean Y - which is it?"

### Sharpen fuzzy language

When the user uses vague or overloaded terms, propose a precise canonical
term. "You're saying 'account' - do you mean the Customer or the User?"

### Discuss concrete scenarios

Stress-test domain relationships with specific scenarios that probe edge
cases and force precision about boundaries between concepts.

### Cross-reference with code

When the user states how something works, check whether the code agrees.
Surface contradictions: "Your code cancels entire Orders, but you just
said partial cancellation is possible - which is right?"

### Update the glossary inline

When a term is resolved, write it into the glossary right there - don't
batch. The glossary is a glossary and nothing else: no implementation
details, no spec content, no scratch notes.

### Offer ADRs sparingly

Only offer an ADR when all three are true (the triple test in
[references/ADR-FORMAT.md](references/ADR-FORMAT.md)): hard to reverse,
surprising without context, the result of a real trade-off. ADRs go in
the Architecture Decision Records section of `docs/knowledge/` using the
project-docs template.

### Sync when the docs change

Doc edits are not done until they reach the KB - run the sync from the
project-docs skill's tracker binding (`yt-sync.sh` for YouTrack,
`gh-wiki-sync.sh` for a GitHub repo wiki). A GitHub project with no wiki
has no mirror to sync: the files in git ARE the record, nothing else to
do.

### Route work to the tracker

When grilling surfaces *work* - a missing feature, a bug, a follow-up -
it becomes a tracker issue (the discovered-work off-ramp, or to-issues
for a batch), never an inline scope expansion, a GAP document, or a
local TODO. Decisions go to docs; work goes to the tracker. No tracker
configured → the offline pending log (story-workflow's offline mode), still
never a TODO file.

</supporting-info>
