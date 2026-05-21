package io.github.aughtone.types.geo

import io.github.aughtone.types.quantitative.Altitude
import io.github.aughtone.types.quantitative.Azimuth
import io.github.aughtone.types.quantitative.Coordinates
import io.github.aughtone.types.quantitative.Speed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents geographical telemetry data, including positioning and optional motion metrics.
 *
 * @property coordinates [Coordinates] The geographical coordinates (latitude and longitude) of the telemetry point.
 * @property azimuth [Azimuth] The direction the object is facing, represented as an angle in degrees. Optional; if null, the direction is unknown or unavailable.
 * @property speed [Speed] The speed of the object at this point. Optional; if null, the speed is unknown or unavailable.
 * @property altitude [Altitude] The altitude of the object at this point. Optional; if null, the altitude is unknown or unavailable.
 * @property timestamp [Long] The timestamp of when this telemetry data was recorded, represented as milliseconds since the epoch. A zero effectively means the time is not available.
 */
@Serializable
data class Telemetry(
    @SerialName("coordinates")
    val coordinates: Coordinates,
    @SerialName("azimuth")
    val azimuth: Azimuth? = null,
    @SerialName("speed")
    val speed: Speed? = null,
    @SerialName("altitude")
    val altitude: Altitude? = null,
    @SerialName("timestamp")
    val timestamp: Long = 0,
)

/**
 * Represents a geographical location with optional orientation, speed, and altitude information.
 *
 * @property coordinates [Coordinates] The geographical
 * coordinates (latitude and longitude) of the location.
 * @property azimuth [Azimuth] The direction the object
 * is facing, represented as an angle in degrees.
 *                   Optional; if null, the direction is unknown or unavailable.
 * @property speed [Speed] The speed of the object at
 * this location.
 *                 Optional; if null, the speed is unknown or unavailable.
 * @property altitude [Altitude] The altitude of the
 * object at this location.
 *                  Optional; if null, the altitude is unknown or unavailable.
 * @property timestamp [Long] The timestamp of when this location data was recorded, represented
 * as milliseconds since the epoch. A zero effectively means the time is not available.
 */
@Deprecated(
    "Use Telemetry instead. The class name was consing confusion between a point and the rich telemetry it is.",
    replaceWith = ReplaceWith("Telemetry", "io.github.aughtone.types.geo.Telemetry")
)
@Serializable
data class Location(
    @SerialName("coordinates")
    val coordinates: Coordinates,
    @SerialName("azimuth")
    val azimuth: Azimuth? = null,
    @SerialName("speed")
    val speed: Speed? = null,
    @SerialName("altitude")
    val altitude: Altitude? = null,
    @SerialName("timestamp")
    val timestamp: Long = 0,
)
