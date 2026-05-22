---
name: manage-skills
description: Manage the lifecycle, organization, and synchronization of AI agent skills between local project workspaces and master skill sources. Use when user wants to refresh skills, publish changes to the master source, or organize the .skills directory.
---

# Skill Management

## Objective
Provide a standardized, interactive workflow for maintaining a consistent library of AI agent skills across multiple projects and centralized master sources.

## Core Hierarchy
1.  **Project Skills (.skills/)**: Local workspace skills, often refined or customized for a specific project.
2.  **Master Sources**: Remote or local authoritative repositories for shared skills. Paths are configured per-machine in ~/.config/agents/<project_id>/skill-sources.json.

## Workflows

### 1. Configuration & Discovery
Before any operation, the agent must verify the project is initialized (.config/project.json).
- **Dependency**: If .config/project.json is missing from the workspace, the agent MUST trigger the **setup-project** initialization workflow before attempting to resolve master sources or global state.
- **Load Sources**: Read ~/.config/agents/<project_id>/skill-sources.json. 
- **Prompt**: If sources are missing, ask the user for the primary master skill repository path (e.g., ~/Workspace/skills/src/skills/).
- **Local-Only Mode**: If the user (especially a non-technical persona) does not have a master source or doesn't want to manage one, the agent must offer to "Stay Local".
    - Record this preference in ~/.config/agents/<project_id>/skill-sources.json as an empty array [].
    - Once recorded, the agent MUST NOT prompt the user again for a master source unless explicitly commanded to "Configure skill sources".

### 2. Downstream Sync (Refresh/Import: Source ➔ Project)
Triggered by commands like "Refresh skills", "Import skills", or "Fetch skills". This workflow brings shared improvements into the current workspace.
1.  **Detection**: Compare local .skills/ with the registered master sources.
2.  **Reporting**: Present a categorized report:
    - **New**: Skills available in sources but missing locally.
    - **Updates**: Local skills that are identical to an older master version but have a newer version available.
    - **Divergent**: Local skills that have been modified and differ from the latest master version.
3.  **Conflict Resolution**: 
    - For **New** and **Update** items: Ask for permission to install/overwrite.
    - For **Divergent** items: **STOP** and display a diff. Ask the user: *"The local version has diverged. Do you want to overwrite your local changes, keep your version, or publish your changes to the master source instead?"*
4.  **Execute**: Copy the entire directory (scripts/resources included) only after user confirmation.
5.  **Post-Refresh Audit**: After a major refresh, the agent should offer to run a **Configuration Audit** (from the setup-project skill) to ensure the project settings are compatible with the latest skill versions.

### 3. Upstream Sync (Publish/Export: Project ➔ Source)
Triggered ONLY by an explicit command (e.g., "Publish skill [name]", "Export skill [name]", or "Save skill [name] to master").
1.  **Select Target**: If multiple sources exist, ask which one should receive the update.
2.  **Validate**: Ensure the skill follows the [write-a-skill](../productivity/write-a-skill/SKILL.md) mandate.
3.  **Preserve Discovery**: Verify that the SKILL.md includes a **Discovery Trail** section capturing the original design logic and AI discussion history.
4.  **Execute**: Copy the local project skill to the selected master repository.

### 4. Global Skill Indexing (The "Capabilities Audit")
To help users understand the full extent of their agent's powers, the agent must maintain a comprehensive index of all installed skills.

*   **Source of Truth**: The capabilities index is strictly located at .skills/INDEX.md.
*   **Trigger**: Triggered by "Index skills", "Show capabilities", or "Help".
*   **Workflow**:
    1.  **Scan & Sync**: Recursively search .skills/ for all SKILL.md files.
    2.  **Generate Index**: Read the YAML front matter of each file.
    3.  **Layout**: Organize by category (folder name) using a list-based format with a blank line between each item.
    - **[Category Name]**
    - **[Skill Name]**: [Human-friendly description of what it does for the user].
*   **Labeling**: Use the skill's internal name from metadata as the label. **NEVER use links, "SKILL.md", or file paths as labels.** The index must be portable and readable across all environments.
*   **Output**: Display the contents of .skills/INDEX.md directly.
*   **Maintenance**: Whenever a skill is **Published** or **Refreshed**, the agent MUST offer to "Update the index" to ensure .skills/INDEX.md remains accurate.

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
*   **Core Output**: The agent MUST display the **Management Commands**, followed by the full contents of **[INDEX.md](./INDEX.md)**, and finally an invitation to ask for help on a specific skill (e.g., "Ask 'Help [skill-name]' for more details").
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
- **2026-05-18**: Added "Global Skill Indexing" workflow. Expanded the "Help" command to include a full capabilities audit by scanning the .skills directory.
- **2026-05-18**: Added "Help & Guidance" workflow with a command reference table. Mandated proactive help offers if the user appears confused or uses deprecated Git terminology.
- **2026-05-18**: Added "Local-Only" mode to suppress repetitive master-source prompts for users who choose to keep skills project-local.
- **2026-05-18**: Added Post-Refresh Audit requirement to ensure project configurations remain compatible with updated skill versions.
- **2026-05-18**: Renamed sync workflows from "Push/Pull" to "Publish/Refresh" (and variants) to prevent AI agents from triggering Git guardrails and to clearly distinguish local library management from Git operations.
