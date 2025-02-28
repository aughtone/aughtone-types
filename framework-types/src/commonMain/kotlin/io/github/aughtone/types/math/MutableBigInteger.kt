/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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
package io.github.aughtone.types.math

import kotlin.math.abs

/**
 * A class used to represent multiprecision integers that makes efficient
 * use of allocated space by allowing a number to occupy only part of
 * an array so that the arrays do not have to be reallocated as often.
 * When performing an operation with many iterations the array used to
 * hold a number is only reallocated when necessary and does not have to
 * be the same size as the number it represents. A mutable number allows
 * calculations to occur on the same number without having to create
 * a new number for every step of the calculation as occurs with
 * BigIntegers.
 *
 * @see BigInteger
 *
 * @author  Michael McCloskey
 * @since   1.3
 */

//import java.util.Arrays

internal class MutableBigInteger {
    /**
     * Holds the magnitude of this MutableBigInteger in big endian order.
     * The magnitude may start at an offset into the value array, and it may
     * end before the length of the value array.
     */
    var value: IntArray

    /**
     * The number of ints of the value array that are currently used
     * to hold the magnitude of this MutableBigInteger. The magnitude starts
     * at an offset and offset + intLen may be less than value.length.
     */
    var intLen: Int

    /**
     * The offset into the value array where the magnitude of this
     * MutableBigInteger begins.
     */
    var offset: Int = 0

    // Constructors
    /**
     * The default constructor. An empty MutableBigInteger is created with
     * a one word capacity.
     */
    constructor() {
        value = IntArray(1)
        intLen = 0
    }

    /**
     * Construct a new MutableBigInteger with a magnitude specified by
     * the int val.
     */
    constructor(value: Int) {
        this@MutableBigInteger.value = IntArray(1)
        intLen = 1
        this@MutableBigInteger.value[0] = value
    }

    /**
     * Construct a new MutableBigInteger with the specified value array
     * up to the length of the array supplied.
     */
    constructor(value: IntArray) {
        this@MutableBigInteger.value = value
        intLen = value.size
    }

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified BigInteger.
     */
    constructor(b: BigInteger) {
        intLen = b.mag.size
        //Java: Arrays.copyOf(b.mag, intLen)
        value =  b.mag.copyOf(newSize = intLen)
    }

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified MutableBigInteger.
     */
    constructor(value: MutableBigInteger) {
        intLen = value.intLen
        //Java: Arrays.copyOfRange(value.value, value.offset, value.offset + intLen)
        this@MutableBigInteger.value = value.value.copyOfRange(fromIndex = value.offset, toIndex = value.offset + intLen)
    }

    private val magnitudeArray: IntArray
        /**
         * Internal helper method to return the magnitude array. The caller is not
         * supposed to modify the returned array.
         */
        get() {
            //Java: Arrays.copyOfRange(value, offset, offset + intLen)
            if (offset > 0 || value.size != intLen) return value.copyOfRange(fromIndex = offset, toIndex = offset + intLen)
            return value
        }

    /**
     * Convert this MutableBigInteger to a long value. The caller has to make
     * sure this MutableBigInteger can be fit into long.
     */
    private fun toLong(): Long {
        require(intLen <= 2) { "this MutableBigInteger exceeds the range of long" }
        if (intLen == 0) return 0
        val d: Long = value[offset].toLong() and LONG_MASK
        return if (intLen == 2) d shl 32 or (value[offset + 1].toLong() and LONG_MASK) else d
    }

    /**
     * Convert this MutableBigInteger to a BigInteger object.
     */
    fun toBigInteger(sign: Int): BigInteger {
        if (intLen == 0 || sign == 0) return BigInteger.ZERO
        return BigInteger(this.magnitudeArray, sign)
    }

    /**
     * Convert this MutableBigInteger to BigDecimal object with the specified sign
     * and scale.
     */
    fun toBigDecimal(sign: Int, scale: Int): BigDecimal {
        if (intLen == 0 || sign == 0) return BigDecimal.valueOf(0, scale)
        val mag = this.magnitudeArray
        val len = mag.size
        val d = mag[0]
        // If this MutableBigInteger can't be fit into long, we need to
        // make a BigInteger object for the resultant BigDecimal object.
        if (len > 2 || (d < 0 && len == 2)) return BigDecimal(
            BigInteger(mag, sign),
            BigDecimal.INFLATED,
            scale,
            0
        )
        val v: Long =
            if (len == 2) ((mag[1].toLong() and LONG_MASK) or ((d.toLong() and LONG_MASK) shl 32)) else d.toLong() and LONG_MASK
        return BigDecimal(null, if (sign == -1) -v else v, scale, 0)
    }

    /**
     * Clear out a MutableBigInteger for reuse.
     */
    fun clear() {
        intLen = 0
        offset = intLen
        var index = 0
        val n = value.size
        while (index < n) {
            value[index] = 0
            index++
        }
    }

    /**
     * Set a MutableBigInteger to zero, removing its offset.
     */
    fun reset() {
        intLen = 0
        offset = intLen
    }

    /**
     * Compare the magnitude of two MutableBigIntegers. Returns -1, 0 or 1
     * as this MutableBigInteger is numerically less than, equal to, or
     * greater than <tt>b</tt>.
     */
    fun compare(b: MutableBigInteger): Int {
        val blen = b.intLen
        if (intLen < blen) return -1
        if (intLen > blen) return 1

        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        val bval = b.value
        var i = offset
        var j = b.offset
        while (i < intLen + offset) {
            val b1 = value[i] + -0x80000000
            val b2 = bval[j] + -0x80000000
            if (b1 < b2) return -1
            if (b1 > b2) return 1
            i++
            j++
        }
        return 0
    }

    /**
     * Compare this against half of a MutableBigInteger object (Needed for
     * remainder tests).
     * Assumes no leading unnecessary zeros, which holds for results
     * from divide().
     */
    fun compareHalf(b: MutableBigInteger): Int {
        val blen = b.intLen
        val len = intLen
        if (len <= 0) return if (blen <= 0) 0 else -1
        if (len > blen) return 1
        if (len < blen - 1) return -1
        val bval = b.value
        var bstart = 0
        var carry = 0
        // Only 2 cases left:len == blen or len == blen - 1
        if (len != blen) { // len == blen - 1
            if (bval[bstart] == 1) {
                ++bstart
                carry = -0x80000000
            } else return -1
        }
        // compare values with right-shifted values of b,
        // carrying shifted-out bits across words
        val value = this@MutableBigInteger.value
        var i = offset
        var j = bstart
        while (i < len + offset) {
            val bv = bval[j++]
            val hb: Long = ((bv ushr 1) + carry).toLong() and LONG_MASK
            val v: Long = value[i++].toLong() and LONG_MASK
            if (v != hb) return if (v < hb) -1 else 1
            carry = (bv and 1) shl 31 // carray will be either 0x80000000 or 0
        }
        return if (carry == 0) 0 else -1
    }

    private val lowestSetBit: Int
        /**
         * Return the index of the lowest set bit in this MutableBigInteger. If the
         * magnitude of this MutableBigInteger is zero, -1 is returned.
         */
        get() {
            if (intLen == 0) return -1
            var j: Int
            val b: Int
            j = intLen - 1
            while ((j > 0) && (value[j + offset] == 0)) {
                j--
            }
            b = value[j + offset]
            if (b == 0) return -1
            return ((intLen - 1 - j) shl 5) + b.countTrailingZeroBits()//Integer.numberOfTrailingZeros(b)
        }

    /**
     * Return the int in use in this MutableBigInteger at the specified
     * index. This method is not used because it is not inlined on all
     * platforms.
     */
    private fun getInt(index: Int): Int {
        return value[offset + index]
    }

    /**
     * Return a long which is equal to the unsigned value of the int in
     * use in this MutableBigInteger at the specified index. This method is
     * not used because it is not inlined on all platforms.
     */
    private fun getLong(index: Int): Long {
        return value[offset + index].toLong() and LONG_MASK
    }

    /**
     * Ensure that the MutableBigInteger is in normal form, specifically
     * making sure that there are no leading zeros, and that if the
     * magnitude is zero, then intLen is zero.
     */
    fun normalize() {
        if (intLen == 0) {
            offset = 0
            return
        }

        var index = offset
        if (value[index] != 0) return

        val indexBound = index + intLen
        do {
            index++
        } while (index < indexBound && value[index] == 0)

        val numZeros = index - offset
        intLen -= numZeros
        offset = (if (intLen == 0) 0 else offset + numZeros)
    }

    /**
     * If this MutableBigInteger cannot hold len words, increase the size
     * of the value array to len words.
     */
    private fun ensureCapacity(len: Int) {
        if (value.size < len) {
            value = IntArray(len)
            offset = 0
            intLen = len
        }
    }

    /**
     * Convert this MutableBigInteger into an int array with no leading
     * zeros, of a length that is equal to this MutableBigInteger's intLen.
     */
    fun toIntArray(): IntArray {
        val result = IntArray(intLen)
        for (i in 0..<intLen) result[i] = value[offset + i]
        return result
    }

    /**
     * Sets the int at index+offset in this MutableBigInteger to val.
     * This does not get inlined on all platforms so it is not used
     * as often as originally intended.
     */
    fun setInt(index: Int, value: Int) {
        this@MutableBigInteger.value[offset + index] = value
    }

    /**
     * Sets this MutableBigInteger's value array to the specified array.
     * The intLen is set to the specified length.
     */
    fun setValue(value: IntArray, length: Int) {
        this@MutableBigInteger.value = value
        intLen = length
        offset = 0
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the new array.
     */
    fun copyValue(src: MutableBigInteger) {
        val len = src.intLen
        if (value.size < len) value = IntArray(len)
//        System.arraycopy(src.value, src.offset, value, 0, len)
        src.value.copyInto(value, 0, src.offset, src.offset + len)
        intLen = len
        offset = 0
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the specified array.
     */
    fun copyValue(copyVal: IntArray) {
        val len =copyVal.size
        if (value.size < len) value = IntArray(len)
        //System.arraycopy(`val`, 0, value, 0, len)
        copyVal.copyInto(value, 0, 0, len)
        intLen = len
        offset = 0
    }

    val isOne: Boolean
        /**
         * Returns true iff this MutableBigInteger has a value of one.
         */
        get() = (intLen == 1) && (value[offset] == 1)

    val isZero: Boolean
        /**
         * Returns true iff this MutableBigInteger has a value of zero.
         */
        get() = (intLen == 0)

    val isEven: Boolean
        /**
         * Returns true iff this MutableBigInteger is even.
         */
        get() = (intLen == 0) || ((value[offset + intLen - 1] and 1) == 0)

    val isOdd: Boolean
        /**
         * Returns true iff this MutableBigInteger is odd.
         */
        get() = if (this.isZero) false else ((value[offset + intLen - 1] and 1) == 1)

    val isNormal: Boolean
        /**
         * Returns true iff this MutableBigInteger is in normal form. A
         * MutableBigInteger is in normal form if it has no leading zeros
         * after the offset, and intLen + offset <= value.length.
         */
        get() {
            if (intLen + offset > value.size) return false
            if (intLen == 0) return true
            return (value[offset] != 0)
        }

    /**
     * Returns a String representation of this MutableBigInteger in radix 10.
     */
    override fun toString(): String {
        val b = toBigInteger(1)
        return b.toString()
    }

    /**
     * Right shift this MutableBigInteger n bits. The MutableBigInteger is left
     * in normal form.
     */
    fun rightShift(n: Int) {
        if (intLen == 0) return
        val nInts = n ushr 5
        val nBits = n and 0x1F
        this.intLen -= nInts
        if (nBits == 0) return
        val bitsInHighWord = BigInteger.bitLengthForInt(value[offset])
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits)
            this.intLen--
        } else {
            primitiveRightShift(nBits)
        }
    }

    /**
     * Left shift this MutableBigInteger n bits.
     */
    fun leftShift(n: Int) {
        /*
         * If there is enough storage space in this MutableBigInteger already
         * the available space will be used. Space to the right of the used
         * ints in the value array is faster to utilize, so the extra space
         * will be taken from the right if possible.
         */
        if (intLen == 0) return
        val nInts = n ushr 5
        val nBits = n and 0x1F
        val bitsInHighWord = BigInteger.bitLengthForInt(value[offset])

        // If shift can be done without moving words, do so
        if (n <= (32 - bitsInHighWord)) {
            primitiveLeftShift(nBits)
            return
        }

        var newLen = intLen + nInts + 1
        if (nBits <= (32 - bitsInHighWord)) newLen--
        if (value.size < newLen) {
            // The array must grow
            val result = IntArray(newLen)
            for (i in 0..<intLen) result[i] = value[offset + i]
            setValue(result, newLen)
        } else if (value.size - offset >= newLen) {
            // Use space on right
            for (i in 0..<newLen - intLen) value[offset + intLen + i] = 0
        } else {
            // Must use space on left
            for (i in 0..<intLen) value[i] = value[offset + i]
            for (i in intLen..<newLen) value[i] = 0
            offset = 0
        }
        intLen = newLen
        if (nBits == 0) return
        if (nBits <= (32 - bitsInHighWord)) primitiveLeftShift(nBits)
        else primitiveRightShift(32 - nBits)
    }

    /**
     * A primitive used for division. This method adds in one multiple of the
     * divisor a back to the dividend result at a specified offset. It is used
     * when qhat was estimated too large, and must be adjusted.
     */
    private fun divadd(a: IntArray, result: IntArray, offset: Int): Int {
        var carry: Long = 0

        for (j in a.indices.reversed()) {
            val sum = (a[j].toLong() and LONG_MASK) +
                    (result[j + offset].toLong() and LONG_MASK) + carry
            result[j + offset] = sum.toInt()
            carry = sum ushr 32
        }
        return carry.toInt()
    }

    /**
     * This method is used for division. It multiplies an n word input a by one
     * word input x, and subtracts the n word product from q. This is needed
     * when subtracting qhat*divisor from dividend.
     */
    private fun mulsub(q: IntArray, a: IntArray, x: Int, len: Int, offset: Int): Int {
        var offset = offset
        val xLong: Long = x.toLong() and LONG_MASK
        var carry: Long = 0
        offset += len

        for (j in len - 1 downTo 0) {
            val product = (a[j].toLong() and LONG_MASK) * xLong + carry
            val difference = q[offset] - product
            q[offset--] = difference.toInt()
            carry = ((product ushr 32)
                    + (if ((difference and LONG_MASK) >
                (((product.toInt().inv()).toLong() and LONG_MASK))
            ) 1 else 0))
        }
        return carry.toInt()
    }

    /**
     * Right shift this MutableBigInteger n bits, where n is
     * less than 32.
     * Assumes that intLen > 0, n > 0 for speed
     */
    private fun primitiveRightShift(n: Int) {
        val `val` = value
        val n2 = 32 - n
        var i = offset + intLen - 1
        var c = `val`[i]
        while (i > offset) {
            val b = c
            c = `val`[i - 1]
            `val`[i] = (c shl n2) or (b ushr n)
            i--
        }
        `val`[offset] = `val`[offset] ushr n
    }

    /**
     * Left shift this MutableBigInteger n bits, where n is
     * less than 32.
     * Assumes that intLen > 0, n > 0 for speed
     */
    private fun primitiveLeftShift(n: Int) {
        val `val` = value
        val n2 = 32 - n
        var i = offset
        var c = `val`[i]
        val m = i + intLen - 1
        while (i < m) {
            val b = c
            c = `val`[i + 1]
            `val`[i] = (b shl n) or (c ushr n2)
            i++
        }
        `val`[offset + intLen - 1] = `val`[offset + intLen - 1] shl n
    }

    /**
     * Adds the contents of two MutableBigInteger objects.The result
     * is placed within this MutableBigInteger.
     * The contents of the addend are not changed.
     */
    fun add(addend: MutableBigInteger) {
        var x = intLen
        var y = addend.intLen
        var resultLen = (if (intLen > addend.intLen) intLen else addend.intLen)
        var result = (if (value.size < resultLen) IntArray(resultLen) else value)

        var rstart = result.size - 1
        var sum: Long
        var carry: Long = 0

        // Add common parts of both numbers
        while (x > 0 && y > 0) {
            x--
            y--
            sum = (value[x + offset].toLong() and LONG_MASK) +
                    (addend.value[y + addend.offset].toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum ushr 32
        }

        // Add remainder of the longer number
        while (x > 0) {
            x--
            if (carry == 0L && result == value && rstart == (x + offset)) return
            sum = (value[x + offset].toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum ushr 32
        }
        while (y > 0) {
            y--
            sum = (addend.value[y + addend.offset].toLong() and LONG_MASK) + carry
            result[rstart--] = sum.toInt()
            carry = sum ushr 32
        }

        if (carry > 0) { // Result must grow in length
            resultLen++
            if (result.size < resultLen) {
                val temp: IntArray = IntArray(resultLen)
                // Result one word longer from carry-out; copy low-order
                // bits into new result.
                //System.arraycopy(result, 0, temp, 1, result.size)
                result.copyInto(temp, 1, 0, result.size)
                temp[0] = 1
                result = temp
            } else {
                result[rstart--] = 1
            }
        }

        value = result
        intLen = resultLen
        offset = result.size - resultLen
    }


    /**
     * Subtracts the smaller of this and b from the larger and places the
     * result into this MutableBigInteger.
     */
    fun subtract(b: MutableBigInteger): Int {
        var b = b
        var a = this

        var result = value
        val sign = a.compare(b)

        if (sign == 0) {
            reset()
            return 0
        }
        if (sign < 0) {
            val tmp = a
            a = b
            b = tmp
        }

        val resultLen = a.intLen
        if (result.size < resultLen) result = IntArray(resultLen)

        var diff: Long = 0
        var x = a.intLen
        var y = b.intLen
        var rstart = result.size - 1

        // Subtract common parts of both numbers
        while (y > 0) {
            x--
            y--

            diff = (a.value[x + a.offset].toLong() and LONG_MASK) -
                    (b.value[y + b.offset].toLong() and LONG_MASK) - (-(diff shr 32).toInt())
            result[rstart--] = diff.toInt()
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x--
            diff = (a.value[x + a.offset].toLong() and LONG_MASK) - (-(diff shr 32).toInt())
            result[rstart--] = diff.toInt()
        }

        value = result
        intLen = resultLen
        offset = value.size - resultLen
        normalize()
        return sign
    }

    /**
     * Subtracts the smaller of a and b from the larger and places the result
     * into the larger. Returns 1 if the answer is in a, -1 if in b, 0 if no
     * operation was performed.
     */
    private fun difference(b: MutableBigInteger): Int {
        var b = b
        var a = this
        val sign = a.compare(b)
        if (sign == 0) return 0
        if (sign < 0) {
            val tmp = a
            a = b
            b = tmp
        }

        var diff: Long = 0
        var x = a.intLen
        var y = b.intLen

        // Subtract common parts of both numbers
        while (y > 0) {
            x--
            y--
            diff = (a.value[a.offset + x].toLong() and LONG_MASK) -
                    (b.value[b.offset + y].toLong() and LONG_MASK) - (-(diff shr 32).toInt())
            a.value[a.offset + x] = diff.toInt()
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x--
            diff = (a.value[a.offset + x].toLong() and LONG_MASK) - (-(diff shr 32).toInt())
            a.value[a.offset + x] = diff.toInt()
        }

        a.normalize()
        return sign
    }

    /**
     * Multiply the contents of two MutableBigInteger objects. The result is
     * placed into MutableBigInteger z. The contents of y are not changed.
     */
    fun multiply(y: MutableBigInteger, z: MutableBigInteger) {
        val xLen = intLen
        val yLen = y.intLen
        val newLen = xLen + yLen

        // Put z into an appropriate state to receive product
        if (z.value.size < newLen) z.value = IntArray(newLen)
        z.offset = 0
        z.intLen = newLen

        // The first iteration is hoisted out of the loop to avoid extra add
        var carry: Long = 0
        var j = yLen - 1
        var k = yLen + xLen - 1
        while (j >= 0) {
            val product = (y.value[j + y.offset].toLong() and LONG_MASK) *
                    (value[xLen - 1 + offset].toLong() and LONG_MASK) + carry
            z.value[k] = product.toInt()
            carry = product ushr 32
            j--
            k--
        }
        z.value[xLen - 1] = carry.toInt()

        // Perform the multiplication word by word
        for (i in xLen - 2 downTo 0) {
            carry = 0
            var j = yLen - 1
            var k = yLen + i
            while (j >= 0) {
                val product = (y.value[j + y.offset].toLong() and LONG_MASK) *
                        (value[i + offset].toLong() and LONG_MASK) +
                        (z.value[k].toLong() and LONG_MASK) + carry
                z.value[k] = product.toInt()
                carry = product ushr 32
                j--
                k--
            }
            z.value[i] = carry.toInt()
        }

        // Remove leading zeros from product
        z.normalize()
    }

    /**
     * Multiply the contents of this MutableBigInteger by the word y. The
     * result is placed into z.
     */
    fun mul(y: Int, z: MutableBigInteger) {
        if (y == 1) {
            z.copyValue(this)
            return
        }

        if (y == 0) {
            z.clear()
            return
        }

        // Perform the multiplication word by word
        val ylong: Long = y.toLong() and LONG_MASK
        val zval = (if (z.value.size < intLen + 1)
            IntArray(intLen + 1)
        else
            z.value)
        var carry: Long = 0
        for (i in intLen - 1 downTo 0) {
            val product = ylong * (value[i + offset].toLong() and LONG_MASK) + carry
            zval[i + 1] = product.toInt()
            carry = product ushr 32
        }

        if (carry == 0L) {
            z.offset = 1
            z.intLen = intLen
        } else {
            z.offset = 0
            z.intLen = intLen + 1
            zval[0] = carry.toInt()
        }
        z.value = zval
    }

    /**
     * This method is used for division of an n word dividend by a one word
     * divisor. The quotient is placed into quotient. The one word divisor is
     * specified by divisor.
     *
     * @return the remainder of the division is returned.
     */
    fun divideOneWord(divisor: Int, quotient: MutableBigInteger): Int {
        val divisorLong: Long = divisor.toLong() and LONG_MASK

        // Special case of one word dividend
        if (intLen == 1) {
            val dividendValue: Long = value[offset].toLong() and LONG_MASK
            val q = (dividendValue / divisorLong).toInt()
            val r = (dividendValue - q * divisorLong).toInt()
            quotient.value[0] = q
            quotient.intLen = if (q == 0) 0 else 1
            quotient.offset = 0
            return r
        }

        if (quotient.value.size < intLen) quotient.value = IntArray(intLen)
        quotient.offset = 0
        quotient.intLen = intLen

        // Normalize the divisor
        val shift: Int = divisor.countLeadingZeroBits()// Integer.numberOfLeadingZeros(divisor)

        var rem = value[offset]
        var remLong: Long = rem.toLong() and LONG_MASK
        if (remLong < divisorLong) {
            quotient.value[0] = 0
        } else {
            quotient.value[0] = (remLong / divisorLong).toInt()
            rem = (remLong - (quotient.value[0] * divisorLong)).toInt()
            remLong = rem.toLong() and LONG_MASK
        }

        var xlen = intLen
        val qWord = IntArray(2)
        while (--xlen > 0) {
            val dividendEstimate = (remLong shl 32) or
                    (value[offset + intLen - xlen].toLong() and LONG_MASK)
            if (dividendEstimate >= 0) {
                qWord[0] = (dividendEstimate / divisorLong).toInt()
                qWord[1] = (dividendEstimate - qWord[0] * divisorLong).toInt()
            } else {
                divWord(qWord, dividendEstimate, divisor)
            }
            quotient.value[intLen - xlen] = qWord[0]
            rem = qWord[1]
            remLong = rem.toLong() and LONG_MASK
        }

        quotient.normalize()
        // Unnormalize
        if (shift > 0) return rem % divisor
        else return rem
    }

    /**
     * Calculates the quotient of this div b and places the quotient in the
     * provided MutableBigInteger objects and the remainder object is returned.
     *
     * Uses Algorithm D in Knuth section 4.3.1.
     * Many optimizations to that algorithm have been adapted from the Colin
     * Plumb C library.
     * It special cases one word divisors for speed. The content of b is not
     * changed.
     *
     */
    fun divide(b: MutableBigInteger, quotient: MutableBigInteger): MutableBigInteger {
        if (b.intLen == 0) throw ArithmeticException("BigInteger divide by zero")

        // Dividend is zero
        if (intLen == 0) {
            quotient.intLen = quotient.offset
            return MutableBigInteger()
        }

        val cmp = compare(b)
        // Dividend less than divisor
        if (cmp < 0) {
            quotient.offset = 0
            quotient.intLen = quotient.offset
            return MutableBigInteger(this)
        }
        // Dividend equal to divisor
        if (cmp == 0) {
            quotient.intLen = 1
            quotient.value[0] = quotient.intLen
            quotient.offset = 0
            return MutableBigInteger()
        }

        quotient.clear()
        // Special case one word divisor
        if (b.intLen == 1) {
            val r = divideOneWord(b.value[b.offset], quotient)
            if (r == 0) return MutableBigInteger()
            return MutableBigInteger(r)
        }

        // Copy divisor value to protect divisor
        val div: IntArray = b.value.copyOfRange(b.offset, b.offset + b.intLen)
        return divideMagnitude(div, quotient)
    }

    /**
     * Internally used  to calculate the quotient of this div v and places the
     * quotient in the provided MutableBigInteger object and the remainder is
     * returned.
     *
     * @return the remainder of the division will be returned.
     */
    fun divide(v: Long, quotient: MutableBigInteger): Long {
        var v = v
        if (v == 0L) throw ArithmeticException("BigInteger divide by zero")

        // Dividend is zero
        if (intLen == 0) {
            quotient.offset = 0
            quotient.intLen = quotient.offset
            return 0
        }
        if (v < 0) v = -v

        val d = (v ushr 32).toInt()
        quotient.clear()
        // Special case on word divisor
        if (d == 0) return divideOneWord(v.toInt(), quotient).toLong() and LONG_MASK
        else {
            val div = intArrayOf(d, (v and LONG_MASK) as Int)
            return divideMagnitude(div, quotient).toLong()
        }
    }

    /**
     * Divide this MutableBigInteger by the divisor represented by its magnitude
     * array. The quotient will be placed into the provided quotient object &
     * the remainder object is returned.
     */
    private fun divideMagnitude(
        divisor: IntArray,
        quotient: MutableBigInteger
    ): MutableBigInteger {
        // Remainder starts as dividend with space for a leading zero

        val rem = MutableBigInteger(IntArray(intLen + 1))
        // System.arraycopy(value, offset, rem.value, 1, intLen)
        value.copyInto(rem.value, 1, offset, offset + intLen)

        rem.intLen = intLen
        rem.offset = 1

        val nlen = rem.intLen

        // Set the quotient size
        val dlen = divisor.size
        val limit = nlen - dlen + 1
        if (quotient.value.size < limit) {
            quotient.value = IntArray(limit)
            quotient.offset = 0
        }
        quotient.intLen = limit
        val q = quotient.value

        // D1 normalize the divisor
        val shift: Int = divisor[0].countLeadingZeroBits()// Integer.numberOfLeadingZeros(divisor[0])
        if (shift > 0) {
            // First shift will not grow array
            BigInteger.primitiveLeftShift(divisor, dlen, shift)
            // But this one might
            rem.leftShift(shift)
        }

        // Must insert leading 0 in rem if its length did not change
        if (rem.intLen == nlen) {
            rem.offset = 0
            rem.value[0] = 0
            rem.intLen++
        }

        val dh = divisor[0]
        val dhLong: Long = dh.toLong() and LONG_MASK
        val dl = divisor[1]
        val qWord = IntArray(2)

        // D2 Initialize j
        for (j in 0..<limit) {
            // D3 Calculate qhat
            // estimate qhat
            var qhat = 0
            var qrem = 0
            var skipCorrection = false
            val nh = rem.value[j + rem.offset]
            val nh2 = nh + -0x80000000
            val nm = rem.value[j + 1 + rem.offset]

            if (nh == dh) {
                qhat = 0.inv()
                qrem = nh + nm
                skipCorrection = qrem + -0x80000000 < nh2
            } else {
                val nChunk = ((nh.toLong()) shl 32) or (nm.toLong() and LONG_MASK)
                if (nChunk >= 0) {
                    qhat = (nChunk / dhLong).toInt()
                    qrem = (nChunk - (qhat * dhLong)).toInt()
                } else {
                    divWord(qWord, nChunk, dh)
                    qhat = qWord[0]
                    qrem = qWord[1]
                }
            }

            if (qhat == 0) continue

            if (!skipCorrection) { // Correct qhat
                val nl: Long = rem.value[j + 2 + rem.offset].toLong() and LONG_MASK
                var rs: Long = ((qrem.toLong() and LONG_MASK) shl 32) or nl
                var estProduct: Long = (dl.toLong() and LONG_MASK) * (qhat.toLong() and LONG_MASK)

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--
                    qrem = ((qrem.toLong() and LONG_MASK) + dhLong) as Int
                    if ((qrem.toLong() and LONG_MASK) >= dhLong) {
                        estProduct -= (dl.toLong() and LONG_MASK)
                        rs = ((qrem.toLong() and LONG_MASK) shl 32) or nl
                        if (unsignedLongCompare(estProduct, rs)) qhat--
                    }
                }
            }

            // D4 Multiply and subtract
            rem.value[j + rem.offset] = 0
            val borrow = mulsub(rem.value, divisor, qhat, dlen, j + rem.offset)

            // D5 Test remainder
            if (borrow + -0x80000000 > nh2) {
                // D6 Add back
                divadd(divisor, rem.value, j + 1 + rem.offset)
                qhat--
            }

            // Store the quotient digit
            q[j] = qhat
        } // D7 loop on j


        // D8 Unnormalize
        if (shift > 0) rem.rightShift(shift)

        quotient.normalize()
        rem.normalize()
        return rem
    }

    /**
     * Compare two longs as if they were unsigned.
     * Returns true iff one is bigger than two.
     */
    private fun unsignedLongCompare(one: Long, two: Long): Boolean {
        return (one + Long.MIN_VALUE) > (two + Long.MIN_VALUE)
    }

    /**
     * This method divides a long quantity by an int to estimate
     * qhat for two multi precision numbers. It is used when
     * the signed value of n is less than zero.
     */
    private fun divWord(result: IntArray, n: Long, d: Int) {
        val dLong: Long = d.toLong() and LONG_MASK

        if (dLong == 1L) {
            result[0] = n.toInt()
            result[1] = 0
            return
        }

        // Approximate the quotient and remainder
        var q = (n ushr 1) / (dLong ushr 1)
        var r = n - q * dLong

        // Correct the approximation
        while (r < 0) {
            r += dLong
            q--
        }
        while (r >= dLong) {
            r -= dLong
            q++
        }

        // n - q*dlong == r && 0 <= r <dLong, hence we're done.
        result[0] = q.toInt()
        result[1] = r.toInt()
    }

    /**
     * Calculate GCD of this and b. This and b are changed by the computation.
     */
    fun hybridGCD(b: MutableBigInteger): MutableBigInteger {
        // Use Euclid's algorithm until the numbers are approximately the
        // same length, then use the binary GCD algorithm to find the GCD.
        var b = b
        var a = this
        val q = MutableBigInteger()

        while (b.intLen != 0) {
            if (abs(a.intLen - b.intLen) < 2) return a.binaryGCD(b) // if (Math.abs(a.intLen - b.intLen) < 2) return a.binaryGCD(b)

            val r = a.divide(b, q)
            a = b
            b = r
        }
        return a
    }

    /**
     * Calculate GCD of this and v.
     * Assumes that this and v are not zero.
     */
    private fun binaryGCD(v: MutableBigInteger): MutableBigInteger {
        // Algorithm B from Knuth section 4.5.2
        var v = v
        var u = this
        val r = MutableBigInteger()

        // step B1
        val s1 = u.lowestSetBit
        val s2 = v.lowestSetBit
        val k = if (s1 < s2) s1 else s2
        if (k != 0) {
            u.rightShift(k)
            v.rightShift(k)
        }

        // step B2
        val uOdd = (k == s1)
        var t = if (uOdd) v else u
        var tsign = if (uOdd) -1 else 1

        var lb: Int
        while ((t.lowestSetBit.also { lb = it }) >= 0) {
            // steps B3 and B4
            t.rightShift(lb)
            // step B5
            if (tsign > 0) u = t
            else v = t

            // Special case one word numbers
            if (u.intLen < 2 && v.intLen < 2) {
                var x = u.value[u.offset]
                val y = v.value[v.offset]
                x = binaryGcd(x, y)
                r.value[0] = x
                r.intLen = 1
                r.offset = 0
                if (k > 0) r.leftShift(k)
                return r
            }

            // step B6
            if ((u.difference(v).also { tsign = it }) == 0) break
            t = if (tsign >= 0) u else v
        }

        if (k > 0) u.leftShift(k)
        return u
    }

    /**
     * Returns the modInverse of this mod p. This and p are not affected by
     * the operation.
     */
    fun mutableModInverse(p: MutableBigInteger): MutableBigInteger {
        // Modulus is odd, use Schroeppel's algorithm
        if (p.isOdd) return modInverse(p)

        // Base and modulus are even, throw exception
        if (this.isEven) throw ArithmeticException("BigInteger not invertible.")

        // Get even part of modulus expressed as a power of 2
        val powersOf2 = p.lowestSetBit

        // Construct odd part of modulus
        val oddMod = MutableBigInteger(p)
        oddMod.rightShift(powersOf2)

        if (oddMod.isOne) return modInverseMP2(powersOf2)

        // Calculate 1/a mod oddMod
        val oddPart = modInverse(oddMod)

        // Calculate 1/a mod evenMod
        val evenPart = modInverseMP2(powersOf2)

        // Combine the results using Chinese Remainder Theorem
        val y1 = modInverseBP2(oddMod, powersOf2)
        val y2 = oddMod.modInverseMP2(powersOf2)

        val temp1 = MutableBigInteger()
        val temp2 = MutableBigInteger()
        val result = MutableBigInteger()

        oddPart.leftShift(powersOf2)
        oddPart.multiply(y1, result)

        evenPart.multiply(oddMod, temp1)
        temp1.multiply(y2, temp2)

        result.add(temp2)
        return result.divide(p, temp1)
    }

    /*
     * Calculate the multiplicative inverse of this mod 2^k.
     */
    fun modInverseMP2(k: Int): MutableBigInteger {
        if (this.isEven) throw ArithmeticException("Non-invertible. (GCD != 1)")

        if (k > 64) return euclidModInverse(k)

        var t = inverseMod32(value[offset + intLen - 1])

        if (k < 33) {
            t = (if (k == 32) t else t and ((1 shl k) - 1))
            return MutableBigInteger(t)
        }

        var pLong: Long = (value[offset + intLen - 1].toLong() and LONG_MASK)
        if (intLen > 1) pLong = pLong or (value[offset + intLen - 2].toLong() shl 32)
        var tLong: Long = t.toLong() and LONG_MASK
        tLong = tLong * (2 - pLong * tLong) // 1 more Newton iter step
        tLong = (if (k == 64) tLong else tLong and ((1L shl k) - 1))

        val result = MutableBigInteger(IntArray(2))
        result.value[0] = (tLong ushr 32).toInt()
        result.value[1] = tLong.toInt()
        result.intLen = 2
        result.normalize()
        return result
    }

    /**
     * Calculate the multiplicative inverse of this mod mod, where mod is odd.
     * This and mod are not changed by the calculation.
     *
     * This method implements an algorithm due to Richard Schroeppel, that uses
     * the same intermediate representation as Montgomery Reduction
     * ("Montgomery Form").  The algorithm is described in an unpublished
     * manuscript entitled "Fast Modular Reciprocals."
     */
    private fun modInverse(mod: MutableBigInteger): MutableBigInteger {
        val p = MutableBigInteger(mod)
        var f = MutableBigInteger(this)
        var g = MutableBigInteger(p)
        var c: SignedMutableBigInteger = SignedMutableBigInteger(1)
        var d: SignedMutableBigInteger = SignedMutableBigInteger()
//        var temp: MutableBigInteger = null
//        var sTemp: SignedMutableBigInteger = null

        var k = 0
        // Right shift f k times until odd, left shift d k times
        if (f.isEven) {
            val trailingZeros = f.lowestSetBit
            f.rightShift(trailingZeros)
            d.leftShift(trailingZeros)
            k = trailingZeros
        }

        // The Almost Inverse Algorithm
        while (!f.isOne) {
            // If gcd(f, g) != 1, number is not invertible modulo mod
            if (f.isZero) throw ArithmeticException("BigInteger not invertible.")

            // If f < g exchange f, g and c, d
            if (f.compare(g) < 0) {
                val temp = f
                f = g
                g = temp
                val sTemp = d
                d = c
                c = sTemp
            }

            // If f == g (mod 4)
            if (((f.value[f.offset + f.intLen - 1] xor
                        g.value[g.offset + g.intLen - 1]) and 3) == 0
            ) {
                f.subtract(g)
                c.signedSubtract(d)
            } else { // If f != g (mod 4)
                f.add(g)
                c.signedAdd(d)
            }

            // Right shift f k times until odd, left shift d k times
            val trailingZeros = f.lowestSetBit
            f.rightShift(trailingZeros)
            d.leftShift(trailingZeros)
            k += trailingZeros
        }

        while (c.sign < 0) c.signedAdd(p)

        return fixup(c, p, k)
    }

    /**
     * Uses the extended Euclidean algorithm to compute the modInverse of base
     * mod a modulus that is a power of 2. The modulus is 2^k.
     */
    fun euclidModInverse(k: Int): MutableBigInteger {
        var b = MutableBigInteger(1)
        b.leftShift(k)
        val mod = MutableBigInteger(b)

        var a = MutableBigInteger(this)
        var q = MutableBigInteger()
        var r = b.divide(a, q)

        var swapper = b
        // swap b & r
        b = r
        r = swapper

        val t1 = MutableBigInteger(q)
        val t0 = MutableBigInteger(1)
        var temp = MutableBigInteger()

        while (!b.isOne) {
            r = a.divide(b, q)

            if (r.intLen == 0) throw ArithmeticException("BigInteger not invertible.")

            swapper = r
            a = swapper

            if (q.intLen == 1) t1.mul(q.value[q.offset], temp)
            else q.multiply(t1, temp)
            swapper = q
            q = temp
            temp = swapper
            t0.add(q)

            if (a.isOne) return t0

            r = b.divide(a, q)

            if (r.intLen == 0) throw ArithmeticException("BigInteger not invertible.")

            swapper = b
            b = r

            if (q.intLen == 1) t0.mul(q.value[q.offset], temp)
            else q.multiply(t0, temp)
            swapper = q
            q = temp
            temp = swapper

            t1.add(q)
        }
        mod.subtract(t1)
        return mod
    }

    companion object {
        // Constants
        /**
         * MutableBigInteger with one element value array with the value 1. Used by
         * BigDecimal divideAndRound to increment the quotient. Use this constant
         * only when the method is not going to modify this object.
         */
        val ONE: MutableBigInteger = MutableBigInteger(1)

        /**
         * Calculate GCD of a and b interpreted as unsigned integers.
         */
        fun binaryGcd(a: Int, b: Int): Int {
            var a = a
            var b = b
            if (b == 0) return a
            if (a == 0) return b

            // Right shift a & b till their last bits equal to 1.
            val aZeros: Int = a.countTrailingZeroBits() // Integer.numberOfTrailingZeros(a)
            val bZeros: Int = b.countTrailingZeroBits() //Integer.numberOfTrailingZeros(b)
            a = a ushr aZeros
            b = b ushr bZeros

            val t = (if (aZeros < bZeros) aZeros else bZeros)

            while (a != b) {
                if ((a + -0x80000000) > (b + -0x80000000)) {  // a > b as unsigned
                    a -= b
                    a = a ushr a.countTrailingZeroBits() //Integer.numberOfTrailingZeros(a)
                } else {
                    b -= a
                    b = b ushr b.countTrailingZeroBits() //Integer.numberOfTrailingZeros(b)
                }
            }
            return a shl t
        }

        /*
     * Returns the multiplicative inverse of val mod 2^32.  Assumes val is odd.
     */
        fun inverseMod32(`val`: Int): Int {
            // Newton's iteration!
            var t = `val`
            t *= 2 - `val` * t
            t *= 2 - `val` * t
            t *= 2 - `val` * t
            t *= 2 - `val` * t
            return t
        }

        /*
     * Calculate the multiplicative inverse of 2^k mod mod, where mod is odd.
     */
        fun modInverseBP2(mod: MutableBigInteger, k: Int): MutableBigInteger {
            // Copy the mod to protect original
            return fixup(MutableBigInteger(1), MutableBigInteger(mod), k)
        }

        /*
     * The Fixup Algorithm
     * Calculates X such that X = C * 2^(-k) (mod P)
     * Assumes C<P and P is odd.
     */
        fun fixup(
            c: MutableBigInteger, p: MutableBigInteger,
            k: Int
        ): MutableBigInteger {
            val temp = MutableBigInteger()
            // Set r to the multiplicative inverse of p mod 2^32
            val r = -inverseMod32(p.value[p.offset + p.intLen - 1])

            var i = 0
            val numWords = k shr 5
            while (i < numWords) {
                // V = R * c (mod 2^j)
                val v = r * c.value[c.offset + c.intLen - 1]
                // c = c + (v * p)
                p.mul(v, temp)
                c.add(temp)
                // c = c / 2^j
                c.intLen--
                i++
            }
            val numBits = k and 0x1f
            if (numBits != 0) {
                // V = R * c (mod 2^j)
                var v = r * c.value[c.offset + c.intLen - 1]
                v = v and ((1 shl numBits) - 1)
                // c = c + (v * p)
                p.mul(v, temp)
                c.add(temp)
                // c = c / 2^j
                c.rightShift(numBits)
            }

            // In theory, c may be greater than p at this point (Very rare!)
            while (c.compare(p) >= 0) c.subtract(p)

            return c
        }
    }
}
