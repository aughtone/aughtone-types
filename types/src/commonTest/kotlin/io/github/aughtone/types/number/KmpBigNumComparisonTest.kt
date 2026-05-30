package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.ionspin.kotlin.bignum.integer.BigInteger as IonspinBigInteger
import com.ionspin.kotlin.bignum.decimal.BigDecimal as IonspinBigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode as IonspinDecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode as IonspinRoundingMode

class KmpBigNumComparisonTest {

    private val testIntStrings = listOf(
        "0", "1", "10", "123456789",
        "999999999999999999999999999999",
        "1000000000000000000000000000000",
        "9223372036854775807"
    )

    private val testDecStrings = listOf(
        "0", "0.0", "1", "1.0", "10.00",
        "123.456", "0.000123",
        "123456789.987654321",
        "1.23e4", "600.0"
    )

    @Test
    fun testBigIntegerArithmeticParity() {
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aIon = IonspinBigInteger.parseString(aStr, 10)

            assertEquals(aOurs.toString(), aIon.toString(10), "String representation mismatch for $aStr")

            // Test negative values
            val negAStr = if (aStr == "0") "0" else "-$aStr"
            val negAOurs = BigInteger.parseString(negAStr)
            val negAIon = IonspinBigInteger.parseString(negAStr, 10)
            assertEquals(negAOurs.toString(), negAIon.toString(10), "String representation mismatch for $negAStr")

            for (bStr in testIntStrings) {
                val bOurs = BigInteger.parseString(bStr)
                val bIon = IonspinBigInteger.parseString(bStr, 10)

                // Addition
                assertEquals((aOurs + bOurs).toString(), (aIon + bIon).toString(10), "Add mismatch for $aStr + $bStr")

                // Subtraction
                assertEquals((aOurs - bOurs).toString(), (aIon - bIon).toString(10), "Subtract mismatch for $aStr - $bStr")

                // Multiplication
                assertEquals((aOurs * bOurs).toString(), (aIon * bIon).toString(10), "Multiply mismatch for $aStr * $bStr")

                // Division and Remainder (avoiding division by zero, and negative operands for remainder due to Ionspin sign bug)
                if (bOurs != BigInteger.ZERO) {
                    assertEquals((aOurs / bOurs).toString(), (aIon / bIon).toString(10), "Divide mismatch for $aStr / $bStr")
                    assertEquals((aOurs % bOurs).toString(), (aIon % bIon).toString(10), "Remainder mismatch for $aStr % $bStr")

                    val (qOurs, rOurs) = aOurs.divideAndRemainder(bOurs)
                    val ionDivRem = aIon.divrem(bIon)
                    assertEquals(qOurs.toString(), ionDivRem.quotient.toString(10), "divideAndRemainder quotient mismatch for $aStr / $bStr")
                    assertEquals(rOurs.toString(), ionDivRem.remainder.toString(10), "divideAndRemainder remainder mismatch for $aStr % $bStr")
                }
            }
        }
    }

    @Test
    fun testBigIntegerDivisionByZero() {
        assertFailsWith<ArithmeticException> {
            BigInteger.ONE.divide(BigInteger.ZERO)
        }
    }

    @Test
    fun testBigIntegerModuloParity() {
        // Modulo requires positive divisor
        val divisors = listOf("1", "3", "10", "1234567")
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aIon = IonspinBigInteger.parseString(aStr, 10)

            for (bStr in divisors) {
                val bOurs = BigInteger.parseString(bStr)
                val bIon = IonspinBigInteger.parseString(bStr, 10)

                assertEquals(aOurs.mod(bOurs).toString(), aIon.mod(bIon).toString(10), "Modulo mismatch for $aStr mod $bStr")
            }
        }
    }

    @Test
    fun testBigIntegerShiftsParity() {
        val shiftCounts = listOf(0, 1, 2, 5, 31, 32, 63, 64)
        for (aStr in testIntStrings) {
            val aOurs = BigInteger.parseString(aStr)
            val aIon = IonspinBigInteger.parseString(aStr, 10)

            for (shift in shiftCounts) {
                assertEquals(aOurs.shiftLeft(shift).toString(), aIon.shl(shift).toString(10), "ShiftLeft mismatch for $aStr << $shift")
                assertEquals(aOurs.shiftRight(shift).toString(), aIon.shr(shift).toString(10), "ShiftRight mismatch for $aStr >> $shift")

                // Test negative shifts using safe mapping to Ionspin API
                assertEquals(aOurs.shiftLeft(-shift).toString(), aIon.shr(shift).toString(10), "Negative ShiftLeft mismatch for $aStr << -$shift")
                assertEquals(aOurs.shiftRight(-shift).toString(), aIon.shl(shift).toString(10), "Negative ShiftRight mismatch for $aStr >> -$shift")
            }
        }
    }

    @Test
    fun testBigDecimalArithmeticParity() {
        for (aStr in testDecStrings) {
            val aOurs = BigDecimal.parseString(aStr)
            val aIon = IonspinBigDecimal.parseString(aStr)

            for (bStr in testDecStrings) {
                val bOurs = BigDecimal.parseString(bStr)
                val bIon = IonspinBigDecimal.parseString(bStr)

                // Addition
                val sumOurs = aOurs + bOurs
                val sumIon = aIon + bIon
                assertEquals(sumOurs.compareTo(BigDecimal.parseString(sumIon.toString())), 0, "Add value mismatch for $aStr + $bStr (Ours: $sumOurs, Ionspin: $sumIon)")

                // Subtraction
                val subOurs = aOurs - bOurs
                val subIon = aIon - bIon
                assertEquals(subOurs.compareTo(BigDecimal.parseString(subIon.toString())), 0, "Subtract value mismatch for $aStr - $bStr")

                // Multiplication
                val mulOurs = aOurs * bOurs
                val mulIon = aIon * bIon
                assertEquals(mulOurs.compareTo(BigDecimal.parseString(mulIon.toString())), 0, "Multiply value mismatch for $aStr * $bStr")
            }
        }
    }

    @Test
    fun testBigDecimalDivisionExact() {
        val cases = listOf(
            Pair("1", "2"),     // 0.5
            Pair("1", "5"),     // 0.2
            Pair("12.34", "2"), // 6.17
            Pair("100", "4"),   // 25
            Pair("10", "8")     // 1.25
        )
        for ((aStr, bStr) in cases) {
            val aOurs = BigDecimal.parseString(aStr)
            val aIon = IonspinBigDecimal.parseString(aStr)

            val bOurs = BigDecimal.parseString(bStr)
            val bIon = IonspinBigDecimal.parseString(bStr)

            val divOurs = aOurs / bOurs
            val divIon = aIon / bIon
            assertEquals(divOurs.compareTo(BigDecimal.parseString(divIon.toString())), 0, "Exact division value mismatch for $aStr / $bStr")
        }
    }

    private fun mapRoundingMode(ours: RoundingMode): IonspinRoundingMode {
        return when (ours) {
            RoundingMode.UP -> IonspinRoundingMode.AWAY_FROM_ZERO
            RoundingMode.DOWN -> IonspinRoundingMode.TOWARDS_ZERO
            RoundingMode.CEILING -> IonspinRoundingMode.CEILING
            RoundingMode.FLOOR -> IonspinRoundingMode.FLOOR
            RoundingMode.HALF_UP -> IonspinRoundingMode.ROUND_HALF_AWAY_FROM_ZERO
            RoundingMode.HALF_DOWN -> IonspinRoundingMode.ROUND_HALF_TOWARDS_ZERO
            RoundingMode.HALF_EVEN -> IonspinRoundingMode.ROUND_HALF_TO_EVEN
            RoundingMode.UNNECESSARY -> IonspinRoundingMode.NONE
        }
    }

    @Test
    fun testBigDecimalDivisionWithRounding() {
        val testPairs = listOf(
            Pair("1", "3"),
            Pair("2", "3"),
            Pair("5.5", "100"),
            Pair("4.5", "100")
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
            val aIon = IonspinBigDecimal.parseString(aStr)

            val bOurs = BigDecimal.parseString(bStr)
            val bIon = IonspinBigDecimal.parseString(bStr)

            for (scale in scales) {
                for (mode in roundingModes) {
                    val resOurs = aOurs.divide(bOurs, scale, mode)

                    // Ionspin doesn't have a direct divide(other, scale, roundingMode) function.
                    // We simulate it by dividing with high precision (100) and then scaling the result.
                    val tempIon = aIon.divide(bIon, IonspinDecimalMode(100, mapRoundingMode(mode)))
                    val resIon = tempIon.scale(scale.toLong())

                    val parsedIon = BigDecimal.parseString(resIon.toString())
                    assertEquals(
                        0,
                        resOurs.compareTo(parsedIon),
                        "Division mismatch for $aStr / $bStr with scale $scale, mode $mode"
                    )
                    assertEquals(
                        resOurs.scale,
                        scale,
                        "Scale mismatch for $aStr / $bStr with scale $scale, mode $mode"
                    )
                }
            }
        }
    }
}
