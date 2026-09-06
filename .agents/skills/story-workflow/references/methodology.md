# Story Workflow Methodology

YouTrack is the single source of truth for task-level work. Local docs record
*decisions* (ADRs) and *requirements* (PRDs); stories and their acceptance
criteria live only in YouTrack. The old GAP files and PRD-embedded AC are
retired — see ADR-0001.

## Document responsibilities

| Artifact | Lives | Contains |
|---|---|---|
| ADR | KB "Architecture Decision Records" (mirror: `docs/knowledge/`) | One decision, its context and consequences |
| PRD | KB "Product Requirements" (mirror: `docs/knowledge/`) | Requirements narrative + links to story IDs (no AC) |
| Story | YouTrack issue | Narrative, `## Acceptance Criteria`, optional `## References`, optional `## QA` |
| Discovered work | YouTrack issue, linked `discovered from` | Anything that surfaced mid-story |

## The story lifecycle

1. **Focus** — `story_set_focus(issueId)` pins the story for this session.
   One focused story per person at a time.
2. **Brief** — `story_get_story_context()` returns scope: the AC list *is*
   the scope. References point at the ADR/PRD background reading.
3. **Work** — implement toward unchecked AC items only.
4. **Record** — `story_update_ac(index, textPrefix, done)` as items are
   verifiably completed. Never batch-check at the end.
5. **Off-ramp** (the scope guard) — anything discovered that does not serve
   an unchecked AC item (bug, refactor, idea, missing feature) goes through
   `story_add_discovered_work(summary)`: a new linked issue, triaged later.
   Then return to the focused story. This is the default reflex; expanding
   the current story via `story_add_ac` is the exception and requires the
   user's explicit agreement.
6. **Complete** — `story_complete_story()` validates: all AC checked, QA
   section present if the story is tagged `needs-gherkin`. Only then move
   the state with the standard `update_issue` tool.

## Why the off-ramp matters

Scope creep in agent sessions is a ratchet: each "while we're here" addition
looks small and reasonable, and the sum destroys focus. Making discovered
work *one cheap tool call* removes the friction that tempts inline fixes —
the work is captured, provenance is linked, nothing is lost, and the story
finishes.

## Where BDD/Gherkin fits

Story + AC checklist is the canonical, human-first format. Gherkin is added
selectively as a `## QA` section on the story:

- Tag a story `needs-gherkin` when it has strict rules or is a candidate
  for test automation; `story_complete_story` will then require the QA
  section before the story can close.
- Otherwise, a QA section may be added at completion time to capture the
  verified behavior as scenarios.

## Writing good AC

Each item is a verifiable outcome, not an activity: "Queue drains
automatically on reconnect", not "Work on reconnect logic". If an item
cannot be checked true/false, rewrite it. 3–7 items is the sweet spot;
more usually means the story should split.
