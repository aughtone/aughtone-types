package io.github.aughtone.types.uri

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a parsed URL.
 *
 * @property scheme The protocol scheme (e.g., "http", "https").
 * @property userInfo The user information part of the authority (e.g., "user:password").
 * @property host The host name or IP address. IPv6 literals keep their brackets (e.g., "[::1]").
 * @property port The port number, or null if the default port for the scheme is used.
 * @property path The path component of the URL.
 * @property query The query string, not including the leading '?'.
 * @property fragment The fragment identifier, not including the leading '#'.
 * @property authority The authority part of the URL, combining userInfo, host and port (e.g., "user@example.com:8080").
 * @property identity A string representing the resource identity from the path, query and fragment (e.g., "/path?query#fragment")
 * @constructor Creates a [Url] instance with the specified components.
 * @see Uri
 */
@Serializable
data class Url(
    @SerialName("scheme")
    val scheme: String,
    @SerialName("userInfo")
    val userInfo: String,
    @SerialName("host")
    val host: String,
    @SerialName("port")
    val port: Int? = null,
    @SerialName("path")
    val path: String,
    @SerialName("query")
    val query: String,
    @SerialName("fragment")
    val fragment: String,
) {
    /**
     * The authority part of the URL, in the form `[userInfo "@"] host [":" port]`.
     * The [userInfo] and [port] parts are omitted when empty or null.
     */
    val authority: String = buildString {
        if (userInfo.isNotEmpty()) append(userInfo).append('@')
        append(host)
        if (port != null) append(':').append(port)
    }

    /**
     * A string representing the resource identity constructed from the [path], [query], and [fragment].
     *
     * It combines these components in the format: `path[?query][#fragment]`, omitting
     * the '?' and '#' delimiters when the corresponding component is empty.
     *
     * For example, if `path` is "/example", `query` is "key=value", and `fragment` is "section1",
     * then `identity` will be "/example?key=value#section1".
     */
    val identity: String = buildString {
        append(path)
        if (query.isNotEmpty()) append('?').append(query)
        if (fragment.isNotEmpty()) append('#').append(fragment)
    }

    /**
     * Returns a string representation of the URL.
     *
     * The string is formatted as: `scheme://authority path[?query][#fragment]`.
     * Empty query and fragment components are omitted, and a '/' is inserted
     * before a non-empty path that does not already start with one.
     *
     * @return A string representation of the URL.
     */
    override fun toString(): String = buildString {
        append(scheme).append("://").append(authority)
        if (path.isNotEmpty() && !path.startsWith("/")) append('/')
        append(identity)
    }

    /**
     * Converts this [Url] instance to a [Uri] instance.
     *
     * @return A [Uri] object representing the same URL components as this [Url].
     */
    fun toUri(): Uri =
        Uri(scheme = scheme, authority = authority, path = path, query = query, fragment = fragment)
}
