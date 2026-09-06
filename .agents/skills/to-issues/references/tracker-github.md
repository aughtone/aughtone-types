# Tracker Binding: GitHub (to-issues)

Connection facts from `.agents/config/story-tools.json` (`tracker.repo` =
owner/name, optional `tracker.project` = Project v2 number). Credentials
come from each developer's own story-tools installer run - never
collected in chat.

| Operation | GitHub command |
|---|---|
| Create a story | MCP `create_issue` / `gh issue create` in `tracker.repo`, body in canonical format |
| Link a blocker | Body line `Depends on #N` (GitHub auto-links; no typed links) |
| Link to a parent | Body line `Part of #N`; on a Projects board, an epic can also be a draft item or milestone |
| Tag every story `triaged` | `triaged` label (the quiz was their triage; `-label:triaged` must stay the untriaged query) |
| Tag AFK-ready slices | `ready-for-agent` label |
| Tag strict-rule slices | `needs-gherkin` label (gates completion on `## QA`) |
| Topical batch tag | A Title Case label named for the feature/PRD ("Trust Insights"); query later with `label:"Trust Insights"` |
| Set release | Milestone per batch when a target release was confirmed |
| List dimension values | `.agents/config/dimensions.md` (gh-pull) - project Status/Priority fields + existing labels; reuse before inventing |
| Group work by topic | The project's own groupings are `topic:<tag>` labels, listed under **Topical labels** in `.agents/config/dimensions.md`. Reuse one before adding. To add: append a line to `.agents/config/topical-tags.md` (the agents' list - it is committed and reviewed), then `story-reconcile/scripts/gh-pull.sh --dimensions-only --push-tags` creates the label. Nothing constrains what a tag looks like; name it so it groups the work usefully. GitHub has one label set for everything, so the `topic:` prefix is the only thing keeping these apart from workflow labels - never drop it. |
| Set Priority | Projects mode: project Priority field. Issues-only: `priority:<level>` label |
| Set Estimate | Projects mode: project Estimate field if one exists; otherwise skip - GitHub has no native estimation |
| Add to the board | Projects mode: `scripts/gh-stage.sh N "Backlog"` (story-workflow's script; adds the item and sets the column) |
