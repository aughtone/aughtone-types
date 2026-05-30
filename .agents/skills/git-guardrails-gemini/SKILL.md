---
name: git-guardrails-gemini
description: Codifies the strict Git safety rules for Gemini CLI. Use this to remind the agent of its limitations regarding destructive Git operations.
---

# Gemini Git Guardrails

This skill defines the non-negotiable safety rules for Gemini regarding Git operations in this workspace.

## 🛑 STRICT PROHIBITIONS

Gemini is **NEVER** allowed to execute the following commands or any variations of them:

- `git commit` (and all variants like `git commit -m`)
- `git push` (all variants including `--force`)
- `git reset` (especially `--hard`)
- `git clean` (all variants)
- `git branch -D` or `git branch -d`
- `git checkout .` or `git restore .` (destructive file restores)
- `git tag` (pushing or deleting tags)
- `git merge` or `git rebase`

## ✅ PERMITTED OPERATIONS

Gemini is **ONLY** allowed to perform the following Git operations:

- `git status`: To check the current state of the workspace.
- `git diff`: To review changes made.
- `git add .`: To stage changes for the user's review.
- `git log`: To review commit history.

## Workflow Mandate

1.  **Stage Changes**: After making file modifications, Gemini should use `git status` and `git diff` to verify the work.
2.  **Suggest Commit**: Gemini MUST suggest a commit message for the staged changes. This can be provided as plain text or via a specialized commit suggestion tool if available in the current environment.
3.  **User Execution**: The user is the **only** one authorized to execute the actual `git commit` and `git push` commands.

## 🛡️ Optional Safety Script

A bash script is provided for environments that support execution hooks or for manual parity with other agents:
[block-dangerous-git-gemini.sh](scripts/block-dangerous-git-gemini.sh)

### Why this script is optional
Unlike some other agents (e.g., Claude Code), Gemini primarily operates on **Instruction-based Safety**. Because the model interprets and follows the mandates in `AGENTS.md` and this skill directly, a hard execution-level block is not strictly required. 

However, this script is included to:
1.  Provide parity with other agent safety workflows.
2.  Support future Gemini-based CLIs that may implement native pre-execution hooks.
3.  Serve as an explicit "sanity check" tool if required.
