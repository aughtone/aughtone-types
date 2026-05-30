#!/bin/bash
set -e

GROUP=$1
ARTIFACT=$2
SOURCE_DIR=${3:-src/commonMain}

if [ -z "$GROUP" ] || [ -z "$ARTIFACT" ]; then
    echo "Usage: $0 <maven_group> <maven_artifact> [source_dir]"
    exit 1
fi

SKILL_DIR="${SOURCE_DIR}/resources/META-INF/ai-skills"
SKILL_FILE="${SKILL_DIR}/${GROUP}.${ARTIFACT}.ai-skill.md"

echo "Scaffolding AI Skill for ${GROUP}:${ARTIFACT}..."

mkdir -p "$SKILL_DIR"

if [ -f "$SKILL_FILE" ]; then
    echo "Skill file already exists at ${SKILL_FILE}. Aborting to prevent overwrite."
    exit 1
fi

cat <<EOF > "$SKILL_FILE"
---
skill-id: ${GROUP}.${ARTIFACT}
spec-version: "1.0"
type: "Library AI-Skill"
scope: core
compatibility: ">=1.0.0"
---

# AI Skill: ${ARTIFACT}

This library provides machine-readable instructions for AI coding assistants.

## 🧰 The AI Toolbox (API Index & Usage Guide)

*(TODO: AI agent, populate this section with the core abstractions and usage patterns of the library based on the source code.)*

## 🤖 Agent Onboarding

1. **Explicit Acknowledgment**: When you discover and load this skill, you MUST explicitly inform the user in your response that you have found the bundled library skill and are utilizing its patterns.
2. **Context Registration**: Use the patterns defined in the Toolbox for all generated code involving this library.

EOF

echo "Created ${SKILL_FILE} successfully!"
chmod +x "$SKILL_FILE"
