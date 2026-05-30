# AI Agent Operations Guide

This document defines the mandates and operational rules for AI assistants (like Gemini) working on the project. 

## 🤖 AI Assistant Instructions

All AI assistants MUST read and adhere to the instructions in this file. This is your primary "system prompt" for this specific workspace.

### Core Mandates

1.  **Context Mapping**: You must map your context directly to the established **13-Sector Documentation Hierarchy** before taking action:
    - 📐 **Mandates** (`./docs/mandates/`): Technical patterns ([ARCH.md](./docs/mandates/ARCH.md)) and business logic ([SPEC.md](./docs/mandates/SPEC.md)).
    - 📋 **Templates** (`./docs/mandates/templates/`): Document layout blueprints.
    - 🎨 **Design** ([DESIGN.md](./DESIGN.md) at root): Canonical design tokens and layout specs.
    - 🖌️ **Mockups** (`./docs/design/`): User interface mockups and screens.
    - 📝 **Stories** (`./docs/prd/`): Product requirements and stories (PRDs).
    - ✅ **Criteria** (`./docs/ac/`): Gherkin verification scenarios (ACs).
    - 🔍 **Discovery** (`./docs/discovery/`): Due diligence, vendor analysis (DDs).
    - 🛠️ **Roadmap** (`./docs/gap/`): Transient trackers for missing capabilities (GAPs).
    - 🛡️ **Regulations** (`./docs/regulations/`): Compliance mappings (PIPEDA, GDPR, Law 25).
    - 📖 **Guides** (`./docs/guides/`): Installation and environment setup instructions.
    - 🧠 **Reference** (`./docs/reference/`): Domain entities and nomenclature.
    - 🔬 **Research** (`./docs/research/`): Deep-dive R&D log entries (RAD).
    - 💡 **Decisions** (`./docs/adr/`): Architectural Decision Records (ADRs).
2.  **Cross-Platform Synchronization**: Whenever you modify core shared libraries, you MUST also update and verify the corresponding bridges or consumers.
3.  **The "Update Docs" Rule**: When told to "update docs", you MUST trigger the **[manage-docs](./.agents/skills/manage-docs/SKILL.md)** skill. You must intelligently disperse new information into the appropriate specialized directories (`mandates/`, `ac/`, `gap/`, `regulations/`, etc.) rather than creating generic text blocks in this file.
4.  **Path Resolution**: Always use absolute paths starting from the project root for any file operations.
5.  **TDD Preference**: This project follows **Test Driven Development (TDD)**. All new features and modifications MUST include corresponding tests.
6.  **Local & Library Skill Discovery**: Before starting any design, planning, or code modification task, you MUST search the active project root for a `.agents/skills/` directory conforming to the [Agent Skills Standard](https://agentskills.io/) and read any relevant local `SKILL.md` files (such as `manage-docs`, `sync-tracking`, or `to-prd`) before proposing an implementation plan or executing modifications. You MUST scan all project dependencies and dependent library paths for embedded library skill files (`*.ai-skill.md`) in `META-INF/ai-skills/` (for JVM/Android/KMP) or `.ai-skills/` (for NPM/Swift/SPM) conforming to the [Agent Skills Standard](https://agentskills.io/) to understand the API patterns and governance of the libraries being consumed. When you discover and load an embedded library `ai-skill.md`, you MUST explicitly inform the user in your response that you have found the bundled library skill and are utilizing its patterns.

### Operational Handover

If you reach a point where you cannot proceed or need to hand off to another agent, you MUST generate a **[handoff](./.agents/skills/handoff/SKILL.md)** document following the standard template.
