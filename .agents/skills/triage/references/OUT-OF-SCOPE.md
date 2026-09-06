# Out-of-Scope Knowledge Base

An **Out of Scope** section (under Reference in `docs/knowledge/`)
stores persistent records of rejected feature requests — one file per
**concept**, not per issue. A rejection with its reasoning is *knowledge*
(per project-docs), and living in `docs/knowledge/` means it syncs to the
tracker's knowledge base with everything else.

Two purposes:

1. **Institutional memory** — why a feature was rejected, so the reasoning
   survives the closed issue.
2. **Deduplication** — when a new request matches a prior rejection,
   surface the previous decision instead of re-litigating it.

## Structure and format

```
docs/knowledge/<Reference>/<Out of Scope>/
├── PROJ-A-51_dark-mode.md
├── PROJ-A-52_plugin-system.md
└── PROJ-A-53_graphql-api.md
```

Concept-named titles, recognizable at a glance (the sync adds the
article-ID prefix). Each file
is a short design-document-style note — paragraphs, code samples where they
sharpen the reasoning:

```markdown
# Dark Mode

This project does not support dark mode or user-facing theming.

## Why this is out of scope

The rendering pipeline assumes a single palette defined in `ThemeConfig`.
Supporting themes would require a theme context, per-component resolution,
and a persistence layer - a significant architectural change that doesn't
align with the project's focus. Theming is a downstream-consumer concern.

## Prior requests

- PROJ-42 — "Add dark mode support"
- PROJ-87 — "Night theme for accessibility"
```

Reasons must be substantive and durable: project scope/philosophy,
technical constraints, strategic choices. "We're too busy right now" is a
deferral, not a rejection — deferrals stay in the tracker as unscheduled
issues.

## When to check

During triage context-gathering, read the directory. Match new issues by
*concept similarity*, not keywords — "night theme" matches `dark-mode.md`.
On a match, surface it: "Similar to out-of-scope/dark-mode.md — rejected
because [reason]. Still feel the same?" The maintainer may **confirm**
(append to Prior requests, close the issue), **reconsider** (update/delete
the file, triage normally), or **distinguish** (related but different —
triage normally).

## When to write

Only when an **enhancement** (not a bug) is rejected `wontfix`:

1. Check for an existing matching concept file.
2. Existing → append the issue to Prior requests. New → create the file
   with concept, decision, reason, first prior request.
3. Comment on the issue explaining the decision, referencing the file.
4. Close with the `wontfix` role.

## Changing your mind

Delete or update the concept file; the new issue proceeds through normal
triage. Old closed issues stay closed — they're historical records.
