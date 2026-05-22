package io.github.aughtone.types.geo

import kotlinx.serialization.Serializable

/**
 * Represents a GeoJSON object, which can be a Geometry, a Feature, or a FeatureCollection.
 * This sealed class is the base for all GeoJSON types, enabling polymorphic serialization.
 *
 * The `type` property common to all GeoJSON objects is handled automatically by `kotlinx.serialization`
 * as a class discriminator and does not need to be explicitly declared in the data classes.
 */
@Serializable
sealed class GeoJson {
    /**
     * A bounding box array that represents the object's geometry.
     * The axes order follows the axes order of geometries. See RFC 7946 Section 5.
     * This property is optional and may be null.
     */
    abstract val bbox: List<Double>?
}
