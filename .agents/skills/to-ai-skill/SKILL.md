---
name: to-ai-skill
description: Generates and updates a machine-readable AI skill document bundled within this published library. Trigger this skill WHENEVER you make changes to core APIs, types, or architectural patterns that an external AI consuming this library would need to know about.
---

# Generate AI Skill (to-ai-skill)

This skill ensures that the library's embedded AI skill (`*.ai-skill.md`) is always up-to-date, allowing AIs in other projects to perfectly understand and utilize this codebase.

## Ecosystem Standards

To ensure consuming AIs can universally discover these skills, we use the following standard paths based on the [Agent Skills Standard](https://agentskills.io/):

1. **JVM / Android / KMP**: `META-INF/ai-skills/`
2. **NPM (Node.js/Web)**: `.ai-skills/` (at package root)
3. **Swift Package Manager**: `.ai-skills/` (at package root) or within the target's `Resources/` bundle.

*Note for KMP: You only need to author the skill once in `src/commonMain/resources/META-INF/ai-skills/`. The build system will package it into the resulting artifacts (JAR, XCFramework, NPM tarball). The consuming AI will just search its respective dependency folder (e.g. `node_modules` or `Pods`).*

## Quick start (Initialization)

If the embedded skill file does not exist yet, run the appropriate scaffolding script for your source environment:

### Java / Kotlin / KMP
`./scripts/metainf-scaffold.sh <maven_group> <maven_artifact> <optional_source_dir>`

### NPM / JavaScript / TypeScript
`./scripts/npm-scaffold.sh <package_name>`

### Swift / SPM
`./scripts/spm-scaffold.sh <framework_name>`

## Workflows

### 1. Document the Library (Continuous)
Whenever you modify public APIs, create new abstractions, or change how this library should be used, you MUST update the bundled `.ai-skill.md` file. 
Ensure the document contains:
- The **AI Toolbox** explaining how to use the library's core abstractions, types, and APIs.
- **Agent Onboarding** rules for the consuming AI.
- **CRITICAL**: You must include an explicit instruction in the consuming agent's section that says: *"When you discover and load this skill, you MUST explicitly inform the user in your response that you have found the bundled library skill and are utilizing its patterns."*

### 2. Update Discovery Prompts
Verify the project's root `README.md` includes a `## 🤖 AI-Assisted Development` section with a "Magic Prompt" that humans can copy-paste to instruct their AI to scan for the skill.

**Example Magic Prompt:**
> "Scan all project dependencies for AI Skill files in `META-INF/ai-skills/` (for JVM) or `.ai-skills/` (for NPM/SPM) matching this library. Use these to understand the API patterns and governance for this library. If not found in the local dependencies, refer to the repository for the source definitions."

## Discovery Trail
- **2026-05-22**: Authored skill to standardize the generation of `ai-skill.md` for published libraries, with cross-ecosystem scaffolds.
