# How This Documentation Works

Everything about this project lives in one of a few kinds of place, and which
place it is depends on one question: **does it have a status that will
someday be "done"?**

## The tracker owns work

Stories, bugs, ideas, technical debt — anything with progress to report — are
[GitHub Issues](https://github.com/aughtone/aughtone-types/issues), never
files. A story's acceptance criteria live on the issue as a task list, which
is what makes "is this done?" answerable. `docs/WORKFLOW.md` describes the
loop and what to say to an agent to move work through it.

`docs/stories/` and `docs/dimensions.md` are **generated snapshots** of the
tracker, refreshed by the story-reconcile skill's pull script so agents have
issue context offline. Never hand-edit them.

## `docs/knowledge/` owns knowledge

Decisions, specifications, requirements, guides, test protocols — anything a
person would look up. Start at [knowledge/README.md](knowledge/README.md);
each section's own `README.md` says what belongs in it, and that is the file
to read before adding a document.

Every document opens the same way: a `# Title` heading with the title alone
(no `ADR-0003:` prefix — the section already says what type it is), then an
identifier line, then a `Keywords:` line written in the words someone would
search *before* they knew the answer, including the options that were
rejected.

**The knowledge base is currently git-native.** The repo's wiki is enabled but
has never been initialized, so there is nothing to sync with and
`docs/knowledge/` is just a normal part of the repo — edit it and commit it.
To turn the mirror on later: save a Home page once in the wiki web UI, then
run `.agents/skills/project-docs/scripts/gh-wiki-sync.sh --force` to adopt
every existing file as a wiki page. Note the wiki would be public, as the repo
is.

## Plain git owns the machinery

`AGENTS.md` at the repo root is the instruction set for AI agents. This file
and `WORKFLOW.md` sit at the `docs/` root. None of it is knowledge, and none
of it would ever sync.

`docs/reference/` holds source **data**, not prose — the ISO 4217 currency
list (`list-one.xml`) and the generated locale table
(`supported_languages.json`) that the published types are built from. It stays
here rather than under `knowledge/` because a mirror carries markdown only,
and a data file parked in the knowledge base is invisible to readers of it.

`docs/_archive/` holds retired documents, kept rather than deleted so the
reasoning behind them survives. Nothing in it is current.

## Where a new document goes

1. Does it track work? → It is an issue. Stop.
2. Otherwise pick the section whose `README.md` describes it, and create the
   file there.
3. No section fits? Create the directory with a `README.md` saying what
   belongs in it. Suggested sections are listed in
   `.agents/skills/project-docs/references/taxonomy.md`.

Writing the document is a separate job from filing it — the `to-adr`,
`to-prd`, `to-rad` and `to-ux` skills each own one document type.
