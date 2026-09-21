package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MultiLineString")
data class GeoMultiLineString(
    val coordinates: List<List<List<Double>>>,
    override val bbox: List<Double>? = null
) : GeoGeometry() {
    init {
        coordinates.forEachIndexed { i, line -> GeoValidation.lineString(line, "MultiLineString line $i") }
    }
}
