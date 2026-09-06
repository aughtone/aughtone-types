# Tracker Binding: GitHub (docs sync)

The knowledge base is the repo's **wiki** - itself a git repo
(`<repo>.wiki.git`), so sync is git plumbing with the same three-way
merge model as YouTrack. Capability-detected: no wiki (disabled, or a
private repo without a paid plan) → `docs/knowledge/` stays git-native
with no mirror, and everything else in this skill still applies.

Sync command (script bundled in this skill):

```
scripts/gh-wiki-sync.sh [KB_DIR] [--repo owner/repo]
                        [--pull-only] [--allow-delete] [--force] [--dry-run]
```

- Domain: `KB_DIR` (default `./docs/knowledge`) ⇄ the repo wiki.
- **Structure flows UP** - the opposite of YouTrack. The wiki has no
  hierarchy UI, so the local tree owns the layout: page names encode the
  path (`architecture/decisions/foo.md` → `Architecture-Decisions-Foo`),
  a section's `README.md` is the section page, `KB_DIR/README.md` is
  Home, and a generated `_Sidebar.md` shows the tree (never edit it in
  the wiki - it is overwritten). Reorganize by moving files locally; an
  unchanged moved file becomes a page rename, a move+edit degrades to
  delete+create (run with `--allow-delete` after a reorganize to prune).
- **Content flows both ways.** Wiki UI edits pull; local edits push;
  both at once three-way merge against the base recorded in
  `KB_DIR/.gh-wiki-sync/` (commit it; never hand-edit). Conflicts get
  git markers, are NEVER pushed until resolved. Exit codes: 0 ok,
  1 error, 2 conflicts to resolve.
- A page created fresh in the wiki UI lands at the KB root on pull,
  reported under "New from wiki" - file it into a section (the next
  sync renames the page to match).
- Deletes: a wiki-side delete prunes an unedited local file, conflicts
  an edited one. A local delete is report-only until `--allow-delete`.
- Bootstrap: empty `KB_DIR` pulls the whole wiki. Non-empty without
  sync state refuses unless `--force`, which adopts every local file as
  a page (local wins on same-named pages) - the legacy-adoption path.
- Setup requirements: the wiki must be **enabled** on the repo and
  **initialized** (GitHub only creates `<repo>.wiki.git` after the
  first page is saved in the web UI - create Home once). The script
  reports each case distinctly.
- Repo resolves from `--repo` or `tracker.repo` in
  `.agents/config/story-tools.json`; token from the story-tools
  connections (`$GITHUB_TOKEN` → pointer connection env → legacy
  `github.env` → `gh auth token`) with the Contents read/write
  permission. Never ask for or accept tokens in conversation - if
  authentication fails, point the user at `.agents/setup.sh` (or the
  installer, on older binds).
- Requires `git` and `python3` on PATH.
- **Links are rewritten, both ways.** Write ordinary repo-relative links
  (`[Scope](../requirements/PRD-0002-scope.md)`); push converts them to the
  wiki's flat page names and pull converts them back, so the working tree
  is the only place paths exist. A target that is not a synced document -
  a directory, a source file, anything outside `docs/knowledge/` - becomes
  a repo link instead. A link that resolves to nothing is left untouched
  and reported, so a typo stays visibly broken rather than becoming a
  plausible URL that 404s.
- **Images.** The wiki has no attachment store, so an embed is served from
  the repo: keep the image beside the document, embed it by relative path
  (`![Flow](checkout-flow.png)`), and push rewrites it to a `?raw=1` repo
  URL that carries the reader's session - so it renders on a private repo
  too. Nothing is uploaded and nothing needs a new name to change: replace
  the file in the repo and the wiki follows.
