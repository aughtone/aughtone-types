package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Represents an altitude measurement.
 *
 * This class encapsulates an altitude value in meters and an optional accuracy
 * measurement. It's designed to be used when precise or estimated altitude information
 * is needed, such as in geographical or environmental applications.
 *
 * @property meters The altitude value in meters. This value represents the distance above
 *   a reference point (e.g., sea level).
 * @property accuracy The accuracy of the altitude measurement in meters. If provided,
 *   this value indicates the margin of error associated with the `meters` value.
 *   For example, an accuracy of 5.0 means the true altitude is likely within 5 meters
 *   above or below the given `meters` value. If not provided (null), the accuracy is
 *   unknown or not applicable.
 *
 * @constructor Creates an Altitude instance with the specified meters and optional accuracy.
 */
@Serializable
data class Altitude(
    @SerialName("meters")
    val meters: Double,
    @SerialName("accuracy")
    val accuracy: Float? = null,
) {
    init {
        accuracy?.let { require(it >= 0.0f) { "Accuracy cannot be negative." } }
    }

    /**
     * Adds another Altitude to this Altitude.
     *
     * @param other The Altitude to add.
     * @return A new Altitude representing the sum.
     */
    operator fun plus(other: Altitude): Altitude {
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            accuracy + other.accuracy
        } else {
            null
        }
        return Altitude(meters + other.meters, newAccuracy)
    }

    /**
     * Subtracts another Altitude from this Altitude.
     *
     * @param other The Altitude to subtract.
     * @return A new Altitude representing the difference.
     */
    operator fun minus(other: Altitude): Altitude {
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            accuracy + other.accuracy
        } else {
            null
        }
        return Altitude(meters - other.meters, newAccuracy)
    }

    /**
     * Multiplies this Altitude by a scalar value.
     *
     * @param other The scalar value to multiply by.
     * @return A new Altitude representing the product.
     */
    operator fun times(other: Double): Altitude {
        return Altitude(meters * other, accuracy?.let { it * abs(other.toFloat()) })
    }

    /**
     * Divides this Altitude by a scalar value.
     *
     * @param other The scalar value to divide by.
     * @return A new Altitude representing the quotient.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Double): Altitude {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        return Altitude(meters / other, accuracy?.let { it / abs(other.toFloat()) })
    }
}
