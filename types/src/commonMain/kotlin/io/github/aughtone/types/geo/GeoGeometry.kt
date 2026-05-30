package io.github.aughtone.types.geo

import kotlinx.serialization.Serializable

/**
 * A sealed class representing any of the seven GeoJSON geometry types.
 */
@Serializable
sealed class GeoGeometry : GeoJson()
