# Agent Capabilities Index

What each skill in `.agents/skills/` is for, in one line. The full trigger
conditions live in each skill's own `SKILL.md` frontmatter — this file is the
map, not the manual.

Skills marked **(managed)** are installed copies owned by the story-tools
suite; the installer overwrites them on every refresh, so improving one is
discovered work, not an edit. See [MANAGED.md](MANAGED.md).

## Doing the work

**story-workflow** (managed): Work one tracker story with strict scope discipline — the acceptance-criteria checklist IS the scope, and discovered work becomes a new linked issue rather than growing the current one.

**triage** (managed): Move issues through the triage state machine — capture cheaply, reproduce, grill, prioritize, disposition. Also the right skill for "record this for later".

**to-issues** (managed): Break a plan, spec or PRD into independently-grabbable stories as tracer-bullet vertical slices.

**story-reconcile** (managed): Adopt the story workflow in a project that has drifted — reconcile GAP files, embedded AC and offline worklogs against the tracker, and pull the local issue snapshot.

**tdd** (managed): Test-driven development, red-green-refactor.

**worklog** (managed): Record working time in a personal cross-project ledger. Experimental.

## Writing it down

**project-docs** (managed): Decide where a document belongs and keep `docs/knowledge/` in sync with the tracker's knowledge base. Owns filing and sections, not authoring.

**to-adr** (managed): Record a decision — the forces, the choice, and what was rejected and why.

**to-prd** (managed): Turn a plan or discussion into a PRD, with verification living on the tracker stories.

**to-rad** (managed): Log an investigation — options weighed, approaches that failed, what a proof of concept proved. Also the answer when someone asks for a "detailed design document".

**to-ux** (managed): Design screens, components and flows, and record what was designed and why. Accessibility included, not bolted on.

**to-wiring** (managed): Audit and maintain the feature wiring rules in `WIRING.md`.

**regulatory-compliance** (managed): Track regulatory requirements (PIPEDA, GDPR) and audit ADRs and code against them.

## Thinking it through

**grill-with-docs** (managed): Get interviewed relentlessly about a plan until every branch is resolved, challenged against the domain glossary, with decisions recorded as they crystallise.

**improve-codebase-architecture** (managed): Find deepening opportunities, informed by the domain glossary and the recorded decisions.

**zoom-out** (managed): Step back and give the higher-level perspective. Only ever invoked explicitly.

**prototype**: Build a throwaway prototype to flesh out a design before committing to it.

## Session hygiene

**handoff** (managed): Compact the conversation into a handoff document for the next agent or session.

**housekeeping** (managed): End-of-session cleanup audit and commit preparation.

**caveman**: Ultra-compressed output mode — drops filler and pleasantries, keeps technical accuracy.

## Guardrails

**git-guardrails-claude-code**: Claude Code hooks that block destructive git commands before they execute.

**git-guardrails-gemini**: The same git safety rules, as instructions for Gemini CLI.

## Building skills

**write-a-skill**: Create a new skill with proper structure, progressive disclosure and bundled resources.
