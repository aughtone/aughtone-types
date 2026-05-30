# Developer Guide

## AI Contribution Workflow

This project utilizes AI agents for development. To maintain architectural integrity and user control, all agents must follow this workflow:

1.  **Analyze**: Understand the request and context.
2.  **Plan**: Draft a step-by-step implementation plan.
3.  **Wait**: Present the plan to the user and stop execution.
4.  **Execute**: Only after receiving explicit approval ("Proceed", "Go ahead", etc.), perform the proposed changes.
5.  **Verify**: Ensure changes meet the Acceptance Criteria (ACs).

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

### 1. SQLDelight (.sq files)
- **Mandatory Imports**: Always import `kotlin.Boolean`, `kotlin.String`, and collections if used as types.
- **Adapter Naming**: Constructor parameters MUST be named `[fieldName]Adapter`.

### 2. Ktor 3.x Networking
- **Content-Type**: Prefer `header(HttpHeaders.ContentType, ...)` over `contentType()` extension.

### 3. Serialization
- **Explicit Typing**: Provide explicit type parameters to `JsonColumnAdapter` to prevent inference failure.

### 4. Native Target Considerations
- **Persistence**: Separate logic into `Sql*` and `Noop*` LocalDataSources.

### 5. Coroutine Dispatchers
- **Prohibition**: Never use `Dispatchers.IO` in `commonMain`. Use `Dispatchers.Default`.

### 6. Test Naming (Kotlin Native Compatibility)
- **Constraint**: Do not use parentheses `()` or other special characters besides spaces and underscores in backticked test function names.
- **Reason**: Certain Native targets (e.g., iOS, Linux) will fail to compile tests with these characters in the symbol names.
