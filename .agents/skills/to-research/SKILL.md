---
name: to-research
description: Manage Technical Research & Development (R&D) logs. Use when starting new research, documenting hypotheses, or logging technical discovery findings.
---

# Skill: Technical Research & Development (R&D)

## Objective
Standardize the process of documenting technical challenges, hypotheses, and architectural decisions to ensure transparency, support SR&ED tax credits, and provide a clear audit trail for complex engineering choices.

## Activation Guard
Before executing any workflow, the agent MUST verify that `"rad_enabled": true` is set in the `.agents/config/project.json` file (falling back to legacy `.config/project.json` if needed) in the workspace root. If it is false or missing, the agent must inform the user that R&D logging is disabled for this project and only proceed if explicitly instructed.

## Core Workflows

### 1. Start New Research
When a complex technical challenge is identified or a design pivot is considered:
1. Create a new file: `docs/research/RAD-[ID]-[slug].md`.
2. Use the [rad-template.md](references/rad-template.md) for the structure.
3. Assign the next incremental ID (resolve ID sequence from `docs/research/README.md`).
4. Document the **Technical Challenge**, **Constraints**, and **Hypothesis**.
5. Update the `docs/research/README.md` index.

### 2. Log Decision & Outcome
When research reaches a conclusion or a pivot is chosen:
1. Update the corresponding `RAD-[ID].md` file.
2. Fill in the **Decision** and **Outcome** sections.
3. Ensure **Validation** points are dated and verifiable.
4. If a decision leads to a permanent architecture change, reference the corresponding ADR.

### 3. Review Research Path
When asked to "Review the reasoning for [Feature]":
1. Search `docs/research/` for the relevant RAD entries.
2. Summarize the **Assumptions** and **Reasoning** that led to the current state.
3. Identify any **GAPs** from `docs/gap/README.md` that were originally identified as research constraints.

## Documentation Architecture
* **`docs/research/README.md`**: The master index of all research activities.
* **`docs/research/RAD-[ID]-[slug].md`**: Deep-dive technical research and decision records.

## Reference Material
- [RAD Template](references/rad-template.md)
- [Master Research Log](../../../docs/RAD.md) (Legacy)
