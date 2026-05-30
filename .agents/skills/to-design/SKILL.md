---
name: to-design
description: Formalizes the design-capturing process as a structured agent capability. Use when the user wants to design a new UI component, feature, or screen to ensure visual samples are generated and properly registered.
---

# Design Process (to-design)

This skill formalizes the design-capturing process, ensuring that every UI component has a documented design progression, generated visual samples, and is registered in the appropriate design index. 

The `to-design` process starts with a tool delegation check, and then strictly follows two phases: **1. Discovery Phase** and **2. Transition Phase**.

## Tool Delegation & Pre-flight Checks

Before starting any design task, the agent MUST perform a pre-flight check to discover available design capabilities:

1. **Capability Discovery**:
   - Check if **Figma MCP** (via `figma_mcp` or similar tools) or **Stitch MCP** (via `stitch` server tools) are registered and available.
   - Check if there is an active/specialized **Design AI** subagent or model available.
2. **Execution & Delegation Strategy**:
   - **Stitch / Figma MCP available**: The agent MUST NOT try to design from scratch manually. Propose delegating to the available MCP server. If both are available, ask the user if they want to delegate to Stitch, Figma, or use both in tandem.
   - **Specialized Design AI available**: Delegate the design generation/mockup tasks to that specialized agent or model.
   - **Standalone Mode (no design tools/AIs available)**:
     - The agent MUST explicitly notify the user: *"I am currently running in standalone design mode. For optimal results, you can integrate design tools such as Figma MCP or Stitch."*
     - Suggest setting up these tool integrations to streamline UI and component design.
3. **Explicit Status Declaration**:
   - In all cases, the agent MUST clearly state to the user what tool, subagent, or mode is currently being used for the design generation (e.g. "I am delegating this screen generation to Stitch...").

## 1. Discovery Phase (The "What" & "How")

At this level, the focus is purely on structure and visuals, separating the design artifacts by component name so they are easy to find.

### A. Sample Generation
The AI MUST generate UI samples/mockups (using the appropriate image generation tools) for every feature/component being designed. These samples must be saved in the corresponding `resources/` directory.

### B. Design Record
Document the design options considered, the decisions made, and the final state in a specific markdown file for the component.

### C. Relative Links
**STRICT RULE:** All links to media/samples in markdown files MUST be relative paths (e.g., `![Alt Text](resources/sample.png)`). Do not use absolute filesystem paths.

### D. Component Scope & Registration
Determining where the design lives depends on its scope:

- **Feature-Specific Components**: 
  - Save the design record in: `docs/design/<feature>/<ComponentName>.md`
  - Save samples in: `docs/design/<feature>/resources/`
  - Register in: The feature's index at `docs/design/<feature>/DESIGN.md`

- **Generic/Shared Components (Cross-Feature)**: 
  - Save the design record in: `docs/design/components/<ComponentName>.md`
  - Save samples in: `docs/design/components/resources/`
  - Register in: The master index at `docs/DESIGN.md`

## 2. Transition Phase (Evaluating Scale & Next Steps)

Once a design is finalized and approved by the user, the agent MUST evaluate the scale of the design to determine the next skill in the pipeline.

### Macro Design (Screens / Complex Organisms / New Features)
Does this design introduce a major new capability, screen, or complex organism?
- **Action**: Warrants a full feature specification. 
- **Transition**: The agent MUST transition to the `to-prd` skill to generate a formal `docs/prd/` and `docs/ac/` pair.

### Micro Design (Atoms / Molecules / Simple Tweaks)
Does this design just tweak an existing flow, add a new state, or introduce a simple reusable molecule/atom?
- **Action**: Does NOT warrant a full PRD.
- **Transition**: The agent MUST transition to updating an *existing* feature's PRD/AC (adding the new design as an Acceptance Criteria), or if it is purely a non-functional UI tweak, proceed directly to creating an `implementation_plan.md` or invoking the `to-issues` skill.
