# Architecture Guidelines

SPEC · 2026-05-22
Keywords: how is this library published, maven central coordinates, gpg
          signing, skip signing for a local build, vanniktech maven publish,
          automatic release, where does the version number come from

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

## 🧱 Shape of the library

- **Zero runtime dependencies beyond `kotlinx.serialization`.** A consumer
  adds these types without inheriting a framework. Anything that would drag
  in a UI, networking or persistence dependency does not belong here — see
  [ADR-0003](../architecture-decision-records/localized-display-names-via-platform-cldr.md)
  for a decision that turned on exactly this constraint.
- **`commonMain` first.** Platform code exists only where a capability is
  genuinely platform-owned, expressed as `expect`/`actual`.
- **Immutable values.** Every type here is a value type; operations return
  new instances rather than mutating.

The release procedure that goes with this lives in the
[Developer Guide](../developer-guides/developer-guide.md).
