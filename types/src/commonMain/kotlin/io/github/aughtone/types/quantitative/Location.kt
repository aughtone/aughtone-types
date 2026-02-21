package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a geographical location, defined by a `Coordinates` object and optional accuracy and altitude information.
 *
 * This data class is used to specify a precise location on the Earth's surface.
 *
 * @property coordinates The geographical coordinates of the location.
 * @property accuracy An optional `Distance` object representing the accuracy of the
 *                    coordinate measurement. A smaller value indicates a more precise
 *                    location. If null, the accuracy is unknown or not applicable.
 * @property altitude An optional `Altitude` object representing the altitude of the
 *                    coordinate. If null, the altitude is unknown or not applicable.
 *
 * @constructor Creates a new Location instance.
 */
@Serializable
data class Location(
    @SerialName("coordinates")
    val coordinates: Coordinates,
    @SerialName("accuracy")
    val accuracy: Distance? = null,
    @SerialName("altitude")
    val altitude: Altitude? = null,
)
