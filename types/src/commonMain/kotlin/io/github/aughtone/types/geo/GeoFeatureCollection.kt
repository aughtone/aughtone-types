package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A collection of [GeoFeature] objects.
 */
@Serializable
@SerialName("FeatureCollection")
data class GeoFeatureCollection(
    val features: List<GeoFeature>,
    override val bbox: List<Double>? = null
) : GeoJson()
