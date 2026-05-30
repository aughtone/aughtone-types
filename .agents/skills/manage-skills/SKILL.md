---
name: manage-skills
description: Manage the lifecycle, organization, and synchronization of AI agent skills between local project workspaces and master skill sources. Use when user wants to refresh skills, publish changes to the master source, or organize the .agents/skills directory.
---

# Skill Management

## Objective
Provide a standardized, interactive workflow for maintaining a consistent library of AI agent skills across multiple projects and centralized master sources.

## Core Hierarchy
1.  **Project Skills (.agents/skills/)**: Local workspace skills, often refined or customized for a specific project.
2.  **Master Sources**: Remote or local authoritative repositories for shared skills. Paths are configured per-machine in ~/.config/agents/<project_id>/skill-sources.json.

## Workflows

### 1. Configuration & Discovery
Before any operation, the agent must verify the project is initialized (`.agents/config/project.json` or legacy `.config/project.json`).
- **Dependency**: If no project configuration file is found in either path, the agent MUST trigger the **setup-project** initialization workflow before attempting to resolve master sources or global state.
- **Load Sources**: Read ~/.config/agents/<project_id>/skill-sources.json. 
- **Prompt**: If sources are missing, ask the user for the primary master skill repository path (e.g., ~/Workspace/skills/src/skills/).
- **Local-Only Mode**: If the user (especially a non-technical persona) does not have a master source or doesn't want to manage one, the agent must offer to "Stay Local".
    - Record this preference in ~/.config/agents/<project_id>/skill-sources.json as an empty array [].
    - Once recorded, the agent MUST NOT prompt the user again for a master source unless explicitly commanded to "Configure skill sources".

### 2. Downstream Sync (Refresh/Import: Source ➔ Project)
Triggered by commands like "Refresh skills", "Import skills", or "Fetch skills". This workflow brings shared improvements into the current workspace.
1.  **Legacy Path Detection & Migration**: 
    - Check if the legacy `.skills/` directory exists in the workspace.
    - If detected, the agent **MUST** explain the situation: the central library is migrating from the old `.skills/` path to the standard `.agents/skills/` path defined by the [Agent Skills Standard](https://agentskills.io/).
    - **Self-Deletion Safety Override**: The agent **MUST NOT** skip running the migration script out of fear of deleting its active instruction directory (`.skills/`). The script safely copies all instructions to `.agents/skills/` first, and the agent's prompt context will remain intact. Deleting the legacy `.skills/` directory is required to complete the migration.
    - **Script Path Resolution**: The agent **MUST** locate the migration script. It will be located locally at `.skills/manage-skills/scripts/migrate.py`, or in the master directory at `<master-dir>/manage-skills/scripts/migrate.py` or `<master-dir>/src/skills/manage-skills/scripts/migrate.py`.
    - **Automation Tool Execution (Dry-Run)**: The agent **MUST** run the dry-run command first to detect customizations:
      `python3 <resolved-script-path> --project-root . --master-dir <master-dir> --dry-run`
      *   The agent **MUST NOT** skip this step or attempt to copy/merge files manually.
    - **Customization Detection & Interactive Prompts**: If new or modified skills are detected, list them explicitly: *"I detected local customizations in your old skills: [list of skills]."*
        *   For each customized/modified skill, the agent **MUST**:
            1.  **Summarize Changes**: Present a concise, high-level summary of the differences (e.g. "You added custom YouTrack integration rules at the bottom of the file" or "You updated the default project parameters").
            2.  **Explain Consequences**: Cleanly and simply describe the consequences of each choice:
                -   **Extend existing skill** (Recommended): You get the latest updates from the master library, and your local additions are safely merged and preserved in a `## Local Extensions` section under a new name (default: `custom-[original-name]`).
                -   **Rename as independent skill**: Your legacy customized version is kept exactly as-is under a new name (default: `custom-[original-name]`). It will not receive future master library updates, keeping your version fully isolated.
                -   **Overwrite**: Replaces your legacy version entirely with the latest master version under the original name. **Warning**: Your local changes will be lost.
                -   **Keep as-is**: Preserves your customized version under its original name, but ignores the latest master library updates.
            3.  **Prompt for Choice & Name**: Ask the user what action they want to take, and what the new name should be (defaulting to `custom-[original-name]` for `Extend` and `Rename`). If the client supports the `ask_question` tool, use a write-in choice to capture a custom name.
    - **Backup Prompt**: Ask the user if they want to back up their old `.skills/` directory before proceeding.
        *   **Interactive Option:** If the client supports the `ask_question` tool, present a single-select question modal asking the user: *"Do you want to back up your old skills and migrate now?"* with choices:
            *   `Back up to skills-backup/ and migrate now`
            *   `Migrate now without backup`
            *   `Skip for now (ask me next time)`
        *   **Fallback:** Otherwise, present the questions in the chat and wait for a response.
    - **Execution Guardrails (If migrating)**:
        *   **Conflict Halt Rule**: If the dry-run command detects any modified or custom skills (conflicts), the agent **MUST NOT** proceed to execute the migration command or delete `.skills/` until it has presented the report, explained the consequences, and obtained the user's decision for each conflicted skill.
        *   **Skip/No Decline Rule**: If the user selects `Skip for now` or declines the migration, the agent **MUST NOT** run the migration script and **MUST NOT** delete the legacy `.skills/` directory.
        *   **Decision Construction**: Construct the decisions JSON string (e.g. `'{"manage-persona": {"action": "extend", "new_name": "extended-persona"}}'`).
        *   **Explicit Deletion Warning**: Before executing the command, the agent **MUST** explicitly warn the user and explain that it is about to delete the legacy `.skills/` folder. The agent **MUST** explain *why* (to prevent duplicate loading conflicts and config errors since all skills have been safely moved to `.agents/skills/` and/or backed up to `skills-backup/`).
        *   The agent **MUST** run the migration command to execute the changes:
            `python3 <resolved-script-path> --project-root . --master-dir <master-dir> --migrate --decisions '<JSON_STRING>'`
            adding `--backup` if backup was selected.
        *   The agent **MUST NOT** perform manual copies. Let the script automatically execute the backup, migrate the skills based on the user's specific decisions, and clean up the old `.skills/` directory.
        *   Trigger a configuration audit to update settings (e.g., `AGENTS.md` scanning rules).
    - **Action on No/Skip**: Proceed with the sync using `.agents/skills/` (if desired by the user), but make sure to prompt the user about migration again the next time any skill operations are run. The legacy `.skills/` directory **MUST** be left completely intact.
2.  **Detection**: Compare local .agents/skills/ with the registered master sources.
2.  **Reporting**: Present a categorized report:
    - **New**: Skills available in sources but missing locally.
    - **Updates**: Local skills that are identical to an older master version but have a newer version available.
    - **Divergent**: Local skills that have been modified and differ from the latest master version.
3.  **Conflict Resolution**: 
    - For **New** and **Update** items: Ask for permission to install/overwrite.
    - For **Divergent** items: **STOP** and display a diff. 
        *   **Interactive Option:** If the client supports the `ask_question` tool, present a single-select question modal with choices: `Overwrite local changes (use latest master)`, `Keep local changes (ignore master updates)`, or `Publish local changes to master instead`.
        *   **Fallback:** Otherwise, ask the user in chat: *"The local version has diverged. Do you want to overwrite your local changes, keep your version, or publish your changes to the master source instead?"*
4.  **Execute**: Copy the entire directory (scripts/resources included) only after user confirmation.
5.  **Post-Refresh Audit**: After a major refresh, the agent should offer to run a **Configuration Audit** (from the setup-project skill) to ensure the project settings are compatible with the latest skill versions.

### 3. Upstream Sync (Publish/Export: Project ➔ Source)
Triggered ONLY by an explicit command (e.g., "Publish skill [name]", "Export skill [name]", or "Save skill [name] to master").
1.  **Select Target**: If multiple sources exist, ask which one should receive the update.
    *   **Interactive Option:** If the client supports the `ask_question` tool and multiple master sources exist, use it to present a single-select question modal for selecting the target master repository. Otherwise, ask in the chat.
2.  **Validate**: Ensure the skill follows the [write-a-skill](../write-a-skill/SKILL.md) mandate.
3.  **Preserve Discovery**: Verify that the SKILL.md includes a **Discovery Trail** section capturing the original design logic and AI discussion history.
4.  **Execute**: Copy the local project skill to the selected master repository.

### 4. Global Skill Indexing (The "Capabilities Audit")
To help users understand the full extent of their agent's powers, the agent must maintain a comprehensive index of all installed skills.

*   **Source of Truth**: The capabilities index is strictly located at .agents/skills/INDEX.md.
*   **Trigger**: Triggered by "Index skills", "Show capabilities", or "Help".
*   **Workflow**:
    1.  **Scan & Sync**: Recursively search .agents/skills/ for all SKILL.md files.
    2.  **Generate Index**: Read the YAML front matter of each file.
    3.  **Layout**: Organize using an alphabetical list-based format with a blank line between each item:
    - **[Skill Name]**: [Human-friendly description of what it does for the user].
*   **Labeling**: Use the skill's internal name from metadata as the label. **NEVER use links, "SKILL.md", or file paths as labels.** The index must be portable and readable across all environments.
*   **Output**: Display the contents of .agents/skills/INDEX.md directly.
*   **Maintenance**: Whenever a skill is **Published** or **Refreshed**, the agent MUST offer to "Update the index" to ensure .agents/skills/INDEX.md remains accurate.

### 5. Skill Drill-down Help
When a user asks for help with a specific skill (e.g., "Help manage-docs"), the agent must provide a detailed breakdown of that skill's specific commands and triggers.

*   **Trigger**: Triggered by "Help [skill-name]" or "What can [skill-name] do?".
*   **Workflow**:
    1.  **Locate**: Find the SKILL.md file matching the requested name.
    2.  **Summarize Triggers**: Extract the "Use when..." section from the YAML description.
    3.  **Summarize Workflows**: List the headings under the ## Workflows section.
    4.  **Present**: Deliver a concise summary of exactly what the agent understands for that skill.
        - *Example for manage-docs:* "I understand commands like **'update docs'** or **'migrate docs'**. I can also help with **'Bootstrapping DESIGN.md'** and **'Polymorphic Layouts'**."

### 6. Help & Guidance (The "Manual")
The agent must provide clear explanations of its capabilities when explicitly asked or when user confusion is detected.

*   **Trigger**: Triggered by "Help" or "What can you do?".
*   **Core Output**:
    *   **Interactive Option:** If the client supports the `ask_question` tool, present a single-select question modal showing a menu of quick actions: `Index Skills (Show Capabilities)`, `Refresh / Import from global library`, `Publish / Export local skill`, `Audit Config (Run Health Check)`, or `Stay Local`.
    *   **Fallback:** Otherwise, the agent MUST display the **Management Commands**, followed by the full contents of **[INDEX.md](./INDEX.md)**, and finally an invitation to ask for help on a specific skill (e.g., "Ask 'Help [skill-name]' for more details").
*   **Management Commands**:
    - **Index Skills**: Show everything I'm currently capable of doing in this project.
    - **Refresh / Import**: Update my skills with the latest versions from your global library.
    - **Publish / Export**: Save a local improvement I've made back to your global library.
    - **Audit Config**: Run a quick health check to make sure my settings are correct for you.
    - **Stay Local**: Tell me to stop asking for a global library and just work with what's here.
*   **Proactivity**: If the user uses Git terminology (Push/Pull) or seems unsure of how to synchronize, the agent should say: *"I noticed you're trying to sync skills. Would you like to see a list of commands and how they work? I can also show you an **Index** of everything I'm currently capable of doing in this project."*

## Rules & Constraints
- **Avoid Git Terms**: NEVER use "Push" or "Pull" when discussing skill synchronization to avoid confusion with Git guardrails.
- **Kebab-Case**: Always use kebab-case for skill directory names.
- **Atomic Operations**: Always copy the full directory to maintain functionality.
- **Explicit Publishing**: Never automatically update a master source.
- **PII Scrubbing**: Ensure all logic in the master source is generic and scrubbed of project-specific PII (unless it's in the Discovery Trail for context).

## Discovery Trail
- **2026-05-18**: Added "Skill Drill-down Help" workflow to allow users to ask for details on specific skills (e.g., "Help manage-docs").
- **2026-05-18**: Removed skill linking from the index to ensure portability and visibility. Mandated plain bold text for skill names.
- **2026-05-18**: Refined skill indexing to ensure the skill name (not the filename) is used as the link text for better readability.
- **2026-05-18**: Refined "Help" output layout. Moved away from tables to a cleaner Category/List format.
- **2026-05-18**: Added "Global Skill Indexing" workflow. Expanded the "Help" command to include a full capabilities audit by scanning the .agents/skills/ directory.
- **2026-05-18**: Added "Help & Guidance" workflow with a command reference table. Mandated proactive help offers if the user appears confused or uses deprecated Git terminology.
- **2026-05-18**: Added "Local-Only" mode to suppress repetitive master-source prompts for users who choose to keep skills project-local.
- **2026-05-18**: Added Post-Refresh Audit requirement to ensure project configurations remain compatible with updated skill versions.
- **2026-05-18**: Renamed sync workflows from "Push/Pull" to "Publish/Refresh" (and variants) to prevent AI agents from triggering Git guardrails and to clearly distinguish local library management from Git operations.
