# Tracker Binding: YouTrack

Connection: MCP endpoint `"<url>/mcp?customToolPackages=story-tools"`, where
`<url>` and the MCP server name come from `.agents/config/story-tools.json`
(server is `youtrack-<nickname>` — read the exact name from the pointer's `mcpServer`; never use another connection's tools). The custom `story_*` tools are a
YouTrack app; YouTrack's built-in MCP tools are always present alongside.

## Operation map

| Operation | Full (app installed) | Fallback (built-in tools only) |
|---|---|---|
| `focus.get` | `story_get_focus` | No server storage - ask the user; restate the ID at checkpoints |
| `focus.set` | `story_set_focus(issueId)` | Same; suggest starring the issue for multi-day work |
| `story.context` | `story_get_story_context` | `get_issue` → parse `## Acceptance Criteria` (`- [ ]`/`- [x]`, in order), `## References`, `## QA` yourself |
| `ac.toggle` | `story_update_ac(index, textPrefix, done)` - refuses on drift | `get_issue` fresh → flip exactly one checkbox in the description → `update_issue` with the full text. Re-read immediately before writing; you have no drift guard |
| `ac.add` | `story_add_ac(text)` | Same read-modify-write, appending a `- [ ]` line |
| `work.discovered` | `story_add_discovered_work(summary, description)` - **pass the issue id explicitly**; omitted, it uses your focused story, which may be in another project - links "discovered from" (falls back to relates-to + `discovered` tag) | `create_issue` in the same project (canonical format) + `link_issues` relates-to the current story |
| `story.completeCheck` | `story_complete_story` - checks AC, `needs-gherkin` tag vs `## QA` | Parse AC yourself: all checked? tag present but no QA section? Report; don't close otherwise |
| `effort.log` | `story_log_work(minutes, comment?)` on the FOCUSED issue - human-approved only. Effort belongs to the issue being worked; a working day belongs to the `worklog` skill, never here | No work-item tool built in: tell the user the number to enter via YouTrack's `work` command, or post it as a comment (`Effort: 2h`) for later entry |
| `story.next` | `search_issues`: `project: {KEY} tag: {ready-for-agent} #Unresolved sort by: priority asc` (drop the tag term if the project doesn't use triage) | same |
| Stage on pickup | predefined `update_issue` - Stage → the in-progress column (read real values via `story_project_dimensions`; fallback: `.agents/config/dimensions.md` from the last pull, else ask once) | same, values from the snapshot or board |
| State change on completion | predefined `update_issue` - boards with a testing/review column: Stage → that column (a human moves it to done after verification); otherwise Stage → done column. Stage+State projects also set State → resolution (e.g. Fixed); single-field projects set that field | same |
| Priority / Estimation set (planning, triage only) | predefined `update_issue` field commands | same |

Focus is safe in read-only mode (it writes only your own app-scoped marker).

## Detecting what's available

Check your tool list. No YouTrack tools at all → the MCP connection isn't
set up: tell the user ONCE how to connect — `.agents/setup.sh` when the
project has it (shipped at bind time), else the story-tools installer
(`scripts/install.sh` in the skills repo) — never collect credentials
yourself — then offer offline mode (the skill's
[references/offline.md](../references/offline.md) fallback binding) so work
can continue and be reconciled later. Built-in tools
but no `story_*` → the app isn't installed or the MCP URL lacks
`?customToolPackages=story-tools`: say so once, then work in fallback mode
(right column above) with extra write care.

## Fallback cautions

- Description read-modify-write can clobber a concurrent human edit:
  read, change one line, write, re-read to confirm. Never "clean up" the
  rest of the description while toggling.
- If write tools are refused (read-only token or app read-only mode),
  switch to the read-only behavior in the main skill.

## Server setup (admins)

Requires YouTrack Cloud or Server 2025.3+. Full experience = the story-tools
app deployed (`scripts/deploy.sh`, needs global Update Project or Low-level
Admin Write) and clients connected with `?customToolPackages=story-tools`
(custom tools are only exposed when named in the URL; re-add the MCP
connection after a deploy that changes tool names). One-time conveniences
the installer creates automatically when the token permits: directed link
type `discovered from`, the reserved workflow tags (shared with All
Users), and per-project time tracking
(work items behind `story_log_work`; enable manually under Project Settings
> Time Tracking if the installer lacked admin rights). Optional app setting
"Read-only mode" refuses story_* writes server-side. Verify with
`scripts/smoke.sh`.
