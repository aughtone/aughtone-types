package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Represents a distance value in meters, optionally with an associated accuracy.
 *
 * @property meters The distance in meters. Must be non-negative.
 * @property accuracy The estimated accuracy of the distance measurement, as a fraction of the measured distance.
 *                   For example, an accuracy of 0.05 means the measurement is accurate to within 5%.
 *                   If null, the accuracy is unknown or not applicable.
 * @throws IllegalArgumentException if [meters] is negative.
 */
@Serializable
data class Distance(
    @SerialName("meters")
    val meters: Double,
    @SerialName("accuracy")
    val accuracy: Float? = null
) {

    init {
        require(meters >= 0.0) { "Distance in meters cannot be negative." }
    }

    /**
     * Adds another Distance to this Distance.
     *
     * The accuracy of the resulting `Distance` is calculated by adding the absolute uncertainties of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Distance to add.
     * @return A new Distance representing the sum.
     */
    operator fun plus(other: Distance): Distance {
        val newMeters = meters + other.meters
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            val absoluteError1 = accuracy * meters
            val absoluteError2 = other.accuracy * other.meters
            if (newMeters > 0) {
                ((absoluteError1 + absoluteError2) / newMeters).toFloat()
            } else {
                null
            }
        } else {
            null
        }
        return Distance(newMeters, newAccuracy)
    }

    /**
     * Subtracts another Distance from this Distance.
     *
     * Since a `Distance` cannot be negative, a difference below zero is floored at zero meters.
     *
     * The accuracy of the resulting `Distance` is calculated by adding the absolute uncertainties of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Distance to subtract.
     * @return A new Distance representing the difference, floored at zero.
     */
    operator fun minus(other: Distance): Distance {
        require(meters >= other.meters) {
            "Cannot subtract ${other.meters}m from ${meters}m: a Distance cannot be negative."
        }
        val newMeters = meters - other.meters
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            val absoluteError1 = accuracy * meters
            val absoluteError2 = other.accuracy * other.meters
            if (newMeters != 0.0) {
                ((absoluteError1 + absoluteError2) / abs(newMeters)).toFloat()
            } else {
                null
            }
        } else {
            null
        }
        return Distance(newMeters, newAccuracy)
    }

    /**
     * Divides this Distance by another, yielding the dimensionless ratio between them — how many
     * times [other] fits into this distance.
     *
     * The result is a [Double] rather than a [Distance], because metres divided by metres has no
     * unit. Returning a `Distance` here would label a bare number as a length.
     *
     * @param other The Distance to divide by.
     * @return The ratio of the two distances.
     * @throws ArithmeticException if [other] is zero.
     */
    operator fun div(other: Distance): Double {
        if (other.meters == 0.0) throw ArithmeticException("Division by zero")
        return meters / other.meters
    }

    /**
     * Divides this Distance by an Integer.
     *
     * Since a `Distance` cannot be negative, a quotient below zero (a negative divisor)
     * is floored at zero meters.
     *
     * The accuracy of the resulting `Distance` is the same as the original `Distance`,
     * as the integer is assumed to be an exact value with no uncertainty.
     *
     * @param other The Integer to divide by.
     * @return A new Distance representing the quotient, floored at zero.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Int): Distance {
        if (other == 0) throw ArithmeticException("Division by zero")
        require(other > 0) { "Cannot divide a Distance by $other: a Distance cannot be negative." }
        return Distance(meters / other.toDouble(), accuracy = accuracy)
    }

    /**
     * Returns the remainder of the division of this Distance by another Distance.
     *
     * The accuracy of a remainder operation is not well-defined, so the resulting accuracy is always `null`.
     *
     * @param other The Distance to divide by.
     * @return A new Distance representing the remainder.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun rem(other: Distance): Distance {
        if (other.meters == 0.0) throw ArithmeticException("Division by zero")
        return Distance(meters % other.meters, accuracy = null)
    }
}

/**
 * Converts this [Int] to a [Distance] object, treating the value as meters.
 *
 * @param accuracy The optional estimated accuracy of the distance measurement.
 * @return A [Distance] instance with this value as the number of meters.
 */
fun Int.toDistance(accuracy: Float? = null) =
    Distance(meters = this.toDouble(), accuracy = accuracy)

/**
 * Converts this [Long] value representing meters into a [Distance] object.
 *
 * @param accuracy The optional estimated accuracy of the distance measurement.
 * @return A new [Distance] instance.
 */
fun Long.toDistance(accuracy: Float? = null) =
    Distance(meters = this.toDouble(), accuracy = accuracy)

/**
 * Converts this [Double] value to a [Distance] instance.
 *
 * @param accuracy The estimated accuracy of the distance measurement, as a fraction of the measured distance.
 *                 If null, the accuracy is unknown.
 * @return A [Distance] object with this value as meters.
 */
fun Double.toDistance(accuracy: Float? = null) = Distance(meters = this, accuracy = accuracy)
