package io.github.aughtone.types.quantitative

import kotlinx.serialization.Serializable

/**
 * Represents an altitude measurement.
 *
 * This class encapsulates an altitude value in meters and an optional accuracy
 * measurement. It's designed to be used when precise or estimated altitude information
 * is needed, such as in geographical or environmental applications.
 *
 * @property meters The altitude value in meters. This value represents the distance above
 *   a reference point (e.g., sea level). It should be a non-negative number.
 * @property accuracy The accuracy of the altitude measurement in meters. If provided,
 *   this value indicates the margin of error associated with the `meters` value.
 *   For example, an accuracy of 5.0 means the true altitude is likely within 5 meters
 *   above or below the given `meters` value. If not provided (null), the accuracy is
 *   unknown or not applicable.
 *
 * @constructor Creates an Altitude instance with the specified meters and optional accuracy.
 *
 * @sample
 *  // Example usage:
 *  val altitude1 = Altitude(150.5) // Altitude of 150.5 meters, no accuracy specified.
 *  val altitude2 = Altitude(200.0, 10.0) // Altitude of 200 meters, with an accuracy of 10 meters.
 *  println("Altitude 1: ${altitude1.meters} meters")
 *  println("Altitude 2: ${altitude2.meters} meters, Accuracy: ${altitude2.accuracy} meters")
 */
@Serializable
data class Altitude(
    val meters: Double,
    val accuracy: Float? = null,
)
