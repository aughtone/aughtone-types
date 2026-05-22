package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A collection of [Geometry] objects.
 */
@Serializable
@SerialName("GeometryCollection")
data class GeometryCollection(
    val geometries: List<Geometry>,
    override val bbox: List<Double>? = null
) : Geometry()
