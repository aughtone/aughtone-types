package io.github.aughtone.types.quantitative

import kotlinx.serialization.Serializable

/**
 * Represents a set of geographical coordinates (WGS84).
 *
 * This data class encapsulates latitude, longitude, and an optional accuracy value.
 *
 * @property latitude The latitude coordinate, representing the north-south position.
 *                   Must be within the range of -90.0 to +90.0.
 * @property longitude The longitude coordinate, representing the east-west position.
 *                    Must be within the range of -180.0 to +180.0.
 * @property accuracy An optional value representing the estimated accuracy of the
 *                   coordinates, typically in meters. A smaller value indicates
 *                   higher accuracy. If null, accuracy is unknown or not applicable.
 */
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
) {
    /**
     *  Applies a delta change to the current coordinates.
     *
     *  This function allows you to shift the current latitude and longitude by the specified amounts.
     *  It creates and returns a new `Coordinates` object representing the adjusted position.
     *
     *  @param deltaLatitude The change in latitude to apply. Positive values move north, negative values move south.
     *  @param deltaLongitude The change in longitude to apply. Positive values move east, negative values move west.
     *  @return A new `Coordinates` object representing the position after applying the delta changes.
     *
     *  Example:
     *  ```kotlin
     *  val originalCoordinates = Coordinates(40.7128, -74.0060) // New York City
     *  val movedCoordinates = originalCoordinates(0.1, 0.2) // Move slightly north-east
     *  println(movedCoordinates) // Output: Coordinates(latitude=40.8128, longitude=-73.806)
     *  ```
     */
    operator fun invoke(deltaLatitude: Double, deltaLongitude: Double): Coordinates {
        return Coordinates(latitude + deltaLatitude, longitude + deltaLongitude)
    }


    /**
     * Adds the latitude and longitude of another [Coordinates] object to this [Coordinates] object,
     * returning a new [Coordinates] object with the combined values.
     *
     * @param other The [Coordinates] object to add to this one.
     * @return A new [Coordinates] object representing the sum of this and the other [Coordinates].
     */
    operator fun plus(other: Coordinates): Coordinates {
        return Coordinates(latitude + other.latitude, longitude + other.longitude)
    }

    /**
     * Subtracts another [Coordinates] object from this one.
     *
     * This function performs vector subtraction on the latitude and longitude components
     * of the two [Coordinates] objects. The result is a new [Coordinates] object where
     * the latitude and longitude are the differences between the corresponding components
     * of the original and the other [Coordinates] object.
     *
     * @param other The [Coordinates] object to subtract from this one.
     * @return A new [Coordinates] object representing the difference between the two.
     *
     * @sample
     * ```kotlin
     * val coord1 = Coordinates(40.7128, -74.0060) // New York City
     * val coord2 = Coordinates(34.0522, -118.2437) // Los Angeles
     * val difference = coord1 - coord2
     * println(difference) // Output will be Coordinates(latitude=6.6606, longitude=44.2377)
     * ```
     */
    operator fun minus(other: Coordinates): Coordinates {
        return Coordinates(latitude - other.latitude, longitude - other.longitude)
    }
}
