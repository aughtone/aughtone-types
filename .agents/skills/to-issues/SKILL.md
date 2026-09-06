---
name: to-issues
description: Break a plan, spec, or PRD into independently-grabbable tracker stories using tracer-bullet vertical slices. Use when the user wants to convert a plan into issues, create implementation tickets, or break down work into stories.
license: MIT
compatibility: Requires a connection to the project's issue tracker (see the tracker binding; YouTrack today).
metadata:
  author: bpappin
  version: "1.16"
---

# To Issues

Break a plan into independently-grabbable stories using vertical slices
(tracer bullets), published to the project's tracker in the canonical story
format so the story-workflow skill can pick each one up directly.

**Tracker dispatch:** read `.agents/config/story-tools.json` →
`tracker.type` (absent → `youtrack`) and use that binding's commands:

- `youtrack` → [references/tracker-youtrack.md](references/tracker-youtrack.md)
- `github` → [references/tracker-github.md](references/tracker-github.md)

Story bodies follow [references/ac-format.md](references/ac-format.md) —
the same contract every story-tools skill parses.

## Process

### 1. Gather context

Work from whatever is already in the conversation. If the user passes an
issue reference (ID, URL, or a PRD path), fetch/read it fully — body,
comments, requirements.

### 2. Explore the codebase (optional)

If you haven't already, explore enough to use the project's domain glossary
in titles and descriptions, and to respect ADRs in the area you're touching.

### 3. Draft vertical slices

Break the plan into **tracer bullet** stories. Each is a thin vertical
slice that cuts through ALL integration layers end-to-end, NOT a horizontal
slice of one layer.

Slices may be **HITL** (require human interaction — an architectural
decision, a design review) or **AFK** (implementable and mergeable without
human interaction). Prefer AFK where possible.

- Each slice delivers a narrow but COMPLETE path through every layer
  (schema, API, UI, tests)
- A completed slice is demoable or verifiable on its own
- Prefer many thin slices over few thick ones

### 4. Quiz the user

Present the breakdown as a numbered list. For each slice: **Title**,
**Type** (HITL/AFK), **Blocked by** (which slices must complete first),
**Requirements covered** (R-numbers / user stories from the source PRD),
**Priority** (proposed - default Normal; the user owns the final call),
**Subsystem** (which component of the monorepo the slice lands in - a
vertical slice may touch several; name the one that owns it; fetch the
project's available values via the binding's dimensions tool and show
them - propose your inference, never invent near-duplicates), and
**Estimate** (a rough one: 1h / 4h / 1d - calibration data, not a promise).
If the work targets a release, confirm it once for the batch - release
membership is the **Fix versions** field, never a tag.

Ask: does the granularity feel right? Are the dependencies correct? Should
any slices merge or split? Are HITL/AFK assignments right? Do the
priorities and estimates look sane? Iterate until approved.

### 5. Publish to the tracker

**Never write a dimension value you have not read.** Before creating or
updating any issue, read the project's dimensions - the binding's dimensions
tool, or `.agents/config/dimensions.md`. If you have not read them *in this session*,
read them now. A value you invented is not a new value, it is a
near-duplicate: the issue drops out of every filter, board and saved search
the real value feeds, and nobody notices until someone wonders where the
work went.

**Topical tags come from the existing set.** Freeform is not permission. Read the project's list before you propose anything - the binding says where it lives, and warns that its dimensions tool may not return tags at all, so an empty or short list from that tool is not evidence the set is small.

**Two different operations, and only one of them can refuse you.** Keeping them apart matters, because the failure you are warned about belongs to one and the procedure you are given belongs to the other.

*Adding a value to the vocabulary* is the binding's two commands: append a line to the agents' list, then run the puller with `--push-tags`. That call creates by design - the label-creation endpoint on GitHub, the field's value set on YouTrack - so nothing refuses you on this path, and a failure here is a real fault worth reporting rather than a value that does not exist. Proposing a genuinely new tag is still a human decision: name it, say why nothing existing fits, and get confirmation. **Writing it is then yours to do, not theirs.**

*Applying a value to an issue* is the other path, and it is the one that meets a value the tracker does not know. If it rejects, that is the value not existing - not a permission you lack, and not a reason to hand the job back. Add it by the path above, then apply it.

**Never apply a value that is not already on the project's list**, whatever the tracker appears to let you do. Some refuse. Others create it silently, which is worse: you get a near-duplicate outside the reviewed list, no error, and nothing to tell you it happened. Never create one in passing, and never leave a dimension unset silently: if you cannot infer a value, say so and ask.

For each approved slice, create a story via the tracker binding, in
dependency order (blockers first) so you can reference real IDs. Each story
body, in canonical format:

```markdown
## Purpose
<why this slice is necessary and what problem it solves - THIS slice's
why, not the feature's. Link the PRD rather than restating it.>

## Specification
<required behaviour in detail: the contract, edge cases, error and empty
states, boundaries. What must be true, not which files to touch. No file
paths or implementation plans, except a prototype-derived snippet that
encodes a decision (state machine, schema, type shape) - trimmed to the
decision-rich parts. "None beyond the AC." for a genuinely trivial slice.>

## Acceptance Criteria
- [ ] Verifiable outcome 1
- [ ] Verifiable outcome 2

## References
- <KB article ID + title of the source PRD, e.g. PROJ-A-41 Some Feature>
- <parent issue ID, if the source was an issue>
```

Write both sections as prose, not bullet fragments. The story is read cold
by whoever picks it up; a checklist alone makes them reconstruct the intent.
Purpose is required on every story. Specification is required wherever
anything is non-obvious, which is most of them.

Then, via the binding: link blockers (depends-on), set each story's
Priority, Subsystem, and Estimation as approved, tag every story `triaged`
(the quiz WAS its triage - these stories are born dispositioned, and
`tag: -triaged` must stay the reliable untriaged-work query), tag AFK
slices `ready-for-agent`, tag
`needs-gherkin` where a slice has strict rules or planned test automation,
and apply one shared **topical tag** to the whole batch - Title Case,
human-readable, usually the feature or PRD name ("Trust Insights") - so the
slices group together in searches and board swimlanes. Reuse the project's
existing topical tags where one fits; never invent near-duplicates. If a
target release was confirmed, set Fix versions on each story.

### 6. Close the loop

If the source was a PRD, update its `## Stories` table with the new IDs
and the requirements each covers. Do NOT close or modify any parent issue.
