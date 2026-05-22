package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MultiPolygon")
data class MultiPolygon(
    val coordinates: List<List<List<List<Double>>>>,
    override val bbox: List<Double>? = null
) : Geometry()
