package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal as JavaBigDecimal
import java.math.BigInteger as JavaBigInteger

/** `toString` and `toPlainString` write what `java.math.BigDecimal` writes, for the same value. */
class BigDecimalStringJvmTest {

    private val unscaled = listOf("0", "1", "-1", "7", "123", "-123", "1000", "123456789", "-98765432109876543210")
    private val scales = listOf(0, 1, 2, 3, 6, 7, 8, 12, 30, -1, -2, -3, -7, -30, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE - 5)

    @Test
    fun `toString matches java math BigDecimal`() {
        val mismatches = mutableListOf<String>()
        for (u in unscaled) for (scale in scales) {
            val ours = BigDecimal(BigInteger(u), scale).toString()
            val java = JavaBigDecimal(JavaBigInteger(u), scale).toString()
            if (ours != java) mismatches += "$u scale $scale: ours=$ours java=$java"
        }
        assertEquals(emptyList(), mismatches)
    }

    @Test
    fun `toPlainString matches java math BigDecimal`() {
        val mismatches = mutableListOf<String>()
        for (u in unscaled) for (scale in scales.filter { kotlin.math.abs(it.toLong()) <= 30 }) {
            val ours = BigDecimal(BigInteger(u), scale).toPlainString()
            val java = JavaBigDecimal(JavaBigInteger(u), scale).toPlainString()
            if (ours != java) mismatches += "$u scale $scale: ours=$ours java=$java"
        }
        assertEquals(emptyList(), mismatches)
    }

    @Test
    fun `a parsed string prints back as java would`() {
        for (text in listOf("1.25", "0.0000001", "1.23E+4", "-0.000001", "1E-7", "600.0", "0.00", "1E+3")) {
            assertEquals(JavaBigDecimal(text).toString(), BigDecimal(text).toString(), text)
        }
    }
}
