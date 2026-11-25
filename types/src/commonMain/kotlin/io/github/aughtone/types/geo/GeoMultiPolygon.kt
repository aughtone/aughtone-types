package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MultiPolygon")
data class GeoMultiPolygon @Throws(IllegalArgumentException::class) constructor(
    @SerialName("coordinates")
    val coordinates: List<List<List<List<Double>>>>,
    @SerialName("bbox")
    override val bbox: List<Double>? = null
) : GeoGeometry() {
    init {
        coordinates.forEachIndexed { i, poly -> GeoValidation.polygon(poly, "MultiPolygon polygon $i") }
    }
}
