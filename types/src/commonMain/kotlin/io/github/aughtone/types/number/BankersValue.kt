package io.github.aughtone.types.number

import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Represents monetary values in cents and uses Banker's Rounding when operations
 * result in non-integer values.
 */
class BankersValue(private var cents: Long) : Comparable<BankersValue> {

    /**
     * Adds another BankersInteger to this BankersInteger.
     *
     * @param other The BankersInteger to add.
     * @return A new BankersInteger representing the sum.
     */
    operator fun plus(other: BankersValue): BankersValue {
        return BankersValue(cents + other.cents)
    }

    /**
     * Subtracts another BankersInteger from this BankersInteger.
     *
     * @param other The BankersInteger to subtract.
     * @return A new BankersInteger representing the difference.
     */
    operator fun minus(other: BankersValue): BankersValue {
        return BankersValue(cents - other.cents)
    }

    /**
     * Multiplies this BankersInteger by another BankersInteger.
     *
     * @param other The BankersInteger to multiply by.
     * @return A new BankersInteger representing the product.
     */
    operator fun times(other: BankersValue): BankersValue {
        return BankersValue(cents * other.cents)
    }

    /**
     * Divides this BankersInteger by another BankersInteger, applying Banker's
     * Rounding to the result.
     *
     * @param other The BankersInteger to divide by.
     * @return A new BankersInteger representing the quotient.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: BankersValue): BankersValue {
        if (other.cents == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other.cents.toDouble()))
    }
    /**
     * Divides this BankersInteger by an Integer, applying Banker's
     * Rounding to the result.
     *
     * @param other The Integer to divide by.
     * @return A new BankersInteger representing the quotient.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Int): BankersValue {
        if (other == 0) throw ArithmeticException("Division by zero")
        return BankersValue(bankersRound(cents.toDouble() / other.toDouble()))
    }
    /**
     * Returns the remainder of the division of this BankersInteger by another BankersInteger, applying Banker's
     * Rounding to the result.
     *
     * @param other The BankersInteger to divide by.
     * @return A new BankersInteger representing the remainder.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun rem(other: BankersValue): BankersValue {
        if (other.cents == 0L) throw ArithmeticException("Division by zero")
        return BankersValue(cents % other.cents)
    }

    /**
     * Compares this BankersInteger with another BankersInteger.
     *
     * @param other The BankersInteger to compare to.
     * @return A negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    override fun compareTo(other: BankersValue): Int {
        return cents.compareTo(other.cents)
    }

    /**
     * Checks if this BankersInteger is equal to another object.
     *
     * @param other The object to compare to.
     * @return True if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BankersValue) return false

        return cents == other.cents
    }

    /**
     * Returns the hash code for this BankersInteger.
     *
     * @return The hash code.
     */
    override fun hashCode(): Int {
        return cents.hashCode()
    }

    /**
     * Returns a string representation of the BankersInteger.
     *
     * @return The string representation.
     */
    override fun toString(): String {
        return cents.toString()
    }
    /**
     * Returns the primitive value of the BankersInteger
     *
     * @return The string representation.
     */
    fun toLong(): Long {
        return cents
    }

    fun toDouble() :Double = cents.toDouble() / 100.0


    /**
     * Converts a monetary value in dollars (represented as a Double) to a
     * monetary value in cents (represented as a Long).
     *
     * This function correctly handles both positive and negative dollar amounts.
     *
     * @param dollars The monetary value in dollars.
     * @return The equivalent monetary value in cents.
     */
    // XXX This doesn't make sense here. Should probably be an extension od Double or a companion.
    fun dollarsToCents(dollars: Double): Long {
        return bankersRound(dollars * 100)
    }

    /**
     * Rounds a double value using Banker's Rounding.
     *
     * @param num The double value to round.
     * @return The rounded integer value.
     */
    private fun bankersRound(num: Double): Long {
        val rounded = num.roundToInt()
        if (num - rounded == 0.5) {
            return if (rounded % 2 == 0) rounded.toLong() else (rounded - 1).toLong()
        } else if (num - rounded == -0.5){
            return if (rounded % 2 == 0) rounded.toLong() else (rounded + 1).toLong()
        } else {
            return num.roundToInt().toLong()
        }
    }
    companion object{
        fun fromLong(value: Long): BankersValue{
            return BankersValue(value)
        }
        fun fromInt(value: Int): BankersValue{
            return BankersValue(value.toLong())
        }
    }
}

//fun main() {
//    val num1 = BankersValue(3)
//    val num2 = BankersValue(2)
//    val num3 = BankersValue(5)
//    val num4 = BankersValue(4)
//    val num5 = BankersValue(1)
//
//    println("Addition: ${num1 + num2}") // 5
//    println("Subtraction: ${num1 - num2}") // 1
//    println("Multiplication: ${num1 * num2}") // 6
//    println("Division: ${num3 / num2}") // 2 (5/2 = 2.5, rounds to 2)
//    println("Division: ${num4 / num2}") // 2 (4/2 = 2.0, rounds to 2)
//    println("Division: ${num3 / num4}") // 1 (5/4 = 1.25, rounds to 1)
//    println("Division: ${num5 / num2}") // 0 (1/2 = 0.5, rounds to 0)
//    println("Division: ${num2 / num5}") // 2 (2/1 = 2, rounds to 2)
//    println("Division: ${num3 / 4}") // 1 (5/4 = 1.25, rounds to 1)
//
//    val num6 = BankersValue(35)
//    val num7 = BankersValue(10)
//    println("Division: ${num6 / num7}") // 4 (35/10 = 3.5, rounds to 4)
//
//    val num8 = BankersValue(25)
//    val num9 = BankersValue(10)
//    println("Division: ${num8 / num9}") // 2 (25/10 = 2.5, rounds to 2)
//
//    val num10 = BankersValue.fromLong(2)
//    val num11 = BankersValue.fromInt(4)
//    println("Addition: ${num10 + num11}") // 6
//
//    val num12 = BankersValue(15)
//    println("Remainder: ${num12 % num7}") // 5 (15 % 10 = 5)
//    println("Remainder: ${num7 % num12}") // 10 (10 % 15 = 10)
//
//    println("Comparison: ${num12 > num7}") // true
//    println("Comparison: ${num7 > num12}") // false
//    println("Comparison: ${num12 == num7}") // false
//
//    val amountInCents = 1234L // Represents $12.34
//    val amountInDollars = centsToDollars(amountInCents)
//    println("$amountInCents cents is equal to $$amountInDollars dollars")
//
//    val amountInCents2 = 56789L
//    val amountInDollars2 = centsToDollars(amountInCents2)
//    println("$amountInCents2 cents is equal to $$amountInDollars2 dollars")
//
//    val negativeCents = -1234L // Represents -$12.34
//    val negativeDollars = centsToDollars(negativeCents)
//    println("Negative value: $negativeDollars")
//
//    val negativeCents2 = -56789L
//    val negativeDollars2 = centsToDollars(negativeCents2)
//    println("Negative value: $negativeDollars2")
//
//val amountInDollars1 = 12.34
//val amountInCents1 = dollarsToCents(amountInDollars1)
//println("$amountInDollars1 dollars is equal to $amountInCents1 cents") // Output: 12.34 dollars is equal to 1234 cents
//
//val amountInDollars2 = 567.89
//val amountInCents2 = dollarsToCents(amountInDollars2)
//println("$amountInDollars2 dollars is equal to $amountInCents2 cents") // Output: 567.89 dollars is equal to 56789 cents
//
//val negativeAmountInDollars = -12.34
//val negativeAmountInCents = dollarsToCents(negativeAmountInDollars)
//println("$negativeAmountInDollars dollars is equal to $negativeAmountInCents cents") // Output: -12.34 dollars is equal to -1234 cents
//
//val negativeAmountInDollars2 = -567.89
//val negativeAmountInCents2 = dollarsToCents(negativeAmountInDollars2)
//println("$negativeAmountInDollars2 dollars is equal to $negativeAmountInCents2 cents") // Output: -567.89 dollars is equal to -56789 cents
//
//val amountInDollars3 = 12.345
//val amountInCents3 = dollarsToCents(amountInDollars3)
//println("$amountInDollars3 dollars is equal to $amountInCents3 cents") // Output: 12.345 dollars is equal to 1235 cents
//
//val amountInDollars4 = 12.344
//val amountInCents4 = dollarsToCents(amountInDollars4)
//println("$amountInDollars4 dollars is equal to $amountInCents4 cents") // Output: 12.344 dollars is equal to 12
//}
