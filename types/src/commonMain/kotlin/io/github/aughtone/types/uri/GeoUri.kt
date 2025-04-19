package io.github.aughtone.types.uri


/**
 * Represents a Geographic Reference Identifier (GRI)
 *
 * @constructor Creates a new GRI with the specified namespace and identity.
 * @see [RFC rfc5870](https://datatracker.ietf.org/doc/html/rfc5870)
 * @see Uri
 */
data class GeoUri(
    val latitude: Double,
    val longitude:Double,
    val altitude:Double?=null,
    val crs: String?="wgs84",
    val uncertainty:Int?=null,
) {
    val scheme: String = "geo"
    fun toUri(): Uri = Uri(scheme = scheme, authority = "$latitude,$longitude${if(altitude!=null)",$altitude" else ""}", path = "", query = "${if(crs!=null)";crs=$crs" else ""}${if(uncertainty!=null)";u=$uncertainty" else ""}", fragment = "")

    override fun toString(): String = "$scheme:$latitude,$longitude${if(altitude!=null)",$altitude" else ""}${if(crs!=null)";crs=$crs" else ""}${if(uncertainty!=null)";u=$uncertainty" else ""}"
}
