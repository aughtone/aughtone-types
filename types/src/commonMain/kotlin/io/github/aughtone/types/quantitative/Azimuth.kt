package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Represents an azimuth, a horizontal angle measured clockwise from a north base line.
 *
 * Azimuth is a fundamental concept in navigation, surveying, and astronomy.
 * It specifies the direction of a celestial object or a point on Earth relative to a reference direction, typically true north.
 * The value of azimuth is always within the range [0, 360) degrees. A value of 360.0 is
 * accepted at construction and normalized to 0.0, so `Azimuth(360.0) == Azimuth(0.0)`.
 *
 * @property degrees The value of the azimuth in degrees, where 0 <= degrees < 360.
 * @property accuracy An optional value representing the accuracy of the azimuth measurement in degrees.
 *                    A null value indicates that the accuracy is unknown or not applicable.
 *
 * @throws IllegalArgumentException if `degrees` is not within the range [0, 360] or if `accuracy` is negative.
 */
@ExposedCopyVisibility
@Serializable
data class Azimuth private constructor(
    @SerialName("degrees")
    val degrees: Double,
    @SerialName("accuracy")
    val accuracy: Float? = null
) {
    init {
        require(degrees in 0.0..360.0) { "Azimuth must be between 0 and 360 degrees." }
        accuracy?.let { require(it >= 0.0f) { "Accuracy cannot be negative." } }
    }

    companion object {
        /**
         * Creates an [Azimuth], normalizing a value of 360.0 degrees to 0.0.
         *
         * @param degrees The azimuth in degrees, in the range [0, 360].
         * @param accuracy An optional accuracy of the measurement in degrees.
         * @throws IllegalArgumentException if `degrees` is outside [0, 360] or `accuracy` is negative.
         */
        operator fun invoke(degrees: Double, accuracy: Float? = null): Azimuth =
            Azimuth(degrees = if (degrees == 360.0) 0.0 else degrees, accuracy = accuracy)

        /**
         * Normalizes an angle in degrees into the range [0, 360) using floor-mod semantics.
         */
        private fun normalize(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0
    }

    /**
     * Adds another Azimuth to this Azimuth.
     *
     * @param other The Azimuth to add.
     * @return A new Azimuth representing the sum, wrapped around 360 degrees.
     */
    operator fun plus(other: Azimuth): Azimuth {
        val newDegrees = normalize(degrees + other.degrees)
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
        val newDegrees = normalize(degrees - other.degrees)
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
     * Uses floor-mod semantics, so negative results wrap around:
     * `Azimuth(90.0) * -1.0` yields 270 degrees.
     *
     * @param other The scalar value to multiply by.
     * @return A new Azimuth representing the product, wrapped around 360 degrees.
     */
    operator fun times(other: Double): Azimuth {
        val newDegrees = normalize(degrees * other)
        return Azimuth(newDegrees, accuracy?.let { it * abs(other.toFloat()) })
    }

    /**
     * Divides this Azimuth by a scalar value.
     *
     * Uses floor-mod semantics, so negative results wrap around:
     * `Azimuth(90.0) / -1.0` yields 270 degrees.
     *
     * @param other The scalar value to divide by.
     * @return A new Azimuth representing the quotient, wrapped around 360 degrees.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Double): Azimuth {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        val newDegrees = normalize(degrees / other)
        return Azimuth(newDegrees, accuracy?.let { it / abs(other.toFloat()) })
    }
}

fun Int.toAzimuth(accuracy: Float? = null) =
    Azimuth(degrees = this.toDouble(), accuracy = accuracy)
fun Long.toAzimuth(accuracy: Float? = null) =
    Azimuth(degrees = this.toDouble(), accuracy = accuracy)
fun Double.toAzimuth(accuracy: Float? = null) = Azimuth(degrees = this, accuracy = accuracy)
