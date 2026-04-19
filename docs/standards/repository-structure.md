# Repository Structure (repository-structure.md)
---
name: repository-structure
description: Defines the 5-sector documentation hierarchy and AI context mapping rules.
---

## The 5 Sectors
1. 📐 Architecture Guidelines (docs/ARCH.md)
2. 🧠 Functional Specifications (docs/SPEC.md)
3. 🎨 Design & UI Rules (docs/DESIGN.md)
4. 📋 Acceptance Criteria (docs/ACs/)
5. 📖 Developer Guide (docs/DEVELOPER.md)

## AI Context Mapping Rules
1. Before writing logic: Reference docs/SPEC.md AND docs/ACs/.
2. Before establishing repo definitions: Reference docs/ARCH.md.
3. When rendering UI: Reference docs/DESIGN.md.
4. Goal-Oriented Design: Formalize User Stories (STORY-ID) in DESIGN.md first.
5. Update Docs: Intelligently disperse context into the specific specialized file.
6. Interaction Protocol: ALWAYS present an implementation plan and wait for explicit user approval before performing any modifications.
