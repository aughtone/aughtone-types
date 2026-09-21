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
import java.math.BigInteger as JdkBigInteger

/**
 * Differential benchmark of the pure-Kotlin [BigInteger] against `java.math.BigInteger`.
 *
 * Every case runs both implementations over identical decimal inputs, so the JDK figure is a
 * baseline rather than a target: it is a mature, intrinsified implementation and beating it is not
 * the goal. What matters is the *ratio*, and how that ratio moves between operand sizes. A ratio
 * that stays flat as [bits] grows says the algorithm is the same order as the JDK's; one that grows
 * with size says it is not, and points at the schoolbook path that has no asymptotic fallback.
 *
 * Operand widths are chosen to cross the thresholds that matter: 64 bits fits in two 32-bit words
 * and exercises the single-word fast paths, 1024 bits forces multi-word Knuth division, and 4096
 * bits is where a quadratic multiply separates from a subquadratic one.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class BigIntegerBenchmark {

    @Param("64", "1024", "4096")
    var bits: Int = 0

    private lateinit var leftText: String
    private lateinit var rightText: String
    private lateinit var modulusText: String

    private lateinit var wide: BigInteger
    private lateinit var left: BigInteger
    private lateinit var right: BigInteger
    private lateinit var modulus: BigInteger
    private lateinit var smallExponent: BigInteger

    private lateinit var jdkWide: JdkBigInteger
    private lateinit var jdkLeft: JdkBigInteger
    private lateinit var jdkRight: JdkBigInteger
    private lateinit var jdkModulus: JdkBigInteger
    private lateinit var jdkSmallExponent: JdkBigInteger

    /**
     * Builds the operands once per parameter set, from a fixed seed so every run of the suite sees
     * the same numbers. The right operand is deliberately half the width of the left: an equal-width
     * divisor makes division a one-digit quotient and skips the correction loop that dominates the
     * multi-word path.
     */
    @Setup
    fun setUp() {
        val random = java.util.Random(0x5EED)
        leftText = JdkBigInteger(bits, random).toString()
        rightText = JdkBigInteger(bits / 2, random).or(JdkBigInteger.ONE).toString()
        modulusText = JdkBigInteger(bits, random).or(JdkBigInteger.ONE).toString()

        left = BigInteger(leftText)
        wide = left.multiply(left)
        right = BigInteger(rightText)
        modulus = BigInteger(modulusText)
        smallExponent = BigInteger("65537")

        jdkLeft = JdkBigInteger(leftText)
        jdkWide = jdkLeft.multiply(jdkLeft)
        jdkRight = JdkBigInteger(rightText)
        jdkModulus = JdkBigInteger(modulusText)
        jdkSmallExponent = JdkBigInteger("65537")
    }

    @Benchmark
    fun addKotlin(): BigInteger = left.add(right)

    @Benchmark
    fun addJdk(): JdkBigInteger = jdkLeft.add(jdkRight)

    @Benchmark
    fun subtractKotlin(): BigInteger = left.subtract(right)

    @Benchmark
    fun subtractJdk(): JdkBigInteger = jdkLeft.subtract(jdkRight)

    @Benchmark
    fun multiplyKotlin(): BigInteger = left.multiply(right)

    @Benchmark
    fun multiplyJdk(): JdkBigInteger = jdkLeft.multiply(jdkRight)

    @Benchmark
    fun divideKotlin(): BigInteger = left.divide(right)

    @Benchmark
    fun divideJdk(): JdkBigInteger = jdkLeft.divide(jdkRight)

    @Benchmark
    fun remainderKotlin(): BigInteger = left.remainder(right)

    @Benchmark
    fun remainderJdk(): JdkBigInteger = jdkLeft.remainder(jdkRight)

    /**
     * Reduces [wide], which is the square of the left operand, rather than the operand itself.
     * A dividend drawn from the same distribution as the modulus falls below it about half the
     * time, and `mod` then returns after a single comparison — which measures the short-circuit
     * rather than the reduction, and does it for some operand sizes and not others.
     */
    @Benchmark
    fun modKotlin(): BigInteger = wide.mod(modulus)

    @Benchmark
    fun modJdk(): JdkBigInteger = jdkWide.mod(jdkModulus)

    @Benchmark
    fun modPowKotlin(): BigInteger = left.modPow(smallExponent, modulus)

    @Benchmark
    fun modPowJdk(): JdkBigInteger = jdkLeft.modPow(jdkSmallExponent, jdkModulus)

    @Benchmark
    fun modInverseKotlin(): BigInteger = left.modInverse(modulus)

    @Benchmark
    fun modInverseJdk(): JdkBigInteger = jdkLeft.modInverse(jdkModulus)

    @Benchmark
    fun shiftLeftKotlin(): BigInteger = left.shiftLeft(37)

    @Benchmark
    fun shiftLeftJdk(): JdkBigInteger = jdkLeft.shiftLeft(37)

    @Benchmark
    fun shiftRightKotlin(): BigInteger = left.shiftRight(37)

    @Benchmark
    fun shiftRightJdk(): JdkBigInteger = jdkLeft.shiftRight(37)

    @Benchmark
    fun andKotlin(): BigInteger = left.and(right)

    @Benchmark
    fun andJdk(): JdkBigInteger = jdkLeft.and(jdkRight)

    @Benchmark
    fun xorKotlin(): BigInteger = left.xor(right)

    @Benchmark
    fun xorJdk(): JdkBigInteger = jdkLeft.xor(jdkRight)

    @Benchmark
    fun toStringKotlin(): String = left.toString()

    @Benchmark
    fun toStringJdk(): String = jdkLeft.toString()

    @Benchmark
    fun parseKotlin(): BigInteger = BigInteger(leftText)

    @Benchmark
    fun parseJdk(): JdkBigInteger = JdkBigInteger(leftText)
}
