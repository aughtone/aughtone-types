# Project Development Guide

This document is the master instruction set for AI agents contributing to this repository.

## 1. Documentation Governance
This repository follows the **5-sector hierarchy**. All knowledge must be dispersed into:
- 📐 Architecture (docs/ARCH.md)
- 🧠 Functional Specifications (docs/SPEC.md)
- 🎨 Design & UI (docs/DESIGN.md)
- 📋 Acceptance Criteria (docs/ACs/README.md)
- 📖 Developer Guide (docs/DEVELOPER.md)
- 📜 Changelog (docs/CHANGELOG.md)

## 2. Governance Standards
All AI agents MUST adhere to these skills from `docs/standards/`:
- **Repository Structure**: Context mapping rules.
- **Quality Engineering**: RAVL 8-pillar methodology.
- **KMP Development**: Build stability patterns.
- **ViewModel & UDF Governance**: Redux purity and synchronization rules.

## 3. Core Development Principles
- **Test-Driven Development (TDD)**: Whenever feasible, write a failing test before implementation.
- **Kotlin Multiplatform**: All code must be multiplatform-first. Be mindful of source set placement (`commonMain`, `androidMain`, etc.).
- **Immutability & Safety**: Maintain data structure immutability and handle serialization (`kotlinx.serialization`) correctly.
- **Consistency**: Adhere to existing patterns; consistency outweighs personal preference.
- **Type Preference (Avoid Shadowing)**: 
    - **`Locale`**: Prefer `io.github.aughtone.types.locale.Locale` when working in `commonMain` where cross-platform consistency is required. Be aware of potential shadowing by `java.util.Locale` or Compose-specific locales and use the fully qualified name if necessary to resolve ambiguity.
    - **`Currency`**: Prefer `io.github.aughtone.types.financial.Currency`.

## 4. AI Interaction Guidelines
- **Verification First**: Check the corresponding AcceptanceCriteria.md before implementation.
- **Mandatory Approval**: ALWAYS present a detailed implementation plan and WAIT for explicit user approval before executing any code changes or tool calls that modify the repository state.
- **Embedded Skills**: This library uses machine-readable skills in `META-INF/ai-skills/io.github.aughtone.types.ai-skill.md`. Always adhere to the patterns defined there.
- **Update Docs**: Intelligently disperse context into the appropriate sector.
- **Project Gaps**: Track identified technical debt or missing features in [GAPS.md](GAPS.md) using the `gap:` prefix.
