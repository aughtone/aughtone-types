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
    val accuracy: Float? = null
) {
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
     * A `Speed` cannot be negative, so subtracting a larger speed is rejected rather than clamped:
     * `3mps - 5mps` throws. Silently returning zero would turn an arithmetic mistake into a
     * plausible-looking measurement that no later check can distinguish from a real one. Guard the
     * call, or compare first, if the operands may be in either order.
     *
     * The accuracy of the resulting `Speed` is calculated by adding the absolute uncertainties of the two operands.
     * If either operand has a null accuracy, the resulting accuracy will also be null. A result of
     * exactly zero carries a null accuracy, there being no measurement for a fraction to be of.
     *
     * @param other The Speed to subtract.
     * @return A new Speed representing the difference, which may be zero.
     * @throws IllegalArgumentException if [other] is faster than this speed.
     */
    operator fun minus(other: Speed): Speed {
        require(mps >= other.mps) {
            "Cannot subtract ${other.mps} mps from $mps mps: a Speed cannot be negative."
        }
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
     * A `Speed` cannot be negative, so a negative scalar is rejected rather than clamped to zero.
     * A scalar of zero is accepted and yields a speed of zero, which is a real speed rather than an
     * error.
     *
     * The accuracy is carried through unchanged, being a fraction of the measurement rather than an
     * absolute quantity, so scaling the speed scales the implied error with it.
     *
     * @param other The scalar value to multiply by. Must not be negative.
     * @return A new Speed representing the product.
     * @throws IllegalArgumentException if [other] is negative.
     */
    operator fun times(other: Double): Speed {
        require(other >= 0.0) { "Cannot multiply a Speed by $other: a Speed cannot be negative." }
        return Speed(mps * other, accuracy = accuracy)
    }

    /**
     * Divides this Speed by a scalar value.
     *
     * A `Speed` cannot be negative, so a negative scalar is rejected rather than clamped to zero.
     * Dividing by zero throws separately, as it does for any number.
     *
     * The accuracy is carried through unchanged, being a fraction of the measurement rather than an
     * absolute quantity, so scaling the speed scales the implied error with it.
     *
     * @param other The scalar value to divide by. Must be positive.
     * @return A new Speed representing the quotient.
     * @throws ArithmeticException if [other] is zero.
     * @throws IllegalArgumentException if [other] is negative.
     */
    operator fun div(other: Double): Speed {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        require(other > 0.0) { "Cannot divide a Speed by $other: a Speed cannot be negative." }
        return Speed(mps / other, accuracy = accuracy)
    }
}

/**
 * Converts this [Int] value to a [Speed] object, treating the value as meters per second.
 *
 * @param accuracy An optional estimate of the accuracy of the speed, as a fraction of the measured speed.
 * @return A [Speed] object with this value as mps.
 */
fun Int.toSpeed(accuracy: Float? = null) = Speed(mps = this.toDouble(), accuracy = accuracy)

/**
 * Converts this [Long] value to a [Speed] object, representing meters per second.
 *
 * @param accuracy An optional estimate of the accuracy of the speed, as a fraction of the measured speed.
 * @return A [Speed] instance with this value as the mps.
 */
fun Long.toSpeed(accuracy: Float? = null) = Speed(mps = this.toDouble(), accuracy = accuracy)

/**
 * Converts this [Double] value to a [Speed] instance.
 *
 * @param accuracy An optional estimate of the accuracy of the speed.
 * @return A [Speed] object with this value as meters per second.
 * @throws IllegalArgumentException if this value or [accuracy] is negative.
 */
fun Double.toSpeed(accuracy: Float? = null) = Speed(mps = this, accuracy = accuracy)
