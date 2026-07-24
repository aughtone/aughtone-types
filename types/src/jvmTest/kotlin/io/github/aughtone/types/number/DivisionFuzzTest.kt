package io.github.aughtone.types.number

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigInteger as JdkBigInteger

class DivisionFuzzTest {

    private val seed = 987654321

    private fun randomJdk(bits: Int, rnd: Random): JdkBigInteger {
        val bytes = ByteArray((bits + 7) / 8)
        rnd.nextBytes(bytes)
        return JdkBigInteger(1, bytes)
    }

    private fun check(uJdk: JdkBigInteger, vJdk: JdkBigInteger) {
        val u = BigInteger.parseString(uJdk.toString())
        val v = BigInteger.parseString(vJdk.toString())
        val (q, r) = u.divideAndRemainder(v)
        assertEquals(uJdk.divide(vJdk).toString(), q.toString(), "quotient for $uJdk / $vJdk")
        assertEquals(uJdk.remainder(vJdk).toString(), r.toString(), "remainder for $uJdk / $vJdk")
        assertEquals(uJdk.divide(vJdk).signum(), q.signum, "quotient signum for $uJdk / $vJdk")
        assertEquals(uJdk.remainder(vJdk).signum(), r.signum, "remainder signum for $uJdk / $vJdk")
    }

    @Test
    fun `seeded differential division fuzz`() {
        val rnd = Random(seed)
        var count = 0
        while (count < 3000) {
            val uBits = 1 + rnd.nextInt(256)
            val vBits = 1 + rnd.nextInt(160)
            var u = randomJdk(uBits, rnd)
            var v = randomJdk(vBits, rnd)
            if (v.signum() == 0) continue
            // Bias towards 0xFFFFFFFF quotient digits, the Knuth qHat overflow case.
            if (count % 5 == 0) {
                u = v.multiply(JdkBigInteger.valueOf(0xFFFFFFFFL)).add(randomJdk(1 + rnd.nextInt(vBits), rnd))
            }
            if (count % 3 == 0) u = u.negate()
            if (count % 7 == 0) v = v.negate()
            if (u.signum() == 0) continue
            check(u, v)
            count++
        }
    }

    @Test
    fun `seeded single word divisor fuzz`() {
        val rnd = Random(seed + 1)
        repeat(2000) {
            // Divisor in the range two to the 31 up to two to the 32: one magnitude word, high bit set.
            val d = JdkBigInteger.valueOf(0x80000000L + (rnd.nextLong() and 0x7FFFFFFFL))
            val u = randomJdk(1 + rnd.nextInt(224), rnd)
            if (u.signum() != 0) check(u, d)
        }
    }

    @Test
    fun `seeded shift fuzz including shift to zero`() {
        val rnd = Random(seed + 2)
        repeat(2000) {
            var u = randomJdk(1 + rnd.nextInt(96), rnd)
            if (u.signum() == 0) u = JdkBigInteger.ONE
            if (rnd.nextBoolean()) u = u.negate()
            val shift = rnd.nextInt(128)
            val ours = BigInteger.parseString(u.toString()).shiftRight(shift)
            val jdk = u.shiftRight(shift)
            assertEquals(jdk.toString(), ours.toString(), "shiftRight value for $u >> $shift")
            assertEquals(jdk.signum(), ours.signum, "shiftRight signum for $u >> $shift")
            if (jdk.signum() == 0) {
                assertEquals(BigInteger.ZERO, ours, "shiftRight zero must be canonical for $u >> $shift")
            }
        }
    }
}
