package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.math.BigInteger as JdkBigInteger
import java.math.BigDecimal as JdkBigDecimal
import java.math.RoundingMode as JdkRoundingMode

class JvmBigNumComparisonTest {

    private val testIntStrings = listOf(
        "0", "1", "-1", "10", "-10", "123456789", "-123456789",
        "999999999999999999999999999999", "-999999999999999999999999999999",
        "1000000000000000000000000000000", "-1000000000000000000000000000000",
        "9223372036854775807", "-9223372036854775808"
    )

    private val testDecStrings = listOf(
        "0", "0.0", "1", "1.0", "-1", "-1.0", "10.00", "-10.0",
        "123.456", "-123.456", "0.000123", "-0.000123",
        "123456789.987654321", "-123456789.987654321",
        "1.23e4", "-1.23e-4", "600.0"
    )

    @Test
    fun testBigDecimalCompareParityAcrossScales() {
        // compareTo decides most comparisons from decimal magnitude without aligning scales. The
        // risk is a bracket that is one digit too tight, which shows up only where two values are
        // close enough to sit in the same magnitude band, so this walks neighbouring values at many
        // scales rather than sampling far-apart ones.
        val random = java.util.Random(0xC0FFEE)
        var checked = 0
        for (digits in listOf(1, 2, 5, 18, 19, 20, 40, 400)) {
            for (trial in 0 until 60) {
                val unscaled = JdkBigInteger(digits * 4, random)
                for (leftScale in listOf(0, 1, 7, 64, 65, -3)) {
                    for (scaleDelta in listOf(-65, -8, -1, 0, 1, 8, 65)) {
                        for (nudge in listOf(-1L, 0L, 1L)) {
                            val leftJdk = JdkBigDecimal(unscaled, leftScale)
                            val rightJdk = JdkBigDecimal(
                                unscaled.add(JdkBigInteger.valueOf(nudge)),
                                leftScale + scaleDelta
                            )
                            for (signed in listOf(1, -1)) {
                                val a = if (signed > 0) leftJdk else leftJdk.negate()
                                val b = if (signed > 0) rightJdk else rightJdk.negate()
                                val ours = BigDecimal(a.toString()).compareTo(BigDecimal(b.toString()))
                                assertEquals(
                                    a.compareTo(b),
                                    ours,
                                    "compareTo mismatch for $a vs $b"
                                )
                                checked++
                            }
                        }
                    }
                }
            }
        }
        assertTrue(checked > 100_000, "expected a broad sweep, only checked $checked")
    }

    @Test
    fun testBigIntegerRadixConversionParity() {
        // Recursive base conversion only engages past a word-count threshold, so sizes are chosen
        // to sit either side of it, and radixes either side of the digit/letter boundary.
        val random = java.util.Random(0xBEEF)
        for (bits in listOf(1, 31, 32, 33, 63, 64, 65, 200, 639, 640, 641, 1500, 5000)) {
            for (trial in 0 until 8) {
                val value = JdkBigInteger(bits, random)
                for (signed in listOf(value, value.negate())) {
                    for (radix in listOf(2, 8, 10, 16, 36)) {
                        val expected = signed.toString(radix)
                        val ours = BigInteger.parseString(signed.toString(radix), radix)
                        assertEquals(expected, ours.toString(radix), "toString mismatch radix $radix")
                        assertEquals(
                            signed.toString(),
                            ours.toString(),
                            "round trip mismatch radix $radix for $signed"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testBigIntegerRadixConversionBoundaryValues() {
        val boundaries = listOf(
            JdkBigInteger.ZERO,
            JdkBigInteger.ONE,
            JdkBigInteger.ONE.negate(),
            JdkBigInteger.TEN.pow(18),
            JdkBigInteger.TEN.pow(19),
            JdkBigInteger.TEN.pow(64),
            JdkBigInteger.TEN.pow(65),
            JdkBigInteger.TWO.pow(640),
            JdkBigInteger.TWO.pow(640).subtract(JdkBigInteger.ONE),
            JdkBigInteger.TWO.pow(1024).negate()
        )
        for (value in boundaries) {
            for (radix in listOf(2, 10, 16, 36)) {
                assertEquals(
                    value.toString(radix),
                    BigInteger.parseString(value.toString(radix), radix).toString(radix),
                    "boundary mismatch radix $radix for $value"
                )
            }
        }
    }

    @Test
    fun testBigIntegerBitLengthParity() {
        for (aStr in testIntStrings) {
            assertEquals(
                JdkBigInteger(aStr).bitLength(),
                BigInteger.parseString(aStr).bitLength(),
                "bitLength mismatch for $aStr"
            )
        }
    }

    @Test
    fun testBigIntegerBitLengthParityAcrossPowersOfTwo() {
        // Powers of two and their neighbours are where the negative case differs by one, and where
        // a word boundary is crossed, so they are worth walking rather than sampling.
        for (exponent in 0..200) {
            val power = JdkBigInteger.TWO.pow(exponent)
            for (offset in listOf(-1, 0, 1)) {
                val value = power.add(JdkBigInteger.valueOf(offset.toLong()))
                for (signed in listOf(value, value.negate())) {
                    assertEquals(
                        signed.bitLength(),
                        BigInteger.parseString(signed.toString()).bitLength(),
                        "bitLength mismatch for $signed"
                    )
                }
            }
        }
    }

    @Test
    fun testBigIntegerArithmeticParity() {
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aJdk = JdkBigInteger(aStr)

            assertEquals(aOurs.toString(), aJdk.toString(), "String representation mismatch for $aStr")

            for (bStr in testIntStrings) {
                val bOurs = BigInteger.parseString(bStr)
                val bJdk = JdkBigInteger(bStr)

                // Addition
                assertEquals((aOurs + bOurs).toString(), (aJdk.add(bJdk)).toString(), "Add mismatch for $aStr + $bStr")

                // Subtraction
                assertEquals((aOurs - bOurs).toString(), (aJdk.subtract(bJdk)).toString(), "Subtract mismatch for $aStr - $bStr")

                // Multiplication
                assertEquals((aOurs * bOurs).toString(), (aJdk.multiply(bJdk)).toString(), "Multiply mismatch for $aStr * $bStr")

                // Division and Remainder
                if (bOurs != BigInteger.ZERO) {
                    assertEquals((aOurs / bOurs).toString(), (aJdk.divide(bJdk)).toString(), "Divide mismatch for $aStr / $bStr")
                    assertEquals((aOurs % bOurs).toString(), (aJdk.remainder(bJdk)).toString(), "Remainder mismatch for $aStr % $bStr")

                    val (qOurs, rOurs) = aOurs.divideAndRemainder(bOurs)
                    val jdkDivRem = aJdk.divideAndRemainder(bJdk)
                    assertEquals(qOurs.toString(), jdkDivRem[0].toString(), "divideAndRemainder quotient mismatch for $aStr / $bStr")
                    assertEquals(rOurs.toString(), jdkDivRem[1].toString(), "divideAndRemainder remainder mismatch for $aStr % $bStr")
                }
            }
        }
    }

    @Test
    fun testBigIntegerModuloParity() {
        val divisors = listOf("1", "3", "10", "1234567")
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aJdk = JdkBigInteger(aStr)

            for (bStr in divisors) {
                val bOurs = BigInteger.parseString(bStr)
                val bJdk = JdkBigInteger(bStr)

                assertEquals(aOurs.mod(bOurs).toString(), aJdk.mod(bJdk).toString(), "Modulo mismatch for $aStr mod $bStr")
            }
        }
    }

    @Test
    fun testBigIntegerBitwiseParity() {
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aJdk = JdkBigInteger(aStr)

            assertEquals(aOurs.not().toString(), aJdk.not().toString(), "NOT mismatch for $aStr")

            for (bStr in testIntStrings) {
                val bOurs = BigInteger.parseString(bStr)
                val bJdk = JdkBigInteger(bStr)

                assertEquals(aOurs.and(bOurs).toString(), aJdk.and(bJdk).toString(), "AND mismatch for $aStr & $bStr")
                assertEquals(aOurs.or(bOurs).toString(), aJdk.or(bJdk).toString(), "OR mismatch for $aStr | $bStr")
                assertEquals(aOurs.xor(bOurs).toString(), aJdk.xor(bJdk).toString(), "XOR mismatch for $aStr ^ $bStr")
            }
        }
    }

    @Test
    fun testBigIntegerShiftsParity() {
        val shiftCounts = listOf(0, 1, 2, 5, 31, 32, 63, 64)
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aJdk = JdkBigInteger(aStr)

            for (shift in shiftCounts) {
                assertEquals(aOurs.shiftLeft(shift).toString(), aJdk.shiftLeft(shift).toString(), "ShiftLeft mismatch for $aStr << $shift")
                assertEquals(aOurs.shiftRight(shift).toString(), aJdk.shiftRight(shift).toString(), "ShiftRight mismatch for $aStr >> $shift")

                // Test negative shifts
                assertEquals(aOurs.shiftLeft(-shift).toString(), aJdk.shiftLeft(-shift).toString(), "Negative ShiftLeft mismatch for $aStr << -$shift")
                assertEquals(aOurs.shiftRight(-shift).toString(), aJdk.shiftRight(-shift).toString(), "Negative ShiftRight mismatch for $aStr >> -$shift")
            }
        }
    }

    @Test
    fun testBigIntegerBitManipulationParity() {
        val bitIndices = listOf(0, 1, 2, 5, 31, 32, 63, 64, 128)
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aJdk = JdkBigInteger(aStr)

            for (bit in bitIndices) {
                assertEquals(aOurs.testBit(bit), aJdk.testBit(bit), "testBit mismatch for $aStr at $bit")
                assertEquals(aOurs.setBit(bit).toString(), aJdk.setBit(bit).toString(), "setBit mismatch for $aStr at $bit")
                assertEquals(aOurs.clearBit(bit).toString(), aJdk.clearBit(bit).toString(), "clearBit mismatch for $aStr at $bit")
            }
        }
    }

    @Test
    fun testBigDecimalArithmeticParity() {
        for (aStr in testDecStrings) {
            val aOurs = BigDecimal.parseString(aStr)
            val aJdk = JdkBigDecimal(aStr)

            for (bStr in testDecStrings) {
                val bOurs = BigDecimal.parseString(bStr)
                val bJdk = JdkBigDecimal(bStr)

                // Addition
                val sumOurs = aOurs + bOurs
                val sumJdk = aJdk.add(bJdk)
                assertEquals(sumOurs.compareTo(BigDecimal.parseString(sumJdk.toPlainString())), 0, "Add value mismatch for $aStr + $bStr (Ours: $sumOurs, Jdk: $sumJdk)")

                // Subtraction
                val subOurs = aOurs - bOurs
                val subJdk = aJdk.subtract(bJdk)
                assertEquals(subOurs.compareTo(BigDecimal.parseString(subJdk.toPlainString())), 0, "Subtract value mismatch for $aStr - $bStr")

                // Multiplication
                val mulOurs = aOurs * bOurs
                val mulJdk = aJdk.multiply(bJdk)
                assertEquals(mulOurs.compareTo(BigDecimal.parseString(mulJdk.toPlainString())), 0, "Multiply value mismatch for $aStr * $bStr")
            }
        }
    }

    @Test
    fun testBigDecimalDivisionExact() {
        val cases = listOf(
            Pair("1", "2"),
            Pair("1", "5"),
            Pair("12.34", "2"),
            Pair("100", "4"),
            Pair("-10", "8")
        )
        for ((aStr, bStr) in cases) {
            val aOurs = BigDecimal.parseString(aStr)
            val aJdk = JdkBigDecimal(aStr)

            val bOurs = BigDecimal.parseString(bStr)
            val bJdk = JdkBigDecimal(bStr)

            val divOurs = aOurs / bOurs
            val divJdk = aJdk.divide(bJdk)
            assertEquals(divOurs.compareTo(BigDecimal.parseString(divJdk.toPlainString())), 0, "Exact division value mismatch for $aStr / $bStr")
        }
    }

    private fun mapRoundingMode(ours: RoundingMode): JdkRoundingMode {
        return when (ours) {
            RoundingMode.UP -> JdkRoundingMode.UP
            RoundingMode.DOWN -> JdkRoundingMode.DOWN
            RoundingMode.CEILING -> JdkRoundingMode.CEILING
            RoundingMode.FLOOR -> JdkRoundingMode.FLOOR
            RoundingMode.HALF_UP -> JdkRoundingMode.HALF_UP
            RoundingMode.HALF_DOWN -> JdkRoundingMode.HALF_DOWN
            RoundingMode.HALF_EVEN -> JdkRoundingMode.HALF_EVEN
            RoundingMode.UNNECESSARY -> JdkRoundingMode.UNNECESSARY
        }
    }

    @Test
    fun testBigDecimalDivisionWithRounding() {
        val testPairs = listOf(
            Pair("1", "3"),
            Pair("2", "3"),
            Pair("5.5", "100"),
            Pair("4.5", "100"),
            Pair("-4.1", "100"),
            Pair("-4.9", "100")
        )
        val scales = listOf(0, 1, 2, 5)
        val roundingModes = listOf(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.HALF_UP,
            RoundingMode.HALF_DOWN,
            RoundingMode.HALF_EVEN
        )

        for ((aStr, bStr) in testPairs) {
            val aOurs = BigDecimal.parseString(aStr)
            val aJdk = JdkBigDecimal(aStr)

            val bOurs = BigDecimal.parseString(bStr)
            val bJdk = JdkBigDecimal(bStr)

            for (scale in scales) {
                for (mode in roundingModes) {
                    val resOurs = aOurs.divide(bOurs, scale, mode)
                    val resJdk = aJdk.divide(bJdk, scale, mapRoundingMode(mode))

                    assertEquals(
                        resOurs.toString(),
                        resJdk.toPlainString(),
                        "Division mismatch for $aStr / $bStr with scale $scale, mode $mode"
                    )
                }
            }
        }
    }

    @Test
    fun testBigDecimalScaleAdjustment() {
        val testStrings = listOf("1.23456", "1.2000", "-0.00123")
        val scales = listOf(0, 1, 2, 5, 8)
        val roundingModes = listOf(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.HALF_UP,
            RoundingMode.HALF_DOWN,
            RoundingMode.HALF_EVEN
        )

        for (str in testStrings) {
            val aOurs = BigDecimal.parseString(str)
            val aJdk = JdkBigDecimal(str)

            for (scale in scales) {
                for (mode in roundingModes) {
                    val resOurs = aOurs.setScale(scale, mode)
                    val resJdk = aJdk.setScale(scale, mapRoundingMode(mode))

                    assertEquals(
                        resOurs.toString(),
                        resJdk.toPlainString(),
                        "setScale mismatch for $str with scale $scale, mode $mode"
                    )
                }
            }
        }
    }

    @Test
    fun testBigDecimalStripTrailingZeros() {
        val testStrings = listOf("600.0", "1.23000", "0.0", "100.00100", "0.000")
        for (str in testStrings) {
            val aOurs = BigDecimal.parseString(str).stripTrailingZeros()
            val aJdk = JdkBigDecimal(str).stripTrailingZeros()

            assertEquals(
                aOurs.compareTo(BigDecimal.parseString(aJdk.toPlainString())),
                0,
                "stripTrailingZeros value mismatch for $str"
            )
        }
    }
}
