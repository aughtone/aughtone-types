package io.github.aughtone.types.uri



/**
 * Represents a Geographic Reference Identifier (GRI or GeoUri) as defined by [RFC 5870](https://datatracker.ietf.org/doc/html/rfc5870).
 *
 * A GRI is a way to specify a location on the Earth using latitude, longitude, and optionally, altitude,
 * coordinate reference system (CRS), and uncertainty.
 *
 * @property latitude The latitude of the location in decimal degrees.
 * @property longitude The longitude of the location in decimal degrees.
 * @property altitude The altitude of the location in meters (optional).
 * @property crs The coordinate reference system (CRS) used for the location (optional, defaults to "wgs84").
 * @property uncertainty The uncertainty of the location in meters (optional).
 */
data class Gri(
    val latitude: Double,
    val longitude:Double,
    val altitude:Double?=null,
    val crs: String?="wgs84",
    val uncertainty:Int?=null,
) {
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
     * - `authority`:  latitude and longitude separated by a comma, optionally followed by altitude prefixed by a comma.
     * - `path`: empty.
     * - `query`: optionally includes "crs" and "u" (uncertainty) parameters.
     * - `fragment`: empty.
     *
     * Example:
     * - `geo:37.786971,-122.399677;crs=wgs84;u=5`
     * - `geo:37.786971,-122.399677,100`
     * - `geo:37.786971,-122.399677`
     *
     * @return A [Uri] object representing this GRI.
     */
    fun toUri(): Uri = Uri(scheme = scheme, authority = "$latitude,$longitude${if(altitude!=null)",$altitude" else ""}", path = "", query = "${if(crs!=null)";crs=$crs" else ""}${if(uncertainty!=null)";u=$uncertainty" else ""}", fragment = "")

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
     * @return A string representation of the GRI.
     */
    override fun toString(): String = "$scheme:$latitude,$longitude${if(altitude!=null)",$altitude" else ""}${if(crs!=null)";crs=$crs" else ""}${if(uncertainty!=null)";u=$uncertainty" else ""}"
}
