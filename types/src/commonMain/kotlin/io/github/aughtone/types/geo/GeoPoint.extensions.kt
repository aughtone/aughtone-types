package io.github.aughtone.types.geo

import io.github.aughtone.types.quantitative.Coordinates

/**
 * Converts these [Coordinates] into a GeoJSON [GeoPoint], putting the longitude first.
 *
 * The two types order their pair differently on purpose: [Coordinates] takes latitude first, the way
 * the pair is spoken, and a GeoJSON position takes longitude first, because RFC 7946 §3.1.1 fixes that
 * order. Both values are `Double`, so converting by hand compiles whichever way round you write it,
 * serializes, and silently puts the point in the wrong hemisphere. This does the reordering for you,
 * and its name says which direction it goes.
 *
 * ```
 * val here = Coordinates(latitude = 45.42, longitude = -75.69)
 * val point = here.toGeoPoint()        // coordinates == [-75.69, 45.42]
 * ```
 *
 * **[Coordinates.accuracy] is dropped**, because GeoJSON defines no member to carry it: RFC 7946 §3.1
 * allows a position a third element, and that element is altitude, not an error estimate. Keep the
 * original [Coordinates] if the accuracy matters, or put it in a [GeoFeature]'s `properties`.
 *
 * The result is a two-element position with no `bbox`. It is always valid GeoJSON, because
 * [Coordinates] already refuses a latitude outside -90..90 or a longitude outside -180..180 at
 * construction.
 *
 * @return A [GeoPoint] whose position is `[longitude, latitude]`.
 */
fun Coordinates.toGeoPoint(): GeoPoint = GeoPoint(longitude = longitude, latitude = latitude)
