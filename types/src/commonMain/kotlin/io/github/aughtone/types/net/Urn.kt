package io.github.aughtone.types.net

data class Urn(
    val namespace: String,
    val identity: String,
) {
    val scheme: String = "urn"
    fun toUri(): Uri = Uri(scheme = scheme, authority = namespace, path = identity, query = "", fragment = "")

    override fun toString(): String = "$scheme:$namespace:$identity"
}
