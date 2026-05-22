package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Polygon")
data class Polygon(
    val coordinates: List<List<List<Double>>>,
    override val bbox: List<Double>? = null
) : Geometry()
