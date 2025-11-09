package io.github.aughtone.types.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Uniform Resource Name (URN).
 *
 * A URN is a location-independent, persistent, resource identifier.
 * It consists of a namespace and an identity part, separated by a colon.
 *
 * @property namespace The namespace identifier (NID) of the URN.
 *                     It identifies the naming authority responsible for
 *                     assigning names within this namespace.
 * @property identity The namespace-specific string (NSS) of the URN.
 *                    It is assigned by the naming authority and uniquely
 *                    identifies the resource within the namespace.
 * @property scheme The scheme of the URN, which is always "urn".
 * @constructor Creates a new URN with the specified namespace and identity.
 * @see [RFC 8141](https://www.rfc-editor.org/rfc/rfc8141.html)
 * @see Uri
 */

@Serializable
data class Urn(
    @SerialName("namespace") val namespace: String,
    @SerialName("identity") val identity: String,
) {
    /**
     * The scheme of the URN, which is always "urn".
     */
    @SerialName("scheme")
    val scheme: String = "urn"

    /**
     * Converts this URN to a [Uri] object.
     *
     * The resulting URI will have:
     * - a scheme of "urn"
     * - an authority equal to the URN's namespace
     * - a path equal to the URN's identity
     * - an empty query and fragment.
     *
     * @return A [Uri] representing this URN.
     */
    fun toUri(): Uri =
        Uri(scheme = scheme, authority = namespace, path = identity, query = "", fragment = "")

    override fun toString(): String = "$scheme:$namespace:$identity"

}

/**
 * Parses a string to create a [Urn] object.
 *
 * The string must be in the format "urn:namespace:identity".
 *
 * @param urnString The string to parse.
 * @return A new [Urn] instance.
 * @throws IllegalArgumentException if the string is not a valid URN.
 */
fun urn(urnString: String): Urn {
    require(urnString.startsWith("urn:")) { "URN must start with 'urn:'" }
    val parts = urnString.substring(4).split(":", limit = 2)
    require(parts.size == 2) { "URN must have namespace and identity" }
    return Urn(namespace = parts[0], identity = parts[1])
}
