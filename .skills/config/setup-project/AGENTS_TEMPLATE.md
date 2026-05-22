# AI Agent Operations Guide

This document defines the mandates and operational rules for AI assistants (like Gemini) working on the project. 

## 🤖 AI Assistant Instructions

All AI assistants MUST read and adhere to the instructions in this file. This is your primary "system prompt" for this specific workspace.

### Core Mandates

1.  **Context Mapping**: You must map your context directly to the established documentation hierarchy before taking action:
    - Reference **[SPEC.md](./docs/mandates/SPEC.md)** for business logic.
    - Reference **[ARCH.md](./docs/mandates/ARCH.md)** for repository, data, and technical patterns.
    - Reference **[DESIGN.md](./DESIGN.md)** for UI/UX modifications.
    - Reference **[README.md](./docs/design/README.md)** when rendering UI screens or feature mockups.
    - Reference **[README.md](./docs/gap/README.md)** for project roadmap and pending tasks.
2.  **Cross-Platform Synchronization**: Whenever you modify core shared libraries, you MUST also update and verify the corresponding bridges or consumers.
3.  **The "Update Docs" Rule**: When told to "update docs", you MUST trigger the **[manage-docs](./.skills/config/manage-docs/SKILL.md)** skill. You must intelligently disperse new information into the appropriate specialized directories (`mandates/`, `ac/`, `gap/`, `regulations/`, etc.) rather than creating generic text blocks in this file.
4.  **Path Resolution**: Always use absolute paths starting from the project root for any file operations.
5.  **TDD Preference**: This project follows **Test Driven Development (TDD)**. All new features and modifications MUST include corresponding tests.

### Operational Handover

If you reach a point where you cannot proceed or need to hand off to another agent, you MUST generate a **[handoff](./.skills/productivity/handoff/SKILL.md)** document following the standard template.
