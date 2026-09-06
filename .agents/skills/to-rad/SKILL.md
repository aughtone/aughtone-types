---
name: to-rad
description: Record a Research & Development log (RAD) - the trail behind a technical question, landing on findings and a recommendation. Use while a hard problem is still being worked out - comparing options, weighing trade-offs, chasing an approach that failed, checking an assumption, and when someone asks for a "DD" - the engineering detailed/technical design document - since no such type exists here and the investigation belongs in a RAD. NOT for UI, UX, visual or accessibility design, which is a different skill entirely; "design" alone never means this. RADs hold what is NOT yet decided; a recommendation that hardens becomes an ADR. Triggers - "what are the options", "let's compare", "I'm not sure which approach", "weigh the trade-offs", "write this up", "DD", "detailed design document", "capture what we worked out", "why did we rule that out", "log the research", "write up the spike", "what did the spike show", "proof of concept", "POC".
license: MIT
compatibility: Standalone for authoring. The project-docs skill owns the KB section layout and the tracker sync; run its sync after writing.
metadata:
  author: bpappin
  version: "1.4"
  supersedes: to-research
---

# Research & Development Logs (to-rad)

A RAD is the record of how a question got worked out — the options
compared, the sources consulted, the dead ends, the trade-offs — landing on
findings and a recommendation. It is the "we figured this out" trail, kept
so a future reader can trust the conclusion instead of re-deriving it.

It sits **upstream of a decision**. It explores, weighs, and recommends; it
does not commit.


> **An RAD is a technical decision, not a capture.** If you are recording
> something you noticed rather than making the call, file it as an issue
> and let triage route it. Nobody should be pushed into writing one because
> the story they picked up turned out to be thin.

## Where it fits

| Document | Answers |
|---|---|
| **RAD** | "What did we research, and what do we recommend?" |
| **ADR** | "What did we decide, and why?" — committed, append-only |
| **PRD** | "What must we build?" — requirements plus tracker stories |
| **Spec** | "How does the thing work now?" — updated in place |

The progression is RAD → ADR → PRD → Spec. A recommendation that hardens
into a commitment graduates into an ADR (`to-adr`); requirements it implies
become a PRD (`to-prd`) and tracker stories; once built and stable, a Spec
describes what exists.

**RADs are where undecided things legitimately live** — open questions,
deferred options, approaches parked rather than killed. An ADR, being a
decision, must not carry those. This is the property that makes RADs worth
having.

## When someone asks for a "DD"

First, disambiguate. **In this system "design" means UX, UI and
accessibility work** — mockups, screens, visual direction — and that is a
different skill. A RAD has nothing to do with it.

A **DD** is the other thing: the engineering detailed-design or
technical-design document. There is usually no such type here, and
inventing one fragments the taxonomy.
The classic design doc splits across what already exists: the decision goes
to an ADR, the requirements to a PRD, how-it-currently-works to a Spec,
mockups to Design, and **the investigation behind all of it to a RAD**.

So when the *technical* shape is still forming and is not PRD-ready, a RAD
is the honest home — not a new document type. Say so, and write the RAD.

If the request turns out to be about screens, components or accessibility,
stop: that is design work, not a RAD.

## The shape

```
Question → Trail → Findings → Recommendation
```

Use the project-docs `research.md` template. Four sections, and the second
is the one that earns its keep.

**Question** — what needed finding out, and what prompted it.

**Trail** — the actual working: options compared, sources consulted, what
was refuted and why, dead ends walked far enough to rule out. **This is the
load-bearing section.** A conclusion without its trail is an assertion; a
reader cannot tell whether the reasoning still holds when circumstances
change. Record disagreement, including where a proposal was rejected and
what replaced it.

**Findings** — what turned out to be true. Separate the measured from the
assumed, and say which is which. "Unverified" ages far better than false
confidence.

**Recommendation** — what to do about it, and what would change the answer.
If it is not yet a commitment, say so plainly and leave the open questions
standing.

## Working it

A RAD usually records a conversation rather than an experiment: propose,
refute, ask for alternatives, weigh, land. Write it **as the deliberation
happens or immediately after** — the reasoning is available for about an
hour and is reconstruction thereafter.

A RAD also covers the other route to a finding: a **proof of concept** -
code written in `poc/<name>/` to settle a question that argument could not.
The write-up is the same document. Say in the Trail what was built, where it
lives or that it has been deleted, and whether it still runs; put the
finding in Findings like any other. There is no separate document type for
this, because a proof of concept is a method of arriving at a finding, not a
different kind of finding.

**Anything established by experiment gets a `Measured against:` line** -
versions, platforms, the date. Measured findings are true against a moving
target and rot silently without it; a reader who cannot tell what a claim
was verified on has to redo the work or trust it blindly. Findings reached
by argument need no such line, and separating the measured from the assumed
inside Findings tells a reader which claims to re-check first.

1. **Find the section.** Do not assume. R&D Logs may be a top-level section
   or nested inside a Product Development pillar. Locate it by its README
   heading and match whatever numbering already exists.
2. **Number it, but keep the number out of the title.** The heading is the
   title alone — `# Signal Enrichment`, not `# RAD-0023: Signal
   Enrichment`. That heading becomes the article title, and a KB index
   full of prefixed titles is unreadable. The identifier goes on the
   metadata line beneath it:

       # Signal Enrichment

       RAD-0023 · 2026-08-05

   Check the project's existing prefixes before minting one — a repo may
   already use a similar-looking prefix for something else entirely.
3. **Author the file plain-named**, with no ID prefix and no id/status
   frontmatter. The sync assigns the article ID and renames the file.
   Pre-naming collides. See project-docs.
4. **Sync**, then update the section README index.

## Afterwards

- A recommendation that gets committed → an ADR, cross-linked both ways.
  The RAD keeps the trail; the ADR carries the decision.
- Work that falls out → tracker stories, not files.
- Requirements that fall out → a PRD.
- Revisit rather than rewrite. A RAD is a record of what was known then;
  if the answer changed, that is a new RAD or an ADR, not an edit.

## Optional by design

Not every project works this way, and nothing here forces it. A developer
who does not want a trail simply does not ask for one — there is no flag to
set and no setup step. The cost of the skill existing is a description an
agent reads; the cost of not having it is re-deriving the reasoning later.

For projects claiming SR&ED or similar R&D tax credits, the record needs to
be audit-grade: see [references/sred.md](references/sred.md) for the
additional sections and evidence standard.
