# Per-language references

`SKILL.md` is written for every consumer. What differs between consumers is not what the library is for but **how it is called**, and that differs by language: a Kotlin example handed to an agent writing Swift produces code that does not compile. So those differences go in one reference file per consumer language, beside `SKILL.md`:

| file | for consumers writing |
|---|---|
| `references/kotlin.md` | Kotlin — JVM, Android, multiplatform common |
| `references/java.md` | Java, where the Kotlin-facing API differs meaningfully |
| `references/swift.md` | Swift, through the Objective-C export or a shim |
| `references/objc.md` | Objective-C directly |
| `references/javascript.md` | JavaScript and TypeScript |

Link each one from `SKILL.md` with a relative path, so an agent finds it: `[Calling it from Swift](references/swift.md)`.

## Which languages consume this library

Read it from the build rather than guessing:

- **An Apple framework or XCFramework** (`binaries.framework`, an XCFramework task, a Swift package or podspec) means Swift consumers, and usually `swift.md`.
- **A JavaScript target that produces a library** (`binaries.library()`, `generateTypeScriptDefinitions()`, a published npm package) means JavaScript consumers, and `javascript.md`. A JS target that only builds an executable or a demo does not.
- **JVM and Android targets** are Kotlin or Java consumers. `SKILL.md` usually covers Kotlin already; add `java.md` only where the Java-facing API differs.

Ship only the languages the library is actually consumed in. When unsure whether a target is published for outside callers, ask the maintainer.

## What goes in a reference

What a consumer in that language **cannot discover from the API surface**. For a Kotlin Multiplatform library called from Swift: which suspend functions arrived as completion handlers and which as `async`; what the synthetic class wrapping top-level functions is called; which default arguments did not survive the export; where generics eroded; which parts are effectively unusable from Swift and what to use instead.

Interop behaviour changes between toolchain releases. A reference that asserts specifics should say what it was measured against, in `metadata.measured-against` or in the reference itself.

## A Swift consumer who never sees the sources jar

A Swift developer who gets the library as an XCFramework never receives the sources jar, so never receives this skill or its references. What does reach them is **the doc comment on each exported declaration**: Kotlin writes it into the framework's Objective-C header. Put the traps a Swift caller must know in those doc comments too — tell the maintainer which ones, since changing the code is outside the skill's directory.
