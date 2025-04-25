package io.github.aughtone.types.number

/**
 * A class representing immutable, arbitrary-precision integers.
 *
 * This class provides operations for arithmetic, bit manipulation,
 * modular arithmetic, and other standard integer operations.
 */
class BigInteger(private val digits: List<Int>, private val isNegative: Boolean = false) : Comparable<BigInteger> {

    init {
        require(digits.isNotEmpty()) { "Digits cannot be empty" }
        require(digits.all { it in 0..9 }) { "Digits must be between 0 and 9" }
    }

    constructor(value: String) : this(
        digits = value.filter { it.isDigit() }.map { it - '0' }.reversed(),
        isNegative = value.trimStart('-').startsWith("-")
    )
    constructor(value: Int): this(value.toString())
    companion object {
        val ZERO = BigInteger("0")
        val ONE = BigInteger("1")
        val TEN = BigInteger("10")
    }

    operator fun plus(other: BigInteger): BigInteger = add(this, other)

    operator fun minus(other: BigInteger): BigInteger = subtract(this, other)

    operator fun times(other: BigInteger): BigInteger = multiply(this, other)

    operator fun div(other: BigInteger): BigInteger = divide(this, other)

    operator fun rem(other: BigInteger): BigInteger = remainder(this, other)

    fun abs(): BigInteger = if (isNegative) BigInteger(digits, false) else this

    override fun compareTo(other: BigInteger): Int {
        if (isNegative && !other.isNegative) return -1
        if (!isNegative && other.isNegative) return 1
        if (isNegative && other.isNegative) return -compareMagnitude(other)
        return compareMagnitude(other)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigInteger) return false
        return isNegative == other.isNegative && digits == other.digits
    }

    override fun hashCode(): Int {
        var result = digits.hashCode()
        result = 31 * result + isNegative.hashCode()
        return result
    }

    override fun toString(): String {
        val stringValue = StringBuilder().apply {
            if (isNegative) append("-")
            digits.reversed().forEach { append(it) }
        }.toString()
        return if(stringValue.isEmpty()){
            "0"
        }else {
            stringValue
        }
    }
    private fun compareMagnitude(other: BigInteger): Int {
        if (digits.size != other.digits.size) {
            return digits.size - other.digits.size
        }

        for (i in digits.size - 1 downTo 0) {
            val digitComparison = digits[i].compareTo(other.digits[i])
            if (digitComparison != 0) {
                return digitComparison
            }
        }
        return 0
    }
    private fun add(a: BigInteger, b: BigInteger): BigInteger {
        if (a.isNegative && !b.isNegative) return subtract(b, BigInteger(a.digits, false))
        if (!a.isNegative && b.isNegative) return subtract(a, BigInteger(b.digits, false))

        val isResultNegative = a.isNegative && b.isNegative
        val maxDigits = maxOf(a.digits.size, b.digits.size)

        val resultDigits = mutableListOf<Int>()
        var carry = 0
        for (i in 0 until maxDigits) {
            val digitA = a.digits.getOrElse(i) { 0 }
            val digitB = b.digits.getOrElse(i) { 0 }
            val sum = digitA + digitB + carry
            resultDigits.add(sum % 10)
            carry = sum / 10
        }
        if (carry > 0) resultDigits.add(carry)
        return BigInteger(resultDigits, isResultNegative).removeLeadingZeros()
    }
    private fun subtract(a: BigInteger, b: BigInteger): BigInteger {
        if (a.isNegative && !b.isNegative) return add(a, BigInteger(b.digits, true))
        if (!a.isNegative && b.isNegative) return add(a, BigInteger(b.digits, false))
        val isResultNegative = (a < b) xor a.isNegative

        val resultDigits = mutableListOf<Int>()
        var borrow = 0
        val (greater, lesser) = if (a >= b) a to b else b to a

        for (i in 0 until greater.digits.size) {
            var digitGreater = greater.digits.getOrElse(i) { 0 }
            val digitLesser = lesser.digits.getOrElse(i) { 0 }

            digitGreater -= borrow
            if (digitGreater < digitLesser) {
                digitGreater += 10
                borrow = 1
            } else {
                borrow = 0
            }
            resultDigits.add(digitGreater - digitLesser)
        }
        return BigInteger(resultDigits, isResultNegative).removeLeadingZeros()
    }
    private fun multiply(a: BigInteger, b: BigInteger): BigInteger {
        val isResultNegative = a.isNegative != b.isNegative

        val productSize = a.digits.size + b.digits.size
        val product = IntArray(productSize) { 0 }
        for (i in a.digits.indices) {
            for (j in b.digits.indices) {
                product[i + j] += a.digits[i] * b.digits[j]
            }
        }
        for (i in product.indices) {
            val digit = product[i] % 10
            val carry = product[i] / 10
            if (carry > 0 && i + 1 < product.size) {
                product[i + 1] += carry
            }
            product[i] = digit
        }
        val resultDigits = product.toMutableList()
        return BigInteger(resultDigits, isResultNegative).removeLeadingZeros()
    }
    private fun divide(a: BigInteger, b: BigInteger): BigInteger {
        if (b == ZERO) throw ArithmeticException("Division by zero")
        if (a < b) return ZERO

        val isResultNegative = a.isNegative != b.isNegative
        val aAbs = a.abs()
        val bAbs = b.abs()

        val quotient = mutableListOf<Int>()
        var remainder = ZERO

        for (digit in aAbs.digits.reversed()) {
            remainder = BigInteger((listOf(digit) + remainder.digits), false)
            val tempQuotient = divideSingleDigit(remainder, bAbs)
            quotient.add(0, tempQuotient.first)
            remainder = tempQuotient.second
        }

        return BigInteger(quotient.reversed(), isResultNegative).removeLeadingZeros()
    }
    private fun divideSingleDigit(dividend: BigInteger, divisor: BigInteger): Pair<Int, BigInteger> {
        var tempQuotient = 0
        var tempRemainder = dividend

        while(tempRemainder >= divisor) {
            tempQuotient++
            tempRemainder = tempRemainder - divisor
        }
        return tempQuotient to tempRemainder
    }
    private fun remainder(a: BigInteger, b: BigInteger): BigInteger {
        if (b == ZERO) throw ArithmeticException("Division by zero")

        val isResultNegative = a.isNegative && a > b
        var tempRemainder = ZERO

        for (digit in a.abs().digits.reversed()) {
            tempRemainder = BigInteger((listOf(digit) + tempRemainder.digits), false)
            val tempQuotient = divideSingleDigit(tempRemainder, b.abs())
            tempRemainder = tempQuotient.second
        }

        return if(isResultNegative) {
            BigInteger(tempRemainder.digits, isResultNegative).removeLeadingZeros()
        } else {
            tempRemainder
        }
    }
    private fun removeLeadingZeros(): BigInteger {
        val trimmedDigits = digits.dropLastWhile { it == 0 }
        return if (trimmedDigits.isEmpty()) ZERO else BigInteger(trimmedDigits, isNegative)
    }
}
//fun main() {
//    val num1 = BigInteger("12345678901234567890")
//    val num2 = BigInteger("98765432109876543210")
//    val num3 = BigInteger("1")
//    val num4 = BigInteger("-100")
//    val num5 = BigInteger("50")
//
//    println("Addition:")
//    println("$num1 + $num2 = ${num1 + num2}")
//    println("$num1 + $num3 = ${num1 + num3}")
//    println("$num3 + $num4 = ${num3 + num4}")
//
//    println("Subtraction:")
//    println("$num2 - $num1 = ${num2 - num1}")
//    println("$num1 - $num2 = ${num1 - num2}")
//    println("$num4 - $num3 = ${num4 - num3}")
//
//    println("Multiplication:")
//    println("$num1 * $num2 = ${num1 * num2}")
//    println("$num3 * $num4 = ${num3 * num4}")
//    println("$num1 * $num3 = ${num1 * num3}")
//    println("$num3 * $num3 = ${num3 * num3}")
//
//    println("Division:")
//    println("$num2 / $num1 = ${num2 / num1}")
//    println("$num1 / $num2 = ${num1 / num2}")
//    println("$num4 / $num5 = ${num4 / num5}")
//    println("$num5 / $num4 = ${num5 / num4}")
//    println("$num2 / $num3 = ${num2 / num3}")
//
//    println("Remainder:")
//    println("$num2 % $num1 = ${num2 % num1}")
//    println("$num1 % $num2 = ${num1 % num2}")
//    println("$num4 % $num5 = ${num4 % num5}")
//    println("$num5 % $num4 = ${num5 % num4}")
//    println("$num2 % $num3 = ${num2 % num3}")
//    println("Comparison:")
//    println("$num1 > $num2: ${num1 > num2}")
//    println("$num1 < $num2: ${num1 < num2}")
//    println("$num1 == $num1: ${num1 == num1}")
//    println("$num4 > $num3: ${num4 > num3}")
//    println("$num3 < $num4: ${num3 < num4}")
//    println("Abs")
//    println("$num4 abs: ${num4.abs()}")
//
//    val num6 = BigInteger("-1000")
//    println("$num6 / $num5 = ${num6 / num5}")
//}
