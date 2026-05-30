package io.github.aughtone.types.number

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BigIntegerTest {

    @Test
    fun `sign extension initialization from byte array`() {
        // [0x80] is -128 in two's complement (1 byte)
        val negative128 = BigInteger.fromByteArray(byteArrayOf(0x80.toByte()))
        assertEquals("-128", negative128.toString())

        // [0x00, 0x80] is 128 (2 bytes, where the leading zero byte prevents sign extension)
        val positive128 = BigInteger.fromByteArray(byteArrayOf(0x00.toByte(), 0x80.toByte()))
        assertEquals("128", positive128.toString())

        // Additional byte array test cases
        val zero = BigInteger.fromByteArray(byteArrayOf(0))
        assertEquals("0", zero.toString())

        val one = BigInteger.fromByteArray(byteArrayOf(1))
        assertEquals("1", one.toString())

        val minusOne = BigInteger.fromByteArray(byteArrayOf(-1))
        assertEquals("-1", minusOne.toString())

        val largePositive = BigInteger.fromByteArray(byteArrayOf(0x12, 0x34, 0x56, 0x78))
        assertEquals("305419896", largePositive.toString())
    }

    @Test
    fun `initialization from String`() {
        assertEquals("0", BigInteger.parseString("0").toString())
        assertEquals("12345678901234567890", BigInteger.parseString("12345678901234567890").toString())
        assertEquals("-12345678901234567890", BigInteger.parseString("-12345678901234567890").toString())
        
        // Radix tests
        assertEquals("255", BigInteger.parseString("FF", 16).toString())
        assertEquals("-255", BigInteger.parseString("-FF", 16).toString())
        assertEquals("15", BigInteger.parseString("1111", 2).toString())
    }

    @Test
    fun `remainder sign follows dividend`() {
        val a1 = BigInteger.valueOf(-10)
        val b1 = BigInteger.valueOf(3)
        assertEquals("-1", a1.remainder(b1).toString())

        val a2 = BigInteger.valueOf(10)
        val b2 = BigInteger.valueOf(-3)
        assertEquals("1", a2.remainder(b2).toString())

        val a3 = BigInteger.valueOf(-10)
        val b3 = BigInteger.valueOf(-3)
        assertEquals("-1", a3.remainder(b3).toString())
    }

    @Test
    fun `modulo strictly yields positive relative range`() {
        val a = BigInteger.valueOf(-10)
        val b = BigInteger.valueOf(3)
        assertEquals("2", a.mod(b).toString())

        // mod requires modulus > 0
        assertFailsWith<ArithmeticException> {
            a.mod(BigInteger.valueOf(0))
        }
        assertFailsWith<ArithmeticException> {
            a.mod(BigInteger.valueOf(-3))
        }
    }

    @Test
    fun `division by zero safety`() {
        val zero = BigInteger.ZERO
        assertFailsWith<ArithmeticException> {
            zero.divide(zero)
        }
        assertFailsWith<ArithmeticException> {
            BigInteger.ONE.divide(zero)
        }
        assertFailsWith<ArithmeticException> {
            BigInteger.ONE.remainder(zero)
        }
    }

    @Test
    fun `infinite stream of 1s in two's complement shiftRight`() {
        val minusOne = BigInteger.valueOf(-1)
        assertEquals("-1", minusOne.shiftRight(1).toString())
        assertEquals("-1", minusOne.shiftRight(10).toString())
        
        // Positive shiftRight floors towards negative infinity
        val minusTen = BigInteger.valueOf(-10)
        // -10 / 2 = -5
        assertEquals("-5", minusTen.shiftRight(1).toString())
        // -5 / 2 = -2.5 -> floored to -3
        assertEquals("-3", minusTen.shiftRight(2).toString())
    }

    @Test
    fun `bit manipulation lookup`() {
        val fifteen = BigInteger.valueOf(15) // binary 1111
        assertTrue(fifteen.testBit(0))
        assertTrue(fifteen.testBit(1))
        assertTrue(fifteen.testBit(2))
        assertTrue(fifteen.testBit(3))
        assertFalse(fifteen.testBit(4))

        val minusOne = BigInteger.valueOf(-1) // concepts: infinite 1s
        assertTrue(minusOne.testBit(0))
        assertTrue(minusOne.testBit(100))

        val zero = BigInteger.ZERO
        assertFalse(zero.testBit(0))
        assertFalse(zero.testBit(100))
    }

    @Test
    fun `bit manipulation modify`() {
        val zero = BigInteger.ZERO
        assertEquals("1", zero.setBit(0).toString())
        assertEquals("16", zero.setBit(4).toString())
        
        val seven = BigInteger.valueOf(7) // binary 111
        assertEquals("5", seven.clearBit(1).toString()) // 101 binary -> 5
        assertEquals("7", seven.clearBit(3).toString()) // no-op
    }

    @Test
    fun `basic arithmetic operations`() {
        val a = BigInteger.parseString("999999999999999999")
        val b = BigInteger.parseString("1")

        assertEquals("1000000000000000000", a.add(b).toString())
        assertEquals("999999999999999998", a.subtract(b).toString())
        assertEquals("999999999999999999", a.multiply(b).toString())
        assertEquals("999999999999999999", a.divide(b).toString())
        
        val two = BigInteger.valueOf(2)
        assertEquals("1999999999999999998", a.multiply(two).toString())
        assertEquals("499999999999999999", a.divide(two).toString())
        assertEquals("1", a.remainder(two).toString())
    }

    @Test
    fun `logical bitwise operations`() {
        val a = BigInteger.valueOf(12) // 1100 binary
        val b = BigInteger.valueOf(10) // 1010 binary

        assertEquals("8", a.and(b).toString()) // 1000 binary -> 8
        assertEquals("14", a.or(b).toString()) // 1110 binary -> 14
        assertEquals("6", a.xor(b).toString()) // 0110 binary -> 6
        assertEquals("-13", a.not().toString()) // ~12 = -13 in two's complement
    }

    @Test
    fun `modular exponentiation and inverse`() {
        // 3^4 mod 5 = 81 mod 5 = 1
        val three = BigInteger.valueOf(3)
        val four = BigInteger.valueOf(4)
        val five = BigInteger.valueOf(5)
        assertEquals("1", three.modPow(four, five).toString())

        // modInverse: 3 * X = 1 mod 5 -> X = 2
        assertEquals("2", three.modInverse(five).toString())
        
        // Non-coprime should fail
        assertFailsWith<ArithmeticException> {
            BigInteger.valueOf(2).modInverse(BigInteger.valueOf(4))
        }
    }

    @Test
    fun `divideAndRemainder combinations`() {
        val a = BigInteger.valueOf(10)
        val b = BigInteger.valueOf(3)
        
        // 10 / 3 = 3 remainder 1
        val (q1, r1) = a.divideAndRemainder(b)
        assertEquals("3", q1.toString())
        assertEquals("1", r1.toString())
        
        // -10 / 3 = -3 remainder -1
        val (q2, r2) = BigInteger.valueOf(-10).divideAndRemainder(b)
        assertEquals("-3", q2.toString())
        assertEquals("-1", r2.toString())
        
        // 10 / -3 = -3 remainder 1
        val (q3, r3) = a.divideAndRemainder(BigInteger.valueOf(-3))
        assertEquals("-3", q3.toString())
        assertEquals("1", r3.toString())
        
        // -10 / -3 = 3 remainder -1
        val (q4, r4) = BigInteger.valueOf(-10).divideAndRemainder(BigInteger.valueOf(-3))
        assertEquals("3", q4.toString())
        assertEquals("-1", r4.toString())
    }

    @Test
    fun `modPow with negative exponent`() {
        val base = BigInteger.valueOf(3)
        val exp = BigInteger.valueOf(-4) // negative exponent
        val mod = BigInteger.valueOf(5)
        
        // 3^-4 mod 5 = (3^-1)^4 mod 5
        // 3^-1 mod 5 = 2 (since 3 * 2 = 6 = 1 mod 5)
        // 2^4 mod 5 = 16 mod 5 = 1
        assertEquals("1", base.modPow(exp, mod).toString())
    }

    @Test
    fun `negative shifts`() {
        val ten = BigInteger.valueOf(10)
        
        // shiftLeft(-1) is shiftRight(1) -> 10 / 2 = 5
        assertEquals("5", ten.shiftLeft(-1).toString())
        
        // shiftRight(-1) is shiftLeft(1) -> 10 * 2 = 20
        assertEquals("20", ten.shiftRight(-1).toString())
    }

    @Test
    fun `byte array conversion additional cases`() {
        // [0x00, 0xFF, 0xFF] is 65535 (positive)
        val pos65535 = BigInteger.fromByteArray(byteArrayOf(0, 0xFF.toByte(), 0xFF.toByte()))
        assertEquals("65535", pos65535.toString())

        // [0xFF, 0xFF] is -1 (negative)
        val neg1 = BigInteger.fromByteArray(byteArrayOf(0xFF.toByte(), 0xFF.toByte()))
        assertEquals("-1", neg1.toString())
        
        // NumberFormatException on empty array
        assertFailsWith<NumberFormatException> {
            BigInteger.fromByteArray(byteArrayOf())
        }
    }

    @Test
    fun `verify spec defined big integer behaviors`() {
        // Row 1: byte[] {0x80} | Init | | -128
        val r1 = BigInteger.fromByteArray(byteArrayOf(0x80.toByte()))
        assertEquals("-128", r1.toString())

        // Row 2: String "0" | Init | | 0
        val r2 = BigInteger.parseString("0")
        assertEquals("0", r2.toString())

        // Row 3: -10 | remainder | 3 | -1
        val r3 = BigInteger.valueOf(-10).remainder(BigInteger.valueOf(3))
        assertEquals("-1", r3.toString())

        // Row 4: -10 | mod | 3 | 2
        val r4 = BigInteger.valueOf(-10).mod(BigInteger.valueOf(3))
        assertEquals("2", r4.toString())

        // Row 5: 0 | divide | 0 | Error
        assertFailsWith<ArithmeticException> {
            BigInteger.ZERO.divide(BigInteger.ZERO)
        }

        // Row 6: -1 | shiftRight | 1 | -1
        val r6 = BigInteger.valueOf(-1).shiftRight(1)
        assertEquals("-1", r6.toString())

        // Row 7: 15 | testBit | 0 | True
        val r7 = BigInteger.valueOf(15).testBit(0)
        assertTrue(r7)
    }

    @Test
    fun `verify edge cases and additional valueOf boundaries`() {
        // valueOf boundary values
        val maxLong = BigInteger.valueOf(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE.toString(), maxLong.toString())

        val minLong = BigInteger.valueOf(Long.MIN_VALUE)
        assertEquals(Long.MIN_VALUE.toString(), minLong.toString())

        val zero = BigInteger.valueOf(0L)
        assertEquals("0", zero.toString())

        val one = BigInteger.valueOf(1L)
        assertEquals("1", one.toString())

        val minusOne = BigInteger.valueOf(-1L)
        assertEquals("-1", minusOne.toString())
    }

    @Test
    fun `verify bit manipulation with large bit indices`() {
        val zero = BigInteger.ZERO
        
        // setBit(256) should yield 2^256
        val largePowerOfTwo = zero.setBit(256)
        assertTrue(largePowerOfTwo.testBit(256))
        assertFalse(largePowerOfTwo.testBit(255))
        assertFalse(largePowerOfTwo.testBit(0))
        
        val cleared = largePowerOfTwo.clearBit(256)
        assertEquals(BigInteger.ZERO, cleared)
    }

    @Test
    fun `verify modular arithmetic boundary conditions`() {
        val ten = BigInteger.valueOf(10)
        
        // Modulo must be positive
        assertFailsWith<ArithmeticException> {
            ten.mod(BigInteger.ZERO)
        }
        assertFailsWith<ArithmeticException> {
            ten.mod(BigInteger.valueOf(-5))
        }

        // modInverse when not coprime must throw ArithmeticException
        assertFailsWith<ArithmeticException> {
            BigInteger.valueOf(6).modInverse(BigInteger.valueOf(9))
        }

        // modPow with exponent 0 should return 1 mod m
        val base = BigInteger.valueOf(7)
        val mod = BigInteger.valueOf(13)
        assertEquals("1", base.modPow(BigInteger.ZERO, mod).toString())
        
        // modPow with negative exponent on non-coprime base must throw ArithmeticException
        assertFailsWith<ArithmeticException> {
            BigInteger.valueOf(6).modPow(BigInteger.valueOf(-2), BigInteger.valueOf(9))
        }
    }

    @Test
    fun `verify large number arithmetic`() {
        val a = BigInteger.parseString("1".repeat(100))
        val b = BigInteger.parseString("9".repeat(100))
        
        // addition: 111...111 + 999...999 = 111...1110 (100 ones and one zero)
        val expectedAdd = BigInteger.parseString("1".repeat(100) + "0")
        assertEquals(expectedAdd, a.add(b))

        // subtraction: 111...111 - 999...999 = -888...888
        val expectedSub = BigInteger.ZERO.subtract(BigInteger.parseString("8".repeat(100)))
        assertEquals(expectedSub, a.subtract(b))
    }

    @Test
    fun `verify parsing with all valid radices`() {
        for (radix in 2..36) {
            val original = BigInteger.valueOf(123456789L)
            val str = original.toString(radix)
            val parsed = BigInteger.parseString(str, radix)
            assertEquals(original, parsed, "Failed for radix $radix")
        }

        // Invalid characters for given radix
        assertFailsWith<NumberFormatException> {
            BigInteger.parseString("2", 2)
        }
        assertFailsWith<NumberFormatException> {
            BigInteger.parseString("G", 16)
        }
        assertFailsWith<NumberFormatException> {
            BigInteger.parseString("")
        }
    }

    @Test
    fun `verify Kotlin arithmetic operators`() {
        val a = BigInteger.valueOf(100)
        val b = BigInteger.valueOf(3)

        assertEquals(BigInteger.valueOf(103), a + b)
        assertEquals(BigInteger.valueOf(97), a - b)
        assertEquals(BigInteger.valueOf(300), a * b)
        assertEquals(BigInteger.valueOf(33), a / b)
        assertEquals(BigInteger.valueOf(1), a % b)
        assertEquals(BigInteger.valueOf(-100), -a)
        assertEquals(BigInteger.valueOf(0), -BigInteger.ZERO)
    }

    @Test
    fun `constructors from string and long`() {
        assertEquals("123", BigInteger("123").toString())
        assertEquals("-123", BigInteger("-123").toString())
        assertEquals("255", BigInteger("FF", 16).toString())
        assertEquals("123456789", BigInteger(123456789L).toString())
    }

    @Test
    fun `conversion to int and long`() {
        val large = BigInteger("12345678901234567890")
        assertEquals(0xEB1F0AD2.toInt(), large.toInt()) // Low 32 bits of 12345678901234567890
        
        // toLong() should truncate or wrap if it doesn't fit
        assertEquals(123L, BigInteger("123").toLong())
        assertEquals(-123L, BigInteger("-123").toLong())
        
        assertEquals(123, BigInteger("123").toInt())
        assertEquals(-123, BigInteger("-123").toInt())
        
        // Overflow cases (following Java BigInteger behavior: return low-order bits)
        val overInt = BigInteger.valueOf(Int.MAX_VALUE.toLong() + 1L)
        assertEquals(Int.MIN_VALUE, overInt.toInt())
        
        val overLong = BigInteger("18446744073709551616") // 2^64
        assertEquals(0L, overLong.toLong())
    }
}
