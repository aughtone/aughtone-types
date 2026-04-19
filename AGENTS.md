# Project Development Guide

This document is the master instruction set for AI agents contributing to this repository.

## 1. Documentation Governance
This repository follows the **5-sector hierarchy**. All knowledge must be dispersed into:
- 📐 Architecture (docs/ARCH.md)
- 🧠 Functional Specifications (docs/SPEC.md)
- 🎨 Design & UI (docs/DESIGN.md)
- 📋 Acceptance Criteria (docs/ACs/README.md)
- 📖 Developer Guide (docs/DEVELOPER.md)

## 2. Governance Standards
All AI agents MUST adhere to these skills from `docs/standards/`:
- **Repository Structure**: Context mapping rules.
- **Quality Engineering**: RAVL 8-pillar methodology.
- **KMP Development**: Build stability patterns.
- **ViewModel & UDF Governance**: Redux purity and synchronization rules.

## 3. AI Interaction Guidelines
- **Verification First**: Check the corresponding AcceptanceCriteria.md before implementation.
- **Mandatory Approval**: ALWAYS present a detailed implementation plan and WAIT for explicit user approval before executing any code changes or tool calls that modify the repository state.
- **Update Docs**: Intelligently disperse context into the appropriate sector.
