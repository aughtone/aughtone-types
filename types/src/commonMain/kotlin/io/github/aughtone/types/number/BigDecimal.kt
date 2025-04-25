package io.github.aughtone.types.number

/**
 * A high-precision decimal number implementation in pure Kotlin.
 * This class provides support for arbitrary precision decimal arithmetic.
 */
class BigDecimal(private val value: String) : Comparable<BigDecimal> {

    private val integerPart: String
    private val fractionalPart: String
    private val scale: Int
    private val isNegative: Boolean

    init {
        require(value.matches(Regex("^-?\\d*(\\.\\d+)?\$"))) { "Invalid BigDecimal format: $value" }

        val sanitizedValue = value.trim()
        isNegative = sanitizedValue.startsWith("-")
        val numberPart = if (isNegative) sanitizedValue.substring(1) else sanitizedValue

        val parts = numberPart.split(".")
        integerPart = parts[0].ifEmpty { "0" }
        fractionalPart = parts.getOrElse(1) { "" }.trimEnd('0')
        scale = fractionalPart.length
    }

    constructor(longValue: Long) : this(longValue.toString())

    constructor(doubleValue: Double) : this(doubleValue.toString())

    companion object {
        val ZERO = BigDecimal("0")
        val ONE = BigDecimal("1")
        val TEN = BigDecimal("10")
    }

    /**
     * Adds another BigDecimal to this BigDecimal.
     *
     * @param other The BigDecimal to add.
     * @return The result of the addition.
     */
    operator fun plus(other: BigDecimal): BigDecimal {
        return add(this, other)
    }

    /**
     * Subtracts another BigDecimal from this BigDecimal.
     *
     * @param other The BigDecimal to subtract.
     * @return The result of the subtraction.
     */
    operator fun minus(other: BigDecimal): BigDecimal {
        return subtract(this, other)
    }

    /**
     * Multiplies this BigDecimal by another BigDecimal.
     *
     * @param other The BigDecimal to multiply by.
     * @return The result of the multiplication.
     */
    operator fun times(other: BigDecimal): BigDecimal {
        return multiply(this, other)
    }

    /**
     * Divides this BigDecimal by another BigDecimal.
     *
     * @param other The BigDecimal to divide by.
     * @return The result of the division.
     * @throws ArithmeticException If dividing by zero.
     */
    operator fun div(other: BigDecimal): BigDecimal {
        return divide(this, other)
    }

    /**
     * Compares this BigDecimal with another BigDecimal.
     *
     * @param other The BigDecimal to compare to.
     * @return A negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    override fun compareTo(other: BigDecimal): Int {
        if (isNegative != other.isNegative) {
            return if (isNegative) -1 else 1
        }

        val compareInteger = integerPart.padStart(maxOf(integerPart.length, other.integerPart.length), '0')
            .compareTo(other.integerPart.padStart(maxOf(integerPart.length, other.integerPart.length), '0'))

        if (compareInteger != 0) {
            return if (isNegative) -compareInteger else compareInteger
        }

        val compareFractional = fractionalPart.padEnd(maxOf(fractionalPart.length, other.fractionalPart.length), '0')
            .compareTo(other.fractionalPart.padEnd(maxOf(fractionalPart.length, other.fractionalPart.length), '0'))

        return if (isNegative) -compareFractional else compareFractional
    }

    /**
     * Returns the string representation of the BigDecimal.
     *
     * @return The string representation of the BigDecimal.
     */
    override fun toString(): String {
        val sign = if (isNegative) "-" else ""
        return if (fractionalPart.isEmpty()) {
            sign + integerPart
        } else {
            sign + integerPart + "." + fractionalPart
        }
    }

    /**
     * Returns true if this BigDecimal is equal to the other object.
     *
     * @param other The object to compare to.
     * @return True if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigDecimal) return false

        return value == other.value
    }

    /**
     * Returns the hash code for this BigDecimal.
     *
     * @return The hash code for this BigDecimal.
     */
    override fun hashCode(): Int {
        return value.hashCode()
    }

    private fun add(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (a.isNegative && !b.isNegative) {
            return subtract(b, BigDecimal(a.value.substring(1)))
        }

        if (!a.isNegative && b.isNegative) {
            return subtract(a, BigDecimal(b.value.substring(1)))
        }
        val isResultNegative = a.isNegative && b.isNegative

        val maxScale = maxOf(a.scale, b.scale)
        val aFractional = a.fractionalPart.padEnd(maxScale, '0')
        val bFractional = b.fractionalPart.padEnd(maxScale, '0')

        val sumFractional = addStrings(aFractional, bFractional)
        val sumInteger = addStrings(a.integerPart, b.integerPart)

        val integerCarry = if(sumFractional.length > maxScale) "1" else ""
        val newIntegerPart = if (integerCarry.isNotEmpty()) {
            addStrings(sumInteger, integerCarry)
        } else {
            sumInteger
        }

        val newFractionalPart = if(sumFractional.length > maxScale) {
            sumFractional.substring(1)
        } else {
            sumFractional
        }

        val resultingString = buildString {
            if(isResultNegative) append("-")
            append(newIntegerPart)
            if(newFractionalPart.isNotEmpty()) {
                append(".")
                append(newFractionalPart)
            }
        }

        return BigDecimal(resultingString)
    }

    private fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (a.isNegative && !b.isNegative) {
            return add(a, BigDecimal("-" + b.value))
        }

        if (!a.isNegative && b.isNegative) {
            return add(a, BigDecimal(b.value.substring(1)))
        }

        val isResultNegative = a < b

        val maxScale = maxOf(a.scale, b.scale)
        val aFractional = a.fractionalPart.padEnd(maxScale, '0')
        val bFractional = b.fractionalPart.padEnd(maxScale, '0')
        val isAOriginalNegative = a.isNegative

        val (bigInt, smallInt) = if (a.compareTo(b) >= 0) a.integerPart to b.integerPart else b.integerPart to a.integerPart
        val (bigFrac, smallFrac) = if (a.compareTo(b) >= 0) aFractional to bFractional else bFractional to aFractional

        val diffFractional = subtractStrings(bigFrac, smallFrac)
        val diffInteger = subtractStrings(bigInt, smallInt)

        val resultingString = buildString {
            if (isResultNegative && !isAOriginalNegative) append("-") else if (isResultNegative && isAOriginalNegative) append("") else if(isAOriginalNegative)append("-")

            append(diffInteger)
            if (diffFractional.isNotEmpty() && diffFractional != "0") {
                append(".")
                append(diffFractional)
            }
        }

        return BigDecimal(resultingString)
    }

    private fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal {
        val isResultNegative = a.isNegative != b.isNegative

        val intPartA = a.integerPart + a.fractionalPart
        val intPartB = b.integerPart + b.fractionalPart

        val product = multiplyStrings(intPartA, intPartB)
        val totalScale = a.scale + b.scale

        val integerPartResult = if (product.length <= totalScale) {
            "0"
        } else {
            product.substring(0, product.length - totalScale)
        }
        val fractionalPartResult = if (totalScale == 0) "" else product.substring(product.length - totalScale).trimEnd('0')


        val result = buildString {
            if (isResultNegative && (integerPartResult != "0" || fractionalPartResult.isNotEmpty())) append("-")
            append(integerPartResult)
            if (fractionalPartResult.isNotEmpty()) {
                append(".")
                append(fractionalPartResult)
            }
        }
        return BigDecimal(result)
    }

    private fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        require(b != ZERO) { "Division by zero" }

        val isResultNegative = a.isNegative != b.isNegative
        val intPartA = a.integerPart + a.fractionalPart
        val intPartB = b.integerPart + b.fractionalPart

        val decimalPrecision = 100

        val scaledDividend = intPartA + "0".repeat(decimalPrecision)

        val quotient = divideStrings(scaledDividend, intPartB)

        val integerPartResult = quotient.substring(0, quotient.length - decimalPrecision)
        val fractionalPartResult = quotient.substring(quotient.length - decimalPrecision).trimEnd('0')

        val result = buildString {
            if (isResultNegative && (integerPartResult != "0" || fractionalPartResult.isNotEmpty())) append("-")
            append(integerPartResult)
            if (fractionalPartResult.isNotEmpty()) {
                append(".")
                append(fractionalPartResult)
            }
        }
        return BigDecimal(result)
    }

    private fun addStrings(a: String, b: String): String {
        val result = StringBuilder()
        var carry = 0
        var i = a.length - 1
        var j = b.length - 1

        while (i >= 0 || j >= 0 || carry > 0) {
            val digitA = if (i >= 0) a[i--] - '0' else 0
            val digitB = if (j >= 0) b[j--] - '0' else 0
            val sum = digitA + digitB + carry
            result.append(sum % 10)
            carry = sum / 10
        }

        return result.reverse().toString()
    }

    private fun subtractStrings(a: String, b: String): String {
        val result = StringBuilder()
        var borrow = 0
        var i = a.length - 1
        var j = b.length - 1

        while (i >= 0 || j >= 0) {
            var digitA = if (i >= 0) a[i--] - '0' else 0
            val digitB = if (j >= 0) b[j--] - '0' else 0

            digitA -= borrow
            if (digitA < digitB) {
                digitA += 10
                borrow = 1
            } else {
                borrow = 0
            }

            result.append(digitA - digitB)
        }

        return result.reverse().toString().trimStart('0').ifEmpty { "0" }
    }

    private fun multiplyStrings(a: String, b: String): String {
        val n = a.length
        val m = b.length
        val product = IntArray(n + m)

        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                val digitA = a[i] - '0'
                val digitB = b[j] - '0'
                product[i + j + 1] += digitA * digitB
            }
        }

        var carry = 0
        for (i in product.size - 1 downTo 0) {
            val temp = (product[i] + carry)
            product[i] = temp % 10
            carry = temp / 10
        }

        val sb = StringBuilder()
        var leadingZeros = true
        for (digit in product) {
            if (digit != 0) {
                leadingZeros = false
            }
            if (!leadingZeros) {
                sb.append(digit)
            }
        }

        return sb.toString().ifEmpty { "0" }
    }

    private fun divideStrings(dividend: String, divisor: String): String {
        val quotient = StringBuilder()
        var remainder = "0"

        for (digit in dividend) {
            remainder += digit
            if (remainder.length > 1 && remainder.startsWith('0')){
                remainder = remainder.substring(1)
            }
            val tempQuotient = divideSingleDigit(remainder, divisor)
            quotient.append(tempQuotient.first)
            remainder = tempQuotient.second
        }
        return quotient.toString().ifEmpty { "0" }
    }

    private fun divideSingleDigit(dividend: String, divisor: String): Pair<Int, String> {
        var tempQuotient = 0
        var tempRemainder = dividend

        while(tempRemainder.compareTo(divisor) >= 0) {
            tempQuotient++
            tempRemainder = subtractStrings(tempRemainder, divisor)
        }
        return tempQuotient to tempRemainder
    }
}
