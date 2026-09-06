# Tracker Binding: GitHub (story-reconcile)

| Operation | GitHub command |
|---|---|
| Snapshot server → local | `scripts/gh-pull.sh [owner/repo] [OUT_DIR]` (bundled in this skill). Output: `docs/stories/OC-0123_title-slug.md` files (short prefix from `tracker.prefix`, zero-padded issue number; blocks `OC-A-0001` past 9999 keep sort order) + `INDEX.md`, plus `.agents/config/dimensions.md` at the docs root (project Status/Priority/etc field values + repo labels for offline picking) - all generated, never hand-edit |
| Create a story from a doc/worklog item | MCP `create_issue` / `gh issue create` (canonical format, AC as task list) |
| Add AC to an existing issue | Read-modify-write the body: fetch fresh, edit the `## Acceptance Criteria` section only |
| Mark discovered/migrated provenance | `discovered` label + body line `Discovered from #N` (or the source doc path) |
| Match candidates | Search `state:all` by title keywords before creating - closed issues count as matches |
| Close obsolete | Close as "not planned" with a comment; never delete |
| Worklog replay: effort entries | GitHub has no work items - post approved effort as a comment on the recorded story (`Effort: 2h`), one per approved entry |
| Worklog replay: stage moves | Projects mode: `scripts/gh-stage.sh N "<column>"` per recorded transition |

Connection facts from `.agents/config/story-tools.json` (`tracker.repo`,
optional `tracker.project`). Token for scripts: `$GITHUB_TOKEN` →
`gh auth token` → the developer's `github.env` connection. Snapshot dir
convention: `docs/stories/`, commit it if the team wants tracker context
in PRs (same convention as YouTrack).
