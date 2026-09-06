# How This Documentation Works

<!-- This is docs/README.md - the git-native front door. It is never
     synced to the knowledge base; it describes the system. Adapt the
     placeholders; keep it readable by someone on their first day. -->

Everything about this project lives in one of three kinds of place.

**The issue tracker owns work**: stories, bugs, ideas, anything with a
status that will someday be "done". If it tracks progress, it is an
issue - never a document. The `docs/stories/` folder is a generated
snapshot of those issues for the agent's benefit; don't edit it.

**The knowledge base owns knowledge**, and `docs/knowledge/` is its
mirror. Decisions, specs, research, guides, mandates - anything a
person would look up. The knowledge base (in the tracker) is where
humans arrange and edit; the mirror is where agents read and write.
A sync keeps the two together:

- Edit an article in the tracker, or edit its file here - either works.
  The sync merges honest divergence and marks true conflicts with git
  markers for a human to resolve.
- **Rearranging happens in the tracker only.** Move or rename articles
  there; the folder tree follows on the next sync. Moving files around
  locally gets undone.
- To add a document from the repo side: create a `.md` file with a
  `# Title` heading in the right section folder (each section's
  `README.md` says what belongs there) and sync. From the tracker side:
  just create the article where it belongs.
- Files are named by article ID (`PROJ-A-12_title.md`) so they survive
  renames and are easy to cite.
- `docs/knowledge/.yt-sync/` is the sync's memory. Commit it; never
  edit it.

**Plain git owns the machinery**: `AGENTS.md` and `WIRING.md` at the
repo root, this file and any agent-maintained indexes at the `docs/`
root, `docs/outbox/` for artifacts written for outsiders, and
`docs/_archive/` for retired files. None of that syncs anywhere.

Git is the version history for all of it - the mirror included. Diffs,
blame, and merges work on knowledge exactly as they do on code.

Questions about where something goes: <!-- name the owner/channel -->.
