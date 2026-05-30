---
name: regulatory-compliance
description: Manage and track regulatory requirements (PIPEDA, GDPR, etc.). Use this to add new regulations, audit compliance across ADRs/Code, or generate mapping documents.
---

# Skill: Regulatory Compliance & Audit

## Objective
Ensure the Coldwater platform adheres to its "Structural Blindness" mandate and remains compliant with international privacy and AI regulations.

## Activation Guard
Before executing any workflow, the agent MUST verify that `"compliance_enabled": true` is set in the `.agents/config/project.json` file (falling back to legacy `.config/project.json` if needed) in the workspace root. 

The agent MUST also read the `"active_regulations"` array. All audits and assessments must be restricted to the names listed in this array.

## Core Workflows

### 1. Add New Regulation
When asked to "Add a regulation" (e.g., "Add HIPAA"):
1.  Create a new mapping file: `docs/regulations/<NAME>.md`.
2.  Use the standard table format: | Requirement | Implementation | Status |.
3.  Add the `<NAME>` to the `"active_regulations"` array in the project configuration file (`.agents/config/project.json` or legacy `.config/project.json`).
4.  Update the [README.md](../../../docs/regulations/README.md) index.

### 2. Pre-Implementation Assessment
Before starting work on a new ADR or Feature:
1.  Read the active regulations defined in the project configuration file and their corresponding files in [](../../../docs/regulations/README.md).
2.  Analyze the proposed design: Does it store cleartext PII? Does it have a deletion path?
3.  Generate an initial **Compliance Mapping** using the [alignment-template.md](references/alignment-template.md) for EACH active regulation.

### 3. Workspace Audit
When asked to "Audit for compliance":
1.  Search `docs/` and `server/` for PII exposure (emails, raw phone numbers).
2.  Review all ADRs: Ensure each has a "Regulatory Alignment" section for all active regulations.
3.  Update the progress tables in the corresponding files (e.g., `docs/regulations/PIPEDA.md`).

### 4. Risk Alerting
If a user proposes a change that violates "Structural Blindness":
1.  IMMEDIATELY pause.
2.  Reference the specific Article or Principle in the [regulations docs](../../../docs/regulations/README.md) that is being contravened.
3.  Propose a "Privacy by Design" alternative (e.g., "Use HMAC-SHA256 tokens instead of raw email").

## Reference Material
- [Global Compliance Strategy](../../../docs/regulations/COMPLIANCE.md)
- [Alignment Template](references/alignment-template.md)
