/*
 * Copyright (c) 1996, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * Portions Copyright (c) 1995  Colin Plumb.  All rights reserved.
 */
package io.github.aughtone.types.math

import java.util.Random
import kotlin.concurrent.Volatile
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Immutable arbitrary-precision integers.  All operations behave as if
 * BigIntegers were represented in two's-complement notation (like Java's
 * primitive integer types).  BigInteger provides analogues to all of Java's
 * primitive integer operators, and all relevant methods from java.lang.Math.
 * Additionally, BigInteger provides operations for modular arithmetic, GCD
 * calculation, primality testing, prime generation, bit manipulation,
 * and a few other miscellaneous operations.
 *
 *
 * Semantics of arithmetic operations exactly mimic those of Java's integer
 * arithmetic operators, as defined in *The Java Language Specification*.
 * For example, division by zero throws an `ArithmeticException`, and
 * division of a negative by a positive yields a negative (or zero) remainder.
 * All of the details in the Spec concerning overflow are ignored, as
 * BigIntegers are made as large as necessary to accommodate the results of an
 * operation.
 *
 *
 * Semantics of shift operations extend those of Java's shift operators
 * to allow for negative shift distances.  A right-shift with a negative
 * shift distance results in a left shift, and vice-versa.  The unsigned
 * right shift operator (`>>>`) is omitted, as this operation makes
 * little sense in combination with the "infinite word size" abstraction
 * provided by this class.
 *
 *
 * Semantics of bitwise logical operations exactly mimic those of Java's
 * bitwise integer operators.  The binary operators (`and`,
 * `or`, `xor`) implicitly perform sign extension on the shorter
 * of the two operands prior to performing the operation.
 *
 *
 * Comparison operations perform signed integer comparisons, analogous to
 * those performed by Java's relational and equality operators.
 *
 *
 * Modular arithmetic operations are provided to compute residues, perform
 * exponentiation, and compute multiplicative inverses.  These methods always
 * return a non-negative result, between `0` and `(modulus - 1)`,
 * inclusive.
 *
 *
 * Bit operations operate on a single bit of the two's-complement
 * representation of their operand.  If necessary, the operand is sign-
 * extended so that it contains the designated bit.  None of the single-bit
 * operations can produce a BigInteger with a different sign from the
 * BigInteger being operated on, as they affect only a single bit, and the
 * "infinite word size" abstraction provided by this class ensures that there
 * are infinitely many "virtual sign bits" preceding each BigInteger.
 *
 *
 * For the sake of brevity and clarity, pseudo-code is used throughout the
 * descriptions of BigInteger methods.  The pseudo-code expression
 * `(i + j)` is shorthand for "a BigInteger whose value is
 * that of the BigInteger `i` plus that of the BigInteger `j`."
 * The pseudo-code expression `(i == j)` is shorthand for
 * "`true` if and only if the BigInteger `i` represents the same
 * value as the BigInteger `j`."  Other pseudo-code expressions are
 * interpreted similarly.
 *
 *
 * All methods and constructors in this class throw
 * `NullPointerException` when passed
 * a null object reference for any input parameter.
 *
 * @see BigDecimal
 *
 * @author  Josh Bloch
 * @author  Michael McCloskey
 * @since JDK1.1
 */
class BigInteger : Number, Comparable<BigInteger?> {
    /**
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero *must* have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     *
     * @serial
     */
    val signum: Int

    /**
     * The magnitude of this BigInteger, in *big-endian* order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int (`mag[0]`) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each BigInteger
     * value.  Note that this implies that the BigInteger zero has a
     * zero-length mag array.
     */
    @JvmField
    val mag: IntArray

    // These "redundant fields" are initialized with recognizable nonsense
    // values, and cached the first time they are needed (or never, if they
    // aren't needed).
    /**
     * One plus the bitCount of this BigInteger. Zeros means unitialized.
     *
     * @serial
     * @see .bitCount
     *
     */
    @Deprecated
    @Deprecated(
        """Deprecated since logical value is offset from stored
      value and correction factor is applied in accessor method."""
    )
    private var bitCount = 0

    /**
     * One plus the bitLength of this BigInteger. Zeros means unitialized.
     * (either value is acceptable).
     *
     * @serial
     * @see .bitLength
     */
    @Deprecated
    @Deprecated(
        """Deprecated since logical value is offset from stored
      value and correction factor is applied in accessor method."""
    )
    private var bitLength = 0

    /**
     * Two plus the lowest set bit of this BigInteger, as returned by
     * getLowestSetBit().
     *
     * @serial
     * @see .getLowestSetBit
     *
     */
    @Deprecated
    @Deprecated(
        """Deprecated since logical value is offset from stored
      value and correction factor is applied in accessor method."""
    )
    var lowestSetBit: Int = 0
        /**
         * Returns the index of the rightmost (lowest-order) one bit in this
         * BigInteger (the number of zero bits to the right of the rightmost
         * one bit).  Returns -1 if this BigInteger contains no one bits.
         * (Computes `(this==0? -1 : log2(this & -this))`.)
         *
         * @return index of the rightmost one bit in this BigInteger.
         */
        get() {
//            @SuppressWarnings("deprecation")
            var lsb = field - 2
            if (lsb == -2) {  // lowestSetBit not initialized yet
                lsb = 0
                if (signum == 0) {
                    lsb -= 1
                } else {
                    // Search for lowest order nonzero int
                    var i: Int
                    var b: Int
                    i = 0
                    while ((getInt(i).also { b = it }) == 0) {
                        i++
                    }
                    lsb += (i shl 5) + b.countTrailingZeroBits() //Integer.numberOfTrailingZeros(b)
                }
                field = lsb + 2
            }
            return lsb
        }
        private set

    /**
     * Two plus the index of the lowest-order int in the magnitude of this
     * BigInteger that contains a nonzero int, or -2 (either value is acceptable).
     * The least significant int has int-number 0, the next int in order of
     * increasing significance has int-number 1, and so forth.
     */
    @Deprecated
    @Deprecated(
        """Deprecated since logical value is offset from stored
      value and correction factor is applied in accessor method."""
    )
    private var firstNonzeroIntNum = 0

    //Constructors
    /**
     * Translates a byte array containing the two's-complement binary
     * representation of a BigInteger into a BigInteger.  The input array is
     * assumed to be in *big-endian* byte-order: the most significant
     * byte is in the zeroth element.
     *
     * @param  val big-endian two's-complement binary representation of
     * BigInteger.
     * @throws NumberFormatException `val` is zero bytes long.
     */
    constructor(`val`: ByteArray) {
        if (`val`.size == 0) throw NumberFormatException("Zero length BigInteger")

        if (`val`[0] < 0) {
            mag = makePositive(`val`)
            signum = -1
        } else {
            mag = stripLeadingZeroBytes(`val`)
            signum = (if (mag.size == 0) 0 else 1)
        }
    }

    /**
     * This private constructor translates an int array containing the
     * two's-complement binary representation of a BigInteger into a
     * BigInteger. The input array is assumed to be in *big-endian*
     * int-order: the most significant int is in the zeroth element.
     */
    private constructor(`val`: IntArray) {
        if (`val`.size == 0) throw NumberFormatException("Zero length BigInteger")

        if (`val`[0] < 0) {
            mag = makePositive(`val`)
            signum = -1
        } else {
            mag = trustedStripLeadingZeroInts(`val`)
            signum = (if (mag.size == 0) 0 else 1)
        }
    }

    /**
     * Translates the sign-magnitude representation of a BigInteger into a
     * BigInteger.  The sign is represented as an integer signum value: -1 for
     * negative, 0 for zero, or 1 for positive.  The magnitude is a byte array
     * in *big-endian* byte-order: the most significant byte is in the
     * zeroth element.  A zero-length magnitude array is permissible, and will
     * result in a BigInteger value of 0, whether signum is -1, 0 or 1.
     *
     * @param  signum signum of the number (-1 for negative, 0 for zero, 1
     * for positive).
     * @param  magnitude big-endian binary representation of the magnitude of
     * the number.
     * @throws NumberFormatException `signum` is not one of the three
     * legal values (-1, 0, and 1), or `signum` is 0 and
     * `magnitude` contains one or more non-zero bytes.
     */
    constructor(signum: Int, magnitude: ByteArray) {
        this.mag = stripLeadingZeroBytes(magnitude)

        if (signum < -1 || signum > 1) throw (NumberFormatException("Invalid signum value"))

        if (this.mag.size == 0) {
            this.signum = 0
        } else {
            if (signum == 0) throw (NumberFormatException("signum-magnitude mismatch"))
            this.signum = signum
        }
    }

    /**
     * A constructor for internal use that translates the sign-magnitude
     * representation of a BigInteger into a BigInteger. It checks the
     * arguments and copies the magnitude so this constructor would be
     * safe for external use.
     */
    private constructor(signum: Int, magnitude: IntArray) {
        this.mag = stripLeadingZeroInts(magnitude)

        if (signum < -1 || signum > 1) throw (NumberFormatException("Invalid signum value"))

        if (this.mag.size == 0) {
            this.signum = 0
        } else {
            if (signum == 0) throw (NumberFormatException("signum-magnitude mismatch"))
            this.signum = signum
        }
    }

    /**
     * Translates the String representation of a BigInteger in the
     * specified radix into a BigInteger.  The String representation
     * consists of an optional minus or plus sign followed by a
     * sequence of one or more digits in the specified radix.  The
     * character-to-digit mapping is provided by `Character.digit`.  The String may not contain any extraneous
     * characters (whitespace, for example).
     *
     * @param val String representation of BigInteger.
     * @param radix radix to be used in interpreting `val`.
     * @throws NumberFormatException `val` is not a valid representation
     * of a BigInteger in the specified radix, or `radix` is
     * outside the range from [Character.MIN_RADIX] to
     * [Character.MAX_RADIX], inclusive.
     * @see Character.digit
     */
    constructor(`val`: String, radix: Int) {
        var cursor = 0
        val numDigits: Int
        val len: Int = `val`.length()

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) throw NumberFormatException(
            "Radix out of range"
        )
        if (len == 0) throw NumberFormatException("Zero length BigInteger")

        // Check for at most one leading sign
        var sign = 1
        val index1 = `val`.lastIndexOf('-')
        val index2 = `val`.lastIndexOf('+')
        if ((index1 + index2) <= -1) {
            // No leading sign character or at most one leading sign character
            if (index1 == 0 || index2 == 0) {
                cursor = 1
                if (len == 1) throw NumberFormatException("Zero length BigInteger")
            }
            if (index1 == 0) sign = -1
        } else throw NumberFormatException("Illegal embedded sign character")

        // Skip leading zeros and compute number of digits in magnitude
        while (cursor < len &&
            Character.digit(`val`.charAt(cursor), radix) === 0
        ) cursor++
        if (cursor == len) {
            signum = 0
            mag = ZERO.mag
            return
        }

        numDigits = len - cursor
        signum = sign

        // Pre-allocate array of expected size. May be too large but can
        // never be too small. Typically exact.
        val numBits = (((numDigits * bitsPerDigit!![radix]) ushr 10) + 1).toInt()
        val numWords = (numBits + 31) ushr 5
        val magnitude = IntArray(numWords)

        // Process first (potentially short) digit group
        var firstGroupLen = numDigits % digitsPerInt!![radix]
        if (firstGroupLen == 0) firstGroupLen = digitsPerInt[radix]
        var group: String? = `val`.substring(cursor, firstGroupLen.let { cursor += it; cursor })
        magnitude[numWords - 1] = Integer.parseInt(group, radix)
        if (magnitude[numWords - 1] < 0) throw NumberFormatException("Illegal digit")

        // Process remaining digit groups
        val superRadix = intRadix!![radix]
        var groupVal = 0
        while (cursor < len) {
            group = `val`.substring(cursor, digitsPerInt[radix].let { cursor += it; cursor })
            groupVal = Integer.parseInt(group, radix)
            if (groupVal < 0) throw NumberFormatException("Illegal digit")
            destructiveMulAdd(magnitude, superRadix, groupVal)
        }
        // Required for cases where the array was overallocated.
        mag = trustedStripLeadingZeroInts(magnitude)
    }

    // Constructs a new BigInteger using a char array with radix=10
    internal constructor(`val`: CharArray) {
        var cursor = 0
        val numDigits: Int
        val len = `val`.size

        // Check for leading minus sign
        var sign = 1
        if (`val`[0] == '-') {
            if (len == 1) throw NumberFormatException("Zero length BigInteger")
            sign = -1
            cursor = 1
        } else if (`val`[0] == '+') {
            if (len == 1) throw NumberFormatException("Zero length BigInteger")
            cursor = 1
        }

        // Skip leading zeros and compute number of digits in magnitude
        while (cursor < len && Character.digit(`val`[cursor], 10) === 0) cursor++
        if (cursor == len) {
            signum = 0
            mag = ZERO.mag
            return
        }

        numDigits = len - cursor
        signum = sign

        // Pre-allocate array of expected size
        val numWords: Int
        if (len < 10) {
            numWords = 1
        } else {
            val numBits = (((numDigits * bitsPerDigit!![10]) ushr 10) + 1).toInt()
            numWords = (numBits + 31) ushr 5
        }
        val magnitude = IntArray(numWords)

        // Process first (potentially short) digit group
        var firstGroupLen = numDigits % digitsPerInt!![10]
        if (firstGroupLen == 0) firstGroupLen = digitsPerInt[10]
        magnitude[numWords - 1] =
            parseInt(`val`, cursor, firstGroupLen.let { cursor += it; cursor })

        // Process remaining digit groups
        while (cursor < len) {
            val groupVal = parseInt(`val`, cursor, digitsPerInt[10].let { cursor += it; cursor })
            destructiveMulAdd(magnitude, intRadix!![10], groupVal)
        }
        mag = trustedStripLeadingZeroInts(magnitude)
    }

    // Create an integer with the digits between the two indexes
    // Assumes start < end. The result may be negative, but it
    // is to be treated as an unsigned value.
    private fun parseInt(source: CharArray, start: Int, end: Int): Int {
        var start = start
        var result: Int = Character.digit(source[start++], 10)
        if (result == -1) throw NumberFormatException(String(source))

        for (index in start..<end) {
            val nextVal: Int = Character.digit(source[index], 10)
            if (nextVal == -1) throw NumberFormatException(String(source))
            result = 10 * result + nextVal
        }

        return result
    }

    /**
     * Translates the decimal String representation of a BigInteger into a
     * BigInteger.  The String representation consists of an optional minus
     * sign followed by a sequence of one or more decimal digits.  The
     * character-to-digit mapping is provided by `Character.digit`.
     * The String may not contain any extraneous characters (whitespace, for
     * example).
     *
     * @param val decimal String representation of BigInteger.
     * @throws NumberFormatException `val` is not a valid representation
     * of a BigInteger.
     * @see Character.digit
     */
    constructor(`val`: String) : this(`val`, 10)

    /**
     * Constructs a randomly generated BigInteger, uniformly distributed over
     * the range 0 to (2<sup>`numBits`</sup> - 1), inclusive.
     * The uniformity of the distribution assumes that a fair source of random
     * bits is provided in `rnd`.  Note that this constructor always
     * constructs a non-negative BigInteger.
     *
     * @param  numBits maximum bitLength of the new BigInteger.
     * @param  rnd source of randomness to be used in computing the new
     * BigInteger.
     * @throws IllegalArgumentException `numBits` is negative.
     * @see .bitLength
     */
    constructor(numBits: Int, rnd: Random) : this(1, randomBits(numBits, rnd))

    /**
     * Constructs a randomly generated positive BigInteger that is probably
     * prime, with the specified bitLength.
     *
     *
     * It is recommended that the [probablePrime][.probablePrime]
     * method be used in preference to this constructor unless there
     * is a compelling need to specify a certainty.
     *
     * @param  bitLength bitLength of the returned BigInteger.
     * @param  certainty a measure of the uncertainty that the caller is
     * willing to tolerate.  The probability that the new BigInteger
     * represents a prime number will exceed
     * (1 - 1/2<sup>`certainty`</sup>).  The execution time of
     * this constructor is proportional to the value of this parameter.
     * @param  rnd source of random bits used to select candidates to be
     * tested for primality.
     * @throws ArithmeticException `bitLength < 2`.
     * @see .bitLength
     */
    constructor(bitLength: Int, certainty: Int, rnd: Random) {
        val prime: BigInteger

        if (bitLength < 2) throw ArithmeticException("bitLength < 2")
        // The cutoff of 95 was chosen empirically for best performance
        prime = (if (bitLength < 95)
            smallPrime(bitLength, certainty, rnd)
        else
            largePrime(bitLength, certainty, rnd))
        signum = 1
        mag = prime.mag
    }

    /**
     * Returns the first integer greater than this `BigInteger` that
     * is probably prime.  The probability that the number returned by this
     * method is composite does not exceed 2<sup>-100</sup>. This method will
     * never skip over a prime when searching: if it returns `p`, there
     * is no prime `q` such that `this < q < p`.
     *
     * @return the first integer greater than this `BigInteger` that
     * is probably prime.
     * @throws ArithmeticException `this < 0`.
     * @since 1.5
     */
    fun nextProbablePrime(): BigInteger? {
        if (this.signum < 0) throw ArithmeticException("start < 0: " + this)

        // Handle trivial cases
        if ((this.signum == 0) || this.equals(ONE)) return TWO

        var result = this.add(ONE)

        // Fastpath for small numbers
        if (result.bitLength() < SMALL_PRIME_THRESHOLD) {
            // Ensure an odd number

            if (!result.testBit(0)) result = result.add(ONE)

            while (true) {
                // Do cheap "pre-test" if applicable
                if (result.bitLength() > 6) {
                    val r = result.remainder(SMALL_PRIME_PRODUCT).longValue()
                    if ((r % 3 == 0L) || (r % 5 == 0L) || (r % 7 == 0L) || (r % 11 == 0L) ||
                        (r % 13 == 0L) || (r % 17 == 0L) || (r % 19 == 0L) || (r % 23 == 0L) ||
                        (r % 29 == 0L) || (r % 31 == 0L) || (r % 37 == 0L) || (r % 41 == 0L)
                    ) {
                        result = result.add(TWO)
                        continue  // Candidate is composite; try another
                    }
                }

                // All candidates of bitLength 2 and 3 are prime by this point
                if (result.bitLength() < 4) return result

                // The expensive test
                if (result.primeToCertainty(DEFAULT_PRIME_CERTAINTY, null)) return result

                result = result.add(TWO)
            }
        }

        // Start at previous even number
        if (result.testBit(0)) result = result.subtract(ONE)

        // Looking for the next large prime
        val searchLen = (result.bitLength() / 20) * 64

        while (true) {
            val searchSieve: io.github.aughtone.types.math.BitSieve =
                io.github.aughtone.types.math.BitSieve(result, searchLen)
            val candidate: BigInteger? = searchSieve.retrieve(
                result,
                DEFAULT_PRIME_CERTAINTY, null
            )
            if (candidate != null) return candidate
            result = result.add(valueOf((2 * searchLen).toLong()))
        }
    }

    /**
     * Returns `true` if this BigInteger is probably prime,
     * `false` if it's definitely composite.
     *
     * This method assumes bitLength > 2.
     *
     * @param  certainty a measure of the uncertainty that the caller is
     * willing to tolerate: if the call returns `true`
     * the probability that this BigInteger is prime exceeds
     * `(1 - 1/2<sup>certainty</sup>)`.  The execution time of
     * this method is proportional to the value of this parameter.
     * @return `true` if this BigInteger is probably prime,
     * `false` if it's definitely composite.
     */
    fun primeToCertainty(certainty: Int, random: Random?): Boolean {
        var rounds = 0
        val n: Int = (Math.min(certainty, Integer.MAX_VALUE - 1) + 1) / 2

        // The relationship between the certainty and the number of rounds
        // we perform is given in the draft standard ANSI X9.80, "PRIME
        // NUMBER GENERATION, PRIMALITY TESTING, AND PRIMALITY CERTIFICATES".
        val sizeInBits = this.bitLength()
        if (sizeInBits < 100) {
            rounds = 50
            rounds = if (n < rounds) n else rounds
            return passesMillerRabin(rounds, random)
        }

        if (sizeInBits < 256) {
            rounds = 27
        } else if (sizeInBits < 512) {
            rounds = 15
        } else if (sizeInBits < 768) {
            rounds = 8
        } else if (sizeInBits < 1024) {
            rounds = 4
        } else {
            rounds = 2
        }
        rounds = if (n < rounds) n else rounds

        return passesMillerRabin(rounds, random) && passesLucasLehmer()
    }

    /**
     * Returns true iff this BigInteger is a Lucas-Lehmer probable prime.
     *
     * The following assumptions are made:
     * This BigInteger is a positive, odd number.
     */
    private fun passesLucasLehmer(): Boolean {
        val thisPlusOne = this.add(ONE)

        // Step 1
        var d = 5
        while (jacobiSymbol(d, this) != -1) {
            // 5, -7, 9, -11, ...
            d = if (d < 0) Math.abs(d) + 2 else -(d + 2)
        }

        // Step 2
        val u = lucasLehmerSequence(d, thisPlusOne, this)

        // Step 3
        return u.mod(this).equals(ZERO)
    }

    /**
     * Returns true iff this BigInteger passes the specified number of
     * Miller-Rabin tests. This test is taken from the DSA spec (NIST FIPS
     * 186-2).
     *
     * The following assumptions are made:
     * This BigInteger is a positive, odd number greater than 2.
     * iterations<=50.
     */
    private fun passesMillerRabin(iterations: Int, rnd: Random?): Boolean {
        // Find a and m such that m is odd and this == 1 + 2**a * m
        var rnd: Random? = rnd
        val thisMinusOne = this.subtract(ONE)
        var m = thisMinusOne
        val a = m.lowestSetBit
        m = m.shiftRight(a)

        // Do the tests
        if (rnd == null) {
            rnd = secureRandom
        }
        for (i in 0..<iterations) {
            // Generate a uniform random on (1, this)
            var b: BigInteger?
            do {
                b = BigInteger(this.bitLength(), rnd)
            } while (b.compareTo(ONE) <= 0 || b.compareTo(this) >= 0)

            var j = 0
            var z = b.modPow(m, this)
            while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                if (j > 0 && z.equals(ONE) || ++j == a) return false
                z = z.modPow(TWO, this)
            }
        }
        return true
    }

    /**
     * This internal constructor differs from its public cousin
     * with the arguments reversed in two ways: it assumes that its
     * arguments are correct, and it doesn't copy the magnitude array.
     */
    internal constructor(magnitude: IntArray, signum: Int) {
        this.signum = (if (magnitude.size == 0) 0 else signum)
        this.mag = magnitude
    }

    /**
     * This private constructor is for internal use and assumes that its
     * arguments are correct.
     */
    private constructor(magnitude: ByteArray, signum: Int) {
        this.signum = (if (magnitude.size == 0) 0 else signum)
        this.mag = stripLeadingZeroBytes(magnitude)
    }

    /**
     * Constructs a BigInteger with the specified value, which may not be zero.
     */
    private constructor(`val`: Long) {
        var `val` = `val`
        if (`val` < 0) {
            `val` = -`val`
            signum = -1
        } else {
            signum = 1
        }

        val highWord = (`val` ushr 32).toInt()
        if (highWord == 0) {
            mag = IntArray(1)
            mag[0] = `val`.toInt()
        } else {
            mag = IntArray(2)
            mag[0] = highWord
            mag[1] = `val`.toInt()
        }
    }

    // Arithmetic Operations
    /**
     * Returns a BigInteger whose value is `(this + val)`.
     *
     * @param  val value to be added to this BigInteger.
     * @return `this + val`
     */
    fun add(`val`: BigInteger): BigInteger {
        if (`val`.signum == 0) return this
        if (signum == 0) return `val`
        if (`val`.signum == signum) return BigInteger(add(mag, `val`.mag)!!, signum)

        val cmp = compareMagnitude(`val`)
        if (cmp == 0) return ZERO
        var resultMag = (if (cmp > 0)
            subtract(mag, `val`.mag)
        else
            subtract(`val`.mag, mag))
        resultMag = trustedStripLeadingZeroInts(resultMag)

        return BigInteger(resultMag, if (cmp == signum) 1 else -1)
    }

    /**
     * Returns a BigInteger whose value is `(this - val)`.
     *
     * @param  val value to be subtracted from this BigInteger.
     * @return `this - val`
     */
    fun subtract(`val`: BigInteger): BigInteger {
        if (`val`.signum == 0) return this
        if (signum == 0) return `val`.negate()
        if (`val`.signum != signum) return BigInteger(add(mag, `val`.mag)!!, signum)

        val cmp = compareMagnitude(`val`)
        if (cmp == 0) return ZERO
        var resultMag = (if (cmp > 0)
            subtract(mag, `val`.mag)
        else
            subtract(`val`.mag, mag))
        resultMag = trustedStripLeadingZeroInts(resultMag)
        return BigInteger(resultMag, if (cmp == signum) 1 else -1)
    }

    /**
     * Returns a BigInteger whose value is `(this * val)`.
     *
     * @param  val value to be multiplied by this BigInteger.
     * @return `this * val`
     */
    fun multiply(`val`: BigInteger): BigInteger? {
        if (`val`.signum == 0 || signum == 0) return ZERO

        var result = multiplyToLen(
            mag, mag.size,
            `val`.mag, `val`.mag.size, null
        )
        result = trustedStripLeadingZeroInts(result)
        return BigInteger(result, if (signum == `val`.signum) 1 else -1)
    }

    /**
     * Package private methods used by BigDecimal code to multiply a BigInteger
     * with a long. Assumes v is not equal to INFLATED.
     */
    fun multiply(v: Long): BigInteger? {
        var v = v
        if (v == 0L || signum == 0) return ZERO
        if (v == BigDecimal.INFLATED) return multiply(valueOf(v))
        val rsign = (if (v > 0) signum else -signum)
        if (v < 0) v = -v
        val dh = v ushr 32 // higher order bits
        val dl = v and LONG_MASK // lower order bits

        val xlen = mag.size
        val value = mag
        var rmag = if (dh == 0L) (IntArray(xlen + 1)) else (IntArray(xlen + 2))
        var carry: Long = 0
        var rstart = rmag.size - 1
        for (i in xlen - 1 downTo 0) {
            val product = (value[i].toLong() and LONG_MASK) * dl + carry
            rmag[rstart--] = product.toInt()
            carry = product ushr 32
        }
        rmag[rstart] = carry.toInt()
        if (dh != 0L) {
            carry = 0
            rstart = rmag.size - 2
            for (i in xlen - 1 downTo 0) {
                val product = (value[i].toLong() and LONG_MASK) * dh +
                        (rmag[rstart].toLong() and LONG_MASK) + carry
                rmag[rstart--] = product.toInt()
                carry = product ushr 32
            }
            rmag[0] = carry.toInt()
        }
        if (carry == 0L) rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.size)
        return BigInteger(rmag, rsign)
    }

    /**
     * Multiplies int arrays x and y to the specified lengths and places
     * the result into z. There will be no leading zeros in the resultant array.
     */
    private fun multiplyToLen(
        x: IntArray,
        xlen: Int,
        y: IntArray,
        ylen: Int,
        z: IntArray?
    ): IntArray {
        var z = z
        val xstart = xlen - 1
        val ystart = ylen - 1

        if (z == null || z.size < (xlen + ylen)) z = IntArray(xlen + ylen)

        var carry: Long = 0
        var j = ystart
        var k = ystart + 1 + xstart
        while (j >= 0) {
            val product = (y[j].toLong() and LONG_MASK) *
                    (x[xstart].toLong() and LONG_MASK) + carry
            z[k] = product.toInt()
            carry = product ushr 32
            j--
            k--
        }
        z[xstart] = carry.toInt()

        for (i in xstart - 1 downTo 0) {
            carry = 0
            var j = ystart
            var k = ystart + 1 + i
            while (j >= 0) {
                val product = (y[j].toLong() and LONG_MASK) *
                        (x[i].toLong() and LONG_MASK) +
                        (z[k].toLong() and LONG_MASK) + carry
                z[k] = product.toInt()
                carry = product ushr 32
                j--
                k--
            }
            z[i] = carry.toInt()
        }
        return z
    }

    /**
     * Returns a BigInteger whose value is `(this<sup>2</sup>)`.
     *
     * @return `this<sup>2</sup>`
     */
    private fun square(): BigInteger? {
        if (signum == 0) return ZERO
        val z = squareToLen(mag, mag.size, null)
        return BigInteger(trustedStripLeadingZeroInts(z), 1)
    }

    /**
     * Returns a BigInteger whose value is `(this / val)`.
     *
     * @param  val value by which this BigInteger is to be divided.
     * @return `this / val`
     * @throws ArithmeticException if `val` is zero.
     */
    fun divide(`val`: BigInteger): BigInteger {
        val q: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger()
        val a: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(this.mag)
        val b: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(`val`.mag)

        a.divide(b, q)
        return q.toBigInteger(if (this.signum == `val`.signum) 1 else -1)
    }

    /**
     * Returns an array of two BigIntegers containing `(this / val)`
     * followed by `(this % val)`.
     *
     * @param  val value by which this BigInteger is to be divided, and the
     * remainder computed.
     * @return an array of two BigIntegers: the quotient `(this / val)`
     * is the initial element, and the remainder `(this % val)`
     * is the final element.
     * @throws ArithmeticException if `val` is zero.
     */
    fun divideAndRemainder(`val`: BigInteger): Array<BigInteger?> {
        val result = kotlin.arrayOfNulls<BigInteger>(2)
        val q: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger()
        val a: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(this.mag)
        val b: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(`val`.mag)
        val r: io.github.aughtone.types.math.MutableBigInteger = a.divide(b, q)
        result[0] = q.toBigInteger(if (this.signum == `val`.signum) 1 else -1)
        result[1] = r.toBigInteger(this.signum)
        return result
    }

    /**
     * Returns a BigInteger whose value is `(this % val)`.
     *
     * @param  val value by which this BigInteger is to be divided, and the
     * remainder computed.
     * @return `this % val`
     * @throws ArithmeticException if `val` is zero.
     */
    fun remainder(`val`: BigInteger): BigInteger {
        val q: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger()
        val a: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(this.mag)
        val b: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(`val`.mag)

        return a.divide(b, q).toBigInteger(this.signum)
    }

    /**
     * Returns a BigInteger whose value is <tt>(this<sup>exponent</sup>)</tt>.
     * Note that `exponent` is an integer rather than a BigInteger.
     *
     * @param  exponent exponent to which this BigInteger is to be raised.
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException `exponent` is negative.  (This would
     * cause the operation to yield a non-integer value.)
     */
    fun pow(exponent: Int): BigInteger? {
        var exponent = exponent
        if (exponent < 0) throw ArithmeticException("Negative exponent")
        if (signum == 0) return (if (exponent == 0) ONE else this)

        // Perform exponentiation using repeated squaring trick
        val newSign = (if (signum < 0 && (exponent and 1) == 1) -1 else 1)
        var baseToPow2 = this.mag
        var result = intArrayOf(1)

        while (exponent != 0) {
            if ((exponent and 1) == 1) {
                result = multiplyToLen(
                    result, result.size,
                    baseToPow2, baseToPow2.size, null
                )
                result = trustedStripLeadingZeroInts(result)
            }
            if ((1.let { exponent = exponent ushr it; exponent }) != 0) {
                baseToPow2 = squareToLen(baseToPow2, baseToPow2.size, null)
                baseToPow2 = trustedStripLeadingZeroInts(baseToPow2)
            }
        }
        return BigInteger(result, newSign)
    }

    /**
     * Returns a BigInteger whose value is the greatest common divisor of
     * `abs(this)` and `abs(val)`.  Returns 0 if
     * `this==0 && val==0`.
     *
     * @param  val value with which the GCD is to be computed.
     * @return `GCD(abs(this), abs(val))`
     */
    fun gcd(`val`: BigInteger): BigInteger {
        if (`val`.signum == 0) return this.abs()
        else if (this.signum == 0) return `val`.abs()

        val a: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(this)
        val b: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(`val`)

        val result: io.github.aughtone.types.math.MutableBigInteger = a.hybridGCD(b)

        return result.toBigInteger(1)
    }

    /**
     * Returns a BigInteger whose value is the absolute value of this
     * BigInteger.
     *
     * @return `abs(this)`
     */
    fun abs(): BigInteger {
        return (if (signum >= 0) this else this.negate())
    }

    /**
     * Returns a BigInteger whose value is `(-this)`.
     *
     * @return `-this`
     */
    fun negate(): BigInteger {
        return BigInteger(this.mag, -this.signum)
    }

    /**
     * Returns the signum function of this BigInteger.
     *
     * @return -1, 0 or 1 as the value of this BigInteger is negative, zero or
     * positive.
     */
    fun signum(): Int {
        return this.signum
    }

    // Modular Arithmetic Operations
    /**
     * Returns a BigInteger whose value is `(this mod m`).  This method
     * differs from `remainder` in that it always returns a
     * *non-negative* BigInteger.
     *
     * @param  m the modulus.
     * @return `this mod m`
     * @throws ArithmeticException `m`  0
     * @see .remainder
     */
    fun mod(m: BigInteger): BigInteger {
        if (m.signum <= 0) throw ArithmeticException("BigInteger: modulus not positive")

        val result = this.remainder(m)
        return (if (result.signum >= 0) result else result.add(m))
    }

    /**
     * Returns a BigInteger whose value is
     * <tt>(this<sup>exponent</sup> mod m)</tt>.  (Unlike `pow`, this
     * method permits negative exponents.)
     *
     * @param  exponent the exponent.
     * @param  m the modulus.
     * @return <tt>this<sup>exponent</sup> mod m</tt>
     * @throws ArithmeticException `m`  0 or the exponent is
     * negative and this BigInteger is not *relatively
     * prime* to `m`.
     * @see .modInverse
     */
    fun modPow(exponent: BigInteger, m: BigInteger): BigInteger {
        var exponent = exponent
        if (m.signum <= 0) throw ArithmeticException("BigInteger: modulus not positive")

        // Trivial cases
        if (exponent.signum == 0) return (if (m.equals(ONE)) ZERO else ONE)

        if (this.equals(ONE)) return (if (m.equals(ONE)) ZERO else ONE)

        if (this.equals(ZERO) && exponent.signum >= 0) return ZERO

        if (this.equals(negConst!![1]) && (!exponent.testBit(0))) return (if (m.equals(ONE)) ZERO else ONE)

        val invertResult: Boolean
        if (((exponent.signum < 0).also { invertResult = it })) exponent = exponent.negate()

        val base = (if (this.signum < 0 || this.compareTo(m) >= 0)
            this.mod(m)
        else
            this)
        val result: BigInteger
        if (m.testBit(0)) { // odd modulus
            result = base.oddModPow(exponent, m)
        } else {
            /*
             * Even modulus.  Tear it into an "odd part" (m1) and power of two
             * (m2), exponentiate mod m1, manually exponentiate mod m2, and
             * use Chinese Remainder Theorem to combine results.
             */

            // Tear m apart into odd part (m1) and power of 2 (m2)

            val p = m.lowestSetBit // Max pow of 2 that divides m

            val m1 = m.shiftRight(p) // m/2**p
            val m2 = ONE.shiftLeft(p) // 2**p

            // Calculate new base from m1
            val base2 = (if (this.signum < 0 || this.compareTo(m1) >= 0)
                this.mod(m1)
            else
                this)

            // Caculate (base ** exponent) mod m1.
            val a1 = (if (m1.equals(ONE)) ZERO else base2.oddModPow(exponent, m1))

            // Calculate (this ** exponent) mod m2
            val a2 = base.modPow2(exponent, p)

            // Combine results using Chinese Remainder Theorem
            val y1 = m2.modInverse(m1)
            val y2 = m1.modInverse(m2)

            result = a1.multiply(m2)!!.multiply(y1)!!.add(a2.multiply(m1)!!.multiply(y2)!!).mod(m)
        }

        return (if (invertResult) result.modInverse(m) else result)
    }

    /**
     * Returns a BigInteger whose value is x to the power of y mod z.
     * Assumes: z is odd && x < z.
     */
    private fun oddModPow(y: BigInteger, z: BigInteger): BigInteger {
        /*
     * The algorithm is adapted from Colin Plumb's C library.
     *
     * The window algorithm:
     * The idea is to keep a running product of b1 = n^(high-order bits of exp)
     * and then keep appending exponent bits to it.  The following patterns
     * apply to a 3-bit window (k = 3):
     * To append   0: square
     * To append   1: square, multiply by n^1
     * To append  10: square, multiply by n^1, square
     * To append  11: square, square, multiply by n^3
     * To append 100: square, multiply by n^1, square, square
     * To append 101: square, square, square, multiply by n^5
     * To append 110: square, square, multiply by n^3, square
     * To append 111: square, square, square, multiply by n^7
     *
     * Since each pattern involves only one multiply, the longer the pattern
     * the better, except that a 0 (no multiplies) can be appended directly.
     * We precompute a table of odd powers of n, up to 2^k, and can then
     * multiply k bits of exponent at a time.  Actually, assuming random
     * exponents, there is on average one zero bit between needs to
     * multiply (1/2 of the time there's none, 1/4 of the time there's 1,
     * 1/8 of the time, there's 2, 1/32 of the time, there's 3, etc.), so
     * you have to do one multiply per k+1 bits of exponent.
     *
     * The loop walks down the exponent, squaring the result buffer as
     * it goes.  There is a wbits+1 bit lookahead buffer, buf, that is
     * filled with the upcoming exponent bits.  (What is read after the
     * end of the exponent is unimportant, but it is filled with zero here.)
     * When the most-significant bit of this buffer becomes set, i.e.
     * (buf & tblmask) != 0, we have to decide what pattern to multiply
     * by, and when to do it.  We decide, remember to do it in future
     * after a suitable number of squarings have passed (e.g. a pattern
     * of "100" in the buffer requires that we multiply by n^1 immediately;
     * a pattern of "110" calls for multiplying by n^3 after one more
     * squaring), clear the buffer, and continue.
     *
     * When we start, there is one more optimization: the result buffer
     * is implcitly one, so squaring it or multiplying by it can be
     * optimized away.  Further, if we start with a pattern like "100"
     * in the lookahead window, rather than placing n into the buffer
     * and then starting to square it, we have already computed n^2
     * to compute the odd-powers table, so we can place that into
     * the buffer and save a squaring.
     *
     * This means that if you have a k-bit window, to compute n^z,
     * where z is the high k bits of the exponent, 1/2 of the time
     * it requires no squarings.  1/4 of the time, it requires 1
     * squaring, ... 1/2^(k-1) of the time, it reqires k-2 squarings.
     * And the remaining 1/2^(k-1) of the time, the top k bits are a
     * 1 followed by k-1 0 bits, so it again only requires k-2
     * squarings, not k-1.  The average of these is 1.  Add that
     * to the one squaring we have to do to compute the table,
     * and you'll see that a k-bit window saves k-2 squarings
     * as well as reducing the multiplies.  (It actually doesn't
     * hurt in the case k = 1, either.)
     */
        // Special case for exponent of one
        if (y.equals(ONE)) return this

        // Special case for base of zero
        if (signum == 0) return ZERO

        val base = mag.clone()
        val exp = y.mag
        val mod = z.mag
        val modLen = mod.size

        // Select an appropriate window size
        var wbits = 0
        var ebits = bitLength(exp, exp.size)
        // if exponent is 65537 (0x10001), use minimum window size
        if ((ebits != 17) || (exp[0] != 65537)) {
            while (ebits > bnExpModThreshTable[wbits]) {
                wbits++
            }
        }

        // Calculate appropriate table size
        val tblmask = 1 shl wbits

        // Allocate table for precomputed odd powers of base in Montgomery form
        val table: Array<IntArray> = kotlin.arrayOfNulls<IntArray>(tblmask)
        for (i in 0..<tblmask) table[i] = IntArray(modLen)

        // Compute the modular inverse
        val inv: Int =
            -io.github.aughtone.types.math.MutableBigInteger.inverseMod32(mod[modLen - 1])

        // Convert base to Montgomery form
        var a = leftShift(base, base.size, modLen shl 5)

        val q: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger()
        val a2: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(a)
        val b2: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(mod)

        val r: io.github.aughtone.types.math.MutableBigInteger = a2.divide(b2, q)
        table[0] = r.toIntArray()

        // Pad table[0] with leading zeros so its length is at least modLen
        if (table[0].size < modLen) {
            val offset = modLen - table[0].size
            val t2 = IntArray(modLen)
            for (i in table[0].indices) t2[i + offset] = table[0][i]
            table[0] = t2
        }

        // Set b to the square of the base
        var b = squareToLen(table[0], modLen, null)
        b = montReduce(b, mod, modLen, inv)

        // Set t to high half of b
        var t = IntArray(modLen)
        for (i in 0..<modLen) t[i] = b[i]

        // Fill in the table with odd powers of the base
        for (i in 1..<tblmask) {
            val prod = multiplyToLen(t, modLen, table[i - 1], modLen, null)
            table[i] = montReduce(prod, mod, modLen, inv)
        }

        // Pre load the window that slides over the exponent
        var bitpos = 1 shl ((ebits - 1) and (32 - 1))

        var buf = 0
        var elen = exp.size
        var eIndex = 0
        for (i in 0..wbits) {
            buf = (buf shl 1) or (if ((exp[eIndex] and bitpos) != 0) 1 else 0)
            bitpos = bitpos ushr 1
            if (bitpos == 0) {
                eIndex++
                bitpos = 1 shl (32 - 1)
                elen--
            }
        }

        var multpos = ebits

        // The first iteration, which is hoisted out of the main loop
        ebits--
        var isone = true

        multpos = ebits - wbits
        while ((buf and 1) == 0) {
            buf = buf ushr 1
            multpos++
        }

        var mult = table[buf ushr 1]

        buf = 0
        if (multpos == ebits) isone = false

        // The main loop
        while (true) {
            ebits--
            // Advance the window
            buf = buf shl 1

            if (elen != 0) {
                buf = buf or if ((exp[eIndex] and bitpos) != 0) 1 else 0
                bitpos = bitpos ushr 1
                if (bitpos == 0) {
                    eIndex++
                    bitpos = 1 shl (32 - 1)
                    elen--
                }
            }

            // Examine the window for pending multiplies
            if ((buf and tblmask) != 0) {
                multpos = ebits - wbits
                while ((buf and 1) == 0) {
                    buf = buf ushr 1
                    multpos++
                }
                mult = table[buf ushr 1]
                buf = 0
            }

            // Perform multiply
            if (ebits == multpos) {
                if (isone) {
                    b = mult.clone()
                    isone = false
                } else {
                    t = b
                    a = multiplyToLen(t, modLen, mult, modLen, a)
                    a = montReduce(a, mod, modLen, inv)
                    t = a
                    a = b
                    b = t
                }
            }

            // Check if done
            if (ebits == 0) break

            // Square the input
            if (!isone) {
                t = b
                a = squareToLen(t, modLen, a)
                a = montReduce(a, mod, modLen, inv)
                t = a
                a = b
                b = t
            }
        }

        // Convert result out of Montgomery form and return
        var t2 = IntArray(2 * modLen)
        for (i in 0..<modLen) t2[i + modLen] = b[i]

        b = montReduce(t2, mod, modLen, inv)

        t2 = IntArray(modLen)
        for (i in 0..<modLen) t2[i] = b[i]

        return BigInteger(1, t2)
    }

    /**
     * Returns a BigInteger whose value is (this ** exponent) mod (2**p)
     */
    private fun modPow2(exponent: BigInteger, p: Int): BigInteger {
        /*
         * Perform exponentiation using repeated squaring trick, chopping off
         * high order bits as indicated by modulus.
         */
        var result = valueOf(1)
        var baseToPow2 = this.mod2(p)
        var expOffset = 0

        var limit = exponent.bitLength()

        if (this.testBit(0)) limit = if ((p - 1) < limit) (p - 1) else limit

        while (expOffset < limit) {
            if (exponent.testBit(expOffset)) result = result.multiply(baseToPow2)!!.mod2(p)
            expOffset++
            if (expOffset < limit) baseToPow2 = baseToPow2.square()!!.mod2(p)
        }

        return result
    }

    /**
     * Returns a BigInteger whose value is this mod(2**p).
     * Assumes that this `BigInteger >= 0` and `p > 0`.
     */
    private fun mod2(p: Int): BigInteger {
        if (bitLength() <= p) return this

        // Copy remaining ints of mag
        val numInts = (p + 31) ushr 5
        val mag = IntArray(numInts)
        for (i in 0..<numInts) mag[i] = this.mag[i + (this.mag.size - numInts)]

        // Mask out any excess bits
        val excessBits = (numInts shl 5) - p
        mag[0] = mag[0].toLong() and (1L shl (32 - excessBits)) - 1

        return (if (mag[0] == 0) BigInteger(1, mag) else BigInteger(mag, 1))
    }

    /**
     * Returns a BigInteger whose value is `(this`<sup>-1</sup> `mod m)`.
     *
     * @param  m the modulus.
     * @return `this`<sup>-1</sup> `mod m`.
     * @throws ArithmeticException `m`  0, or this BigInteger
     * has no multiplicative inverse mod m (that is, this BigInteger
     * is not *relatively prime* to m).
     */
    fun modInverse(m: BigInteger): BigInteger {
        if (m.signum != 1) throw ArithmeticException("BigInteger: modulus not positive")

        if (m.equals(ONE)) return ZERO

        // Calculate (this mod m)
        var modVal = this
        if (signum < 0 || (this.compareMagnitude(m) >= 0)) modVal = this.mod(m)

        if (modVal.equals(ONE)) return ONE

        val a: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(modVal)
        val b: io.github.aughtone.types.math.MutableBigInteger =
            io.github.aughtone.types.math.MutableBigInteger(m)

        val result: io.github.aughtone.types.math.MutableBigInteger = a.mutableModInverse(b)
        return result.toBigInteger(1)
    }

    // Shift Operations
    /**
     * Returns a BigInteger whose value is `(this << n)`.
     * The shift distance, `n`, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
     *
     * @param  n shift distance, in bits.
     * @return `this << n`
     * @throws ArithmeticException if the shift distance is `Integer.MIN_VALUE`.
     * @see .shiftRight
     */
    fun shiftLeft(n: Int): BigInteger {
        if (signum == 0) return ZERO
        if (n == 0) return this
        if (n < 0) {
            if (n == Integer.MIN_VALUE) {
                throw ArithmeticException("Shift distance of Integer.MIN_VALUE not supported.")
            } else {
                return shiftRight(-n)
            }
        }

        val nInts = n ushr 5
        val nBits = n and 0x1f
        val magLen = mag.size
        var newMag: IntArray? = null

        if (nBits == 0) {
            newMag = IntArray(magLen + nInts)
            for (i in 0..<magLen) newMag[i] = mag[i]
        } else {
            var i = 0
            val nBits2 = 32 - nBits
            val highBits = mag[0] ushr nBits2
            if (highBits != 0) {
                newMag = IntArray(magLen + nInts + 1)
                newMag[i++] = highBits
            } else {
                newMag = IntArray(magLen + nInts)
            }
            var j = 0
            while (j < magLen - 1) newMag[i++] = mag[j++] shl nBits or (mag[j] ushr nBits2)
            newMag[i] = mag[j] shl nBits
        }

        return BigInteger(newMag, signum)
    }

    /**
     * Returns a BigInteger whose value is `(this >> n)`.  Sign
     * extension is performed.  The shift distance, `n`, may be
     * negative, in which case this method performs a left shift.
     * (Computes <tt>floor(this / 2<sup>n</sup>)</tt>.)
     *
     * @param  n shift distance, in bits.
     * @return `this >> n`
     * @throws ArithmeticException if the shift distance is `Integer.MIN_VALUE`.
     * @see .shiftLeft
     */
    fun shiftRight(n: Int): BigInteger {
        if (n == 0) return this
        if (n < 0) {
            if (n == Integer.MIN_VALUE) {
                throw ArithmeticException("Shift distance of Integer.MIN_VALUE not supported.")
            } else {
                return shiftLeft(-n)
            }
        }

        val nInts = n ushr 5
        val nBits = n and 0x1f
        val magLen = mag.size
        var newMag: IntArray? = null

        // Special case: entire contents shifted off the end
        if (nInts >= magLen) return (if (signum >= 0) ZERO else negConst!![1])

        if (nBits == 0) {
            val newMagLen = magLen - nInts
            newMag = IntArray(newMagLen)
            for (i in 0..<newMagLen) newMag[i] = mag[i]
        } else {
            var i = 0
            val highBits = mag[0] ushr nBits
            if (highBits != 0) {
                newMag = IntArray(magLen - nInts)
                newMag[i++] = highBits
            } else {
                newMag = IntArray(magLen - nInts - 1)
            }

            val nBits2 = 32 - nBits
            var j = 0
            while (j < magLen - nInts - 1) newMag[i++] =
                (mag[j++] shl nBits2) or (mag[j] ushr nBits)
        }

        if (signum < 0) {
            // Find out whether any one-bits were shifted off the end.
            var onesLost = false
            var i = magLen - 1
            val j = magLen - nInts
            while (i >= j && !onesLost) {
                onesLost = (mag[i] != 0)
                i--
            }
            if (!onesLost && nBits != 0) onesLost = (mag[magLen - nInts - 1] shl (32 - nBits) != 0)

            if (onesLost) newMag = javaIncrement(newMag)
        }

        return BigInteger(newMag, signum)
    }

    fun javaIncrement(`val`: IntArray): IntArray {
        var `val` = `val`
        var lastSum = 0
        var i = `val`.size - 1
        while (i >= 0 && lastSum == 0) {
            lastSum = (1.let { `val`[i] += it; `val`[i] })
            i--
        }
        if (lastSum == 0) {
            `val` = IntArray(`val`.size + 1)
            `val`[0] = 1
        }
        return `val`
    }

    // Bitwise Operations
    /**
     * Returns a BigInteger whose value is `(this & val)`.  (This
     * method returns a negative BigInteger if and only if this and val are
     * both negative.)
     *
     * @param val value to be AND'ed with this BigInteger.
     * @return `this & val`
     */
    fun and(`val`: BigInteger): BigInteger {
        val result = IntArray(Math.max(intLength(), `val`.intLength()))
        for (i in result.indices) result[i] = (getInt(result.size - i - 1)
                and `val`.getInt(result.size - i - 1))

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(this | val)`.  (This method
     * returns a negative BigInteger if and only if either this or val is
     * negative.)
     *
     * @param val value to be OR'ed with this BigInteger.
     * @return `this | val`
     */
    fun or(`val`: BigInteger): BigInteger {
        val result = IntArray(Math.max(intLength(), `val`.intLength()))
        for (i in result.indices) result[i] = (getInt(result.size - i - 1)
                or `val`.getInt(result.size - i - 1))

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(this ^ val)`.  (This method
     * returns a negative BigInteger if and only if exactly one of this and
     * val are negative.)
     *
     * @param val value to be XOR'ed with this BigInteger.
     * @return `this ^ val`
     */
    fun xor(`val`: BigInteger): BigInteger {
        val result = IntArray(Math.max(intLength(), `val`.intLength()))
        for (i in result.indices) result[i] = (getInt(result.size - i - 1)
                xor `val`.getInt(result.size - i - 1))

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(~this)`.  (This method
     * returns a negative value if and only if this BigInteger is
     * non-negative.)
     *
     * @return `~this`
     */
    fun not(): BigInteger {
        val result = IntArray(intLength())
        for (i in result.indices) result[i] = getInt(result.size - i - 1).inv()

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is `(this & ~val)`.  This
     * method, which is equivalent to `and(val.not())`, is provided as
     * a convenience for masking operations.  (This method returns a negative
     * BigInteger if and only if `this` is negative and `val` is
     * positive.)
     *
     * @param val value to be complemented and AND'ed with this BigInteger.
     * @return `this & ~val`
     */
    fun andNot(`val`: BigInteger): BigInteger {
        val result = IntArray(Math.max(intLength(), `val`.intLength()))
        for (i in result.indices) result[i] = (getInt(result.size - i - 1)
                and `val`.getInt(result.size - i - 1).inv())

        return valueOf(result)
    }


    // Single Bit Operations
    /**
     * Returns `true` if and only if the designated bit is set.
     * (Computes `((this & (1<<n)) != 0)`.)
     *
     * @param  n index of bit to test.
     * @return `true` if and only if the designated bit is set.
     * @throws ArithmeticException `n` is negative.
     */
    fun testBit(n: Int): Boolean {
        if (n < 0) throw ArithmeticException("Negative bit address")

        return (getInt(n ushr 5) and (1 shl (n and 31))) != 0
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit set.  (Computes `(this | (1<<n))`.)
     *
     * @param  n index of bit to set.
     * @return `this | (1<<n)`
     * @throws ArithmeticException `n` is negative.
     */
    fun setBit(n: Int): BigInteger {
        if (n < 0) throw ArithmeticException("Negative bit address")

        val intNum = n ushr 5
        val result = IntArray(Math.max(intLength(), intNum + 2))

        for (i in result.indices) result[result.size - i - 1] = getInt(i)

        result[result.size - intNum - 1] = result[result.size - intNum - 1] or (1 shl (n and 31))

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit cleared.
     * (Computes `(this & ~(1<<n))`.)
     *
     * @param  n index of bit to clear.
     * @return `this & ~(1<<n)`
     * @throws ArithmeticException `n` is negative.
     */
    fun clearBit(n: Int): BigInteger {
        if (n < 0) throw ArithmeticException("Negative bit address")

        val intNum = n ushr 5
        val result = IntArray(Math.max(intLength(), ((n + 1) ushr 5) + 1))

        for (i in result.indices) result[result.size - i - 1] = getInt(i)

        result[result.size - intNum - 1] =
            result[result.size - intNum - 1] and (1 shl (n and 31)).inv()

        return valueOf(result)
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit flipped.
     * (Computes `(this ^ (1<<n))`.)
     *
     * @param  n index of bit to flip.
     * @return `this ^ (1<<n)`
     * @throws ArithmeticException `n` is negative.
     */
    fun flipBit(n: Int): BigInteger {
        if (n < 0) throw ArithmeticException("Negative bit address")

        val intNum = n ushr 5
        val result = IntArray(Math.max(intLength(), intNum + 2))

        for (i in result.indices) result[result.size - i - 1] = getInt(i)

        result[result.size - intNum - 1] = result[result.size - intNum - 1] xor (1 shl (n and 31))

        return valueOf(result)
    }


    // Miscellaneous Bit Operations
    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, *excluding* a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  (Computes
     * `(ceil(log2(this < 0 ? -this : this+1)))`.)
     *
     * @return number of bits in the minimal two's-complement
     * representation of this BigInteger, *excluding* a sign bit.
     */
    fun bitLength(): Int {
        @SuppressWarnings("deprecation") var n = bitLength - 1
        if (n == -1) { // bitLength not initialized yet
            val m = mag
            val len = m.size
            if (len == 0) {
                n = 0 // offset by one to initialize
            } else {
                // Calculate the bit length of the magnitude
                val magBitLength = ((len - 1) shl 5) + bitLengthForInt(mag[0])
                if (signum < 0) {
                    // Check if magnitude is a power of two
                    var pow2 = (Integer.bitCount(mag[0]) === 1)
                    var i = 1
                    while (i < len && pow2) {
                        pow2 = (mag[i] == 0)
                        i++
                    }

                    n = (if (pow2) magBitLength - 1 else magBitLength)
                } else {
                    n = magBitLength
                }
            }
            bitLength = n + 1
        }
        return n
    }

    /**
     * Returns the number of bits in the two's complement representation
     * of this BigInteger that differ from its sign bit.  This method is
     * useful when implementing bit-vector style sets atop BigIntegers.
     *
     * @return number of bits in the two's complement representation
     * of this BigInteger that differ from its sign bit.
     */
    fun bitCount(): Int {
        @SuppressWarnings("deprecation") var bc = bitCount - 1
        if (bc == -1) {  // bitCount not initialized yet
            bc = 0 // offset by one to initialize
            // Count the bits in the magnitude
            for (i in mag.indices) bc += Integer.bitCount(mag[i])
            if (signum < 0) {
                // Count the trailing zeros in the magnitude
                var magTrailingZeroCount = 0
                var j: Int
                j = mag.size - 1
                while (mag[j] == 0) {
                    magTrailingZeroCount += 32
                    j--
                }
                magTrailingZeroCount += Integer.numberOfTrailingZeros(mag[j])
                bc += magTrailingZeroCount - 1
            }
            bitCount = bc + 1
        }
        return bc
    }

    // Primality Testing
    /**
     * Returns `true` if this BigInteger is probably prime,
     * `false` if it's definitely composite.  If
     * `certainty` is  0, `true` is
     * returned.
     *
     * @param  certainty a measure of the uncertainty that the caller is
     * willing to tolerate: if the call returns `true`
     * the probability that this BigInteger is prime exceeds
     * (1 - 1/2<sup>`certainty`</sup>).  The execution time of
     * this method is proportional to the value of this parameter.
     * @return `true` if this BigInteger is probably prime,
     * `false` if it's definitely composite.
     */
    fun isProbablePrime(certainty: Int): Boolean {
        if (certainty <= 0) return true
        val w = this.abs()
        if (w.equals(TWO)) return true
        if (!w.testBit(0) || w.equals(ONE)) return false

        return w.primeToCertainty(certainty, null)
    }

    // Comparison Operations
    /**
     * Compares this BigInteger with the specified BigInteger.  This
     * method is provided in preference to individual methods for each
     * of the six boolean comparison operators (&lt;, ==,
     * &gt;, &gt;=, !=, &lt;=).  The suggested
     * idiom for performing these comparisons is: `(x.compareTo(y)` &lt;*op*&gt; `0)`, where
     * &lt;*op*&gt; is one of the six comparison operators.
     *
     * @param  val BigInteger to which this BigInteger is to be compared.
     * @return -1, 0 or 1 as this BigInteger is numerically less than, equal
     * to, or greater than `val`.
     */
    fun compareTo(`val`: BigInteger): Int {
        if (signum == `val`.signum) {
            when (signum) {
                1 -> return compareMagnitude(`val`)
                -1 -> return `val`.compareMagnitude(this)
                else -> return 0
            }
        }
        return if (signum > `val`.signum) 1 else -1
    }

    /**
     * Compares the magnitude array of this BigInteger with the specified
     * BigInteger's. This is the version of compareTo ignoring sign.
     *
     * @param val BigInteger whose magnitude array to be compared.
     * @return -1, 0 or 1 as this magnitude array is less than, equal to or
     * greater than the magnitude aray for the specified BigInteger's.
     */
    fun compareMagnitude(`val`: BigInteger): Int {
        val m1 = mag
        val len1 = m1.size
        val m2 = `val`.mag
        val len2 = m2.size
        if (len1 < len2) return -1
        if (len1 > len2) return 1
        for (i in 0..<len1) {
            val a = m1[i]
            val b = m2[i]
            if (a != b) return if ((a.toLong() and LONG_MASK) < (b.toLong() and LONG_MASK)) -1 else 1
        }
        return 0
    }

    /**
     * Compares this BigInteger with the specified Object for equality.
     *
     * @param  x Object to which this BigInteger is to be compared.
     * @return `true` if and only if the specified Object is a
     * BigInteger whose value is numerically equal to this BigInteger.
     */
    override fun equals(x: Any?): Boolean {
        // This test is just an optimization, which may or may not help
        if (x === this) return true

        if (x !is BigInteger) return false

        val xInt = x
        if (xInt.signum != signum) return false

        val m = mag
        val len = m.size
        val xm = xInt.mag
        if (len != xm.size) return false

        for (i in 0..<len) if (xm[i] != m[i]) return false

        return true
    }

    /**
     * Returns the minimum of this BigInteger and `val`.
     *
     * @param  val value with which the minimum is to be computed.
     * @return the BigInteger whose value is the lesser of this BigInteger and
     * `val`.  If they are equal, either may be returned.
     */
    fun min(`val`: BigInteger): BigInteger {
        return (if (compareTo(`val`) < 0) this else `val`)
    }

    /**
     * Returns the maximum of this BigInteger and `val`.
     *
     * @param  val value with which the maximum is to be computed.
     * @return the BigInteger whose value is the greater of this and
     * `val`.  If they are equal, either may be returned.
     */
    fun max(`val`: BigInteger): BigInteger {
        return (if (compareTo(`val`) > 0) this else `val`)
    }


    // Hash Function
    /**
     * Returns the hash code for this BigInteger.
     *
     * @return hash code for this BigInteger.
     */
    fun hashCode(): Int {
        var hashCode = 0

        for (i in mag.indices) hashCode = (31 * hashCode + (mag[i].toLong() and LONG_MASK)).toInt()

        return hashCode * signum
    }

    /**
     * Returns the String representation of this BigInteger in the
     * given radix.  If the radix is outside the range from [ ][Character.MIN_RADIX] to [Character.MAX_RADIX] inclusive,
     * it will default to 10 (as is the case for
     * `Integer.toString`).  The digit-to-character mapping
     * provided by `Character.forDigit` is used, and a minus
     * sign is prepended if appropriate.  (This representation is
     * compatible with the [(String,][.BigInteger] constructor.)
     *
     * @param  radix  radix of the String representation.
     * @return String representation of this BigInteger in the given radix.
     * @see Integer.toString
     *
     * @see Character.forDigit
     *
     * @see .BigInteger
     */
    fun toString(radix: Int): String {
        var radix = radix
        if (signum == 0) return "0"
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) radix = 10

        // Compute upper bound on number of digit groups and allocate space
        val maxNumDigitGroups = (4 * mag.size + 6) / 7
        val digitGroup: Array<String?>? = arrayOfNulls<String>(maxNumDigitGroups)

        // Translate number to string, a digit group at a time
        var tmp = this.abs()
        var numGroups = 0
        while (tmp.signum != 0) {
            val d = longRadix!![radix]

            val q: MutableBigInteger = MutableBigInteger()
            val a: MutableBigInteger = MutableBigInteger(tmp.mag)
            val b: MutableBigInteger = MutableBigInteger(d.mag)
            val r: MutableBigInteger = a.divide(b, q)
            val q2: BigInteger = q.toBigInteger(tmp.signum * d.signum)
            val r2: BigInteger = r.toBigInteger(tmp.signum * d.signum)

            digitGroup!![numGroups++] = r2.longValue().toString(radix) //Long.toString(r2.longValue(), radix)
            tmp = q2
        }

        // Put sign (if any) and first digit group into result buffer
        val buf = StringBuilder(numGroups * digitsPerLong!![radix] + 1)
        if (signum < 0) buf.append('-')
        buf.append(digitGroup!![numGroups - 1])

        // Append remaining digit groups padded with leading zeros
        for (i in numGroups - 2 downTo 0) {
            // Prepend (any) leading zeros for this digit group
            val numLeadingZeros: Int = digitsPerLong[radix] - digitGroup[i].length()
            if (numLeadingZeros != 0) buf.append(zeros!![numLeadingZeros])
            buf.append(digitGroup[i])
        }
        return buf.toString()
    }

    /**
     * Returns the decimal String representation of this BigInteger.
     * The digit-to-character mapping provided by
     * `Character.forDigit` is used, and a minus sign is
     * prepended if appropriate.  (This representation is compatible
     * with the [(String)][.BigInteger] constructor, and
     * allows for String concatenation with Java's + operator.)
     *
     * @return decimal String representation of this BigInteger.
     * @see Character.forDigit
     *
     * @see .BigInteger
     */
    override fun toString(): String {
        return toString(10)
    }

    /**
     * Returns a byte array containing the two's-complement
     * representation of this BigInteger.  The byte array will be in
     * *big-endian* byte-order: the most significant byte is in
     * the zeroth element.  The array will contain the minimum number
     * of bytes required to represent this BigInteger, including at
     * least one sign bit, which is `(ceil((this.bitLength() +
     * 1)/8))`.  (This representation is compatible with the
     * [(byte[])][.BigInteger] constructor.)
     *
     * @return a byte array containing the two's-complement representation of
     * this BigInteger.
     * @see .BigInteger
     */
    fun toByteArray(): ByteArray {
        val byteLen = bitLength() / 8 + 1
        val byteArray = ByteArray(byteLen)

        var i = byteLen - 1
        var bytesCopied = 4
        var nextInt = 0
        var intIndex = 0
        while (i >= 0) {
            if (bytesCopied == 4) {
                nextInt = getInt(intIndex++)
                bytesCopied = 1
            } else {
                nextInt = nextInt ushr 8
                bytesCopied++
            }
            byteArray[i] = nextInt.toByte()
            i--
        }
        return byteArray
    }

    /**
     * Converts this BigInteger to an `int`.  This
     * conversion is analogous to a
     * *narrowing primitive conversion* from `long` to
     * `int` as defined in section 5.1.3 of
     * <cite>The Java Language Specification</cite>:
     * if this BigInteger is too big to fit in an
     * `int`, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to an `int`.
     */
    fun intValue(): Int {
        var result = 0
        result = getInt(0)
        return result
    }

    /**
     * Converts this BigInteger to a `long`.  This
     * conversion is analogous to a
     * *narrowing primitive conversion* from `long` to
     * `int` as defined in section 5.1.3 of
     * <cite>The Java Language Specification</cite>:
     * if this BigInteger is too big to fit in a
     * `long`, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to a `long`.
     */
    fun longValue(): Long {
        var result: Long = 0

        for (i in 1 downTo 0) result = (result shl 32) + (getInt(i).toLong() and LONG_MASK)
        return result
    }

    /**
     * Converts this BigInteger to a `float`.  This
     * conversion is similar to the
     * *narrowing primitive conversion* from `double` to
     * `float` as defined in section 5.1.3 of
     * <cite>The Java Language Specification</cite>:
     * if this BigInteger has too great a magnitude
     * to represent as a `float`, it will be converted to
     * [Float.NEGATIVE_INFINITY] or [ ][Float.POSITIVE_INFINITY] as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the BigInteger value.
     *
     * @return this BigInteger converted to a `float`.
     */
    fun floatValue(): Float {
        // Somewhat inefficient, but guaranteed to work.
        return Float.parseFloat(this.toString())
    }

    /**
     * Converts this BigInteger to a `double`.  This
     * conversion is similar to the
     * *narrowing primitive conversion* from `double` to
     * `float` as defined in section 5.1.3 of
     * <cite>The Java Language Specification</cite>:
     * if this BigInteger has too great a magnitude
     * to represent as a `double`, it will be converted to
     * [Double.NEGATIVE_INFINITY] or [ ][Double.POSITIVE_INFINITY] as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the BigInteger value.
     *
     * @return this BigInteger converted to a `double`.
     */
    fun doubleValue(): Double {
        // Somewhat inefficient, but guaranteed to work.
        return Double.parseDouble(this.toString())
    }

    /**
     * These routines provide access to the two's complement representation
     * of BigIntegers.
     */
    /**
     * Returns the length of the two's complement representation in ints,
     * including space for at least one sign bit.
     */
    private fun intLength(): Int {
        return (bitLength() ushr 5) + 1
    }

    /* Returns sign bit */
    private fun signBit(): Int {
        return if (signum < 0) 1 else 0
    }

    /* Returns an int of sign bits */
    private fun signInt(): Int {
        return if (signum < 0) -1 else 0
    }

    /**
     * Returns the specified int of the little-endian two's complement
     * representation (int 0 is the least significant).  The int number can
     * be arbitrarily high (values are logically preceded by infinitely many
     * sign ints).
     */
    private fun getInt(n: Int): Int {
        if (n < 0) return 0
        if (n >= mag.size) return signInt()

        val magInt = mag[mag.size - n - 1]

        return (if (signum >= 0) magInt else (if (n <= firstNonzeroIntNum()) -magInt else magInt.inv()))
    }

    /**
     * Returns the index of the int that contains the first nonzero int in the
     * little-endian binary representation of the magnitude (int 0 is the
     * least significant). If the magnitude is zero, return value is undefined.
     */
    private fun firstNonzeroIntNum(): Int {
        var fn = firstNonzeroIntNum - 2
        if (fn == -2) { // firstNonzeroIntNum not initialized yet
            fn = 0

            // Search for the first nonzero int
            var i: Int
            val mlen = mag.size
            i = mlen - 1
            while (i >= 0 && mag[i] == 0) {
                i--
            }
            fn = mlen - i - 1
            firstNonzeroIntNum = fn + 2 // offset by two to initialize
        }
        return fn
    }

    /**
     * Reconstitute the `BigInteger` instance from a stream (that is,
     * deserialize it). The magnitude is read in as an array of bytes
     * for historical reasons, but it is converted to an array of ints
     * and the byte array is discarded.
     * Note:
     * The current convention is to initialize the cache fields, bitCount,
     * bitLength and lowestSetBit, to 0 rather than some other marker value.
     * Therefore, no explicit action to set these fields needs to be taken in
     * readObject because those fields already have a 0 value be default since
     * defaultReadObject is not being used.
     */
    @Throws(java.io.IOException::class, ClassNotFoundException::class)
    private fun readObject(s: java.io.ObjectInputStream) {
        /*
         * In order to maintain compatibility with previous serialized forms,
         * the magnitude of a BigInteger is serialized as an array of bytes.
         * The magnitude field is used as a temporary store for the byte array
         * that is deserialized. The cached computation fields should be
         * transient but are serialized for compatibility reasons.
         */

        // prepare to read the alternate persistent fields

        val fields: ObjectInputStream.GetField = s.readFields()

        // Read the alternate persistent fields that we care about
        val sign: Int = fields.get("signum", -2)
        val magnitude = fields.get("magnitude", null) as ByteArray

        // Validate signum
        if (sign < -1 || sign > 1) {
            var message = "BigInteger: Invalid signum value"
            if (fields.defaulted("signum")) message = "BigInteger: Signum not present in stream"
            throw StreamCorruptedException(message)
        }
        if ((magnitude.size == 0) != (sign == 0)) {
            var message = "BigInteger: signum-magnitude mismatch"
            if (fields.defaulted("magnitude")) message =
                "BigInteger: Magnitude not present in stream"
            throw StreamCorruptedException(message)
        }

        // Commit final fields via Unsafe
        unsafe.putIntVolatile(this, signumOffset, sign)

        // Calculate mag field from magnitude and discard magnitude
        unsafe.putObjectVolatile(
            this, magOffset,
            stripLeadingZeroBytes(magnitude)
        )
    }

    /**
     * Save the `BigInteger` instance to a stream.
     * The magnitude of a BigInteger is serialized as a byte array for
     * historical reasons.
     *
     * @serialData two necessary fields are written as well as obsolete
     * fields for compatibility with older versions.
     */
    @Throws(IOException::class)
    private fun writeObject(s: ObjectOutputStream) {
        // set the values of the Serializable fields
        val fields: ObjectOutputStream.PutField = s.putFields()
        fields.put("signum", signum)
        fields.put("magnitude", magSerializedForm())
        // The values written for cached fields are compatible with older
        // versions, but are ignored in readObject so don't otherwise matter.
        fields.put("bitCount", -1)
        fields.put("bitLength", -1)
        fields.put("lowestSetBit", -2)
        fields.put("firstNonzeroByteNum", -2)

        // save them
        s.writeFields()
    }

    /**
     * Returns the mag array as an array of bytes.
     */
    private fun magSerializedForm(): ByteArray {
        val len = mag.size

        val bitLen = (if (len == 0) 0 else ((len - 1) shl 5) + bitLengthForInt(mag[0]))
        val byteLen = (bitLen + 7) ushr 3
        val result = ByteArray(byteLen)

        var i = byteLen - 1
        var bytesCopied = 4
        var intIndex = len - 1
        var nextInt = 0
        while (i >= 0) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--]
                bytesCopied = 1
            } else {
                nextInt = nextInt ushr 8
                bytesCopied++
            }
            result[i] = nextInt.toByte()
            i--
        }
        return result
    }

    companion object {
        /**
         * This mask is used to obtain the value of an int as if it were unsigned.
         */
        const val LONG_MASK: Long = 0xffffffffL

        // bitsPerDigit in the given radix times 1024
        // Rounded up to avoid underallocation.
        private val bitsPerDigit: LongArray? = longArrayOf(
            0, 0,
            1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672,
            3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633,
            4696, 4756, 4814, 4870, 4923, 4975, 5025, 5074, 5120, 5166, 5210,
            5253, 5295
        )

        // Multiply x array times word y in place, and add word z
        private fun destructiveMulAdd(x: IntArray, y: Int, z: Int) {
            // Perform the multiplication word by word
            val ylong = y.toLong() and LONG_MASK
            val zlong = z.toLong() and LONG_MASK
            val len = x.size

            var product: Long = 0
            var carry: Long = 0
            for (i in len - 1 downTo 0) {
                product = ylong * (x[i].toLong() and LONG_MASK) + carry
                x[i] = product.toInt()
                carry = product ushr 32
            }

            // Perform the addition
            var sum = (x[len - 1].toLong() and LONG_MASK) + zlong
            x[len - 1] = sum.toInt()
            carry = sum ushr 32
            for (i in len - 2 downTo 0) {
                sum = (x[i].toLong() and LONG_MASK) + carry
                x[i] = sum.toInt()
                carry = sum ushr 32
            }
        }

        private fun randomBits(numBits: Int, rnd: Random): ByteArray {
            require(numBits >= 0) { "numBits must be non-negative" }
            val numBytes = ((numBits.toLong() + 7) / 8).toInt() // avoid overflow
            val randomBits = ByteArray(numBytes)

            // Generate random bytes and mask out any excess bits
            if (numBytes > 0) {
                rnd.nextBytes(randomBits)
                val excessBits = 8 * numBytes - numBits
                randomBits[0] = randomBits[0].toInt() and (1 shl (8 - excessBits)) - 1
            }
            return randomBits
        }

        // Minimum size in bits that the requested prime number has
        // before we use the large prime number generating algorithms
        private const val SMALL_PRIME_THRESHOLD = 95

        // Certainty required to meet the spec of probablePrime
        private const val DEFAULT_PRIME_CERTAINTY = 100

        /**
         * Returns a positive BigInteger that is probably prime, with the
         * specified bitLength. The probability that a BigInteger returned
         * by this method is composite does not exceed 2<sup>-100</sup>.
         *
         * @param  bitLength bitLength of the returned BigInteger.
         * @param  rnd source of random bits used to select candidates to be
         * tested for primality.
         * @return a BigInteger of `bitLength` bits that is probably prime
         * @throws ArithmeticException `bitLength < 2`.
         * @see .bitLength
         * @since 1.4
         */
        fun probablePrime(bitLength: Int, rnd: Random): BigInteger? {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")

            // The cutoff of 95 was chosen empirically for best performance
            return (if (bitLength < SMALL_PRIME_THRESHOLD) smallPrime(
                bitLength,
                DEFAULT_PRIME_CERTAINTY,
                rnd
            ) else largePrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd))
        }

        /**
         * Find a random number of the specified bitLength that is probably prime.
         * This method is used for smaller primes, its performance degrades on
         * larger bitlengths.
         *
         * This method assumes bitLength > 1.
         */
        private fun smallPrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            val magLen = (bitLength + 31) ushr 5
            val temp: IntArray? = IntArray(magLen)
            val highBit = 1 shl ((bitLength + 31) and 0x1f) // High bit of high int
            val highMask = (highBit shl 1) - 1 // Bits to keep in high int

            while (true) {
                // Construct a candidate
                for (i in 0..<magLen) temp!![i] = rnd.nextInt()
                temp!![0] = (temp[0] and highMask) or highBit // Ensure exact length
                if (bitLength > 2) temp[magLen - 1] =
                    temp[magLen - 1] or 1 // Make odd if bitlen > 2


                val p = BigInteger(temp, 1)

                // Do cheap "pre-test" if applicable
                if (bitLength > 6) {
                    val r = p.remainder(SMALL_PRIME_PRODUCT).longValue()
                    if ((r % 3 == 0L) || (r % 5 == 0L) || (r % 7 == 0L) || (r % 11 == 0L) ||
                        (r % 13 == 0L) || (r % 17 == 0L) || (r % 19 == 0L) || (r % 23 == 0L) ||
                        (r % 29 == 0L) || (r % 31 == 0L) || (r % 37 == 0L) || (r % 41 == 0L)
                    ) continue  // Candidate is composite; try another
                }

                // All candidates of bitLength 2 and 3 are prime by this point
                if (bitLength < 4) return p

                // Do expensive test if we survive pre-test (or it's inapplicable)
                if (p.primeToCertainty(certainty, rnd)) return p
            }
        }

        private val SMALL_PRIME_PRODUCT =
            valueOf(3L * 5 * 7 * 11 * 13 * 17 * 19 * 23 * 29 * 31 * 37 * 41)

        /**
         * Find a random number of the specified bitLength that is probably prime.
         * This method is more appropriate for larger bitlengths since it uses
         * a sieve to eliminate most composites before using a more expensive
         * test.
         */
        private fun largePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            var p: BigInteger
            p = BigInteger(bitLength, rnd).setBit(bitLength - 1)
            p.mag[p.mag.size - 1] = p.mag[p.mag.size - 1] and -0x2

            // Use a sieve length likely to contain the next prime number
            val searchLen = (bitLength / 20) * 64
            var searchSieve: io.github.aughtone.types.math.BitSieve =
                io.github.aughtone.types.math.BitSieve(p, searchLen)
            var candidate: BigInteger? = searchSieve.retrieve(p, certainty, rnd)

            while ((candidate == null) || (candidate.bitLength() != bitLength)) {
                p = p.add(valueOf((2 * searchLen).toLong()))
                if (p.bitLength() != bitLength) p = BigInteger(bitLength, rnd).setBit(bitLength - 1)
                p.mag[p.mag.size - 1] = p.mag[p.mag.size - 1] and -0x2
                searchSieve = io.github.aughtone.types.math.BitSieve(p, searchLen)
                candidate = searchSieve.retrieve(p, certainty, rnd)
            }
            return candidate
        }

        /**
         * Computes Jacobi(p,n).
         * Assumes n positive, odd, n>=3.
         */
        private fun jacobiSymbol(p: Int, n: BigInteger): Int {
            var p = p
            if (p == 0) return 0

            // Algorithm and comments adapted from Colin Plumb's C library.
            var j = 1
            var u = n.mag[n.mag.size - 1]

            // Make p positive
            if (p < 0) {
                p = -p
                val n8 = u and 7
                if ((n8 == 3) || (n8 == 7)) j = -j // 3 (011) or 7 (111) mod 8
            }

            // Get rid of factors of 2 in p
            while ((p and 3) == 0) p = p shr 2
            if ((p and 1) == 0) {
                p = p shr 1
                if (((u xor (u shr 1)) and 2) != 0) j = -j // 3 (011) or 5 (101) mod 8
            }
            if (p == 1) return j
            // Then, apply quadratic reciprocity
            if ((p and u and 2) != 0)  // p = u = 3 (mod 4)?
                j = -j
            // And reduce u mod p
            u = n.mod(valueOf(p.toLong())).intValue()

            // Now compute Jacobi(u,p), u < p
            while (u != 0) {
                while ((u and 3) == 0) u = u shr 2
                if ((u and 1) == 0) {
                    u = u shr 1
                    if (((p xor (p shr 1)) and 2) != 0) j = -j // 3 (011) or 5 (101) mod 8
                }
                if (u == 1) return j
                // Now both u and p are odd, so use quadratic reciprocity
                assert(u < p)
                val t = u
                u = p
                p = t
                if ((u and p and 2) != 0)  // u = p = 3 (mod 4)?
                    j = -j
                // Now u >= p, so it can be reduced
                u %= p
            }
            return 0
        }

        private fun lucasLehmerSequence(z: Int, k: BigInteger, n: BigInteger): BigInteger {
            val d = valueOf(z.toLong())
            var u = ONE
            var u2: BigInteger
            var v = ONE
            var v2: BigInteger

            for (i in k.bitLength() - 2 downTo 0) {
                u2 = u.multiply(v)!!.mod(n)

                v2 = v.square()!!.add(d.multiply(u.square()!!)!!).mod(n)
                if (v2.testBit(0)) v2 = v2.subtract(n)

                v2 = v2.shiftRight(1)

                u = u2
                v = v2
                if (k.testBit(i)) {
                    u2 = u.add(v).mod(n)
                    if (u2.testBit(0)) u2 = u2.subtract(n)

                    u2 = u2.shiftRight(1)
                    v2 = v.add(d.multiply(u)!!).mod(n)
                    if (v2.testBit(0)) v2 = v2.subtract(n)
                    v2 = v2.shiftRight(1)

                    u = u2
                    v = v2
                }
            }
            return u
        }

        @Volatile
        private var staticRandom: Random? = null

        private val secureRandom: Random?
            get() {
                if (staticRandom == null) {
                    staticRandom = SecureRandom()
                }
                return staticRandom
            }

        //Static Factory Methods
        /**
         * Returns a BigInteger whose value is equal to that of the
         * specified `long`.  This "static factory method" is
         * provided in preference to a (`long`) constructor
         * because it allows for reuse of frequently used BigIntegers.
         *
         * @param  val value of the BigInteger to return.
         * @return a BigInteger with the specified value.
         */
        @JvmStatic
        fun valueOf(`val`: Long): BigInteger {
            // If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed constant
            if (`val` == 0L) return ZERO
            if (`val` > 0 && `val` <= MAX_CONSTANT) return posConst!![`val`.toInt()]
            else if (`val` < 0 && `val` >= -MAX_CONSTANT) return negConst!![-`val`.toInt()]

            return BigInteger(`val`)
        }

        /**
         * Returns a BigInteger with the given two's complement representation.
         * Assumes that the input array will not be modified (the returned
         * BigInteger will reference the input array if feasible).
         */
        private fun valueOf(`val`: IntArray?): BigInteger {
            return (if (`val`!![0] > 0) BigInteger(`val`, 1) else BigInteger(`val`))
        }

        // Constants
        /**
         * Initialize static constant array when class is loaded.
         */
        private const val MAX_CONSTANT = 16
        private val posConst: Array<BigInteger>? = kotlin.arrayOfNulls<BigInteger>(MAX_CONSTANT + 1)
        private val negConst: Array<BigInteger>? = kotlin.arrayOfNulls<BigInteger>(MAX_CONSTANT + 1)

        init {
            for (i in 1..MAX_CONSTANT) {
                val magnitude = IntArray(1)
                magnitude[0] = i
                posConst!![i] = BigInteger(magnitude, 1)
                negConst!![i] = BigInteger(magnitude, -1)
            }
        }

        /**
         * The BigInteger constant zero.
         *
         * @since   1.2
         */
        @JvmField
        val ZERO: BigInteger = BigInteger(IntArray(0), 0)

        /**
         * The BigInteger constant one.
         *
         * @since   1.2
         */
        val ONE: BigInteger = valueOf(1)

        /**
         * The BigInteger constant two.  (Not exported.)
         */
        private val TWO = valueOf(2)

        /**
         * The BigInteger constant ten.
         *
         * @since   1.5
         */
        val TEN: BigInteger = valueOf(10)

        /**
         * Adds the contents of the int arrays x and y. This method allocates
         * a new int array to hold the answer and returns a reference to that
         * array.
         */
        private fun add(x: IntArray, y: IntArray): IntArray? {
            // If x is shorter, swap the two arrays
            var x = x
            var y = y
            if (x.size < y.size) {
                val tmp = x
                x = y
                y = tmp
            }

            var xIndex = x.size
            var yIndex = y.size
            val result: IntArray? = IntArray(xIndex)
            var sum: Long = 0

            // Add common parts of both numbers
            while (yIndex > 0) {
                sum = (x[--xIndex].toLong() and LONG_MASK) +
                        (y[--yIndex].toLong() and LONG_MASK) + (sum ushr 32)
                result!![xIndex] = sum.toInt()
            }

            // Copy remainder of longer number while carry propagation is required
            var carry = (sum ushr 32 != 0L)
            while (xIndex > 0 && carry) carry =
                (((x[xIndex] + 1).also { result!![--xIndex] = it }) == 0)

            // Copy remainder of longer number
            while (xIndex > 0) result!![--xIndex] = x[xIndex]

            // Grow result if necessary
            if (carry) {
                val bigger: IntArray? = IntArray(result!!.size + 1)
                System.arraycopy(result, 0, bigger, 1, result.size)
                bigger!![0] = 0x01
                return bigger
            }
            return result
        }

        /**
         * Subtracts the contents of the second int arrays (little) from the
         * first (big).  The first int array (big) must represent a larger number
         * than the second.  This method allocates the space necessary to hold the
         * answer.
         */
        private fun subtract(big: IntArray, little: IntArray): IntArray {
            var bigIndex = big.size
            val result: IntArray? = IntArray(bigIndex)
            var littleIndex = little.size
            var difference: Long = 0

            // Subtract common parts of both numbers
            while (littleIndex > 0) {
                difference = (big[--bigIndex].toLong() and LONG_MASK) -
                        (little[--littleIndex].toLong() and LONG_MASK) +
                        (difference shr 32)
                result!![bigIndex] = difference.toInt()
            }

            // Subtract remainder of longer number while borrow propagates
            var borrow = (difference shr 32 != 0L)
            while (bigIndex > 0 && borrow) borrow =
                (((big[bigIndex] - 1).also { result!![--bigIndex] = it }) == -1)

            // Copy remainder of longer number
            while (bigIndex > 0) result!![--bigIndex] = big[bigIndex]

            return result!!
        }

        /**
         * Squares the contents of the int array x. The result is placed into the
         * int array z.  The contents of x are not changed.
         */
        private fun squareToLen(x: IntArray, len: Int, z: IntArray?): IntArray {
            /*
         * The algorithm used here is adapted from Colin Plumb's C library.
         * Technique: Consider the partial products in the multiplication
         * of "abcde" by itself:
         *
         *               a  b  c  d  e
         *            *  a  b  c  d  e
         *          ==================
         *              ae be ce de ee
         *           ad bd cd dd de
         *        ac bc cc cd ce
         *     ab bb bc bd be
         *  aa ab ac ad ae
         *
         * Note that everything above the main diagonal:
         *              ae be ce de = (abcd) * e
         *           ad bd cd       = (abc) * d
         *        ac bc             = (ab) * c
         *     ab                   = (a) * b
         *
         * is a copy of everything below the main diagonal:
         *                       de
         *                 cd ce
         *           bc bd be
         *     ab ac ad ae
         *
         * Thus, the sum is 2 * (off the diagonal) + diagonal.
         *
         * This is accumulated beginning with the diagonal (which
         * consist of the squares of the digits of the input), which is then
         * divided by two, the off-diagonal added, and multiplied by two
         * again.  The low bit is simply a copy of the low bit of the
         * input, so it doesn't need special care.
         */
            var z = z
            val zlen = len shl 1
            if (z == null || z.size < zlen) z = IntArray(zlen)

            // Store the squares, right shifted one bit (i.e., divided by 2)
            var lastProductLowWord = 0
            run {
                var j = 0
                var i = 0
                while (j < len) {
                    val piece = (x[j].toLong() and LONG_MASK)
                    val product = piece * piece
                    z[i++] = (lastProductLowWord shl 31) or (product ushr 33).toInt()
                    z[i++] = (product ushr 1).toInt()
                    lastProductLowWord = product.toInt()
                    j++
                }
            }

            // Add in off-diagonal sums
            var i = len
            var offset = 1
            while (i > 0) {
                var t = x[i - 1]
                t = mulAdd(z, x, offset, i - 1, t)
                addOne(z, offset - 1, i, t)
                i--
                offset += 2
            }

            // Shift back up and set low bit
            primitiveLeftShift(z, zlen, 1)
            z[zlen - 1] = z[zlen - 1] or (x[len - 1] and 1)

            return z
        }

        /**
         * Package private method to return bit length for an integer.
         */
        @JvmStatic
        fun bitLengthForInt(n: Int): Int {
            return 32 - Integer.numberOfLeadingZeros(n)
        }

        /**
         * Left shift int array a up to len by n bits. Returns the array that
         * results from the shift since space may have to be reallocated.
         */
        private fun leftShift(a: IntArray, len: Int, n: Int): IntArray {
            val nInts = n ushr 5
            val nBits = n and 0x1F
            val bitsInHighWord = bitLengthForInt(a[0])

            // If shift can be done without recopy, do so
            if (n <= (32 - bitsInHighWord)) {
                primitiveLeftShift(a, len, nBits)
                return a
            } else { // Array must be resized
                if (nBits <= (32 - bitsInHighWord)) {
                    val result: IntArray? = IntArray(nInts + len)
                    for (i in 0..<len) result!![i] = a[i]
                    Companion.primitiveLeftShift(result!!, result.size, nBits)
                    return result
                } else {
                    val result: IntArray? = IntArray(nInts + len + 1)
                    for (i in 0..<len) result!![i] = a[i]
                    Companion.primitiveRightShift(result!!, result.size, 32 - nBits)
                    return result
                }
            }
        }

        // shifts a up to len right n bits assumes no leading zeros, 0<n<32
        fun primitiveRightShift(a: IntArray, len: Int, n: Int) {
            val n2 = 32 - n
            var i = len - 1
            var c = a[i]
            while (i > 0) {
                val b = c
                c = a[i - 1]
                a[i] = (c shl n2) or (b ushr n)
                i--
            }
            a[0] = a[0] ushr n
        }

        // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
        @JvmStatic
        fun primitiveLeftShift(a: IntArray, len: Int, n: Int) {
            if (len == 0 || n == 0) return

            val n2 = 32 - n
            var i = 0
            var c = a[i]
            val m = i + len - 1
            while (i < m) {
                val b = c
                c = a[i + 1]
                a[i] = (b shl n) or (c ushr n2)
                i++
            }
            a[len - 1] = a[len - 1] shl n
        }

        /**
         * Calculate bitlength of contents of the first len elements an int array,
         * assuming there are no leading zero ints.
         */
        private fun bitLength(`val`: IntArray, len: Int): Int {
            if (len == 0) return 0
            return ((len - 1) shl 5) + bitLengthForInt(`val`[0])
        }

        var bnExpModThreshTable: IntArray = intArrayOf(
            7, 25, 81, 241, 673, 1793,
            Integer.MAX_VALUE
        ) // Sentinel

        /**
         * Montgomery reduce n, modulo mod.  This reduces modulo mod and divides
         * by 2^(32*mlen). Adapted from Colin Plumb's C library.
         */
        private fun montReduce(n: IntArray, mod: IntArray, mlen: Int, inv: Int): IntArray {
            var c = 0
            var len = mlen
            var offset = 0

            do {
                val nEnd = n[n.size - 1 - offset]
                val carry = mulAdd(n, mod, offset, mlen, inv * nEnd)
                c += addOne(n, offset, mlen, carry)
                offset++
            } while (--len > 0)

            while (c > 0) c += subN(n, mod, mlen)

            while (intArrayCmpToLen(n, mod, mlen) >= 0) subN(n, mod, mlen)

            return n
        }


        /*
     * Returns -1, 0 or +1 as big-endian unsigned int array arg1 is less than,
     * equal to, or greater than arg2 up to length len.
     */
        private fun intArrayCmpToLen(arg1: IntArray, arg2: IntArray, len: Int): Int {
            for (i in 0..<len) {
                val b1 = arg1[i].toLong() and LONG_MASK
                val b2 = arg2[i].toLong() and LONG_MASK
                if (b1 < b2) return -1
                if (b1 > b2) return 1
            }
            return 0
        }

        /**
         * Subtracts two numbers of same length, returning borrow.
         */
        private fun subN(a: IntArray, b: IntArray, len: Int): Int {
            var len = len
            var sum: Long = 0

            while (--len >= 0) {
                sum = (a[len].toLong() and LONG_MASK) -
                        (b[len].toLong() and LONG_MASK) + (sum shr 32)
                a[len] = sum.toInt()
            }

            return (sum shr 32).toInt()
        }

        /**
         * Multiply an array by one word k and add to result, return the carry
         */
        fun mulAdd(out: IntArray, `in`: IntArray, offset: Int, len: Int, k: Int): Int {
            var offset = offset
            val kLong = k.toLong() and LONG_MASK
            var carry: Long = 0

            offset = out.size - offset - 1
            for (j in len - 1 downTo 0) {
                val product = (`in`[j].toLong() and LONG_MASK) * kLong +
                        (out[offset].toLong() and LONG_MASK) + carry
                out[offset--] = product.toInt()
                carry = product ushr 32
            }
            return carry.toInt()
        }

        /**
         * Add one word to the number a mlen words into a. Return the resulting
         * carry.
         */
        fun addOne(a: IntArray, offset: Int, mlen: Int, carry: Int): Int {
            var offset = offset
            var mlen = mlen
            offset = a.size - 1 - mlen - offset
            val t = (a[offset].toLong() and LONG_MASK) + (carry.toLong() and LONG_MASK)

            a[offset] = t.toInt()
            if ((t ushr 32) == 0L) return 0
            while (--mlen >= 0) {
                if (--offset < 0) { // Carry out of number
                    return 1
                } else {
                    a[offset]++
                    if (a[offset] != 0) return 0
                }
            }
            return 1
        }

        /* zero[i] is a string of i consecutive zeros. */
        private val zeros: Array<String?>? = kotlin.arrayOfNulls<String>(64)

        init {
            zeros!![63] =
                "000000000000000000000000000000000000000000000000000000000000000"
            for (i in 0..62) zeros[i] = zeros[63]!!.substring(0, i)
        }

        /**
         * Returns a copy of the input array stripped of any leading zero bytes.
         */
        private fun stripLeadingZeroInts(`val`: IntArray?): IntArray {
            val vlen = `val`!!.size
            var keep: Int

            // Find first nonzero byte
            keep = 0
            while (keep < vlen && `val`[keep] == 0) {
                keep++
            }
            return java.util.Arrays.copyOfRange(`val`, keep, vlen)
        }

        /**
         * Returns the input array stripped of any leading zero bytes.
         * Since the source is trusted the copying may be skipped.
         */
        private fun trustedStripLeadingZeroInts(`val`: IntArray?): IntArray {
            val vlen = `val`!!.size
            var keep: Int

            // Find first nonzero byte
            keep = 0
            while (keep < vlen && `val`[keep] == 0) {
                keep++
            }
            return (if (keep == 0) `val` else java.util.Arrays.copyOfRange(`val`, keep, vlen))
        }

        /**
         * Returns a copy of the input array stripped of any leading zero bytes.
         */
        private fun stripLeadingZeroBytes(a: ByteArray?): IntArray {
            val byteLength = a!!.size
            var keep: Int

            // Find first nonzero byte
            keep = 0
            while (keep < byteLength && a[keep].toInt() == 0) {
                keep++
            }

            // Allocate new array and copy relevant part of input array
            val intLength = ((byteLength - keep) + 3) ushr 2
            val result = IntArray(intLength)
            var b = byteLength - 1
            for (i in intLength - 1 downTo 0) {
                result[i] = a[b--].toInt() and 0xff
                val bytesRemaining = b - keep + 1
                val bytesToTransfer: Int = Math.min(3, bytesRemaining)
                var j = 8
                while (j <= (bytesToTransfer shl 3)) {
                    result[i] = result[i] or ((a[b--].toInt() and 0xff) shl j)
                    j += 8
                }
            }
            return result
        }

        /**
         * Takes an array a representing a negative 2's-complement number and
         * returns the minimal (no leading zero bytes) unsigned whose value is -a.
         */
        private fun makePositive(a: ByteArray?): IntArray {
            var keep: Int
            var k: Int
            val byteLength = a!!.size

            // Find first non-sign (0xff) byte of input
            keep = 0
            while (keep < byteLength && a[keep].toInt() == -1) {
                keep++
            }


            /* Allocate output array.  If all non-sign bytes are 0x00, we must
         * allocate space for one extra output byte. */
            k = keep
            while (k < byteLength && a[k].toInt() == 0) {
                k++
            }

            val extraByte = if (k == byteLength) 1 else 0
            val intLength = ((byteLength - keep + extraByte) + 3) / 4
            val result: IntArray? = IntArray(intLength)

            /* Copy one's complement of input into output, leaving extra
         * byte (if it exists) == 0x00 */
            var b = byteLength - 1
            for (i in intLength - 1 downTo 0) {
                result!![i] = a[b--].toInt() and 0xff
                var numBytesToTransfer: Int = Math.min(3, b - keep + 1)
                if (numBytesToTransfer < 0) numBytesToTransfer = 0
                var j = 8
                while (j <= 8 * numBytesToTransfer) {
                    result[i] = result[i] or ((a[b--].toInt() and 0xff) shl j)
                    j += 8
                }

                // Mask indicates which bits must be complemented
                val mask = -1 ushr (8 * (3 - numBytesToTransfer))
                result[i] = result[i].inv() and mask
            }

            // Add one to one's complement to generate two's complement
            for (i in result!!.indices.reversed()) {
                result[i] = ((result[i].toLong() and LONG_MASK) + 1).toInt()
                if (result[i] != 0) break
            }

            return result
        }

        /**
         * Takes an array a representing a negative 2's-complement number and
         * returns the minimal (no leading zero ints) unsigned whose value is -a.
         */
        private fun makePositive(a: IntArray?): IntArray {
            var keep: Int
            var j: Int

            // Find first non-sign (0xffffffff) int of input
            keep = 0
            while (keep < a!!.size && a[keep] == -1) {
                keep++
            }

            /* Allocate output array.  If all non-sign ints are 0x00, we must
         * allocate space for one extra output int. */
            j = keep
            while (j < a.size && a[j] == 0) {
                j++
            }
            val extraInt = (if (j == a.size) 1 else 0)
            val result: IntArray? = IntArray(a.size - keep + extraInt)

            /* Copy one's complement of input into output, leaving extra
         * int (if it exists) == 0x00 */
            for (i in keep..<a.size) result!![i - keep + extraInt] = a[i].inv()

            // Add one to one's complement to generate two's complement
            var i = result!!.size - 1
            while ((result[i] = result[i] + 1) == 0) {
                i--
            }

            return result
        }

        /*
     * The following two arrays are used for fast String conversions.  Both
     * are indexed by radix.  The first is the number of digits of the given
     * radix that can fit in a Java long without "going negative", i.e., the
     * highest integer n such that radix**n < 2**63.  The second is the
     * "long radix" that tears each number into "long digits", each of which
     * consists of the number of digits in the corresponding element in
     * digitsPerLong (longRadix[i] = i**digitPerLong[i]).  Both arrays have
     * nonsense values in their 0 and 1 elements, as radixes 0 and 1 are not
     * used.
     */
        private val digitsPerLong: IntArray? = intArrayOf(
            0, 0,
            62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14,
            14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12
        )

        private val longRadix: Array<BigInteger>? = arrayOf<BigInteger>(
            null, null,
            valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL),
            valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL),
            valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L),
            valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L),
            valueOf(0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L),
            valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL),
            valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L),
            valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L),
            valueOf(0x5da0e1e53c5c8000L), valueOf(0xb16a458ef403f19L),
            valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L),
            valueOf(0x5658597bcaa24000L), valueOf(0x6feb266931a75b7L),
            valueOf(0xc29e98000000000L), valueOf(0x14adf4b7320334b9L),
            valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL),
            valueOf(0x5a3c23e39c000000L), valueOf(0x4e900abb53e6b71L),
            valueOf(0x7600ec618141000L), valueOf(0xaee5720ee830681L),
            valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L),
            valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L),
            valueOf(0x41c21cb8e1000000L)
        )

        /*
     * These two arrays are the integer analogue of above.
     */
        private val digitsPerInt: IntArray? = intArrayOf(
            0, 0, 30, 19, 15, 13, 11,
            11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5
        )

        private val intRadix: IntArray? = intArrayOf(
            0, 0,
            0x40000000, 0x4546b3db, 0x40000000, 0x48c27395, 0x159fd800,
            0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00, 0xcc6db61,
            0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f, 0x10000000,
            0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000, 0x6b5a6e1d,
            0x6c20a40, 0x8d2d931, 0xb640000, 0xe8d4a51, 0x1269ae40,
            0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840, 0x34e63b41,
            0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519, 0x39aa400
        )

        /** use serialVersionUID from JDK 1.1. for interoperability  */
        private val serialVersionUID = -8287574255936472291L

        /**
         * Serializable fields for BigInteger.
         *
         * @serialField signum  int
         * signum of this BigInteger.
         * @serialField magnitude int[]
         * magnitude array of this BigInteger.
         * @serialField bitCount  int
         * number of bits in this BigInteger
         * @serialField bitLength int
         * the number of bits in the minimal two's-complement
         * representation of this BigInteger
         * @serialField lowestSetBit int
         * lowest set bit in the twos complement representation
         */
        private val serialPersistentFields: Array<ObjectStreamField?> = arrayOf<ObjectStreamField?>(
            ObjectStreamField("signum", Integer.TYPE),
            ObjectStreamField("magnitude", ByteArray::class.java),
            ObjectStreamField("bitCount", Integer.TYPE),
            ObjectStreamField("bitLength", Integer.TYPE),
            ObjectStreamField("firstNonzeroByteNum", Integer.TYPE),
            ObjectStreamField("lowestSetBit", Integer.TYPE)
        )

        // Support for resetting final fields while deserializing
        private val unsafe: sun.misc.Unsafe = sun.misc.Unsafe.getUnsafe()
        private const val signumOffset: Long = 0
        private const val magOffset: Long = 0

        init {
            try {
                signumOffset =
                    unsafe.objectFieldOffset(BigInteger::class.java.getDeclaredField("signum"))
                magOffset = unsafe.objectFieldOffset(BigInteger::class.java.getDeclaredField("mag"))
            } catch (ex: Exception) {
                throw Error(ex)
            }
        }
    }
}
