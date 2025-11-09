# AI Development Guidelines for AOTypes

## Core Principles
- **Test-Driven Development (TDD) is a priority.** Whenever feasible, please write a failing test before writing the implementation code. All new functionality should be accompanied by tests.
- **Kotlin is the required language.** All new code, including tests and build scripts, should be written in Kotlin.
- **Maintain Consistency.** Adhere to the existing code style, formatting, and architectural patterns found in the project. Consistency is more important than personal preference.

## Project-Specific Notes
- This is a Kotlin Multiplatform project. Be mindful of code placement in the correct source sets (e.g., `commonMain`, `commonTest`, `androidMain`).
- When adding or modifying data types, ensure that serialization (`kotlinx.serialization`) is handled correctly if the type is intended to be serialized.

## General Instructions
- Be concise in your explanations.
- When modifying files, prefer targeted edits over rewriting the entire file, unless a full rewrite is necessary for a refactor.
