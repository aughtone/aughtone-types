# Tracker Binding: YouTrack (docs sync)

Sync command (script bundled in this skill):

```
scripts/yt-sync.sh [KB_DIR] [--project KEY] [--root "Title"]
                   [--pull-only] [--allow-delete] [--force] [--dry-run]
```

- Domain: `KB_DIR` (default `./docs/knowledge`) ⇄ the project's whole
  knowledge base (`--root "Title"` scopes to one top-level article's
  subtree; its body becomes `KB_DIR/README.md`).
- Layout: an article with children is a directory (its body is the
  directory's `README.md`); a leaf article is a file. A leaf is named
  `<ID>_title-slug.md`; a directory is named for its section
  (`decisions/`, `research/`) with no ID - the ID is in its `README.md`.
- Per-article three-way merge against the base recorded in
  `KB_DIR/.yt-sync/` (commit it; never hand-edit). Local-only change →
  push; KB-only change → pull; both → merge, conflicts get git markers
  and are never pushed until resolved. Exit codes: 0 ok, 1 error,
  2 conflicts to resolve.
- Structure flows down: KB moves/renames move local files (reported
  under Moved - update indexes that referenced old paths). Local moves
  are moved back. A new local file's directory picks its parent at
  birth; missing section dirs birth stub section articles from their
  `README.md`.
- Deletes: KB delete prunes an unedited local file, conflicts an edited
  one. Local delete is report-only unless `--allow-delete` (soft-deletes
  the article).
- **Import order decides article IDs.** New files are pushed shallow
  first (a section must exist before its children), `README.md` first
  within a directory, then in natural filename order - `2-scope.md`
  before `10-rollout.md`, not the lexical opposite. So a numeric prefix
  on a new doc is how you say "these belong in this sequence": the
  articles are created in that order, get ascending IDs, and since the
  sync renames each file to `<ID>_title-slug.md` afterwards, the
  prefix's job is done and it disappears. Ordering survives in the IDs.
  (Titles come from the `# Heading`, so the number never leaks into the
  article name.)
- Bootstrap: empty `KB_DIR` pulls the whole KB. Non-empty without sync
  state refuses unless `--force`, which adopts every file as a new
  article (the legacy-adoption path).
- **Leaf → section recovery.** A section created as a bare file is a
  leaf article. Local layout is DERIVED, not chosen: an article with
  children is a directory, one without is a file, decided from the KB
  every sync. So promoting it locally is not just ineffective, it
  duplicates - `mkdir X/` + `X/README.md` + deleting the flat file
  gives you the flat file back on the next pull (a local delete is
  report-only, so the article still has no children, so its path is
  still a file) AND a second section article, because the unrecognised
  directory births a stub from its README.
  Fix it from the KB side instead: give the article a child in
  YouTrack, so it becomes a parent. Then `rm` the local leaf file and
  sync - the directory and its `README.md` materialize, and any
  local-only body text you want to keep goes into that `README.md`.
  To avoid it entirely: create a section as a directory with a
  `README.md` from the start - the sync births the section article
  from that README.
- Project key resolves from `--project`, `$YOUTRACK_PROJECT`, or
  `.agents/config/story-tools.json`; credentials from the story-tools
  installer connections (`~/.agents/story-tools/connections/`). Never
  ask for or accept tokens in conversation - if authentication fails,
  point the user at `.agents/setup.sh` (or the installer, on older
  binds).
- **Images.** Put the file beside the document and embed it by filename
  alone - `![Flow](checkout-flow.png)`. YouTrack resolves an embed against
  that article's own attachments, by name, so a path (`images/x.png`)
  renders in the repo and breaks in the KB. Push uploads any referenced
  file the article does not already have; a name present on both sides is
  left alone, so syncs stay cheap and repeat runs do nothing.
  **To change an image, give it a new filename.** Re-uploading a name adds
  a *second* attachment rather than replacing the first, and YouTrack then
  picks between them unpredictably (JT-83227, JT-62753) - so the sync will
  not do it. Deleting the old attachment by hand breaks the embed in any
  older revision that referenced it; leaving it costs nothing. Sweep unused
  attachments in the YouTrack UI when someone cares.
  Pull fetches the other way: an image referenced by the body but missing
  from the repo is downloaded from the article's attachments and written
  where the reference points - so an image added in the browser lands in
  the working tree on the next sync. An attachment nothing references is
  left where it is.
- Requires `git` on PATH (three-way merges use `git merge-file`).
- YouTrack renders the same markdown the repo holds, task lists included.
