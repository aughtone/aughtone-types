package io.github.aughtone.types.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

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
 * @property properties The feature's properties, as an arbitrary JSON object. RFC 7946 permits any
 *                       JSON value here — numbers, booleans, nulls, nested objects and arrays — so
 *                       this is a [JsonObject] rather than a map of strings, and round-trips without
 *                       reinterpretation. Use [stringProperty] and its siblings to read values
 *                       without handling [kotlinx.serialization.json.JsonElement] directly.
 * @property id An optional identifier. RFC 7946 permits a string or a number, so this is a
 *              [JsonPrimitive]; booleans and nulls are rejected.
 * @property bbox An optional bounding box for the feature as per RFC 7946 Section 5.
 *                 The array length is 2*n where n is the number of dimensions in the contained geometry.
 */
@Serializable
@SerialName("Feature")
data class GeoFeature(
    @SerialName("geometry")
    val geometry: GeoGeometry?,
    @SerialName("properties")
    val properties: JsonObject? = null,
    @SerialName("id")
    val id: JsonPrimitive? = null,
    @SerialName("bbox")
    override val bbox: List<Double>? = null
) : GeoJson() {
    init {
        if (id != null) {
            require(id !is JsonNull) { "A GeoJSON Feature id must be a string or a number, not null." }
            require(id.isString || id.booleanOrNull == null) {
                "A GeoJSON Feature id must be a string or a number, not a boolean."
            }
        }
    }
}

/**
 * Reads [key] as text.
 *
 * Scalars come back unquoted — `"x"`, `123`, `true`. Objects and arrays come back as their JSON
 * text, so a property that is set never looks unset. Returns `null` only when the key is absent or
 * its value is JSON `null`; use `properties?.containsKey(key)` to tell those two apart.
 */
fun GeoFeature.stringProperty(key: String): String? {
    val element = properties?.get(key) ?: return null
    return when {
        element is JsonNull -> null
        element is JsonPrimitive -> element.content
        else -> element.toString()
    }
}

/** Reads [key] as an [Int], or `null` if it is absent, null, or not a number. */
fun GeoFeature.intProperty(key: String): Int? =
    (properties?.get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.intOrNull

/** Reads [key] as a [Double], or `null` if it is absent, null, or not a number. */
fun GeoFeature.doubleProperty(key: String): Double? =
    (properties?.get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.doubleOrNull

/** Reads [key] as a [Boolean], or `null` if it is absent, null, or not a boolean. */
fun GeoFeature.booleanProperty(key: String): Boolean? =
    (properties?.get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.booleanOrNull

/** Reads [key] as a nested object, or `null` if it is absent or not an object. */
fun GeoFeature.objectProperty(key: String): JsonObject? = properties?.get(key) as? JsonObject

/** Reads [key] as an array, or `null` if it is absent or not an array. */
fun GeoFeature.arrayProperty(key: String): JsonArray? = properties?.get(key) as? JsonArray
