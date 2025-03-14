package io.github.aughtone.types.geo

import io.github.aughtone.types.quantitative.Altitude
import io.github.aughtone.types.quantitative.Azimuth
import io.github.aughtone.types.quantitative.Coordinates
import io.github.aughtone.types.quantitative.Speed
import kotlinx.serialization.Serializable

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
@Serializable
data class Location(
    val coordinates: Coordinates,
    val azimuth: Azimuth? = null,
    val speed: Speed? = null,
    val altitude: Altitude? = null,
    val timestamp: Long = 0,
)
