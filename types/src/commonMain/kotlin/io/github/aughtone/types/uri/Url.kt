package io.github.aughtone.types.uri

/**
 * Represents a parsed URL.
 *
 * @property scheme The protocol scheme (e.g., "http", "https").
 * @property userInfo The user information part of the authority (e.g., "user:password").
 * @property host The host name or IP address.
 * @property port The port number, or null if the default port for the scheme is used.
 * @property path The path component of the URL.
 * @property query The query string, not including the leading '?'.
 * @property fragment The fragment identifier, not including the leading '#'.
 * @property authority The authority part of the URL, combining host and port (e.g., "example.com:8080").
 * @property identity A string representing the resource identity from the path, query and fragment (e.g., "/path?query#fragment")
 * @constructor Creates a [Url] instance with the specified components.
 * @see Uri
 */
data class Url(
    val scheme: String,
    val userInfo: String,
    val host: String,
    val port: Int? = null,
    val path: String,
    val query: String,
    val fragment: String,
) {
    /**
     * The authority part of the URL, combining [host] and [port] (e.g., "example.com:8080").
     * If the [port] is null it will not be included, except if the [host] is empty.
     * If the [host] is empty, it will return the port with a leading ":".
     */
    val authority: String = "$host:$port"
    /**
     * A string representing the resource identity constructed from the [path], [query], and [fragment].
     *
     * It combines these components in the format: `path?query#fragment`.
     *
     * For example, if `path` is "/example", `query` is "key=value", and `fragment` is "section1",
     * then `identity` will be "/example?key=value#section1".
     */
    val identity: String = "$path?$query#$fragment"

    /**
     * Returns a string representation of the URL.
     *
     * The string is formatted as: `scheme://authority/path?query#fragment`.
     *
     * @return A string representation of the URL.
     */
    override fun toString(): String = "$scheme://${authority}/$path?$query#$fragment"

    /**
     * Converts this [Url] instance to a [Uri] instance.
     *
     * @return A [Uri] object representing the same URL components as this [Url].
     */
    fun toUri(): Uri =
        Uri(scheme = scheme, authority = authority, path = path, query = query, fragment = fragment)
}
