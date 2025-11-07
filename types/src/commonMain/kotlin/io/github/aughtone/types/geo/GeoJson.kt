package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
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

/**
 * A sealed class representing any of the seven GeoJSON geometry types.
 */
@Serializable
sealed class Geometry : GeoJson()

@Serializable
@SerialName("Point")
data class Point(
    val coordinates: List<Double>,
    override val bbox: List<Double>? = null
) : Geometry() {
    constructor(longitude: Double, latitude: Double) : this(listOf(longitude, latitude))

    init {
        require(coordinates.size == 2) { "Point must have exactly two coordinates" }
    }
}

@Serializable
@SerialName("MultiPoint")
data class MultiPoint(
    val coordinates: List<List<Double>>,
    override val bbox: List<Double>? = null
) : Geometry()

@Serializable
@SerialName("LineString")
data class LineString(
    val coordinates: List<List<Double>>,
    override val bbox: List<Double>? = null
) : Geometry()

@Serializable
@SerialName("MultiLineString")
data class MultiLineString(
    val coordinates: List<List<List<Double>>>,
    override val bbox: List<Double>? = null
) : Geometry()

@Serializable
@SerialName("Polygon")
data class Polygon(
    val coordinates: List<List<List<Double>>>,
    override val bbox: List<Double>? = null
) : Geometry()

@Serializable
@SerialName("MultiPolygon")
data class MultiPolygon(
    val coordinates: List<List<List<List<Double>>>>,
    override val bbox: List<Double>? = null
) : Geometry()

/**
 * A collection of [Geometry] objects.
 */
@Serializable
@SerialName("GeometryCollection")
data class GeometryCollection(
    val geometries: List<Geometry>,
    override val bbox: List<Double>? = null
) : Geometry()

/**
 * A GeoJSON object that contains a [Geometry] object and additional properties.
 */
@Serializable
@SerialName("Feature")
data class Feature(
    val geometry: Geometry?,
    val properties: Map<String, String>? = null,
    val id: String? = null,
    override val bbox: List<Double>? = null
) : GeoJson()

/**
 * A collection of [Feature] objects.
 */
@Serializable
@SerialName("FeatureCollection")
data class FeatureCollection(
    val features: List<Feature>,
    override val bbox: List<Double>? = null
) : GeoJson()
