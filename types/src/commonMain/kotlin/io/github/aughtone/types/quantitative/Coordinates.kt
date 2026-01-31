package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Earth radius in meters.
 */
private const val EARTH_RADIUS = 6371e3

/**
 * Represents a geographical coordinate, defined by latitude and longitude.
 *
 * This data class is used to specify a precise location on the Earth's surface.
 *
 * @property latitude The latitude of the coordinate, in decimal degrees.
 *                    Positive values represent latitudes north of the equator, while
 *                    negative values represent latitudes south of the equator.
 *                    The value must be within the range of -90.0 to 90.0 degrees.
 * @property longitude The longitude of the coordinate, in decimal degrees.
 *                     Positive values represent longitudes east of the Prime Meridian,
 *                     while negative values represent longitudes west of the Prime Meridian.
 *                     The value must be within the range of -180.0 to 180.0 degrees.
 * @property accuracy The horizontal accuracy of the coordinate in meters.
 *
 * @constructor Creates a new Coordinates instance.
 * @throws IllegalArgumentException if the provided latitude or longitude values are outside
 *         their valid ranges.
 */
@Serializable
data class Coordinates(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("accuracy")
    val accuracy: Float? = null,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }
        accuracy?.let { require(it >= 0.0f) { "Accuracy cannot be negative." } }
    }

    /**
     * Calculates the distance between this and another `Coordinates` using the Haversine formula.
     *
     * @param other The other `Coordinates`.
     * @return The distance between the two coordinates as a `Distance` object.
     */
    operator fun minus(other: Coordinates): Distance {
        val lat1Rad = toRadians(this.latitude)
        val lon1Rad = toRadians(this.longitude)
        val lat2Rad = toRadians(other.latitude)
        val lon2Rad = toRadians(other.longitude)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2) + (cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2))
        val c = 2 * asin(sqrt(a))

        val distanceInMeters = EARTH_RADIUS * c

        val newAccuracy = if (this.accuracy != null && other.accuracy != null) {
            val absoluteAccuracySum = this.accuracy + other.accuracy
            if (distanceInMeters > 0) {
                (absoluteAccuracySum / distanceInMeters).toFloat()
            } else {
                null
            }
        } else {
            null
        }

        return Distance(distanceInMeters, accuracy = newAccuracy)
    }

    /**
     * Calculates a new `Coordinates` by moving a certain distance from a starting point in a given direction.
     *
     * @param distance The distance to move.
     * @param bearing The direction to move in.
     * @return The new `Coordinates`.
     */
    fun plus(distance: Distance, bearing: Azimuth): Coordinates {
        val lat1Rad = toRadians(this.latitude)
        val lon1Rad = toRadians(this.longitude)
        val bearingRad = toRadians(bearing.degrees)

        val lat2Rad = asin(sin(lat1Rad) * cos(distance.meters / EARTH_RADIUS) + cos(lat1Rad) * sin(distance.meters / EARTH_RADIUS) * cos(bearingRad))
        val lon2Rad = lon1Rad + atan2(sin(bearingRad) * sin(distance.meters / EARTH_RADIUS) * cos(lat1Rad), cos(distance.meters / EARTH_RADIUS) - sin(lat1Rad) * sin(lat2Rad))

        val newAccuracy = if (this.accuracy != null && distance.accuracy != null) {
            val distanceAbsoluteAccuracy = distance.meters * distance.accuracy
            (this.accuracy + distanceAbsoluteAccuracy).toFloat()
        } else {
            null
        }

        return Coordinates(
            latitude = toDegrees(lat2Rad),
            longitude = toDegrees(lon2Rad),
            accuracy = newAccuracy
        )
    }

    /**
     * Returns the antipodal point on the globe.
     *
     * @return The new `Coordinates`.
     */
    operator fun unaryMinus(): Coordinates {
        return Coordinates(
            latitude = -latitude,
            longitude = if (longitude <= 0) longitude + 180 else longitude - 180,
            accuracy = this.accuracy
        )
    }
}
