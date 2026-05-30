---
name: manage-docs
description: Manage the 14-sector documentation hierarchy, perform semantic migrations of legacy documentation, and provide proactive recording of architectural and functional decisions. Use when commanded to "update docs", "migrate docs", or when project standards change.
---

# Documentation Management

## The 14-Sector Hierarchy

All knowledge must be dispersed into the appropriate specialized directory to ensure high-quality AI context mapping and prevent documentation bloat. Technical specifications MUST live in the project repository (docs-as-code) rather than external wikis to ensure atomic versioning and peer review.

| Sector | Path | Focus |
| :--- | :--- | :--- |
| **Mandates** | `docs/mandates/` | Global "How" and "What" (`ARCH.md`, `SPEC.md`). |
| **Templates** | `docs/mandates/templates/` | Blueprints for different document archetypes. |
| **Design** | `DESIGN.md` | The canonical design specification and tokens (Root). **Domain: Designer Persona.** |
| **Mockups** | `docs/design/` | Individual feature designs and mockups. **Domain: Designer Persona.** |
| **Stories** | `docs/prd/` | Feature requirements and user stories (**PRDs**). Linked to **AC**. |
| **Criteria** | `docs/ac/` | Testable success outcomes (**ACs**). Linked to **PRD**. |
| **Discovery** | `docs/discovery/` | The "Why" and "Due Diligence" (DD). Vendor research and cost analysis. |
| **Roadmap** | `docs/gap/` | The "Future" (GAPs). Transient trackers for missing capabilities. |
| **Regulations**| `docs/regulations/` | Compliance mappings (PIPEDA, GDPR, Law 25). |
| **Guides** | `docs/guides/` | The "Environment" (Onboarding and Installation). |
| **Reference** | `docs/reference/` | Domain-specific knowledge and IDV methods. |
| **Research** | `docs/research/` | Deep-dive R&D logs (RAD). |
| **Decisions** | `docs/adr/` | Architectural Decision Records (ADR). |
| **Vendors** | `docs/vendors/` | Third-party vendor specifications, integration manuals, and schemas. |

## Workflows

### 1. Document Metadata (YAML Frontmatter)
All specification documents (PRDs, ACs, and GAPs) MUST lead with a YAML frontmatter block for system metadata, bounded by `---` at the top of the file:
```yaml
---
id: #NEW            # Stays '#NEW' until synced with YouTrack, which updates it to the YouTrack key (e.g., EVO-85)
status: TODO        # TODO, WIP, DONE, OBSOLETE
type: PRD           # PRD, AC, Gap, Design
priority: High      # Optional: High, Medium, Low
parent: EVO-34      # Optional: Link to YouTrack parent Epic key
---
```

### 2. Polymorphic Layouts (Archetypes)
When creating new documentation (PRDs, GAPs, Designs), the agent must select the correct layout based on the active persona:
1.  **Detection**: Check the active persona's `layout_preference`.
2.  **Selection**: 
    - Use `docs/mandates/templates/prd-game-design.md` if layout is `game-design`.
    - Use `docs/mandates/templates/prd-standard.md` if layout is `standard` or no persona is active.
3.  **Customization**: If the user asks for a specific layout not in their persona, honor the request but specify it in the YAML frontmatter (e.g., `layout: game-design`).

### 3. Bootstrapping DESIGN.md
If the root `DESIGN.md` is missing, the agent MUST offer to bootstrap it using the [Google DESIGN.md specification](https://stitch.withgoogle.com/docs/design-md/specification/). It should incorporate project-specific UI rules found in any legacy design documentation.

### 4. Spec Pairing (PRD + AC)
When a feature is defined, it is recorded as a pair of files using a shared ID:
1.  **Requirement**: `docs/prd/[ID]-slug.md`. Contains user stories and implementation decisions.
2.  **Verification**: `docs/ac/[ID]-slug.md`. Contains testable Gherkin-style scenarios.
3.  **Note**: If a task is purely informational or non-testable, omit the AC and mark the PRD's Verification section as "N/A".

### 5. Migration Workflow ("migrate docs")
Use this to ingest legacy documentation or clean up an unorganized repository.
1.  **Scan**: Search the project root and folders like `wiki/`, `notes/`, or `architecture/` for `.md` and `.txt` files.
2.  **Classify**: Semantically analyze the content to determine its target sector (e.g., "Given/When/Then" ➔ `docs/ac/`).
3.  **Report**: Present a proposal table of source files and their proposed structured paths.
4.  **Execute**: Move files, update cross-references, and initialize missing `README.md` indexes.
5.  **Refactor**: 
    - Convert any legacy inline metadata (e.g., `**ID:**`, `**Status:**`) into the YAML frontmatter block format at the top of the file.
    - Rename any existing `docs/issue/` directory to `docs/prd/`.

### 5. Agentic Proactivity (Soft Offer)
Monitor the conversation for "Resolution Signals" and offer to record them:
- **The Trade-off Signal**: If 2+ options were compared and one chosen ➔ Offer a **DD** (Discovery) or **ADR** (Decision).
- **The Feature Signal**: If "how it should work" is mapped ➔ Offer a **PRD** and **AC** pair.
- **The Roadmap Signal**: If a task is important but out-of-scope ➔ Offer a **GAP**.

### 6. Due Diligence (DD) Records
Before making significant architectural choices, create a DD record in `docs/discovery/`.
- **Discovery Trail**: ALWAYS include a section summarizing the back-and-forth discussion and AI reasoning that led to the findings.

### 7. Architectural Decision Records (ADR)
Record hard-to-reverse or surprising technical decisions in `docs/adr/`.
- **Format**: `ADR-slug.md`. Keep it concise (1-3 sentences for context/decision).
- **Linking**: Cross-link to related **DD** or **PRD** records.

## Discovery Trail
- **2026-05-19**: Unified hierarchy to a 13-Sector model to prevent numbering confusion and explicitly mapped Architectural Decision Records to `docs/adr/` to fix a contradictory rule.
- **2026-05-18**: Integrated `DESIGN.md` root specification into the 6-sector hierarchy. Added bootstrapping workflow to ensure `DESIGN.md` is created following the Google spec. Defined `DESIGN.md` and `docs/design/` as the exclusive domain of the **Designer Persona**.
- **2026-05-18 (v2)**: Introduced Polymorphic Layouts (Documentation Archetypes). Added `docs/mandates/templates/` directory to house blueprints for `standard` and `game-design` layouts. Linked layout selection to the user's active persona preference.
