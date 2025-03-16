package io.github.aughtone.types.net

import kotlinx.serialization.Serializable

/**
 * Represents a Uniform Resource Identifier (URI) as defined by RFC 3986.
 *
 * A URI is a compact sequence of characters that identifies an abstract or physical resource.
 *
 * @property scheme The scheme component of the URI (e.g., "http", "https", "ftp").
 * @property userInfo The user information component, if present, which is typically a username and optional password.
 * @property authority The authority component, typically consisting of a host and an optional port number.
 * @property port The port number, if specified in the authority component.
 * @property path The path component, which typically identifies a resource within the scope of the authority.
 * @property query The query component, which typically contains parameters or data for the resource.
 * @property fragment The fragment component, which typically identifies a secondary resource or a specific part of a primary resource.
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

    override fun toString(): String = "$scheme://${authority}/$path?$query#$fragment"

    fun toUrn(): Urn = Urn(namespace = authority, identity = path)

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
