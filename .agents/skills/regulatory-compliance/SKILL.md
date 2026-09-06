---
name: regulatory-compliance
description: Manage and track regulatory requirements (PIPEDA, GDPR, etc.). Use this to add new regulations, audit compliance across ADRs and code, run pre-implementation assessments, or generate compliance mapping documents.
license: MIT
metadata:
  author: bpappin
  version: "1.2"
---

# Regulatory Compliance & Audit

## Objective

Keep the project compliant with its active privacy and AI regulations:
maintain per-regulation mapping documents, assess new designs before
implementation, audit the workspace for exposure, and flag violations
as they happen.

## Activation guard

Before executing any workflow, verify that `"compliance_enabled": true`
is set in `.agents/config/project.json` at the workspace root. If it is
absent or false, tell the user compliance tracking isn't enabled for this
project and stop.

Also read the `"active_regulations"` array from the same file. All audits
and assessments are restricted to the regulations named there. The core
principles of common regulations are catalogued in
[references/standards.md](references/standards.md).

## Where things live

- Per-regulation mapping documents: `<NAME>.md` files in the
  **Regulations** section of `docs/knowledge/` (project knowledge — an
  auditor reads these in the knowledge base; the project-docs sync keeps
  both sides current).
- Index: that section's `README.md` (the section article).
- Compliance mappings for a design use
  [references/alignment-template.md](references/alignment-template.md).
- Remediation work found by an audit goes to the tracker, never to
  local TODO lists (see Workspace Audit below).

## Core workflows

### 1. Add a regulation

When asked to "add a regulation" (e.g., "Add HIPAA"):

1. Create the mapping file `<NAME>.md` in the Regulations section of
   `docs/knowledge/` (create the section per project-docs if missing) using
   the standard table format: | Requirement | Implementation | Status |.
2. Add `<NAME>` to the `"active_regulations"` array in
   `.agents/config/project.json`.
3. Update the section's `README.md` index, then run the project-docs
   sync.

### 2. Pre-implementation assessment

Before starting work on a new ADR or feature:

1. Read the active regulations and their mapping files in the
   Regulations section of `docs/knowledge/`.
2. Analyze the proposed design: does it store cleartext PII? Does it
   have a deletion path? Is collection limited to what's necessary?
3. Generate an initial **Compliance Mapping** from
   [references/alignment-template.md](references/alignment-template.md)
   for EACH active regulation, and attach it to the ADR or design doc.

### 3. Workspace audit

When asked to "audit for compliance":

1. Search `docs/` and the source tree for PII exposure (emails, raw
   phone numbers, cleartext identifiers).
2. Review all ADRs in the Architecture Decision Records section: each
   should have a "Regulatory
   Alignment" section covering the active regulations.
3. Update the progress tables in the corresponding mapping files, then
   sync docs.
4. Route findings that require code or design changes to the tracker:
   the discovered-work off-ramp (story-workflow) for items found while a
   story is focused, or the to-issues skill for a planned remediation
   batch. Audit findings are work, not documentation.

### 4. Risk alerting

If the user proposes a change that violates an active regulation's
principles:

1. IMMEDIATELY pause.
2. Reference the specific Article or Principle being contravened, citing
   the project's mapping doc (Regulations section, `<NAME>.md`) or
   [references/standards.md](references/standards.md).
3. Propose a "Privacy by Design" alternative (e.g., "Use HMAC-SHA256
   tokens instead of raw email").

## Reference material

- [Regulatory standards catalog](references/standards.md)
- [Alignment template](references/alignment-template.md)
- Project mapping docs: the Regulations section of `docs/knowledge/`
