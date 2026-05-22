package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a GeoJSON Point geometry as defined in RFC 7946.
 *
 * A Point is a specific [Geometry] type representing a single position in space.
 * It consists of at least two elements (longitude and latitude) and may contain an
 * optional third element (altitude).
 *
 * According to RFC 7946:
 * - The first element is the longitude (decimal degrees).
 * - The second element is the latitude (decimal degrees).
 * - The third element is the altitude or elevation (meters above/below the WGS 84 ellipsoid).
 *
 * @property coordinates A list of [Double] values representing the position.
 *                       Index 0 is longitude, index 1 is latitude, and optional index 2 is altitude.
 * @property bbox An optional bounding box for the point as per RFC 7946 Section 5.
 *                       The array length is 2*n where n is the number of dimensions.
 *                       - 2D: [minX, minY, maxX, maxY]
 *                       - 3D: [minX, minY, minZ, maxX, maxY, maxZ]
 */
@Serializable
@SerialName("Point")
data class Point(
    val coordinates: List<Double>,
    override val bbox: List<Double>? = null
) : Geometry() {
    /**
     * Secondary constructor for creating a 2D Point.
     *
     * @param longitude The longitude in decimal degrees.
     * @param latitude The latitude in decimal degrees.
     */
    constructor(longitude: Double, latitude: Double) : this(listOf(longitude, latitude))

    /**
     * Secondary constructor for creating a 3D Point with altitude.
     *
     * @param longitude The longitude in decimal degrees.
     * @param latitude The latitude in decimal degrees.
     * @param altitude The altitude in meters above or below the WGS 84 reference ellipsoid.
     */
    constructor(longitude: Double, latitude: Double, altitude: Double) : this(listOf(longitude, latitude, altitude))

    /**
     * Convenience property to access the longitude of the point.
     */
    val longitude: Double get() = coordinates[0]

    /**
     * Convenience property to access the latitude of the point.
     */
    val latitude: Double get() = coordinates[1]

    /**
     * Convenience property to access the optional altitude of the point.
     * Returns null if the point only contains 2D coordinates.
     */
    val altitude: Double? get() = coordinates.getOrNull(2)

    init {
        require(coordinates.size >= 2) { "Point must have at least two coordinates (longitude, latitude)" }
    }
}
