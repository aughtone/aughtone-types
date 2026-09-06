# Project Development Guide

This document is the master instruction set for AI agents contributing to this repository.

# Working in this repository

## This repository is public

Everything committed here is world-readable and permanent — a public git
history cannot be unpublished, only added to. Write for a stranger who
found this repo, not for the maintainer.

**Never commit personal details.** Concretely, that means no real names in
prose or code comments, no personal email addresses, no machine or
hostnames, no absolute home paths, no employer, client or private project
names, no internal tracker keys or instance URLs, and nothing about the
maintainer's business, billing, tax position or working habits. Verbatim
quotes from a working conversation are the most common way all of this
leaks at once — a design discussion is full of them.

The exceptions are deliberate and narrow: the copyright line in `LICENSE`
and `NOTICE.md`, maintainer attribution in `README.md`, the GitHub handle
and its `users.noreply.github.com` address as vendor in the YouTrack app
manifest, and the published Maven coordinates used as worked examples. Do
not add to that list without being asked, and do not strip what is on it.

Authorship of the tool is fine; identity beyond it is not. A GitHub handle
is the right granularity — it is already public, and it is the name the
project is known by.

**Document the pattern, not the person.** When a design came out of
someone's specific situation — how they work, what tools they pay for, what
their client expects — the reusable content is the *pattern*: this is a
common way developers work, here is why the obvious design fails against
it, here is how this one covers it. Written that way the reasoning survives
intact and nothing traces back to an individual. If a fact only makes sense
as "the maintainer does X", it does not belong here.

This applies hardest to documents an agent generates from a conversation —
RADs, ADRs, design notes, session summaries. Those are written while the
conversation is still in context, which is exactly when quoting feels
natural and is most dangerous.

**Examples use placeholders.** `acme`, `example.com`, `PROJ-123`,
`<instance>.youtrack.cloud`, `owner/repo`. Never a real project, org or
instance, even one that happens to be public — a real name in an example
reads as a live reference and invites someone to go look.

**Check before you commit.** Grep your own additions for names, emails,
hosts, home paths and project names before proposing them. If something is
borderline, leave it out and say so — it is far cheaper to add a detail
later than to remove one from a public history.


## 2. Core Development Principles

- **Test-Driven Development (TDD)**: Whenever feasible, write a failing test before implementation.
- **Kotlin Multiplatform**: All code must be multiplatform-first. Be mindful of source set placement (`commonMain`, `androidMain`, etc.). Avoid using parentheses `()` in test function names (e.g. `fun test name() {}`), as these are illegal characters that crash Apple and Linux native compilation.
- **Immutability & Safety**: Maintain data structure immutability and handle serialization (`kotlinx.serialization`) correctly.
- **Consistency**: Adhere to existing patterns; consistency outweighs personal preference.
- **Type Preference (Avoid Shadowing)**: 
    - **`Locale`**: Prefer `io.github.aughtone.types.locale.Locale` when working in `commonMain` where cross-platform consistency is required. Be aware of potential shadowing by `java.util.Locale` or Compose-specific locales and use the fully qualified name if necessary to resolve ambiguity.
    - **`Currency`**: Prefer `io.github.aughtone.types.financial.Currency`.
    - **`BigInteger` & `BigDecimal`**: Prefer `io.github.aughtone.types.number.BigInteger` and `io.github.aughtone.types.number.BigDecimal` for multiplatform arbitrary-precision arithmetic instead of platform-specific types or standard double/long representation.
    - **GeoJSON Types (`GeoPoint`, etc.)**: Prefer `io.github.aughtone.types.geo.GeoPoint` over generic graphics `Point` classes. All GeoJSON types in this library use the `Geo` prefix to prevent conflicts, while retaining standard `@SerialName("Point")` annotations for RFC 7946 compliance.

## 3. Additional Guidelines

- **Verification First**: A story's acceptance criteria live on its tracker
  issue as a task list — that checklist IS the scope. Read it before
  implementing and check items off only when verifiably complete. Never
  copy AC into a document; a PRD that carries its own checklist drifts from
  the tracker immediately.
- **Technical debt is an issue, not a file.** Anything found in passing —
  a gap, a missing feature, a cross-platform inconsistency — is captured as
  a GitHub issue labelled `needs-triage` (the `triage` skill does this), and
  you return to the story you were on. Discovered work never expands the
  current story. The retired `GAPS.md` is in `docs/_archive/` for its
  history; nothing new goes there.
- **No embedded skill files.** Do not add `*.ai-skill.md`,
  `META-INF/ai-skills/` or `META-INF/agents/skills/` to this repo, and do
  not scan dependencies for them.

## 4. Where things live

[docs/README.md](docs/README.md) is the front door and explains the whole
system. In short: work is [GitHub Issues](https://github.com/aughtone/aughtone-types/issues),
knowledge is `docs/knowledge/` (each section's `README.md` says what belongs
in it), and `docs/WORKFLOW.md` describes how work moves. `docs/stories/` and
`docs/dimensions.md` are generated snapshots of the tracker — read them,
never edit them.
