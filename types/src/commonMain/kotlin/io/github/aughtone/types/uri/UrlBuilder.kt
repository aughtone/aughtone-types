package io.github.aughtone.types.uri



/**
 * A class for building URLs in a structured and readable way.
 *
 * This class provides methods to add different parts of a URL,
 * such as the scheme, host, port, path segments, and query parameters.
 *
 * Path segments and query parameters are percent-encoded per RFC 3986
 * (spaces become "%20", never "+"). Repeated query parameter names are
 * preserved in insertion order.
 *
 * Example Usage:
 * ```kotlin
 * val url = UrlBuilder().apply {
 *     scheme = "https"
 *     host = "www.example.com"
 *     port = 8080
 *     addPathSegment("api")
 *     addPathSegments("v1", "users")
 *     addQueryParameter("id", "123")
 *     addQueryParameter("sort", "name")
 * }.build()
 * println(url) // Output: https://www.example.com:8080/api/v1/users?id=123&sort=name
 * ```
 */
class UrlBuilder {

    /**
     * The scheme of the URL (e.g., "http" or "https").
     */
    var scheme: String? = null

    /**
     * The host of the URL (e.g., "www.example.com").
     */
    var host: String? = null

    /**
     * The port of the URL (e.g., 80 or 443
    ).
     */
    var port: Int? = null

    /**
     * The list of path segments of the URL.
     */
    private val pathSegments: MutableList<String> = mutableListOf()

    /**
     * The query parameters of the URL, as name/value pairs in insertion order.
     * A list is used so repeated parameter names are preserved.
     */
    private val queryParameters: MutableList<Pair<String, String>> = mutableListOf()

    /**
     * Adds a path segment to the URL.
     *
     * @param segment The path segment to add.
     * @return This UrlBuilder instance.
     */
    fun addPathSegment(segment: String): UrlBuilder {
        pathSegments.add(UrlEncoder.encode(segment))
        return this
    }

    /**
     * Adds multiple path segments to the URL.
     *
     * @param segments The path segments to add.
     * @return This UrlBuilder instance.
     */
    fun addPathSegments(vararg segments: String): UrlBuilder {
        segments.forEach { addPathSegment(it) }
        return this
    }

    /**
     * Adds a query parameter to the URL. Calling this again with the same
     * name appends another parameter instead of replacing the first.
     ** @param name The name of the query parameter.
     * @param value The value of the query parameter.
     * @return This UrlBuilder instance.
     */
    fun addQueryParameter(name: String, value: String): UrlBuilder {
        queryParameters.add(UrlEncoder.encode(name) to UrlEncoder.encode(value))
        return this
    }

    /**
     * Builds the URL string.
     *
     * @return The complete URL string.
     * @throws IllegalStateException If the URL cannot be built due to missing components.
     */
    fun build(): String {
        // Check if we have at least a scheme and host
        if (scheme == null || host == null) {
            throw IllegalStateException("Scheme and host must be set to build a URL")
        }

        val url = StringBuilder()

        // Scheme
        url.append("$scheme://")

        // Host
        url.append(host)

        // Port (if present)
        port?.let { url.append(":$it") }

        // Path segments
        if (pathSegments.isNotEmpty()) {
            url.append("/")
            url.append(pathSegments.joinToString("/"))
        }

        // Query parameters
        if (queryParameters.isNotEmpty()) {
            url.append("?")
            url.append(
                queryParameters.joinToString("&") { (name, value) ->
                    "$name=$value"
                }
            )
        }

        return url.toString()
    }
}
