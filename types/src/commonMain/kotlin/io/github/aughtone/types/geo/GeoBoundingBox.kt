package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A GeoJSON compatible representation of a bounding box as defined in RFC 7946 Section 5.
 *
 * A bounding box (bbox) is an array of length 2*n where n is the number of dimensions.
 * It contains all axes of the most southwesterly point followed by all axes of the
 * more northeasterly point.
 *
 * This is **not** a [GeoGeometry]. RFC 7946 makes a bounding box a `bbox` *member* of a geometry or
 * feature — a flat array of numbers — not a geometry in its own right. It previously extended
 * [GeoGeometry], which meant it serialized as a geometry object that no conformant GeoJSON reader
 * accepts, and allowed a bounding box to be passed anywhere a geometry was expected.
 *
 * Use [toDoubleArray] to obtain the RFC form, and assign it to the `bbox` property of the geometry
 * or feature it bounds.
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
    @SerialName("west")
    val west: Double,
    @SerialName("south")
    val south: Double,
    @SerialName("east")
    val east: Double,
    @SerialName("north")
    val north: Double,
    @SerialName("minAltitude")
    val minAltitude: Double? = null,
    @SerialName("maxAltitude")
    val maxAltitude: Double? = null
) {
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

    /**
     * This bounding box in the RFC 7946 flat-array form, ready to assign to a geometry's or
     * feature's `bbox` member.
     */
    fun toBbox(): List<Double> = toDoubleArray().toList()
}
