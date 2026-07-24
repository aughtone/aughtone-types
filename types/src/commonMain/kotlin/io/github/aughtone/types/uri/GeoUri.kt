package io.github.aughtone.types.uri

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents a Geographic Reference Identifier (GRI or GeoUri) as defined by [RFC 5870](https://datatracker.ietf.org/doc/html/rfc5870).
 *
 * A GRI is a way to specify a location on the Earth using latitude, longitude, and optionally, altitude,
 * coordinate reference system (CRS), and uncertainty.
 *
 * Coordinates are validated at construction: latitude must be within -90..90,
 * longitude within -180..180, altitude finite and uncertainty non-negative.
 *
 * @property latitude The latitude of the location in decimal degrees.
 * @property longitude The longitude of the location in decimal degrees.
 * @property altitude The altitude of the location in meters (optional).
 * @property crs The coordinate reference system (CRS) used for the location (optional, defaults to "wgs84").
 * @property uncertainty The uncertainty of the location in meters (optional).
 * @throws IllegalArgumentException if a coordinate is out of range or the uncertainty is negative.
 */
@Serializable
data class GeoUri(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("altitude")
    val altitude: Double? = null,
    @SerialName("crs")
    val crs: String? = "wgs84",
    @SerialName("uncertainty")
    val uncertainty: Int? = null,
) {
    init {
        require(latitude in -90.0..90.0) { "latitude must be within -90.0..90.0: $latitude" }
        require(longitude in -180.0..180.0) { "longitude must be within -180.0..180.0: $longitude" }
        require(altitude == null || altitude.isFinite()) { "altitude must be finite: $altitude" }
        require(uncertainty == null || uncertainty >= 0) { "uncertainty must be >= 0: $uncertainty" }
    }

    /**
     * The URI scheme for Geographic Reference Identifiers (GRI).
     *
     * This is always "geo" as defined by [RFC 5870](https://datatracker.ietf.org/doc/html/rfc5870).
     */
    val scheme: String = "geo"

    /**
     * Converts this GRI to a [Uri] object.
     *
     * The resulting URI will have:
     * - `scheme`: "geo"
     * - `authority`: empty (geo URIs have no authority component).
     * - `path`: latitude and longitude separated by a comma, optionally followed by the
     *   altitude prefixed by a comma, and the ";crs=" and ";u=" path parameters.
     * - `query`: empty.
     * - `fragment`: empty.
     *
     * Example paths:
     * - `37.786971,-122.399677;crs=wgs84;u=5`
     * - `37.786971,-122.399677,100`
     * - `37.786971,-122.399677`
     *
     * @return A [Uri] object representing this GRI.
     */
    fun toUri(): Uri = Uri(
        scheme = scheme,
        authority = "",
        path = descriptor(),
        query = "",
        fragment = ""
    )

    /**
     * Builds the scheme-specific part: `lat,lng[,alt][;crs=...][;u=...]`.
     */
    private fun descriptor(): String = buildString {
        append(formatCoordinate(latitude)).append(',').append(formatCoordinate(longitude))
        if (altitude != null) append(',').append(formatCoordinate(altitude))
        if (crs != null) append(";crs=").append(crs)
        if (uncertainty != null) append(";u=").append(uncertainty)
    }

    /**
     * Returns a string representation of the GRI in the format "geo:latitude,longitude;crs=crs;u=uncertainty".
     *
     * The output string follows the [rfc5870](https://datatracker.ietf.org/doc/html/rfc5870) specification,
     * including:
     *  - The scheme "geo".
     *  - The latitude and longitude separated by a comma.
     *  - The optional altitude prefixed with a comma.
     *  - The optional coordinate reference system (crs) prefixed by ";crs=".
     *  - The optional uncertainty prefixed by ";u=".
     *
     * Coordinates are always rendered as plain decimal numbers (never scientific
     * notation) with no trailing ".0", so the output is identical across
     * JVM, JS, Wasm and native targets.
     *
     * @return A string representation of the GRI.
     */
    override fun toString(): String = "$scheme:${descriptor()}"
}

/**
 * Formats a [Double] as a plain decimal string per the RFC 5870 `num` grammar:
 * no scientific notation and no trailing ".0", identical on all platforms.
 */
private fun formatCoordinate(value: Double): String {
    val repr = value.toString()
    val eIndex = repr.indexOfFirst { it == 'e' || it == 'E' }
    val plain = if (eIndex < 0) repr else {
        val exponent = repr.substring(eIndex + 1).toInt()
        var mantissa = repr.substring(0, eIndex)
        val negative = mantissa.startsWith("-")
        if (negative) mantissa = mantissa.substring(1)
        val dot = mantissa.indexOf('.')
        val digits: String
        val pointPosition: Int
        if (dot < 0) {
            digits = mantissa
            pointPosition = mantissa.length + exponent
        } else {
            digits = mantissa.removeRange(dot, dot + 1)
            pointPosition = dot + exponent
        }
        val expanded = when {
            pointPosition <= 0 -> "0." + "0".repeat(-pointPosition) + digits
            pointPosition >= digits.length -> digits + "0".repeat(pointPosition - digits.length)
            else -> digits.substring(0, pointPosition) + "." + digits.substring(pointPosition)
        }
        (if (negative) "-" else "") + expanded
    }
    return if ('.' in plain) plain.trimEnd('0').trimEnd('.') else plain
}
