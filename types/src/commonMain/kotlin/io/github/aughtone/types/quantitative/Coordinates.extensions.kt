package io.github.aughtone.types.quantitative

import kotlin.math.PI


/**
 * Returns a new [Coordinates] object by adding delta values to latitude and longitude.
 *
 * Note: This function performs a simple arithmetic addition. It does not account for wrapping
 * at the poles or the international date line, and is only suitable for small deltas
 * where geographical accuracy is not critical. For more accurate calculations, use
 * the `plus(distance: Distance, bearing: Azimuth)` function.
 *
 *  ```kotlin
 *  val originalCoordinates = Coordinates(40.7128, -74.0060) // New York City
 *  val movedCoordinates = originalCoordinates.add(0.1, 0.2) // Move slightly north-east
 *  println(movedCoordinates) // Output: Coordinates(latitude=40.8128, longitude=-73.806)
 *  ```
 *
 * @param deltaLatitude The change in latitude to apply.
 * @param deltaLongitude The change in longitude to apply.
 * @return A new [Coordinates] object with the updated latitude and longitude.
 */
fun Coordinates.add(deltaLatitude: Double, deltaLongitude: Double): Coordinates {
    return Coordinates(latitude + deltaLatitude, longitude + deltaLongitude)
}


/**
 * Splits the Coordinates object into a pair of Double values representing latitude and longitude.
 *
 * This function extracts the latitude and longitude values from the Coordinates object and returns
 * them as a Pair. The first element of the Pair is the latitude, and the second element is the longitude.
 *
 * @receiver The Coordinates object to split.
 * @return A Pair containing the latitude (first) and longitude (second) as Double values.
 */
fun Coordinates.split(): Pair<Double, Double> = Pair(this.latitude, this.longitude)


/**
 * Converts an angle from degrees to radians.
 *
 * @param degrees The angle in degrees.
 * @return The angle in radians.
 */
internal fun toRadians(degrees: Double): Double = degrees * PI / 180.0

/**
 * Converts an angle from radians to degrees.
 *
 * @param radians The angle in radians.
 * @return The angle in degrees.
 */
internal fun toDegrees(radians: Double): Double = radians * 180.0 / PI
