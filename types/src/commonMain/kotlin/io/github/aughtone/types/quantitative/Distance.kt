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
    val accuracy: Float? = null) {

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
     * The accuracy of the resulting `Distance` is calculated by adding the absolute uncertainties of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Distance to subtract.
     * @return A new Distance representing the difference.
     */
    operator fun minus(other: Distance): Distance {
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
     * Multiplies this Distance by another Distance.
     *
     * The accuracy of the resulting `Distance` is calculated by adding the relative accuracies of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Distance to multiply by.
     * @return A new Distance representing the product.
     */
    operator fun times(other: Distance): Distance {
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            accuracy + other.accuracy
        } else {
            null
        }
        return Distance(meters * other.meters, accuracy = newAccuracy)
    }

    /**
     * Divides this Distance by another Distance.
     *
     * The accuracy of the resulting `Distance` is calculated by adding the relative accuracies of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Distance to divide by.
     * @return A new Distance representing the quotient.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Distance): Distance {
        if (other.meters == 0.0) throw ArithmeticException("Division by zero")
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            accuracy + other.accuracy
        } else {
            null
        }
        return Distance(meters / other.meters, accuracy = newAccuracy)
    }

    /**
     * Divides this Distance by an Integer.
     *
     * The accuracy of the resulting `Distance` is the same as the original `Distance`,
     * as the integer is assumed to be an exact value with no uncertainty.
     *
     * @param other The Integer to divide by.
     * @return A new Distance representing the quotient.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Int): Distance {
        if (other == 0) throw ArithmeticException("Division by zero")
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
