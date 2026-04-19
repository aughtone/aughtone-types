# Architecture Guidelines

## 📦 Distribution & Publishing

AOTypes is a Kotlin Multiplatform library published to **Maven Central** using the `com.vanniktech.maven-publish` plugin.

### Coordinates
- **Group**: `io.github.aughtone`
- **Artifact**: `types`
- **Version**: Managed via `libs.versions.versionName`

### Infrastructure
- **Plugin**: `com.vanniktech.maven-publish`
- **Target**: Maven Central (OSSRH)
- **Automatic Release**: Enabled (`automaticRelease = true`)
- **Signing**: Mandatory GPG signing (can be bypassed with `-Pskip-signing` for local builds).
