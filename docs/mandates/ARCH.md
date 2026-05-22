# Architecture Guidelines

## 📦 Distribution & Publishing

Aughtone Types is a Kotlin Multiplatform library published to **Maven Central** using the `com.vanniktech.maven-publish` plugin.

### Coordinates
- **Group**: `io.github.aughtone`
- **Artifact**: `types`
- **Version**: Managed via `libs.versions.versionName`

### Infrastructure
- **Plugin**: `com.vanniktech.maven-publish`
- **Target**: Maven Central (OSSRH)
- **Automatic Release**: Enabled (`automaticRelease = true`)
- **Signing**: Mandatory GPG signing (can be bypassed with `-Pskip-signing` for local builds).

## 🏗️ ViewModel & UDF Governance

### 1. State Principles
- **Immutability**: State MUST be immutable. Use `.copy()`.
- **Static Structure**: Use a single data class. NO sealed classes for state models.

### 2. Reducer & Effect Categorization
- `.noEffect()`: Pure state transition.
- `.withEffect { ... }`: Side effect independent of new state.
- `.withFullEffect { newState, dispatch -> ... }`: Side effect requiring new state or subsequent actions.

### 3. Command & Sync Strategy
- **Suspend Commands**: Isolation from state, no internal `reduxStore.state` access.
- **Trigger Strategy**: Commands called from within effect environment.
