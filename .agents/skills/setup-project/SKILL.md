---
name: setup-project
description: Onboard and configure a project workspace for tracking, sync, and agent interaction. Use when starting a new project or refreshing workspace configuration.
---

# Project Setup

## Objective
A comprehensive onboarding skill to configure a project workspace for issue tracking, synchronization, and agent interaction. This skill establishes the foundational configuration required by all other tracking skills.

## Core Mechanics
### 0. Legacy Path Migration
Before initializing or auditing any project configuration, the agent MUST check if the legacy `.skills/` directory exists. If detected, the agent MUST immediately stop and trigger the **Legacy Path Detection & Migration** workflow from the `manage-skills` skill to migrate the project to the standard `.agents/skills/` directory defined by the [Agent Skills Standard](https://agentskills.io/).

### 1. Shared Project Configuration (`.agents/config/project.json`)
The agent must ensure that a `.agents/config/project.json` file exists in the project workspace (falling back to legacy `.config/project.json` if present). This file is the **Primary Link** between the workspace and the agent's global state and MUST be tracked in version control.

*   **Immediate Capture:** If `.agents/config/project.json` (and legacy `.config/project.json`) does not exist, explicitly ask the user for the configuration variables below. The moment a `project_id` is established, the agent MUST immediately write the `.agents/config/project.json` file. The agent MUST NOT proceed with other configuration steps until this local file is written.
    *   **Interactive Option:** If the client supports the `ask_question` tool, use it to present multiple-choice options for selecting the `sync_target`, enabling/disabling sync, and enabling optional modules. Otherwise, present the questions in the chat and wait for a response.
*   **Version Control**: The agent MUST verify the project's `.gitignore` and explicitly append `!.agents/config/project.json` (if `.agents/` or `*.json` is ignored) to ensure this file is tracked by Git so other agents know where to find things.
*   **Prompt Requirements:**
    *   `project_id`: A unique system identifier. The agent MUST ask the user for this ID and explicitly explain how it will be used (mapping local state, workspace caches, and credentials under `~/.config/agents/<project_id>` and `~/.secrets/agents/<project_id>`).
        *   **Defaulting & Proactivity:** Default the ID to the project's root directory name. Explicitly tell the user this default is being used and explain they can easily modify it later by editing the config file.
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

### 4. Agent Mandates (`AGENTS.md` and `CLAUDE.md`)
*   **Audit AGENTS.md**: Audit `AGENTS.md` at the project root.
    - **If missing**: Create it from `AGENTS_TEMPLATE.md`.
    - **If present**: Ensure it includes the **Local & Library Skill Discovery** rule.
    - **Conflict Resolution**: If the file already exists, you MUST NOT overwrite it. Instead, parse the file:
        1. If the `Local & Library Skill Discovery` rule is present but outdated (e.g. referencing old `.skills/` or `.agents/skills/` library workspace rules rather than `META-INF/ai-skills/` or `.ai-skills/`), replace that specific section with the new standard version.
        2. If the section is missing entirely, append it under the `AI Interaction Guidelines` section (creating the section header if missing).
        3. Do not modify or discard any custom project conventions, architecture overviews, or directories defined by developers.
*   **Redirect CLAUDE.md**: Generate a root `CLAUDE.md` file that explicitly redirects Claude Code to read `AGENTS.md` (e.g., *"Before taking action, you MUST read the instructions in [AGENTS.md](AGENTS.md) and adhere to the project's local skill workflows."*).
*   **Recommend GEMINI.md**: Print a recommendation for Gemini users to append the global skill discovery rules to their `~/.gemini/GEMINI.md` user_global configuration file.
*   **Safety**: Remind the user that technical specs live in `/docs` (not Wikis) for versioning.

### 5. Local Agent Configuration (`~/.config/agents/<project_id>/`)
*   **Action:** Ensure the directory exists.
*   **Skill Sources**: Initialize `skill-sources.json` if missing, prompting for the master skill repository path (e.g., `~/Workspace/skills/src/skills/`).
*   **Constraint:** NEVER store machine-specific paths or personal state in the project workspace.

### 7. Configuration Audit & Health Check
To ensure the agent functions correctly as project standards evolve, the agent should proactively audit the configuration whenever it detects a mismatch or missing local state.

*   **Trigger**: Triggered by "Audit config" or when an agent detects missing keys/local files during other operations.
*   **Audit Points**:
    1.  **Legacy Folder Setup**: Check if the legacy `.skills/` directory exists. If detected, immediately halt the audit and trigger migration.
    2.  **Shared Config**: Check `.agents/config/project.json` (or legacy `.config/project.json`) for all mandatory keys (`project_id`, `sync_target`, etc.).
    3.  **Local State**: Verify `~/.config/agents/<project_id>/` contains `persona.json` and `skill-sources.json`.
*   **Human-Friendly Guidance**: When prompting a user (especially a non-technical one like a Designer), use clear, outcome-oriented language:
    - *Instead of:* "Update your sync_target in project.json."
    - *Use:* "I noticed your project isn't connected to the task board yet. Would you like me to help you link it so I can sync your design notes automatically?"
    - *Instead of:* "Initialize your local persona."
    - *Use:* "I don't know your role in this project yet! Are you working as a **Developer** or a **Designer**? This helps me use the right templates for your work."
    - *Instead of:* "Provide a master skill source path."
    - *Use:* "I can synchronize my capabilities with your global library if you have one. If you'd rather I just work with what's in this project and not ask again, let me know and I'll stay local."

## Discovery Trail
- **2026-05-19**: Refactored secret verification. Removed the general "Secrets" check from `setup-project` and delegated it to specialized skills (e.g., `sync`) to ensure agents only prompt for secrets when needed.
- **2026-05-18**: Added "Immediate Capture" and "Version Control" mandates to ensure `.agents/config/project.json` is written to disk and un-ignored in `.gitignore` immediately upon establishing the `project_id`. This guarantees other agents can reliably locate global state.
- **2026-05-18**: Added "Simplified Workflow" and `sync_enabled` flag to help non-technical members opt out of automated tracking and credential prompts. Refined health check guidance to be more permissive.
- **2026-05-18**: Added "Local-Only" mode to skill management to prevent repetitive prompting for users without a master source. Refined guidance language to be even more permissive for non-technical personas.
- **2026-05-18**: Added Configuration Audit workflow with a focus on non-technical proactivity. Mandated clear, outcome-oriented language when guiding users through setup corrections.
- **2026-05-18**: Added proactivity requirement for `project_id` defaults. Clarified `project_id` description and added current directory name as default. Updated `github_project` and `github_repo` requirements.
