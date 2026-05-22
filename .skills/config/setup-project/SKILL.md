---
name: setup-project
description: Onboard and configure a project workspace for tracking, sync, and agent interaction. Use when starting a new project or refreshing workspace configuration.
---

# Project Setup

## Objective
A comprehensive onboarding skill to configure a project workspace for issue tracking, synchronization, and agent interaction. This skill establishes the foundational configuration required by all other tracking skills.

## Core Mechanics
### 1. Shared Project Configuration (`.config/project.json`)
The agent must ensure that a `.config/project.json` file exists in the project workspace. This file is the **Primary Link** between the workspace and the agent's global state and MUST be tracked in version control.

*   **Immediate Capture:** If `.config/project.json` does not exist, explicitly ask the user for the configuration variables below. The moment a `project_id` is established, the agent MUST immediately write the `.config/project.json` file. The agent MUST NOT proceed with other configuration steps until this local file is written.
*   **Version Control**: The agent MUST verify the project's `.gitignore` and explicitly append `!.config/project.json` (if `.config/` or `*.json` is ignored) to ensure this file is tracked by Git so other agents know where to find things.
*   **Prompt Requirements:**
    *   `project_id`: A unique system identifier used for local config and secrets. (e.g., your Firebase Project ID like `coldwater-5e502`. **Default: The current directory name**).
        *   **Proactivity:** Tell the user what ID you are defaulting to and explain that they can change it later by editing `.config/project.json`.
    *   `sync_target`: Primary issue tracker (e.g., `youtrack`, `github`).
    *   `github_repo`: The repository URL (if target is GitHub).
    *   `github_project`: The GitHub Project V2 URL (optional, e.g., `https://github.com/orgs/ORG/projects/NUM`).
*   **Simplified Workflow (Opt-out):** For non-technical members (Designers), explicitly offer to disable automated tracking.
    *   `sync_enabled`: Set to `false` to stop prompts for GitHub tokens and board integration.
    *   **Action:** If disabled, the agent should not nag for board links or sync status.
*   **Optional Features:** Ask to enable **Compliance Tracking** (`active_regulations`) and **R&D Logging**.

### 2. Documentation Architecture
The agent must verify or initialize the hierarchy:
*   `docs/mandates/`: Technical and functional overviews (`ARCH.md`, `SPEC.md`).
*   `docs/prd/`: Feature stories and requirements.
*   `docs/ac/`: Testable acceptance criteria.
*   `docs/discovery/`: Due Diligence (DD) research and cost evaluations.
*   `docs/gap/`: Roadmap and functional gaps.
*   `docs/adr/`: Architectural Decision Records.
*   `docs/regulations/`: Regulatory compliance mappings.
*   `docs/research/`: Deep-dive RAD logs.
*   `docs/guides/`, `docs/reference/`, `docs/design/`.

### 3. Legacy Doc Discovery
After initialization, the agent MUST offer to organize existing documentation:
> *"I noticed existing documentation files scattered in your project. Would you like me to analyze and organize them into the new 6-sector hierarchy (Mandates, Stories, Discovery, etc.)?"*
*   **Action**: If yes, trigger the `migrate docs` workflow from the `manage-docs` skill.

### 4. Agent Mandates (`AGENTS.md`)
*   **Action:** Generate or audit `AGENTS.md` at the project root. Ensure the **"Update Docs Rule"** correctly references the `manage-docs` skill.
*   **Safety**: Remind the user that technical specs live in `/docs` (not Wikis) for versioning.

### 5. Local Agent Configuration (`~/.config/agents/<project_id>/`)
*   **Action:** Ensure the directory exists.
*   **Skill Sources**: Initialize `skill-sources.json` if missing, prompting for the master skill repository path (e.g., `~/Workspace/skills/src/skills/`).
*   **Constraint:** NEVER store machine-specific paths or personal state in the project workspace.

### 7. Configuration Audit & Health Check
To ensure the agent functions correctly as project standards evolve, the agent should proactively audit the configuration whenever it detects a mismatch or missing local state.

*   **Trigger**: Triggered by "Audit config" or when an agent detects missing keys/local files during other operations.
*   **Audit Points**:
    1.  **Shared Config**: Check `.config/project.json` for all mandatory keys (`project_id`, `sync_target`, etc.).
    2.  **Local State**: Verify `~/.config/agents/<project_id>/` contains `persona.json` and `skill-sources.json`.
*   **Human-Friendly Guidance**: When prompting a user (especially a non-technical one like a Designer), use clear, outcome-oriented language:
    - *Instead of:* "Update your sync_target in project.json."
    - *Use:* "I noticed your project isn't connected to the task board yet. Would you like me to help you link it so I can sync your design notes automatically?"
    - *Instead of:* "Initialize your local persona."
    - *Use:* "I don't know your role in this project yet! Are you working as a **Developer** or a **Designer**? This helps me use the right templates for your work."
    - *Instead of:* "Provide a master skill source path."
    - *Use:* "I can synchronize my capabilities with your global library if you have one. If you'd rather I just work with what's in this project and not ask again, let me know and I'll stay local."

## Discovery Trail
- **2026-05-19**: Refactored secret verification. Removed the general "Secrets" check from `setup-project` and delegated it to specialized skills (e.g., `sync`) to ensure agents only prompt for secrets when needed.
- **2026-05-18**: Added "Immediate Capture" and "Version Control" mandates to ensure `.config/project.json` is written to disk and un-ignored in `.gitignore` immediately upon establishing the `project_id`. This guarantees other agents can reliably locate global state.
- **2026-05-18**: Added "Simplified Workflow" and `sync_enabled` flag to help non-technical members opt out of automated tracking and credential prompts. Refined health check guidance to be more permissive.
- **2026-05-18**: Added "Local-Only" mode to skill management to prevent repetitive prompting for users without a master source. Refined guidance language to be even more permissive for non-technical personas.
- **2026-05-18**: Added Configuration Audit workflow with a focus on non-technical proactivity. Mandated clear, outcome-oriented language when guiding users through setup corrections.
- **2026-05-18**: Added proactivity requirement for `project_id` defaults. Clarified `project_id` description and added current directory name as default. Updated `github_project` and `github_repo` requirements.
