# Story Format Contract

This is the exact format `app/ac-parser.js` parses. Every story_* tool and
every human editing a story description relies on it.

```markdown
## Purpose
Why this slice is necessary and what problem it solves.

## Specification
Required behaviour in detail - the contract, the edge cases, the error
states, the boundaries.

## Acceptance Criteria
- [ ] Verifiable outcome one
- [x] Verifiable outcome two (done)

## References
- PROJ-A-41 Some Feature (KB article; mirror file docs/knowledge/**/PROJ-A-41_*.md)
- PROJ-A-17 ADR-0007 Conflict Strategy

## QA
Feature: <name>
  Scenario: <name>
    Given ...
    When ...
    Then ...
```

Rules:

- Section headings are level-2 (`##`), matched case-insensitively; a section
  runs until the next `##` heading or end of description.
- AC items are markdown task-list entries (`- [ ]` / `- [x]`, `*` also
  accepted). YouTrack renders them as clickable checkboxes, so humans and
  tools share one surface.
- Item identity for updates is index + text prefix. Tools refuse a toggle
  when the prefix doesn't match the item at that index (drift guard against
  concurrent human edits).
- `## References` is optional: one path/link per line, ADRs and PRDs by repo
  path so any agent can open them.
- `## QA` is optional Gherkin. Required at completion only when the story is
  tagged `needs-gherkin`.
- `## Open Questions` is optional - what the source raised and nobody has
  answered yet. Used by story-reconcile for narrative-only stories.
- Everything outside these sections is untouched by the tools.

## Purpose and Specification

Prose, not bullet fragments. A story is read cold by whoever picks it up -
a new developer, a fresh agent, you in three months - and the two sections
exist so that reader does not have to reconstruct intent from a checklist.

**`## Purpose` is required on every story.** One or two paragraphs: why the
work is necessary and what problem it solves. Scope it to THIS slice - what
breaks, or stays broken, without it. The feature-level why belongs in the
PRD; link it in `## References` rather than restating it, or the story
becomes a copy that drifts.

**`## Specification` is required wherever anything is non-obvious**, which
is most stories. It is behaviour at high resolution: the contract, the edge
cases, the error and empty states, the boundaries, what to do when an input
is absent or malformed. `None beyond the AC.` is a legitimate body for a
genuinely trivial slice - write that rather than padding.

Specification says **what must be true, not which files to touch.** No file
paths, no implementation plans, no layer-by-layer instructions - those date
faster than the story and take the approach away from whoever picks it up.
The one exception is unchanged: a prototype-derived snippet that *encodes a
decision* (a state machine, a schema, a type shape), trimmed to the
decision-rich parts.

**Specification is prose; AC is the checklist.** The tools parse and toggle
AC, and AC is the completion gate - so Specification must never become a
second checklist competing for that role. Every AC item should trace back to
something the Specification states, but the Specification carries the detail
that would make a checkbox unreadable.

Backward compatible: older stories open with an unnamed narrative paragraph
instead. Read it as Purpose. Nothing needs rewriting to stay valid - add the
sections when you next touch the story.
