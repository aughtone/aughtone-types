# Agent Capabilities Index

### Config

**manage-docs**: Manage the 13-sector documentation hierarchy, perform semantic migrations of legacy documentation, and provide proactive recording of architectural and functional decisions.

**manage-skills**: Manage the lifecycle, organization, and synchronization of AI agent skills between local project workspaces and master skill sources.

**persona**: Initialize and manage persona state via a global persona.json file.

**setup-project**: Onboard and configure a project workspace for tracking, sync, and agent interaction.

### Engineering
**to-ai-skill**: Generates and updates a machine-readable AI skill document bundled within this published library.

**grill-with-docs**: Grilling session that challenges your plan against the existing domain model, sharpens terminology, and updates documentation (CONTEXT.md, ADRs) inline as decisions crystallise.

**improve-codebase-architecture**: Find deepening opportunities in a codebase, informed by the domain language in CONTEXT.md and the decisions in docs/adr/.

**prototype**: Build a throwaway prototype to flesh out a design before committing to it.

**tdd**: Test-driven development with red-green-refactor loop.

**to-design**: Formalizes the design-capturing process as a structured agent capability to ensure visual samples are generated and properly registered.

**to-issues**: Break a plan, spec, or PRD into independently-grabbable issues on the project issue tracker using tracer-bullet vertical slices.

**to-prd**: Turn conversation context and codebase understanding into a formal PRD and matching AC spec.

**triage**: Triage issues through a state machine driven by triage roles.

**zoom-out**: Tell the agent to zoom out and give broader context or a higher-level perspective.

### Governance
**rad-research**: Manage Technical Research & Development (R&D) logs.

**regulatory-compliance**: Manage and track regulatory requirements (PIPEDA, GDPR, etc.).

### Misc
**git-guardrails-claude-code**: Set up Claude Code hooks to block dangerous git commands.

**git-guardrails-gemini**: Codifies the strict Git safety rules for Gemini CLI.

### Productivity
**caveman**: Ultra-compressed communication mode. Cuts token usage ~75% by dropping filler while keeping full technical accuracy.

**grill-me**: Interview the user relentlessly about a plan or design until reaching shared understanding.

**handoff**: Compact the current conversation into a handoff document for another agent to pick up.

**write-a-skill**: Create new agent skills with proper structure, progressive disclosure, and bundled resources.

### Tracking
**generate-epic**: Generate comprehensive Agile Epic documents (Format B).

**generate-issue**: Generate local Markdown issue documents (Format A).

**sync**: Publish local Markdown issue documents (Format A and Format B) to a remote issue tracker.
