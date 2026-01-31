package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Represents a speed value, measured in meters per second (mps), along with an optional accuracy.
 *
 * @property mps The speed in meters per second. Must be a non-negative value.
 * @property accuracy An optional estimate of the accuracy of the speed, as a fraction of the measured speed.
 *                   If provided, it represents the possible error range of the `mps` value.
 *                   A null value indicates that no accuracy information is available. Must be non negative if provided.
 * @throws IllegalArgumentException if `mps` is negative or if `accuracy` is negative when provided.
 */
@Serializable
data class Speed(
    @SerialName("mps")
    val mps: Double,
    @SerialName("accuracy")
    val accuracy: Float? = null) {
    init {
        require(mps >= 0.0) { "Speed in mps cannot be negative." }
        accuracy?.let { require(it >= 0.0f) { "Accuracy cannot be negative." } }
    }

    /**
     * Adds another Speed to this Speed.
     *
     * The accuracy of the resulting `Speed` is calculated by adding the absolute uncertainties of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Speed to add.
     * @return A new Speed representing the sum.
     */
    operator fun plus(other: Speed): Speed {
        val newMps = mps + other.mps
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            val absoluteError1 = accuracy * mps
            val absoluteError2 = other.accuracy * other.mps
            if (newMps > 0) {
                ((absoluteError1 + absoluteError2) / newMps).toFloat()
            } else {
                null
            }
        } else {
            null
        }
        return Speed(newMps, newAccuracy)
    }

    /**
     * Subtracts another Speed from this Speed.
     *
     * The accuracy of the resulting `Speed` is calculated by adding the absolute uncertainties of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null.
     *
     * @param other The Speed to subtract.
     * @return A new Speed representing the difference.
     */
    operator fun minus(other: Speed): Speed {
        val newMps = mps - other.mps
        val newAccuracy = if (accuracy != null && other.accuracy != null) {
            val absoluteError1 = accuracy * mps
            val absoluteError2 = other.accuracy * other.mps
            if (newMps != 0.0) {
                ((absoluteError1 + absoluteError2) / abs(newMps)).toFloat()
            } else {
                null
            }
        } else {
            null
        }
        return Speed(newMps, newAccuracy)
    }

    /**
     * Multiplies this Speed by a scalar value.
     *
     * @param other The scalar value to multiply by.
     * @return A new Speed representing the product.
     */
    operator fun times(other: Double): Speed {
        return Speed(mps * other, accuracy = accuracy)
    }

    /**
     * Divides this Speed by a scalar value.
     *
     * @param other The scalar value to divide by.
     * @return A new Speed representing the quotient.
     * @throws ArithmeticException if dividing by zero.
     */
    operator fun div(other: Double): Speed {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        return Speed(mps / other, accuracy = accuracy)
    }
}
