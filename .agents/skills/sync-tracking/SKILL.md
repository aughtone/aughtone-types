---
name: sync-tracking
description: Push local Markdown requirement documents (PRDs and ACs) to a remote issue tracker (YouTrack, GitHub, Jira) using local Python scripts. Use when you are ready to publish finalized requirement documents.
---

# Issue Synchronization (sync-tracking)

## Objective
Provide the agent with the knowledge and execution steps required to push local Markdown requirement documents (PRDs and ACs) to the remote issue tracker (YouTrack, GitHub, Jira) using the local Python synchronization scripts.

## Core Mechanics

### 1. Verification of Requirements
Before attempting a sync, the agent MUST verify:
*   The `.agents/config/project.json` config (or legacy `.config/project.json`) exists and defines a `sync_target` (e.g., `youtrack`, `github`).
*   The appropriate sync script exists locally in `.agents/skills/sync-tracking/scripts/` (e.g., `yt.py` or `gh.py`).
*   The secrets vault directory (`~/.secrets/agents/<project_id>/`) exists and contains the necessary `.env` file for the active target.
*   **Human-Friendly Guidance**:
    - *Instead of:* "Missing sync secrets."
    - *Use:* "I noticed your GitHub secrets are missing from the vault (~/.secrets/agents/<project_id>/github.env). Would you like me to help you set them up so I can sync your documentation with the task board?"

### 2. Execution Preparation
The agent must prepare the environment for the script execution:
*   Identify the target Markdown files to sync (typically located in `docs/prd/` and `docs/ac/`).
    *   **Interactive Target Selection:** If the client supports the `ask_question` tool, scan the directories for unsynced files (those containing `id: #NEW` in their frontmatter) and present them as a multi-select checklist to allow the user to select which ones to sync. If the tool is not supported, present the list in the chat and ask for confirmation.
*   Ensure the Python environment is ready.

### 3. Running the Sync Script
Execute the script from the root of the project workspace. 

*   **Dry Run:** ALWAYS propose a dry-run first if the script supports it (e.g., passing a `--dry-run` flag) to ensure parsing succeeds without making remote mutations.
*   **Execution:** Run the script, passing the target file or directory as an argument.
    ```bash
    # Run the sync command which automatically scans the documentation paths
    python3 .agents/skills/sync-tracking/scripts/yt.py sync
    ```
*   **Opening the Tracker:** Launch the tracker's website or project board in a browser.
    ```bash
    # Launch the configured tracker website
    python3 .agents/skills/sync-tracking/scripts/yt.py open
    ```

### 4. Post-Sync Validation
*   Check the output of the script to confirm successful sync.
*   Verify that `[#NEW]` tags in the local Markdown files have been successfully replaced with real remote IDs (e.g., `[ID-123]`) by the script.
*   If the script fails due to parsing errors, the agent must review the Markdown files to ensure they strictly adhere to **PRD** or **AC** formatting and correct any issues.
