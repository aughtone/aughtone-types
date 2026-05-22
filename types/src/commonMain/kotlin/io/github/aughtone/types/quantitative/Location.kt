package io.github.aughtone.types.quantitative

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Deprecated(
    "Use Point instead. The class name was causing confusion.",
    replaceWith = ReplaceWith("Point", "io.github.aughtone.types.quantitative.Point3d")
)
@Serializable
data class Location(
    @SerialName("coordinates")
    val coordinates: Coordinates,
    @SerialName("accuracy")
    val accuracy: Distance? = null,
    @SerialName("altitude")
    val altitude: Altitude? = null,
)
