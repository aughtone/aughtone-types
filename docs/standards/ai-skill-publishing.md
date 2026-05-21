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

### 2. Include Mandatory Metadata

Every `.ai-skill.md` file MUST begin with a YAML frontmatter block.

```markdown
---
skill-id: [library_maven_group].[library_maven_artifact]
spec-version: "1.0"
type: "Aughtone AI-Skill"
scope: [core|api|guides|...]
compatibility: "[version_range]"
---
```

### 3. Add Skill Content

#### **Core Content:**
- **The AI Toolbox**: Group primary APIs by domain. Define "Preference" rules (e.g., "Use Extension X over Utility Y").
- **Compliance & Standards**: List specific RFCs, ISOs, or industry specs.
- **Immutability & Data**: Define the approach to state and data handling.

#### **Agent Onboarding (Mandatory):**
Include a specific `## 🤖 Agent Onboarding` section. This provides instructions for an AI agent on how to bootstrap this library into a *new* workspace.

### 4. Update the `README.md`

Add this section to your root `README.md` to make the skills discoverable:

```markdown
## 🤖 AI-Assisted Development
This library includes embedded, machine-readable "skills" to help AI assistants understand its APIs and best practices.

- **Discovery**: Look for `META-INF/ai-skills/*.ai-skill.md`

### 🪄 Magic Prompt for AI Assistants
If you are using an AI assistant (like Claude, Gemini, or ChatGPT) to write code with this library, paste this prompt first:

> "Scan all project dependencies for AI Skill files in `META-INF/ai-skills/` with the prefix `[library_maven_group]`. Use these to understand the API patterns, types, and governance for this library. If they are not found in the local classpath, refer to [Public Repository URL] for the source definitions."
```

---

## 🪄 Automated Bootstrapping: The Unified Magic Prompt

To initialize this standard in a repository automatically, copy the prompt below and provide it to an AI agent (like Claude or Gemini).

### **The Meta-Prompt**

**Role:** You are a Senior Staff Engineer specializing in Developer Experience (DX) and AI-Assisted Development.
**Task:** Analyze this repository and initialize the "AI Skill Publishing" and "Agent Governance" standards.

**1. Identity & Standardized Naming**
- Determine Maven `group` and `artifact` from build files.
- Path: `src/commonMain/resources/META-INF/ai-skills/[group].[artifact].ai-skill.md`

**2. Create AI-Skill Definition**
Generate the file with YAML frontmatter (`skill-id`, `spec-version: 1.0`, `type: "Aughtone AI-Skill"`) and sections for the **AI Toolbox**, **Compliance**, **Immutability**, and **🤖 Usage Rules**.

**3. Create Agent Onboarding Guide (AGENTS.md)**
Generate a guide in the root for repository contributors covering:
- Documentation Hierarchy (e.g., 5-sector hierarchy).
- Core Principles (TDD, Immutability-first).
- Interaction Rules (Plan-first, mandatory approval).
- Linkage to the Skill file created in step 2.

**4. Update README.md**
Append the `## 🤖 AI-Assisted Development` section (from Step 4 of this standard) to the end of the root `README.md`.
