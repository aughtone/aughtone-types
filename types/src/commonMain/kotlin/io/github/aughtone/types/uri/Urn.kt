package io.github.aughtone.types.uri

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Uniform Resource Name (URN).
 *
 * A URN is a location-independent, persistent, resource identifier.
 * It consists of a namespace and an identity part, separated by a colon,
 * optionally followed by an r-component ("?+"), a q-component ("?=") and
 * an f-component ("#") as defined by RFC 8141.
 *
 * The namespace (NID) is validated against RFC 8141: 2 to 32 characters,
 * starting and ending with a letter or digit, with letters, digits and
 * hyphens in between. NID comparison is case-insensitive, so
 * `Urn("ISBN", x) == Urn("isbn", x)`; the stored [namespace] keeps the
 * case it was constructed with.
 *
 * @property namespace The namespace identifier (NID) of the URN.
 *                     It identifies the naming authority responsible for
 *                     assigning names within this namespace.
 * @property identity The namespace-specific string (NSS) of the URN.
 *                    It is assigned by the naming authority and uniquely
 *                    identifies the resource within the namespace.
 * @property rComponent The r-component (resolution parameters), without the leading "?+", or null.
 * @property qComponent The q-component (query parameters), without the leading "?=", or null.
 * @property fComponent The f-component (fragment), without the leading "#", or null.
 * @property scheme The scheme of the URN, which is always "urn".
 * @constructor Creates a new URN with the specified namespace and identity.
 * @throws IllegalArgumentException if the namespace is not a valid RFC 8141 NID or the identity is empty.
 * @see [RFC 8141](https://www.rfc-editor.org/rfc/rfc8141.html)
 * @see Uri
 */

@Serializable
data class Urn(
    @SerialName("namespace")
    val namespace: String,
    @SerialName("identity")
    val identity: String,
    @SerialName("rComponent")
    val rComponent: String? = null,
    @SerialName("qComponent")
    val qComponent: String? = null,
    @SerialName("fComponent")
    val fComponent: String? = null,
) {
    init {
        require(NID_REGEX.matches(namespace)) {
            "URN namespace (NID) must be 2-32 characters, start and end with a letter or digit, and contain only letters, digits and hyphens: \"$namespace\""
        }
        require(identity.isNotEmpty()) { "URN identity (NSS) must not be empty" }
    }

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
     * - an empty authority (URNs have no authority component)
     * - a path of "namespace:identity"
     * - a query holding the r/q-components as they appear in the URN string, if present
     * - a fragment equal to the f-component, if present.
     *
     * @return A [Uri] representing this URN.
     */
    fun toUri(): Uri = Uri(
        scheme = scheme,
        authority = "",
        path = "$namespace:$identity",
        query = buildString {
            if (rComponent != null) append('+').append(rComponent)
            if (qComponent != null) {
                if (rComponent != null) append('?')
                append('=').append(qComponent)
            }
        },
        fragment = fComponent ?: "",
    )


    /**
     * Returns a string representation of this URN.
     *
     * The string representation is in the format
     * "urn:namespace:identity[?+rComponent][?=qComponent][#fComponent]".
     *
     * @return The string representation of this URN.
     */
    override fun toString(): String = buildString {
        append(scheme).append(':').append(namespace).append(':').append(identity)
        if (rComponent != null) append("?+").append(rComponent)
        if (qComponent != null) append("?=").append(qComponent)
        if (fComponent != null) append('#').append(fComponent)
    }

    /**
     * Equality per RFC 8141 NID rules: the [namespace] is compared case-insensitively,
     * all other components are compared exactly.
     */
    override fun equals(other: Any?): Boolean = other is Urn &&
            namespace.equals(other.namespace, ignoreCase = true) &&
            identity == other.identity &&
            rComponent == other.rComponent &&
            qComponent == other.qComponent &&
            fComponent == other.fComponent

    override fun hashCode(): Int {
        var result = namespace.lowercase().hashCode()
        result = 31 * result + identity.hashCode()
        result = 31 * result + (rComponent?.hashCode() ?: 0)
        result = 31 * result + (qComponent?.hashCode() ?: 0)
        result = 31 * result + (fComponent?.hashCode() ?: 0)
        return result
    }

    companion object {
        // RFC 8141: NID = (alphanum) 0*30(ldh) (alphanum)
        private val NID_REGEX = Regex("[A-Za-z0-9][A-Za-z0-9-]{0,30}[A-Za-z0-9]")
    }
}

/**
 * Parses a string to create a [Urn] object.
 *
 * The string must be in the format
 * "urn:namespace:identity[?+rComponent][?=qComponent][#fComponent]".
 * The "urn:" scheme is matched case-insensitively per RFC 8141.
 *
 * @param urnString The string to parse.
 * @return A new [Urn] instance.
 * @throws IllegalArgumentException if the string is not a valid URN.
 */
fun urn(urnString: String): Urn {
    require(urnString.length > 4 && urnString.regionMatches(0, "urn:", 0, 4, ignoreCase = true)) {
        "URN must start with 'urn:'"
    }
    val rest = urnString.substring(4)
    val colon = rest.indexOf(':')
    require(colon >= 0) { "URN must have namespace and identity" }
    val namespace = rest.substring(0, colon)
    var tail = rest.substring(colon + 1)

    var fComponent: String? = null
    val hash = tail.indexOf('#')
    if (hash >= 0) {
        fComponent = tail.substring(hash + 1)
        tail = tail.substring(0, hash)
    }

    var rComponent: String? = null
    var qComponent: String? = null
    val rIndex = tail.indexOf("?+")
    val qIndex = tail.indexOf("?=")
    when {
        // The r-component precedes the q-component per RFC 8141.
        rIndex >= 0 && (qIndex < 0 || rIndex < qIndex) -> {
            val qAfter = tail.indexOf("?=", rIndex + 2)
            rComponent = if (qAfter >= 0) tail.substring(rIndex + 2, qAfter) else tail.substring(rIndex + 2)
            if (qAfter >= 0) qComponent = tail.substring(qAfter + 2)
            tail = tail.substring(0, rIndex)
        }

        qIndex >= 0 -> {
            qComponent = tail.substring(qIndex + 2)
            tail = tail.substring(0, qIndex)
        }
    }

    return Urn(
        namespace = namespace,
        identity = tail,
        rComponent = rComponent,
        qComponent = qComponent,
        fComponent = fComponent,
    )
}
