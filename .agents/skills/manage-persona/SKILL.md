---
name: manage-persona
description: Initialize and manage persona state via a global persona.json file. Adapts agent behavior, technical focus, and output style to match the user's active role (Developer, Designer, BA, etc) and specialty. Use when starting an interaction, setting up a workspace, or when the user mentions their role or persona.
---

# Skill: Persona Management & Adaptation

## Objective
Enable the AI agent to dynamically adapt its behavior, technical focus, and output style based on the user's active persona instance. This allows a single user to adopt multiple roles (e.g., "Developer" vs. "Designer") or seamlessly switch between multiple specialized profiles of the same role.

## Core Mechanics: Global State Management
The agent must manage persona state using a global, project-specific JSON file to ensure local state is never committed to version control.

1.  **Context Resolution:**
    *   The agent MUST first identify the current `project_id` by reading the `.agents/config/project.json` file (falling back to legacy `.config/project.json` if needed) in the workspace root.
    *   The persona state file is strictly located at: `~/.config/agents/<project_id>/persona.json`.

2.  **Initialization & Context Loading:**
    *   Upon starting an interaction, the agent MUST read the `persona.json` file.
    *   **If the file exists:** Adopt the behavior and focus defined by the `active_persona_id` inside the JSON object.
    *   **If the file does NOT exist:** The agent MUST explicitly pause and ask the user to initialize their first persona by providing their **Name**, **Role**, and **Specialty**.

3.  **State Creation & Schema:**
    *   When adding a new persona or creating the file, the agent must use the following JSON schema:
        ```json
        {
          "active_persona_id": "brill-1",
          "personas": {
            "brill-1": {
              "name": "Brill",
              "role": ["Developer", "Designer"],
              "specialty": ["Backend", "UX Mechanics"],
              "layout_preference": "standard"
            },
            "jordan-1": {
              "name": "Jordan",
              "role": "Designer",
              "specialty": "Game Design",
              "layout_preference": "game-design"
            }
          }
        }
        ```
    *   The agent must update the `active_persona_id` when the user requests to switch personas.

## Dynamic Persona Profiles
The agent must shift its operational boundaries dynamically based on the active persona profile.

*   **name:** Identifies the specific individual or profile handle.
*   **role:** Defines the primary domain of operation (Can be a String or an Array of Strings for "dual-hat" personas).
*   **specialty:** Further narrows the focus within the role (Can be a String or an Array).
*   **layout_preference:** The default documentation archetype to use (e.g., `standard`, `game-design`).

### Initialization Workflow
If `persona.json` is missing or a new persona is being created, the agent must ask:
*   **Interactive Option:** If the client supports the `ask_question` tool, use it to prompt the user with multiple-choice questions for selecting their active role (e.g. Developer, Designer, Product Owner) and layout preference. Otherwise, present the options as a text list in the chat and wait for a response.
1.  **Role/Specialty**: e.g., Developer (Backend) or Designer (Game Design).
2.  **Layout Archetype**: Provide the known options:
    - `standard`: User stories and functional requirements.
    - `game-design`: Mechanics, loops, and economy.
3.  **Defaulting**: If no preference is stated or the user persona is uninitialized, the agent defaults to the `standard` layout.

## Discovery Trail
- **2026-05-18**: Added `layout_preference` to persona profiles to support polymorphic documentation archetypes. Included bootstrapping prompts for known layouts (`standard`, `game-design`).
- **2026-05-20**: Upgraded persona schema to support "dual-hat" setups (e.g., arrays for `role` and `specialty`) to seamlessly blend responsibilities when a user is acting as both Developer and Designer.
