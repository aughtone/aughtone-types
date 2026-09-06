# Tracker Binding: YouTrack (reconcile)

Connection facts come from `.agents/config/story-tools.json`; credentials
from the story-tools installer connections (never collected in chat).

| Reconcile operation | YouTrack command |
|---|---|
| Snapshot server → local | `scripts/yt-pull.sh <PROJECT_KEY> docs/stories` (bundled in this skill; also usable: `search_issues` via MCP). Output: `docs/stories/PROJ-2_title.md` files + `INDEX.md`, plus `.agents/config/dimensions.md` at the docs root (project field values + topical tags for offline picking) - all generated, never hand-edit |
| Search for MATCH candidates | `search_issues` (MCP) with the project key from the config |
| Create a NEW story | `create_issue` (MCP predefined) in the config's project, body in canonical format |
| Add approved missing AC to a MATCHED story | `story_add_ac` - this is its sanctioned use. If the story-tools app isn't installed: careful `get_issue` → append `- [ ]` line → `update_issue` |
| Link a created story to its source | `link_issues` (relates-to), when a source story exists |

Snapshot dir convention: `docs/stories/`. Commit it if the team wants
offline/PR-visible reference, or gitignore it - ask once.
