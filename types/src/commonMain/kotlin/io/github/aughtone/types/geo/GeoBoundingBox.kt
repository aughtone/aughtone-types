package io.github.aughtone.types.geo

import kotlinx.serialization.Serializable

/**
 * A GeoJSON compatible representation of a bounding box as defined in RFC 7946 Section 5.
 *
 * A bounding box (bbox) is an array of length 2*n where n is the number of dimensions.
 * It contains all axes of the most southwesterly point followed by all axes of the
 * more northeasterly point.
 *
 * @property west The westernmost longitude in decimal degrees.
 * @property south The southernmost latitude in decimal degrees.
 * @property east The easternmost longitude in decimal degrees.
 * @property north The northernmost latitude in decimal degrees.
 * @property minAltitude The minimum altitude in meters (optional).
 * @property maxAltitude The maximum altitude in meters (optional).
 */
@Serializable
data class GeoBoundingBox(
    val west: Double,
    val south: Double,
    val east: Double,
    val north: Double,
    val minAltitude: Double? = null,
    val maxAltitude: Double? = null
): GeoGeometry() {
    /**
     * Secondary constructor for creating a [GeoBoundingBox] from a list of coordinates.
     * The list must have either 4 elements [west, south, east, north] or 6 elements
     * [west, south, minAltitude, east, north, maxAltitude].
     *
     * @param bbox A list of coordinates.
     * @throws IllegalArgumentException if the list size is not 4 or 6.
     */
    constructor(bbox: List<Double>) : this(
        west = if (bbox.size == 4 || bbox.size == 6) bbox[0] else throw IllegalArgumentException("Bounding box must have either 4 or 6 coordinates (found ${bbox.size})"),
        south = bbox[1],
        east = if (bbox.size == 6) bbox[3] else bbox[2],
        north = if (bbox.size == 6) bbox[4] else bbox[3],
        minAltitude = if (bbox.size == 6) bbox[2] else null,
        maxAltitude = if (bbox.size == 6) bbox[5] else null
    )

    /**
     * Secondary constructor for creating a [GeoBoundingBox] from an array of coordinates.
     *
     * @param bbox An array of coordinates.
     */
    constructor(bbox: DoubleArray) : this(bbox.toList())

    /**
     * Converts to a double array matching standard GeoJSON bbox format:
     * [west, south, (minAltitude), east, north, (maxAltitude)]
     *
     * If only one of [minAltitude] or [maxAltitude] is present, the known value is used for
     * both bounds so that the altitude information is not lost.
     */
    fun toDoubleArray(): DoubleArray {
        val lowAltitude = minAltitude ?: maxAltitude
        val highAltitude = maxAltitude ?: minAltitude
        return if (lowAltitude != null && highAltitude != null) {
            doubleArrayOf(west, south, lowAltitude, east, north, highAltitude)
        } else {
            doubleArrayOf(west, south, east, north)
        }
    }

    override val bbox: List<Double>?
        get() = toDoubleArray().toList()
}
