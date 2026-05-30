---
name: to-prd
description: Turn conversation context and codebase understanding into a formal PRD and matching AC spec. Use when user wants to formalize a plan or feature requirement.
---

# Product Requirements (to-prd)

This skill synthesizes current context into a functional specification consisting of a **Requirement (PRD)** and its corresponding **Verification (AC)**.

## Process

### 1. ID Acquisition
Before creating files, the agent MUST obtain a unique ID:
- **Remote**: If a sync target is configured, run the sync script (e.g., `gh.py`) in `create-skeleton` mode to get a real issue ID.
- **Local**: If no remote is available, use the next sequential ID from the `docs/prd/` directory.

### 2. Synthesis
Identify the major modules and opportunities to extract "Deep Modules" (isolatable, testable logic). Use the project's domain glossary and respect existing ADRs.

### 3. File Creation
Generate the following pair (unless the feature is purely informational, in which case omit the AC):

**Requirement: `docs/prd/[ID]-slug.md`**
- **Context**: Summarize "Why" this is being built (reference `docs/discovery/` if available).
- **User Stories**: A LONG, extensive list of "As an <actor>, I want <feature>, so that <benefit>".
- **Implementation Decisions**: Specific architectural choices, API contracts, and schema changes.
- **Verification**: Link to the matching AC record, which serves as the verification tier for these requirements.

**Verification: `docs/ac/[ID]-slug.md`**
- **Test Scenarios**: Gherkin-style (Given/When/Then) definitions for every story in the PRD, functioning as the verification criteria.
- **Story Link**: Reference the matching PRD record.

## PRD Template

```md
## Problem Statement
The problem from the user's perspective.

## User Stories
1. As an <actor>, I want <feature>, so that <benefit>

## Implementation Decisions
- Module boundaries and interfaces.
- Architectural choices (respecting ADRs).
- NO code snippets or file paths (unless encoding a specific protocol/state machine).

## Verification
- **AC Link**: [ID-slug.md]
```

## Review Checklist
- [ ] Are stories comprehensive?
- [ ] Is there a "Discovery Trail" in the context section?
- [ ] Are implementation decisions decoupled from specific files?
- [ ] Is the Verification pair initialized?
