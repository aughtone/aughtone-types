package io.github.aughtone.types.geo

import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

/**
 * Asserts two JSON documents are structurally equal, rather than textually identical.
 *
 * Serialized numbers are not stable across targets — a `Double` of `100.0` renders as `100.0` on the
 * JVM and `100` on JS and Wasm — so comparing raw text makes the same correct payload pass on one
 * target and fail on another. Comparing parsed [kotlinx.serialization.json.JsonElement] trees decides
 * numeric equality semantically and leaves formatting out of it.
 *
 * Use a plain string comparison only where the literal text is the thing under test — RFC-mandated
 * key order, for instance — and say so at the call site.
 */
fun assertJsonEquals(expected: String, actual: String, message: String? = null) {
    assertEquals(
        Json.parseToJsonElement(expected),
        Json.parseToJsonElement(actual),
        message,
    )
}
