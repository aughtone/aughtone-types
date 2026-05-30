package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a GeoJSON Feature object as defined in RFC 7946.
 *
 * A Feature object represents a spatially bounded entity. It contains a [GeoGeometry]
 * object and additional properties.
 *
 * According to RFC 7946:
 * - A Feature object has a "type" member with the value "Feature".
 * - It must have a "geometry" member, which can be a [GeoGeometry] object or null.
 * - It must have a "properties" member, which can be any JSON object or null.
 *
 * @property geometry The [GeoGeometry] object associated with the feature, or null if the feature
 *                     does not have a specific geometry.
 * @property properties A map of additional properties associated with the feature.
 *                       Keys are strings, and values are currently constrained to strings in this implementation.
 * @property id An optional identifier for the feature. If present, it should be a string or a number.
 * @property bbox An optional bounding box for the feature as per RFC 7946 Section 5.
 *                 The array length is 2*n where n is the number of dimensions in the contained geometry.
 */
@Serializable
@SerialName("Feature")
data class GeoFeature(
    val geometry: GeoGeometry?,
    val properties: Map<String, String>? = null,
    val id: String? = null,
    override val bbox: List<Double>? = null
) : GeoJson()
