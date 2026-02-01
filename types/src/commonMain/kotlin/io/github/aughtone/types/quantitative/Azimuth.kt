package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Represents an azimuth, a horizontal angle measured clockwise from a north base line.
 *
 * Azimuth is a fundamental concept in navigation, surveying, and astronomy.
 * It specifies the direction of a celestial object or a point on Earth relative to a reference direction, typically true north.
 * The value of azimuth is always within the range of 0 to 360 degrees.
 *
 * @property degrees The value of the azimuth in degrees, where 0 <= degrees < 360.
 * @property accuracy An optional value representing the accuracy of the azimuth measurement in degrees.
 *                    A null value indicates that the accuracy is unknown or not applicable.
 *
 * @throws IllegalArgumentException if `degrees` is not within the range [0, 360) or if `accuracy` is negative.
 */
@Serializable
data class Azimuth(
    @SerialName("degrees")
    val degrees: Double,
    @SerialName("accuracy")
    val accuracy: Float? = null
) {
    init {
        require(degrees in 0.0..360.0) { "Azimuth must be between 0 and 360 degrees." }
        accuracy?.let { require(it >= 0.0f) { "Accuracy cannot be negative." } }
    }

    /**
     * Adds another Azimuth to this Azimuth.
     *
     * @param other The Azimuth to add.
     * @return A new Azimuth representing the sum, wrapped around 360 degrees.
     */
    operator fun plus(other: Azimuth): Azimuth {
        val newDegrees = (degrees + other.degrees) % 360.0
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            accuracy + other.accuracy
        } else {
            null
        }
        return Azimuth(newDegrees, newAccuracy)
    }

    /**
     * Subtracts another Azimuth from this Azimuth.
     *
     * @param other The Azimuth to subtract.
     * @return A new Azimuth representing the difference, wrapped around 360 degrees.
     */
    operator fun minus(other: Azimuth): Azimuth {
        val newDegrees = (degrees - other.degrees + 360.0) % 360.0
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            accuracy + other.accuracy
        } else {
            null
        }
        return Azimuth(newDegrees, newAccuracy)
    }

    /**
     * Multiplies this Azimuth by a scalar value.
     *
     * @param other The scalar value to multiply by.
     * @return A new Azimuth representing the product, wrapped around 360 degrees.
     */
    operator fun times(other: Double): Azimuth {
        val newDegrees = (degrees * other) % 360.0
        return Azimuth(newDegrees, accuracy?.let { it * abs(other.toFloat()) })
    }

    /**
     * Divides this Azimuth by a scalar value.
     *
     * @param other The scalar value to divide by.
     * @return A new Azimuth representing the quotient, wrapped around 360 degrees.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Double): Azimuth {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        val newDegrees = (degrees / other) % 360.0
        return Azimuth(newDegrees, accuracy?.let { it / abs(other.toFloat()) })
    }
}
