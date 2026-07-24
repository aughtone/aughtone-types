package io.github.aughtone.types.uri

import kotlinx.serialization.SerialName
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
 * @property authority The authority component of the URI. May be empty for
 * authority-less URIs such as "urn:" or "geo:".
 * @property path The path component of the URI.
 * @property query The query string component of the URI.
 * @property fragment The fragment identifier component of the URI.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Uniform_Resource_Identifier">Uniform Resource Identifier</a>
 * @see <a href="https://auth0.com/blog/url-uri-urn-differences/">URL, URI, URN Differences</a>
 */
@Serializable
data class Uri(
    @SerialName("scheme")
    val scheme: String,
    @SerialName("authority")
    val authority: String,
    @SerialName("path")
    val path: String,
    @SerialName("query")
    val query: String,
    @SerialName("fragment")
    val fragment: String,
) {
    // URI = scheme ":" ["//" authority] path ["?" query] ["#" fragment]
    // See: https://en.wikipedia.org/wiki/Uniform_Resource_Identifier
    // See: https://auth0.com/blog/url-uri-urn-differences/

    /**
     * Returns a string representation of the URI.
     *
     * The string is formatted according to the general URI syntax:
     * `scheme:[//authority]path[?query][#fragment]`
     *
     * The "//" prefix is only emitted when [authority] is non-empty, and the
     * '?' and '#' delimiters are omitted when the corresponding component is empty.
     *
     * @return A string representation of the URI.
     */
    override fun toString(): String = buildString {
        append(scheme).append(':')
        if (authority.isNotEmpty()) {
            append("//").append(authority)
            if (path.isNotEmpty() && !path.startsWith("/")) append('/')
        }
        append(path)
        if (query.isNotEmpty()) append('?').append(query)
        if (fragment.isNotEmpty()) append('#').append(fragment)
    }

    /**
     * Converts this URI to a Uniform Resource Name (URN).
     *
     * A URN is a specific type of URI that identifies a resource by a name in a particular namespace.
     * This conversion expects the path component to be in the URN form `namespace:identity`;
     * if the path contains no ':' the authority is used as the namespace as a fallback.
     *
     * @return A new [Urn] instance representing this URI as a URN.
     * @throws IllegalArgumentException if the resulting namespace is not a valid RFC 8141 NID.
     */
    fun toUrn(): Urn = if (':' in path) {
        Urn(namespace = path.substringBefore(':'), identity = path.substringAfter(':'))
    } else {
        Urn(namespace = authority, identity = path)
    }

    /**
     * Converts this [Uri] to a [Url].
     *
     * This function constructs a [Url] from the components of this [Uri]. It parses the authority
     * component to extract the user info, host, and port.
     *
     * The authority component is expected to follow the format: `[userinfo@]host[:port]`.
     *
     * - **userinfo**: The part of the authority before the last "@" character, or an empty string if "@" is not present.
     * - **host**: The host name, IPv4 address, or bracketed IPv6 literal (brackets are kept).
     * - **port**: The decimal port after the host, or null if absent.
     *
     * @return A [Url] object representing the converted URI.
     *
     * @throws IllegalArgumentException if the authority contains an unterminated
     * IPv6 literal or a port that is not a decimal number.
     */
    fun toUrl(): Url {
        // authority = [userinfo "@"] host [":" port]
        val userInfo = if ('@' in authority) authority.substringBeforeLast('@') else ""
        val hostPort = authority.substringAfterLast('@')
        val host: String
        val portString: String?
        if (hostPort.startsWith("[")) {
            val end = hostPort.indexOf(']')
            require(end >= 0) { "Unterminated IPv6 host in authority: $authority" }
            host = hostPort.substring(0, end + 1)
            val rest = hostPort.substring(end + 1)
            portString = when {
                rest.isEmpty() -> null
                rest.startsWith(":") -> rest.substring(1)
                else -> throw IllegalArgumentException("Invalid characters after IPv6 host in authority: $authority")
            }
        } else {
            val colon = hostPort.lastIndexOf(':')
            host = if (colon >= 0) hostPort.substring(0, colon) else hostPort
            portString = if (colon >= 0) hostPort.substring(colon + 1) else null
        }
        val port = portString?.let { candidate ->
            require(candidate.isNotEmpty() && candidate.all { it in '0'..'9' }) {
                "Invalid port \"$candidate\" in authority: $authority"
            }
            candidate.toInt()
        }
        return Url(
            scheme = scheme,
            userInfo = userInfo,
            host = host,
            port = port,
            path = path,
            query = query,
            fragment = fragment
        )
    }

}
