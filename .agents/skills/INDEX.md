# Agent Capabilities Index

**caveman**: Ultra-compressed communication mode. Cuts token usage ~75% by dropping filler, articles, and pleasantries while keeping full technical accuracy.

**git-guardrails-claude-code**: Set up Claude Code hooks to block dangerous git commands (push, reset --hard, clean, branch -D, etc.) before they execute.

**git-guardrails-gemini**: Codifies the strict Git safety rules for Gemini CLI. Use this to remind the agent of its limitations regarding destructive Git operations.

**grill-me**: Interview the user relentlessly about a plan or design until reaching shared understanding, resolving each branch of the decision tree.

**grill-with-docs**: Grilling session that challenges your plan against the existing domain model, sharpens terminology, and updates documentation (CONTEXT.md, ADRs) inline as decisions crystallise.

**handoff**: Compact the current conversation into a handoff document for another agent to pick up.

**improve-codebase-architecture**: Find deepening opportunities in a codebase, informed by the domain language in CONTEXT.md and the decisions in docs/adr/.

**manage-docs**: Manage the 14-sector documentation hierarchy, perform semantic migrations of legacy documentation, and provide proactive recording of architectural and functional decisions.

**manage-persona**: Initialize and manage persona state via a global persona.json file. Adapts agent behavior, technical focus, and output style to match the user's active role (Developer, Designer, BA, etc) and specialty.

**manage-skills**: Manage the lifecycle, organization, and synchronization of AI agent skills between local project workspaces and master skill sources.

**prototype**: Build a throwaway prototype to flesh out a design before committing to it.

**regulatory-compliance**: Manage and track regulatory requirements (PIPEDA, GDPR, etc.).

**setup-project**: Onboard and configure a project workspace for tracking, sync, and agent interaction.

**sync-tracking**: Push local Markdown requirement documents (PRDs and ACs) to a remote issue tracker (YouTrack, GitHub, Jira) using local Python scripts.

**tdd**: Test-driven development with red-green-refactor loop.

**to-ai-skill**: Generates and updates a machine-readable AI skill document bundled within this published library.

**to-design**: Formalizes the design-capturing process as a structured agent capability.

**to-issues**: Break a plan, spec, or PRD into independently-grabbable issues on the project issue tracker using tracer-bullet vertical slices.

**to-prd**: Turn conversation context and codebase understanding into a formal PRD and matching AC spec.

**to-research**: Manage Technical Research & Development (R&D) logs.

**triage**: Triage issues through a state machine driven by triage roles.

**write-a-skill**: Create new agent skills with proper structure, progressive disclosure, and bundled resources.

**zoom-out**: Tell the agent to zoom out and give broader context or a higher-level perspective.
