package io.github.aughtone.types.number

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import java.math.BigDecimal as JdkBigDecimal
import java.math.RoundingMode as JdkRoundingMode

/**
 * Differential benchmark of the pure-Kotlin [BigDecimal] against `java.math.BigDecimal`.
 *
 * The JDK type has a fast path this one does not: it keeps small values in a `long` and only
 * inflates to a `BigInteger` when it must. The [digits] parameter crosses that boundary
 * deliberately — 8 digits stays inside the JDK's `long` representation, 40 digits is past it, and
 * 400 digits puts both implementations firmly into multi-word arithmetic where the gap should be
 * whatever the underlying [BigInteger] gap is and no more.
 *
 * [scaleDivide] and [setScaleDown] are the cases worth watching. Both route through
 * `divideAndRemainder` on a power of ten, so a regression in the integer division path shows up
 * here first, and the rounding correction is code neither implementation shares.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class BigDecimalBenchmark {

    @Param("8", "40", "400")
    var digits: Int = 0

    private lateinit var leftText: String
    private lateinit var rightText: String

    private lateinit var left: BigDecimal
    private lateinit var right: BigDecimal
    private lateinit var trailingZeros: BigDecimal

    private lateinit var jdkLeft: JdkBigDecimal
    private lateinit var jdkRight: JdkBigDecimal
    private lateinit var jdkTrailingZeros: JdkBigDecimal

    private lateinit var sameScale: BigDecimal
    private lateinit var jdkSameScale: JdkBigDecimal

    private var scale: Int = 0
    private lateinit var unscaled: BigInteger
    private lateinit var jdkUnscaled: java.math.BigInteger

    /**
     * Builds operands with a scale of roughly a quarter their digit count, so neither side is a
     * plain integer wearing a decimal point. The right operand carries a trailing 7, which keeps it
     * coprime with ten — that is what stops exact division terminating early and leaves the rounding
     * path measured — and as a side effect gives it one more decimal place than the left. So
     * `compare` here is the unequal-scale case, which is the one that has to align before it can
     * answer.
     */
    @Setup
    fun setUp() {
        val random = java.util.Random(0x5EED)
        scale = digits / 4
        leftText = buildString {
            repeat(digits - scale) { append('0' + (1 + random.nextInt(9))) }
            append('.')
            repeat(scale) { append('0' + random.nextInt(10)) }
        }
        rightText = buildString {
            repeat(digits - scale - 1) { append('0' + (1 + random.nextInt(9))) }
            append('.')
            repeat(scale) { append('0' + random.nextInt(10)) }
            append('7')
        }

        left = BigDecimal(leftText)
        right = BigDecimal(rightText)
        trailingZeros = BigDecimal(leftText + "000000000000")

        jdkLeft = JdkBigDecimal(leftText)
        jdkRight = JdkBigDecimal(rightText)
        jdkTrailingZeros = JdkBigDecimal(leftText + "000000000000")

        // Same scale as `left`, so operations against it never align scales and never build a
        // power of ten. Differencing this against the unequal-scale case isolates that cost.
        sameScale = BigDecimal(right.unscaledValue, left.scale)
        jdkSameScale = JdkBigDecimal(jdkRight.unscaledValue(), jdkLeft.scale())

        unscaled = left.unscaledValue
        jdkUnscaled = jdkLeft.unscaledValue()
    }

    @Benchmark
    fun addKotlin(): BigDecimal = left.add(right)

    @Benchmark
    fun addJdk(): JdkBigDecimal = jdkLeft.add(jdkRight)

    @Benchmark
    fun subtractKotlin(): BigDecimal = left.subtract(right)

    @Benchmark
    fun subtractJdk(): JdkBigDecimal = jdkLeft.subtract(jdkRight)

    @Benchmark
    fun multiplyKotlin(): BigDecimal = left.multiply(right)

    @Benchmark
    fun multiplyJdk(): JdkBigDecimal = jdkLeft.multiply(jdkRight)

    @Benchmark
    fun scaleDivideKotlin(): BigDecimal = left.divide(right, 32, RoundingMode.HALF_EVEN)

    @Benchmark
    fun scaleDivideJdk(): JdkBigDecimal = jdkLeft.divide(jdkRight, 32, JdkRoundingMode.HALF_EVEN)

    @Benchmark
    fun setScaleUpKotlin(): BigDecimal = left.setScale(64, RoundingMode.HALF_EVEN)

    @Benchmark
    fun setScaleUpJdk(): JdkBigDecimal = jdkLeft.setScale(64, JdkRoundingMode.HALF_EVEN)

    @Benchmark
    fun setScaleDownKotlin(): BigDecimal = left.setScale(1, RoundingMode.HALF_EVEN)

    @Benchmark
    fun setScaleDownJdk(): JdkBigDecimal = jdkLeft.setScale(1, JdkRoundingMode.HALF_EVEN)

    @Benchmark
    fun stripTrailingZerosKotlin(): BigDecimal = trailingZeros.stripTrailingZeros()

    @Benchmark
    fun stripTrailingZerosJdk(): JdkBigDecimal = jdkTrailingZeros.stripTrailingZeros()

    /**
     * The same operations against an operand that shares [left]'s scale. `add`, `subtract`,
     * `compareTo`, `divide` and `setScale` all align differing scales by multiplying through a
     * power of ten that is rebuilt from scratch on every call, so the gap between each of these and
     * its unequal-scale twin above is the cost of that rebuild.
     */
    @Benchmark
    fun addSameScaleKotlin(): BigDecimal = left.add(sameScale)

    @Benchmark
    fun addSameScaleJdk(): JdkBigDecimal = jdkLeft.add(jdkSameScale)

    @Benchmark
    fun compareSameScaleKotlin(): Int = left.compareTo(sameScale)

    @Benchmark
    fun compareSameScaleJdk(): Int = jdkLeft.compareTo(jdkSameScale)

    @Benchmark
    fun compareKotlin(): Int = left.compareTo(right)

    @Benchmark
    fun compareJdk(): Int = jdkLeft.compareTo(jdkRight)

    /**
     * Both sides build a fresh value before converting, because `java.math.BigDecimal` memoizes its
     * decimal text in a private `stringCache` field. Reusing one instance measures a field read on
     * the JDK side against a full radix conversion on ours, which is not a comparison. Construction
     * from an unscaled [BigInteger] and a scale is a field assignment on both sides, so what is left
     * is the conversion. `doubleValue` needs the same treatment: the JDK implementation falls back
     * to `parseDouble(toString())`, so it reads the same cache.
     */
    @Benchmark
    fun toDoubleKotlin(): Double = BigDecimal(unscaled, scale).toDouble()

    @Benchmark
    fun toDoubleJdk(): Double = JdkBigDecimal(jdkUnscaled, scale).toDouble()

    @Benchmark
    fun toStringKotlin(): String = BigDecimal(unscaled, scale).toString()

    @Benchmark
    fun toStringJdk(): String = JdkBigDecimal(jdkUnscaled, scale).toString()

    @Benchmark
    fun parseKotlin(): BigDecimal = BigDecimal(leftText)

    @Benchmark
    fun parseJdk(): JdkBigDecimal = JdkBigDecimal(leftText)
}
