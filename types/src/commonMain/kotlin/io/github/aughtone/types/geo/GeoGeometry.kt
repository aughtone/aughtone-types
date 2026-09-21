package io.github.aughtone.types.geo

import kotlinx.serialization.Serializable

/**
 * A sealed class representing any of the seven GeoJSON geometry types defined by RFC 7946.
 */
@Serializable
sealed class GeoGeometry : GeoJson()
