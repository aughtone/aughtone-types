package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A collection of [Feature] objects.
 */
@Serializable
@SerialName("FeatureCollection")
data class FeatureCollection(
    val features: List<Feature>,
    override val bbox: List<Double>? = null
) : GeoJson()
