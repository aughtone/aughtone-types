package io.github.aughtone.types.net

data class Url(
    val scheme: String,
    val userInfo: String,
    val host: String,
    val port: Int? = null,
    val path: String,
    val query: String,
    val fragment: String,
) {
    val authority: String = "$host:$port"
    val identity: String = "$path?$query#$fragment"

    override fun toString(): String = "$scheme://${authority}/$path?$query#$fragment"

    fun toUri(): Uri =
        Uri(scheme = scheme, authority = authority, path = path, query = query, fragment = fragment)
}
