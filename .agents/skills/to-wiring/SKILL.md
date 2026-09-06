---
name: to-wiring
description: Audit, check, and maintain the application's internal feature wiring rules in WIRING.md at the repo root. Use when starting a project, proposing a new feature plan, or auditing feature-to-feature integration hooks.
license: MIT
metadata:
  author: bpappin
  version: "1.2"
---

# Application Wiring (to-wiring)

## Objective

Provide an interactive, project-specific workflow to define, check, and
maintain the internal connections ("wiring") between features in the
codebase — so integration hooks are planned upfront and never forgotten
as the app grows.

## Core hierarchy

1. **Project Wiring Rulebook (`WIRING.md`, repo root)** — pure agent
   machinery beside `AGENTS.md`: git-native, never synced, deliberately
   NOT in the knowledge base. It defines:
   - The project's specific connection mechanisms (direct imports,
     dependency injection, callbacks, ...).
   - Global services and features that other modules must integrate with.
   - Explicit feature-to-feature connection rules.
2. **AI instructions (`AGENTS.md`)** — rules in the project's root agent
   instructions mandating that agents read and respect
   `WIRING.md` when planning new code.

## Workflows

### 1. Bootstrapping & configuration

Triggered by "setup wiring", "initialize wiring rules", or when a wiring
command runs and `WIRING.md` does not exist at the repo root.

1. **Q&A interview** — ask the user, one at a time:
   - *Wiring mechanisms*: "How do different features/components typically
     connect and interact in this codebase? (direct imports/method calls,
     callback interfaces, DI frameworks like Hilt or Dagger, global
     notifications?)"
   - *Existing global services*: "Are there 'global' services or shared
     modules other features should integrate with? (shared database
     helper, analytics, logger, user session, notifications?)"
   - *Locations*: "Where do these global systems live in your directory
     structure (core/, shared/, utils/)?"
2. **Generate WIRING.md** — load this skill's
   [assets/WIRING_TEMPLATE.md](assets/WIRING_TEMPLATE.md), substitute the
   user's answers, and write the result to `WIRING.md` at the repo root.
3. **Inject mandates** — run the AGENTS.md update from the audit workflow
   below.

### 2. Interactive wiring audit ("audit wiring")

Triggered by "audit wiring", "update WIRING.md", or after bootstrapping.

1. **Code/docs scan** — scan the source tree, the Product Requirements section of
   `docs/knowledge/`, and the
   tracker's stories (story-workflow / search via the project's tracker
   binding) to detect wiring candidates:
   - Files or classes in common/shared directories.
   - Features whose PRDs or stories depend on other modules.
   - Widely imported classes or services.
2. **Verify connection patterns** — if the communication mechanisms
   aren't clear from the codebase, ask the user.
3. **Candidate-screening Q&A** — for each candidate: "I detected
   [Feature/Module X] at [path]. Should we track this as a wiring target
   or global service? If yes, what triggers or connection rules apply?"
   Use a multi-select question tool if the client has one; otherwise walk
   through them in chat.
4. **Update WIRING.md** — append approved candidates and their rules
   under **Feature Connections & Triggers**.
5. **Inject AGENTS.md rules** — ensure the project's root `AGENTS.md`
   contains this block:

   ```markdown
   - **Wiring & Integration Checks**:
     - Before proposing any implementation plan, you MUST read
       `WIRING.md` at the repo root.
     - Check the proposed feature against all wiring and connection rules.
     - Include an "Integration Hooks" section in the plan showing how the
       feature hooks up to the systems listed.
     - Suggest adding the new feature to `WIRING.md` if it
       qualifies as a global service.
   ```

### 3. Planning checks ("check wiring")

Triggered during implementation planning of a new feature, or on "check
wiring".

1. **Read rules** — read `WIRING.md` at the repo root.
2. **Cross-reference** — compare the proposed feature's PRD and tracker
   story against the integration triggers of all listed global services
   and features.
3. **Identify integration hooks** — for any matched trigger, define how
   the hookup will be built using the project's connection patterns.
4. **Detect global candidates** — if the new feature itself behaves like
   a global service (provides database tables, telemetry, notifications,
   shared state, or is designed to be called by multiple future modules),
   ask: "Should we register it in WIRING.md so other features hook into
   it?"
5. **Document in plan** — add an `Integration Hooks` section to the
   implementation plan proving the connection rules are satisfied. If the
   check surfaces missing hookups in *existing* features, that's
   discovered work — route it to the tracker (story-workflow off-ramp or
   to-issues), don't fold it into the current plan.

## Rules & constraints

- **Zero architectural assumptions** — never assume an event bus, DI,
  pub/sub, or any specific library. Adapt entirely to the connection
  patterns the user specifies.
- **Maintain context** — never auto-delete existing wiring rules unless
  the user explicitly confirms the feature is deprecated or removed.
- **Strict screening** — always screen candidates through Q&A. Never
  unilaterally add a wiring rule without user confirmation.
