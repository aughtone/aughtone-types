# Standard: AI Skill Publishing

This document outlines a standardized pattern for embedding machine-readable "skills" or documentation within a published library. This allows AI development assistants to discover and utilize a library's features more effectively, improving the developer experience.

## The "Why"

As AI assistants become more integrated into IDEs, they need a reliable way to understand the specific APIs, patterns, and conventions of a library. While human-readable documentation is essential, a structured, machine-readable format allows an AI to:

-   Quickly learn a library's core abstractions.
-   Provide more accurate, context-aware code completions.
-   Scaffold new implementations using the library's best practices.
-   Avoid common pitfalls and anti-patterns.

By embedding this information directly within the library's resources, we ensure that the skills are versioned and distributed alongside the code they describe.

## The "How"

### 1. Use Namespaced Filenames

To prevent collisions when multiple libraries are included in a project (e.g., in a shadow JAR), skills must use a unique, namespaced filename and a specific suffix.

The standard path and naming pattern is:

```
src/commonMain/resources/META-INF/ai-skills/[library_maven_group].[library_maven_artifact](-[scope]).ai-skill.md
```

**Example:**

For a library with Maven coordinates `io.github.aughtone:types`, the primary skill file would be:

```
src/commonMain/resources/META-INF/ai-skills/io.github.aughtone.types.ai-skill.md
```

If the library has multiple specific skillsets (e.g., core and networking), you would use:
- `io.github.aughtone.types-core.ai-skill.md`
- `io.github.aughtone.types-networking.ai-skill.md`

### 2. Include Mandatory Metadata

Every `.ai-skill.md` file MUST begin with a YAML frontmatter block. This allows AI assistants to verify the identity and compatibility of the skill even if it is moved or extracted.

```markdown
---
skill-id: [library_maven_group].[library_maven_artifact]
scope: [core|api|guides|...]
compatibility: "[version_range]"
---
```

**Example:**

```markdown
---
skill-id: io.github.aughtone.types
scope: core
compatibility: ">=1.0.0"
---
```

### 3. Add Skill Content

Below the metadata block, add Markdown content that describes the library's features and how an AI should integrate them.

#### **Core Content:**
-   **Core data types** and their serialization formats.
*   **Key functions** and their intended usage.
-   **Step-by-step guides** for common tasks.
-   **Architectural patterns** and best practices.

#### **Agent Onboarding (Mandatory):**
Include a specific `## 🤖 Agent Onboarding` section. This section provides explicit instructions for an AI agent on how to bootstrap this library into a new workspace.

**Example:**
```markdown
## 🤖 Agent Onboarding
1. **Context Registration**: Add this skill file to the `AGENTS.md` of the consuming project.
2. **README Verification**: Ensure the root `README.md` contains the "AI-Assisted Development" section.
3. **Usage Rules**: When generating code using this library, always prioritize the immutable patterns defined in the [Types Section](#).
```

### 4. Update the `README.md`

To make this feature discoverable by both humans and AIs, add a section to your root `README.md` file.

```markdown
## 🤖 AI-Assisted Development

This library includes embedded, machine-readable "skills" to enhance the experience of developers using AI code assistants. These skills help the AI understand our library's APIs and best practices, leading to more accurate and idiomatic code suggestions.

-   **AI Skill Discovery**: Look for `META-INF/ai-skills/*.ai-skill.md`

To learn how to add this capability to your own library, see our [AI Skill Publishing Standard](docs/standards/ai-skill-publishing.md).
```

### 5. Developer Quick Start

To add AI skills to your library, follow these steps:

1.  **Identify the Namespace**: Use your library's Maven coordinates (e.g., `com.example.lib`).
2.  **Create the File**: Create `src/commonMain/resources/META-INF/ai-skills/com.example.lib.ai-skill.md`.
3.  **Add Metadata**: Paste the YAML frontmatter at the top of the file.
4.  **Write Content**: Describe your core APIs and patterns.
5.  **Add Agent Onboarding**: Copy/paste the `## 🤖 Agent Onboarding` section and customize it.
6.  **Update README**: Add the `## 🤖 AI-Assisted Development` section to your root `README.md`.
7.  **Publish**: Build and publish your library. The skill will be bundled automatically as a resource.

### 6. Automated Bootstrapping

If you are using an AI agent (like Antigravity), you can use the following "Magic Prompt" to initialize this standard in your project automatically:

> **Prompt:** `agent: bootstrap-ai-skills [group]:[artifact]`

**When an agent receives this prompt, it MUST perform the following actions:**

1.  **Identify Namespace**: Read `build.gradle.kts` or `gradle.properties` to confirm the library's identity.
2.  **Create Skill File**: Create the file `src/commonMain/resources/META-INF/ai-skills/[group].[artifact].ai-skill.md`.
3.  **Inject Metadata**: Add the YAML frontmatter with `skill-id`, `scope: core`, and `compatibility`.
4.  **Scaffold Content**: Add placeholders for API descriptions and the mandatory `## 🤖 Agent Onboarding` section.
5.  **Update README**: Append the `## 🤖 AI-Assisted Development` section to the root `README.md`.
6.  **Update AGENTS.md**: Add a reference to the new skill file in the project's `AGENTS.md` to ensure immediate governance.

This protocol allows for instantaneous, zero-config onboarding of libraries into an AI-assisted development workflow.

