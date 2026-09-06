# Application Wiring Rules

This file documents the dependencies, triggers, and integration points between application features. Every new feature must be checked against these rules, and any new wiring must be registered here.

## Core Integration Patterns
*This section describes the connection mechanisms used in this project (e.g. direct imports, callbacks, dependency injection) based on the project setup.*

- **Mechanism**: [Describe patterns, e.g. direct method calls, dependency injection, callback interfaces]
- **Shared Path(s)**: [Paths housing shared/global systems, e.g., src/core/]

## Global Services
*Global modules or services that other features should integrate with (e.g. DatabaseHelper, Logging, Session Manager).*

- **[Global Service Name]**:
  - **Location**: `path/to/file`
  - **Triggers / Integration Rules**:
    - When [action] -> [integration required]

## Feature Connections & Triggers
*Specific triggers and rules mapping how features interact with each other.*

### [Feature Name]
- **Internal Triggers**:
  - `[Event/Action Name]` -> Must trigger **[Connected Feature]** to [describe action].
