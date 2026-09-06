# Tracker Binding: GitHub

Issues store the stories; a GitHub Project (v2) supplies the board. Two
capability-detected modes, NOT a configuration choice:

- **Projects mode (full)** - the pointer has `tracker.project` (a project
  number): Stage lives in the project's Status field, and the stage
  scripts move items between columns.
- **Issues-only (fallback)** - no project number: everything works except
  Stage (issues are just open/closed). Never fake Stage with labels.

Connection: agents get GitHub's hosted MCP server (registered by the
installer with a PAT header; server name = the pointer's `tracker.mcpServer`,
e.g. `github-occurrence`) when available; bundled scripts and the `gh` CLI
cover the rest. Connections are per-project by default (named from the
project directory - a fine-grained PAT is scoped to one org, so tokens
don't globally share). Script token resolution: `$GITHUB_TOKEN` → the
pointer's connection env → legacy `github.env` → `gh auth token`.

## Operation map

| Operation | How |
|---|---|
| `focus.get` / `focus.set` | No server storage - ask the user; restate the issue number at checkpoints. Suggest self-assigning for multi-day work |
| `story.context` | MCP `get_issue` (+ comments) or `gh issue view N --json body,labels,milestone,assignees` → parse `## Acceptance Criteria` (`- [ ]`/`- [x]`, in order), `## References`, `## QA` yourself |
| `ac.toggle` | Read-modify-write the issue body: fetch fresh, flip exactly ONE checkbox, update, re-read to confirm. No drift guard exists - never "clean up" the rest of the body |
| `ac.add` | Same read-modify-write, appending a `- [ ]` line (explicit user approval only) |
| `work.discovered` | Create an issue in the same repo (canonical story format), labels: `discovered` + the story's topical labels; body starts `Discovered from #<n>.` GitHub has no typed links - the `#<n>` reference is the provenance |
| `story.completeCheck` | Parse AC yourself: all checked? `needs-gherkin` label but no `## QA` section? Open discovered issues referencing this one? Report; don't close otherwise |
| `effort.log` | GitHub has no work items - record approved effort as a comment on the FOCUSED issue (`Effort: 2h`), never silently. The developer's working day is not effort and does not go here - see the `worklog` skill |
| `story.next` | Search: `label:ready-for-agent state:open` , prefer `priority:show-stopper` > `priority:critical` > ... labels when present |
| Stage on pickup | Projects mode: `scripts/gh-stage.sh N "Develop"` (real column names from `.agents/config/dimensions.md` or the script's error listing; auto-adds the issue to the project). Issues-only: no-op - say so once |
| Stage on completion | Projects mode: move to the review/testing column when one exists (implementer-done = ready for verification; a human moves it to done), else the done column. Also close the issue when the board's done column implies it - ask if unsure. Issues-only: close the issue (completed) |
| Priority / Estimate (planning, triage only) | Projects mode: project fields (edit in UI or GraphQL). Issues-only: `priority:<level>` labels |

## Detecting what's available

`.agents/config/story-tools.json` → `tracker.type: "github"`, `tracker.repo`
(owner/name), optional `tracker.project` (number). No `github` MCP tools
and no `gh` CLI → tell the user ONCE how to connect: `.agents/setup.sh`
when the project has it (shipped at bind time), else the story-tools
installer from the skills repo. Then offer offline mode
([references/offline.md](offline.md)) - never collect a token in
conversation.

## Fallback cautions

- Body read-modify-write can clobber a concurrent edit - fetch
  immediately before writing, change one line only.
- `wontfix`: close as "not planned" (`gh issue close N --reason "not planned"`),
  never delete.
- Reserved labels (`triaged`, `ready-for-agent`, roles, `discovered`,
  `needs-gherkin`) are machinery - match case-insensitively, never
  repurpose as topical labels.

## Per-developer setup (multi-developer teams)

The pointer file (`.agents/config/story-tools.json`) and the skills are
COMMITTED with the repo - a new developer inherits the whole workflow from
`git clone`. What each developer does once, on their own machine:

1. **Create a personal PAT** (their own account - tokens are never
   shared). Preferred: **fine-grained token** - resource owner = the
   ORG that owns the repo, repository access = the project repo(s),
   repository permissions Issues RW + Contents RW + Pull requests R +
   Metadata R, organization permissions **Projects RW**. Org-owned
   Projects v2 boards work with fine-grained tokens; USER-owned boards
   still need a classic token (`repo` + `project` scopes). Classic +
   org SSO: **Configure SSO → Authorize** the token, or every org call
   404s and looks like a permissions bug.
2. **Run setup once**: `.agents/setup.sh` in the project clone (shipped
   there at bind time - no extra repo to fetch; older binds without it:
   `<skills-repo>/scripts/install.sh --github`). It creates their
   connection (token prompt is hidden; `gh auth` works as a no-storage
   alternative) and registers the GitHub MCP server in every agent
   config present on the machine - Claude Code, **Gemini CLI**
   (`~/.gemini/settings.json`), Antigravity, VS Code/Copilot.
3. **Restart their agent sessions** so they fetch the MCP tool list.

Rotating a token later: `--register` (on `.agents/setup.sh` or the
installer) re-pushes it into the agent configs (registrations embed the
token; the installer warns when one goes stale).

## Server setup (once per repo, any maintainer)

The installer's bind step creates the reserved label set in the repo and
verifies the Projects board when a project number is given. The board's
Status columns should match the workflow (e.g. Backlog / Develop /
Review / Done); `gh-pull.sh` snapshots whatever exists into
`.agents/config/dimensions.md` so agents use the real column names.
