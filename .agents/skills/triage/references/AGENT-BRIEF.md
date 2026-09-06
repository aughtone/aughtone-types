# Writing Agent Briefs

An agent brief is a structured comment posted on a tracker issue when it
moves to `ready-for-agent`. Together with the issue's `## Acceptance
Criteria` section, it is the authoritative specification an AFK agent works
from. The original issue body and discussion are context — the brief is the
contract.

**Split of responsibilities:** the acceptance criteria go into the ISSUE
DESCRIPTION as a canonical `## Acceptance Criteria` task list (so the
story-workflow skill can parse, toggle, and gate on them); the brief
comment carries everything else — behavior, interfaces, scope boundaries.

## Principles

### Durability over precision

The issue may sit in `ready-for-agent` for weeks; the codebase will change.
Write the brief to stay useful as files are renamed, moved, refactored.

- **Do** describe interfaces, types, and behavioral contracts
- **Do** name specific types, function signatures, or config shapes to look for
- **Don't** reference file paths — they go stale
- **Don't** reference line numbers
- **Don't** assume the current implementation structure will remain

### Behavioral, not procedural

Describe **what** the system should do, not **how** to implement it. The
agent explores the codebase fresh and makes its own implementation calls.

- **Good:** "The `SkillConfig` type should accept an optional `schedule` field of type `CronExpression`"
- **Bad:** "Open src/types/skill.ts and add a schedule field on line 42"

### Complete acceptance criteria

The agent needs to know when it's done. Every criterion must be concrete
and independently verifiable — they live in the issue description's
`## Acceptance Criteria` checklist.

- **Good:** "Querying `tag: needs-triage` returns issues that have been through initial classification"
- **Bad:** "Triage should work correctly"

### Explicit scope boundaries

State what is out of scope. This prevents gold-plating and assumptions
about adjacent features — and gives the story-workflow scope guard
something concrete to enforce.

## Template (brief comment)

```markdown
## Agent Brief

**Category:** bug / enhancement
**Summary:** one-line description of what needs to happen

**Current behavior:**
What happens now. For bugs, the broken behavior; for enhancements, the
status quo the feature builds on.

**Desired behavior:**
What should happen after the work is complete. Be specific about edge
cases and error conditions.

**Key interfaces:**
- `TypeName` — what needs to change and why
- `functionName()` — what it returns now vs what it should return
- Config shape — any new options needed

**Acceptance criteria:** in the issue description's `## Acceptance
Criteria` section (added during triage).

**Out of scope:**
- Thing that should NOT be changed in this issue
- Adjacent feature that might seem related but is separate
```

## Example (bug)

```markdown
## Agent Brief

**Category:** bug
**Summary:** Skill description truncation drops mid-word, producing broken output

**Current behavior:**
Descriptions over 1024 characters are truncated at exactly 1024 characters
regardless of word boundaries, ending mid-word ("…wants to confi").

**Desired behavior:**
Truncation breaks at the last word boundary before 1024 characters and
appends "..." to indicate truncation.

**Key interfaces:**
- The `SkillMetadata` type's `description` field — no type change; the
  validation/processing that populates it must respect word boundaries
- Any function that reads SKILL.md frontmatter and extracts the description

**Acceptance criteria:** in the issue description (4 items).

**Out of scope:**
- Changing the 1024-char limit itself
- Multi-line description support
```

## Anti-example

"The triage thing is broken. Look at the main file and fix it. The function
around line 150 has the issue." — no category, vague, stale file/line
references, no criteria, no scope boundaries, no current-vs-desired.
