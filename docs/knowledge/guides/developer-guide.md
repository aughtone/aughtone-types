# Developer Guide

GUIDE · 2026-05-22
Keywords: how do I publish a release, gradle publish to maven central, skip
          gpg signing locally, test name compilation failure on iOS, illegal
          character in test symbol, Dispatchers.IO in commonMain, getting
          started

## 🚀 Publishing to Maven Central

This project uses the `vanniktech.mavenPublish` plugin for automated deployment.

### Release Steps
1.  **Version Update**: Increment the version in `gradle.properties` or wherever `libs.versions.versionName` is defined.
2.  **Clean Build**: Run `./gradlew clean build` to ensure all targets compile.
3.  **Publish**:
    ```bash
    ./gradlew publishAllPublicationsToMavenCentralRepository
    ```
    *Note: If you need to skip GPG signing (e.g., for local testing), use `-Pskip-signing`.*

### Automation
The `mavenPublishing` block in `types/build.gradle.kts` is configured with `automaticRelease = true`, meaning once the artifacts are uploaded and validated, they will be automatically released to Maven Central without manual staging intervention.

## 💻 Kotlin Multiplatform (KMP) Development Standards

### 1. Test Naming (Kotlin Native Compatibility)
- **Constraint**: Do not use parentheses `()` or other special characters besides spaces and underscores in backticked test function names.
- **Reason**: Certain Native targets (e.g., iOS, Linux) will fail to compile tests with these characters in the symbol names.

### 2. Coroutine Dispatchers
- **Prohibition**: Never use `Dispatchers.IO` in `commonMain`. Use `Dispatchers.Default`.

### 3. Serialization
- **Explicit `@SerialName`**: Every `@Serializable` member carries an explicit
  `@SerialName`; the wire key is never left to the implicit Kotlin name.
  Exceptions must be documented in-code with the reason.
- **No custom serializers.** Same exception rule.

### 4. Enums
- Never use an enum's `ordinal` for persistence, serialization, comparison or
  wire order. Members are PascalCase.

## How agents work here

Agent instructions are not documentation — they live in [`AGENTS.md`](../../../AGENTS.md)
at the repo root, and how work flows through the tracker is
[`docs/WORKFLOW.md`](../../WORKFLOW.md).
