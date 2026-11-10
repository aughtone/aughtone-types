# Regenerating The Display-Name Tables

Two generated files carry locale display names, and this is how to rebuild them:

- `types/src/commonMain/kotlin/io/github/aughtone/types/locale/LocaleDisplayNameSupplement.kt` — names for pairs some platform cannot produce.
- `types/src/linuxMain/kotlin/io/github/aughtone/types/locale/LocaleDisplayNameTable.kt` — the whole matrix, for Linux, which has no system CLDR.

Do this when a coverage test fails, which is the signal that a platform's CLDR has moved, or when the shipped locale set changes. [ADR-0005](../decisions/completeness-over-consistency-for-display-names.md) explains why the tables exist and why they hold what they do.

## Why it cannot be one command

**Most names come from the pinned CLDR release, and the rest from the platforms.** Names are composed from `cldr-localenames-full` at the version pinned in [docs/reference/README.md](../../reference/README.md): language, script and territory names from the display language's own files, joined with its `localeDisplayPattern`. Only a pair that release cannot name is taken from a platform.

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
./gradlew :types:jvmTest :types:jsBrowserTest :types:wasmJsBrowserTest :types:iosSimulatorArm64Test --tests "*TableEmitterTest*" --rerun-tasks --continue
```

**`--continue` is required.** With the supplement lookup commented out, other display-name tests on the JVM and JS fail, and without it the first failing task stops the targets after it from running at all.

`linuxX64` and `iosX64` cannot execute on an arm64 macOS host, and they do not need to: Linux consumes the merged table rather than contributing to it, and the iOS targets share `appleMain`.

**3. Harvest from the test XML**, at `types/build/test-results/<task>/*TableEmitter*.xml`, reading every `<system-out>` section. Each row is `N|displayLanguage|languageTag|name`. **The browser targets prefix the first line of each `println` with `[log] `**, so strip it, or the first row of the batch is lost and the count check below fails by one.

**Compare the emitted `EMIT-COUNT` against the number of rows you parsed, and stop if they differ.** The transport is exact; a mismatch means the parser is wrong. The specific trap: **two resource entries build the same language tag** — `zh` carrying a `Hans` script, and `zh-Hans` — so 87 pairs legitimately appear twice, and anything keyed by `(displayLanguage, tag)` silently collapses them. That looks exactly like 87 dropped rows.

**4. Compose from CLDR, then fill from the platforms.** For every pair, compose the name from the pinned CLDR package. Where it lacks the language, script or territory name, take the platform's name, in the order JVM, then Apple, then JS, then Wasm: platforms disagree about wording for roughly 40% of the names they share, so one has to win, and the JVM is the most complete. **Reject three kinds of platform answer**, all of which look like names:

- **an echo of the codes**, such as `zh (Hans)` or `pa (Guru)`, which Apple returns for some pairs it cannot name;
- **the platform's English fallback, in a display language it has no data for.** A browser asked for names in Mohawk answers in English, so the same name turns up for one tag across every such display language. Keep a name in a display language with no CLDR data only when it is the only display language that platform gives it in and it is not that platform's English name. That is how Apple's own name for Mohawk in Mohawk survives while its English fallbacks do not.
- **an English name in a display language that does have CLDR data, but not enough.** Asked for `de-AT` in Tibetan, a platform answers "Austrian German". Reject a platform-filled name whose parts — the language, and each script or region in parentheses — are all CLDR English names, unless the display language's own CLDR data gives the same string. Filipino CLDR genuinely uses English names and `tl` is served from `fil` data, so the `tl` names that look English stay. A name only partly English, such as "Cree (Kanada)", is what the platform returns natively and is kept. (#45)

**When only adding locales, leave existing pairs alone.** Platform data drifts between runs, so a full regeneration rewrites a handful of established platform-filled names for no reason. Recompose everything, check that the existing pairs come out unchanged apart from that drift, and then ship the existing entries as they were plus the new pairs.

**5. Drop every name equal to the locale's English `displayName`.** CLDR answers many untranslated entries with the English word, and those add bytes to produce exactly what the English fallback already produces. Around a third of candidates go this way. Keeping them would also make `localizedDisplayNameOrNull` return English while implying a translation exists.

**6. Emit the two files.** Both hold one packed string per display language — `tag\u0001name\u0001tag\u0001name…`, sorted by tag — not one declaration per name. That matters: Kotlin/Native compiles a handful of large literals far more comfortably than tens of thousands of small ones.

**Emit `size` as a `by lazy` count over the payload, never as a literal.** Both files derive it today, and it must stay derived. A literal written by the generator went stale in both files at once — each declared one number while its own header comment declared another — and nothing caught it, because at the time nothing read either. A derived count cannot disagree with what it counts.

- The **supplement** holds the names for pairs at least one platform's raw harvest lacks.
- The **Linux table** holds every pair any platform can name.

**7. Remove the emitter, restore the supplement lookup, and run `./gradlew check`.**

**8. Update the baselines.** `JvmCoverageTest` and `AppleCoverageTest` each hold two baselines — the platform's own gap count, and how many pairs remain untranslated after the supplement — and `LocaleDisplayNameSupplementTest` pins the supplement's derived size and the display-language count exactly. The coverage baselines are **tolerant**, within 5%, because they measure host data: a different OS, simulator runtime or JDK reports slightly different counts, and an exact pin failed a release on CI over a drift of six pairs. Every run prints the actual counts, so watch the logs for drift. Change a baseline only once you have looked at what moved.

The Linux table's `size` is **not** pinned by any test, because there is no `linuxTest` source set and `linuxX64` cannot execute on an arm64 macOS host anyway. It is derived from the payload, so it cannot contradict the file it lives in, but nothing asserts the payload itself.

## What to expect

These are the baselines the tests hold as of 2026-11-09, after the Canadian Indigenous languages were added in #44 and the English fallbacks removed in #45, taken from the tests and the shipped files rather than from a generation run. The coverage rows are measured on one host and tolerate 5% drift on another:

| | value | asserted in |
|---|---|---|
| Supplement names | 4,584 | `LocaleDisplayNameSupplementTest` |
| Linux matrix names | 19,816 | nothing — derived only |
| Display languages | 114 | `LocaleDisplayNameSupplementTest` |
| JVM: own gaps / untranslated after the supplement | 10,276 / 8,609 | `JvmCoverageTest` |
| Apple: own gaps / untranslated after the supplement | 8,875 / 8,562 | `AppleCoverageTest` |

**Most of the untranslated pairs are expected.** About 6,300 of them are names rendered *in* one of the 25 Canadian Indigenous languages, which no CLDR release and almost no platform has data for. A jump of that size from adding a display language with no CLDR data is the honest answer, not a broken bridge.

**Mind which denominator you are quoting.** The shipped `localeResourceMap` has 253 entries but only 252 distinct language tags — `zh` carrying a `Hans` script and `zh-Hans` build the same tag — so the matrix is 28,842 pairs counted by resource entry and 28,728 counted by tag. The coverage tests deduplicate by tag. Getting this wrong is what produced three different wrong answers for the unnameable-pair count, recorded in ADR-0005.

Treat the table as the current state rather than as targets. If a run produces something far away from it — a supplement of 12 entries, or of 12,000 — something upstream is wrong, most likely the emitter reading the supplement back or falling back to English.
