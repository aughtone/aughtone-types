package io.github.aughtone.types.uri

import kotlinx.serialization.Serializable


/**
 * Represents a Uniform Resource Identifier (URI).
 *
 * A URI is a compact sequence of characters that identifies an abstract or physical resource.
 * This class provides a structured representation of a URI, including its scheme, authority, path, query, and fragment components.
 *
 * The general format of a URI is:
 * `scheme:[//authority]path[?query][#fragment]`
 *
 * Where:
 * - **scheme**: The naming scheme of the URI (e.g., "http", "https", "ftp").
 * - **authority**: The authority component, typically consisting of a userinfo, host, and port.
 * - **path**: The hierarchical path to the resource.
 * - **query**: The query string, providing additional parameters to the resource.
 * - **fragment**: The fragment identifier, specifying a portion of the resource.
 *
 * This class also provides methods to convert a URI to other related resource identifiers like
 * [Urn] and [Url].
 *
 * @property scheme The scheme of the URI.
 * @property authority The authority component of the URI.
 * @property path The path component of the URI.
 * @property query The query string component of the URI.
 * @property fragment The fragment identifier component of the URI.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Uniform_Resource_Identifier">Uniform Resource Identifier</a>
 * @see <a href="https://auth0.com/blog/url-uri-urn-differences/">URL, URI, URN Differences</a>
 */
@Serializable
data class Uri(
    val scheme: String,
    val authority: String,
    val path: String,
    val query: String,
    val fragment: String,
) {
    // URI = scheme ":" ["//" authority] path ["?" query] ["#" fragment]
    // See: https://en.wikipedia.org/wiki/Uniform_Resource_Identifier
    // See: https://auth0.com/blog/url-uri-urn-differences/

    /**
     * Returns a string representation of the URI.
     *
     * The string is formatted according to the general URI syntax:
     * `scheme://authority/path?query#fragment`
     *
     * @return A string representation of the URI.
     */
    override fun toString(): String = "$scheme://${authority}/$path?$query#$fragment"

    /**
     * Converts this URI to a Uniform Resource Name (URN).
     *
     * A URN is a specific type of URI that identifies a resource by a name in a particular namespace.
     * This conversion assumes that the authority component of the URI corresponds to the URN namespace,
     * and the path component corresponds to the URN-specific identifier.
     *
     * @return A new [Urn] instance representing this URI as a URN.
     */
    fun toUrn(): Urn = Urn(namespace = authority, identity = path)

    /**
     * Converts this [Uri] to a [Url].
     *
     * This function constructs a [Url] from the components of this [Uri]. It parses the authority
     * component to extract the user info, host, and port.
     *
     * The authority component is expected to follow the format: `[userinfo@]host[:port]`.
     *
     * - **userinfo**: Extracted from the part of the authority before the first "@" character. If "@" is not present, it defaults to an empty string.
     * - **host**: Extracted from the part of the authority after "@" and before ":".
     * - **port**: Extracted from the part of the authority after the last ":", if present, otherwise it defaults to 0.
     *
     * @return A [Url] object representing the converted URI.
     *
     * @throws NumberFormatException if the port part of the authority is present but not a valid integer.
     */
    fun toUrl(): Url = Url(
        scheme = scheme,
        // authority = [userinfo "@"] host [":" port]
        userInfo = authority.substringBefore(
            "@",
            missingDelimiterValue = ""
        ), // XXX Need to sort out how to parse this out of the authority.
        host = authority.substringAfter("@").substringBefore(":"),
        port = authority.substringAfter("@").substringAfter(":", missingDelimiterValue = "")
            .let { if (it.isNotEmpty()) it.toInt() else 0 },
        path = path,
        query = query,
        fragment = fragment
    )

}
