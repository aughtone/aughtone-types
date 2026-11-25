package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MultiPoint")
data class GeoMultiPoint @Throws(IllegalArgumentException::class) constructor(
    val coordinates: List<List<Double>>,
    override val bbox: List<Double>? = null
) : GeoGeometry() {
    init { GeoValidation.positions(coordinates, "MultiPoint") }
}
