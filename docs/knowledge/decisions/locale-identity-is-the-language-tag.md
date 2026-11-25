# Locale Identity Is The Language Tag, And Neither Locale Nor Currency Is A Data Class

ADR-0007 · 2026-11-24 · Status: accepted
Keywords: why is Locale not a data class, Locale has no copy, Currency has no copy, Locale equals
          ignores displayName, EN-us normalized to en-US, Locale companion invoke, Swift
          Locale.Companion.shared.invoke, decoding a payload with uppercase codes, languageTag in
          JSON, Currency code uppercased

## Context

`Locale` and `Currency` were data classes. `Currency` already overrode equality to compare the ISO 4217 code alone, on the principle that equality is what the governing standard says identifies a value — the URI types follow it too. `Locale` did not: data-class equality compared all five fields, including the display name, so two locales for one tag named by different sources were unequal (#48), and became more so once `displayName` started following the reader's language ([ADR-0006](display-names-in-the-readers-language.md)).

Two other things a data class provides were wrong for both types:

- **`copy` builds inconsistent values.** `enUs.copy(regionCode = "FR")` is `en-FR` named "English (United States)"; a copied `Currency` with a new code keeps another currency's name and symbol. Kotlin does not let a data class override or deprecate `copy`.
- **`componentN`** destructures four codes and a name, or seven currency fields, positionally — meaningless.

Constraints that were not negotiable: kotlinx.serialization without a custom serializer (a project rule, with exceptions only when documented), and an unchanged JSON shape, including decoding payloads written by earlier releases.

## Decision

**A `Locale` is identified by its BCP 47 language tag, and a `Currency` by its ISO 4217 code; neither is a data class.** Both have a private constructor and a companion `invoke` taking the old constructor's arguments, so `Locale("en", "US", displayName = …)` still compiles. Codes are case-normalized however a value is made, decoding included: language and variant lowercase, script title case, region uppercase, currency codes uppercase; a blank region, script or variant is absent. `equals` and `hashCode` use the tag or code, and `toString` returns it.

Normalization without a custom serializer works by storing the codes as written in private serialized properties and exposing `@Transient` public properties initialized from them, which the generated deserializer also runs. `languageTag`, which earlier releases serialized, is still read and written under its old key and ignored on input.

## Alternatives that lost

- **Keep the data classes and override `equals` and `hashCode`**, as `Currency` already did. Non-breaking, and it fixed equality, but `copy` stays and cannot be removed or deprecated while the class is a data class.
- **An internal constructor**, so only the library builds locales from its own data. Rejected: consumers legitimately build their own.
- **A custom serializer** to normalize on decode. Rejected under the project rule, since the `@Transient` pattern achieves it.
- **Converting the other value types too.** Rejected: most value types here are rightly data classes, and each should be decided on its own merits rather than for symmetry.

## Consequences

**Harder.** It is a source and binary break: `copy` and `componentN` are gone, and Java and Swift callers reach construction through the companion (`Locale.Companion.shared.invoke(...)` in Swift). It shipped in a minor release, 4.3.0, deliberately — the consumer base was small enough that waiting for a major version cost more than the break. Behaviour changed too: two locales that differ only in name are now equal, and a decoded payload with `"EN"` reads back as `en`.

**Easier.** A `Locale` or `Currency` from any source is one value in a `Set` or as a map key. An inconsistent value cannot be made by copying. The JSON shape is unchanged, and tests pin it.

**A generated `copy` is no longer a hidden throw.** On types that validate themselves, Objective-C's `doCopy` re-runs validation and cannot carry `@Throws`; for these two types the question no longer arises.

## Corrections

- 2026-11-24: Consequences said this is "a source and binary break" and that the old constructor is gone. As first shipped it was, and a library compiled against 4.2 failed at run time on `new Locale(...)`. Before release the constructors were made `@PublishedApi internal` instead of private, which keeps their 4.2 JVM signatures public while Kotlin source still goes through `invoke`, and the pre-`LanguageDisplay` `localizedDisplayName` signatures were kept as hidden overloads. Only compiled calls to `copy` and `componentN` break. Verified by running the published `format-readable` 4.3.0, built against 4.2.0, and a Java caller compiled against 4.2.0, against the new jar.
