# Regenerating The Display-Name Tables

Two generated files carry locale display names, and this is how to rebuild them:

- `types/src/commonMain/kotlin/io/github/aughtone/types/locale/LocaleDisplayNameSupplement.kt` — names for pairs some platform cannot produce.
- `types/src/linuxMain/kotlin/io/github/aughtone/types/locale/LocaleDisplayNameTable.kt` — the whole matrix, for Linux, which has no system CLDR.

Do this when a coverage test fails, which is the signal that a platform's CLDR has moved, or when the shipped locale set changes. [ADR-0005](../decisions/completeness-over-consistency-for-display-names.md) explains why the tables exist and why they hold what they do.

## Why it cannot be one command

**No platform can fill its own gaps.** The JVM has no Inuktitut data, so a JVM-side tool cannot generate an Inuktitut name; Apple has some, and the browser has different ones again. The table works precisely because the platforms lack *different* pairs, which means the data has to be collected from each of them and merged.

Collecting from a platform means running code on it, which means a test. So regeneration runs a temporary test on every target and merges what they print. That is more ceremony than a Gradle task, and it is the reason there isn't one.

## The steps

**1. Add the emitter.** Create `types/src/commonTest/kotlin/io/github/aughtone/types/locale/TableEmitterTest.kt`:

```kotlin
package io.github.aughtone.types.locale

import kotlin.test.Test

class TableEmitterTest {
    @Test
    fun emitNames() {
        val all = localeResourceMap.values.sortedBy { it.languageTag }
        val displayLanguages = all.map { it.languageCode }.distinct().sorted()
        val out = StringBuilder()
        var n = 0
        for (tag in displayLanguages) {
            val displayIn = Locale.getLocale(tag) ?: continue
            for (target in all) {
                val name = target.localizedDisplayNameOrNull(displayIn = displayIn) ?: continue
                out.append("N|").append(tag).append('|').append(target.languageTag).append('|').append(name).append('\n')
                n++
            }
        }
        println("EMIT-COUNT $n")
        println(out.toString())
        println("EMIT-END")
    }
}
```

**It must call `localizedDisplayNameOrNull`, not `localizedDisplayName`.** The latter falls back to English, so the emitter would record English as though it were a translation and the table would grow by thousands of useless entries.

**Comment out the supplement lookup first.** `localizedDisplayNameOrNull` consults the supplement, so leaving it in place makes the emitter read back the table it is generating. The second run would then look complete regardless of what the platforms actually have.

**2. Run it on every target that can run:**

```bash
./gradlew :types:jvmTest :types:jsBrowserTest :types:wasmJsBrowserTest :types:iosSimulatorArm64Test --tests "*TableEmitterTest*" --rerun-tasks
```

`linuxX64` and `iosX64` cannot execute on an arm64 macOS host, and they do not need to: Linux consumes the merged table rather than contributing to it, and the iOS targets share `appleMain`.

**3. Harvest from the test XML**, at `types/build/test-results/<task>/*TableEmitter*.xml`, reading every `<system-out>` section. Each row is `N|displayLanguage|languageTag|name`.

**Compare the emitted `EMIT-COUNT` against the number of rows you parsed, and stop if they differ.** The transport is exact; a mismatch means the parser is wrong. The specific trap: **two resource entries build the same language tag** — `zh` carrying a `Hans` script, and `zh-Hans` — so 87 pairs legitimately appear twice, and anything keyed by `(displayLanguage, tag)` silently collapses them. That looks exactly like 87 dropped rows.

**4. Merge, with a precedence order.** Platforms disagree about wording for roughly 40% of the names they share, so one has to win: JVM, then Apple, then JS, then Wasm. JVM leads because it is the most complete and is already the source of the English `displayName`.

**5. Drop every name equal to the locale's English `displayName`.** CLDR answers many untranslated entries with the English word, and those add bytes to produce exactly what the English fallback already produces. Around a third of candidates go this way. Keeping them would also make `localizedDisplayNameOrNull` return English while implying a translation exists.

**6. Emit the two files.** Both hold one packed string per display language — `tag\u0001name\u0001tag\u0001name…`, sorted by tag — not one declaration per name. That matters: Kotlin/Native compiles a handful of large literals far more comfortably than tens of thousands of small ones. Keep the `size` constant in step; the tests read it.

- The **supplement** holds the merged names for pairs at least one platform lacks.
- The **Linux table** holds every pair any platform can name.

**7. Remove the emitter, restore the supplement lookup, and run `./gradlew check`.**

**8. Update the coverage tests.** `JvmCoverageTest` and `AppleCoverageTest` pin two numbers each: the platform's own gap count, and how many pairs remain untranslated after the supplement. They will fail with the new values in the message. Change them only once you have looked at what moved — that check is the whole point of them.

## What to expect

Roughly, as of 2026-09-28: each platform lacks 1,084–1,266 pairs of 17,889; between them they can name all but 21; the supplement holds about 1,685 names after trimming; and 801 pairs remain untranslated on JVM and Apple, 23 on JS and Wasm.

Treat these as the order of magnitude rather than targets. If a run produces something far away from them — a supplement of 12 entries, or of 12,000 — something upstream is wrong, most likely the emitter reading the supplement back or falling back to English.
