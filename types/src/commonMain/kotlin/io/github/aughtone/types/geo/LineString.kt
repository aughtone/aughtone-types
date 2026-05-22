package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LineString")
data class LineString(
    val coordinates: List<List<Double>>,
    override val bbox: List<Double>? = null
) : Geometry()
