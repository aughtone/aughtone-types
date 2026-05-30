package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A collection of [GeoGeometry] objects.
 */
@Serializable
@SerialName("GeometryCollection")
data class GeometryCollection(
    val geometries: List<GeoGeometry>,
    override val bbox: List<Double>? = null
) : GeoGeometry()
