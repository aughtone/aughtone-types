package io.github.aughtone.types.uri

/**
 * Thrown when a string cannot be parsed as the URI form that was asked for.
 *
 * It extends [IllegalArgumentException] deliberately: construction in this package already reports
 * malformed input that way, so a caller already catching `IllegalArgumentException` keeps working
 * while a caller who wants to name the failure now can.
 *
 * A named type also survives the language boundary in a way a bare `IllegalArgumentException` from
 * `require` does not, which is what makes the result-type form available by composition rather than
 * by shipping a second API:
 *
 * ```
 * runOutcome { url(untrustedString) }   // Outcome<Url>, carrying UriParseException
 * ```
 *
 * @property input The string that failed to parse.
 */
class UriParseException(
    val input: String,
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

private fun fail(input: String, reason: String): Nothing =
    throw UriParseException(input, "Cannot parse \"$input\": $reason")

private val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*$")

/**
 * Splits `scheme:rest`, validating the scheme against the RFC 3986 grammar and lowercasing it.
 * Schemes are case-insensitive, so lowercasing loses nothing and makes comparison work.
 */
private fun splitScheme(value: String, expected: String? = null): Pair<String, String> {
    val colon = value.indexOf(':')
    if (colon <= 0) fail(value, "no scheme — expected \"scheme:\" before the rest of the URI.")
    val scheme = value.substring(0, colon)
    if (!SCHEME.matches(scheme)) {
        fail(value, "\"$scheme\" is not a valid scheme; it must start with a letter and contain only letters, digits, '+', '-' and '.'.")
    }
    val lowered = scheme.lowercase()
    if (expected != null && lowered != expected) {
        fail(value, "expected the \"$expected\" scheme but found \"$lowered\".")
    }
    return lowered to value.substring(colon + 1)
}

/** Splits `rest` into authority, path, query and fragment. Authority is null when absent. */
private fun splitAfterScheme(rest: String): Quad {
    var remainder = rest
    var authority: String? = null
    if (remainder.startsWith("//")) {
        remainder = remainder.substring(2)
        val end = remainder.indexOfFirst { it == '/' || it == '?' || it == '#' }
        authority = if (end < 0) remainder else remainder.substring(0, end)
        remainder = if (end < 0) "" else remainder.substring(end)
    }
    val fragmentAt = remainder.indexOf('#')
    val fragment = if (fragmentAt < 0) "" else remainder.substring(fragmentAt + 1)
    if (fragmentAt >= 0) remainder = remainder.substring(0, fragmentAt)

    val queryAt = remainder.indexOf('?')
    val query = if (queryAt < 0) "" else remainder.substring(queryAt + 1)
    if (queryAt >= 0) remainder = remainder.substring(0, queryAt)

    return Quad(authority, remainder, query, fragment)
}

private class Quad(val authority: String?, val path: String, val query: String, val fragment: String)

/**
 * Parses an RFC 3986 URI.
 *
 * The scheme is lowercased, because it is case-insensitive. Nothing else is normalized: percent
 * encoding is preserved exactly, and dot segments in the path are left alone, since resolving them
 * changes what the path denotes and is the caller's decision.
 *
 * @throws UriParseException if [value] is not a well-formed URI.
 */
fun uri(value: String): Uri {
    val (scheme, rest) = splitScheme(value)
    val parts = splitAfterScheme(rest)
    return Uri(
        scheme = scheme,
        authority = parts.authority ?: "",
        path = parts.path,
        query = parts.query,
        fragment = parts.fragment,
    )
}

/** As [uri], but returns `null` instead of throwing. */
fun uriOrNull(value: String): Uri? = try { uri(value) } catch (e: UriParseException) { null }

/**
 * Parses an RFC 3986 URL — a URI with an authority, from which host, port and user information are
 * broken out.
 *
 * The scheme and host are lowercased, both being case-insensitive. An IPv6 host keeps its brackets,
 * as the RFC requires. Percent encoding is preserved and dot segments are not resolved.
 *
 * @throws UriParseException if [value] is not a well-formed URL, or carries no authority.
 */
fun url(value: String): Url {
    val (scheme, rest) = splitScheme(value)
    val parts = splitAfterScheme(rest)
    val authority = parts.authority
        ?: fail(value, "a URL needs an authority — expected \"$scheme://host\".")
    if (authority.isEmpty()) fail(value, "the authority is empty.")

    val at = authority.lastIndexOf('@')
    val userInfo = if (at < 0) "" else authority.substring(0, at)
    val hostAndPort = authority.substring(at + 1)

    val host: String
    val port: Int?
    if (hostAndPort.startsWith("[")) {
        val close = hostAndPort.indexOf(']')
        if (close < 0) fail(value, "an IPv6 host must be closed with ']'.")
        host = hostAndPort.substring(0, close + 1)
        val tail = hostAndPort.substring(close + 1)
        port = when {
            tail.isEmpty() -> null
            tail.startsWith(":") -> parsePort(value, tail.substring(1))
            else -> fail(value, "unexpected \"$tail\" after the IPv6 host.")
        }
    } else {
        val colon = hostAndPort.lastIndexOf(':')
        if (colon < 0) {
            host = hostAndPort; port = null
        } else {
            host = hostAndPort.substring(0, colon)
            port = parsePort(value, hostAndPort.substring(colon + 1))
        }
    }
    if (host.isEmpty()) fail(value, "the host is empty.")

    return Url(
        scheme = scheme,
        userInfo = userInfo,
        host = host.lowercase(),
        port = port,
        path = parts.path,
        query = parts.query,
        fragment = parts.fragment,
    )
}

private fun parsePort(input: String, text: String): Int? {
    if (text.isEmpty()) return null
    val port = text.toIntOrNull() ?: fail(input, "\"$text\" is not a valid port.")
    if (port !in 0..65535) fail(input, "port $port is outside 0..65535.")
    return port
}

/** As [url], but returns `null` instead of throwing. */
fun urlOrNull(value: String): Url? = try { url(value) } catch (e: UriParseException) { null }

/**
 * Parses an RFC 5870 `geo:` URI — `geo:latitude,longitude[,altitude][;crs=...][;u=...]`.
 *
 * Parameter names are case-insensitive and are lowercased. Coordinate range checks come from
 * [GeoUri]'s own constructor, so a syntactically valid but out-of-range coordinate is reported the
 * same way whether it was constructed or parsed.
 *
 * @throws UriParseException if [value] is not a well-formed `geo:` URI.
 */
fun geoUri(value: String): GeoUri {
    val (_, rest) = splitScheme(value, expected = "geo")
    val segments = rest.split(';')
    val coordinates = segments.first().split(',')
    if (coordinates.size !in 2..3) {
        fail(value, "expected \"latitude,longitude\" with an optional altitude, found ${coordinates.size} coordinate(s).")
    }

    fun number(text: String, name: String): Double =
        text.trim().toDoubleOrNull() ?: fail(value, "$name \"$text\" is not a number.")

    val latitude = number(coordinates[0], "latitude")
    val longitude = number(coordinates[1], "longitude")
    val altitude = if (coordinates.size == 3) number(coordinates[2], "altitude") else null

    var crs: String? = null
    var uncertainty: Int? = null
    for (parameter in segments.drop(1)) {
        if (parameter.isEmpty()) continue
        val equals = parameter.indexOf('=')
        if (equals < 0) fail(value, "parameter \"$parameter\" is missing a value.")
        val name = parameter.substring(0, equals).lowercase()
        val parameterValue = parameter.substring(equals + 1)
        when (name) {
            // RFC 5870 §3.3: crs labels are case-insensitive and lowercase is preferred.
            "crs" -> crs = parameterValue.lowercase()
            "u" -> uncertainty = parameterValue.toIntOrNull()
                ?: fail(value, "uncertainty \"$parameterValue\" is not an integer.")
            else -> Unit // RFC 5870 permits unknown parameters; they are not retained.
        }
    }

    return try {
        GeoUri(latitude, longitude, altitude, crs ?: GeoUri.DEFAULT_CRS, uncertainty)
    } catch (e: IllegalArgumentException) {
        throw UriParseException(value, "Cannot parse \"$value\": ${e.message}", e)
    }
}

/** As [geoUri], but returns `null` instead of throwing. */
fun geoUriOrNull(value: String): GeoUri? = try { geoUri(value) } catch (e: UriParseException) { null }

/** As [urn], but returns `null` instead of throwing. */
fun urnOrNull(value: String): Urn? = try { urn(value) } catch (e: IllegalArgumentException) { null }
