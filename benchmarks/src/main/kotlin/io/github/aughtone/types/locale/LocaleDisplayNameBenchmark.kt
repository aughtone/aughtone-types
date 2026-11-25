package io.github.aughtone.types.locale

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup

/**
 * What reading a locale's name costs, now that [Locale.displayName] resolves in the current
 * language on every read rather than returning a stored string.
 *
 * [displayNameRepeated] is the hot path a UI hits — the same locale read again and again, which the
 * per-instance cache should make cheap. [sortByDisplayName] and [searchByName] are what a locale
 * picker does: every comparison or filter step reads a name. [currentOrNull] is the platform lookup
 * underneath all of them.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class LocaleDisplayNameBenchmark {

    private lateinit var german: Locale
    private lateinit var french: Locale
    private lateinit var all: List<Locale>

    @Setup
    fun setUp() {
        german = localeFor("de-DE")!!
        french = localeFor("fr")!!
        all = availableLocales()
    }

    @Benchmark
    fun currentOrNull(): Locale? = Locale.currentOrNull

    @Benchmark
    fun displayNameRepeated(): String = german.displayName

    @Benchmark
    fun localizedDisplayNameInFrench(): String = german.localizedDisplayName(displayIn = french)

    @Benchmark
    fun sortByDisplayName(): List<Locale> = all.sortedBy { it.displayName }

    @Benchmark
    fun searchByName(): List<Locale> = localesByName("an")
}
