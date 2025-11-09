package io.github.aughtone.types.quantitative



/**
 * Returns a new [Coordinate] object with latitude, and longitude increased by deltaLatitude,
 * and deltaLongitude.
 *
 *  ```kotlin
 *  val originalCoordinates = Coordinates(40.7128, -74.0060) // New York City
 *  val movedCoordinates = originalCoordinates(0.1, 0.2) // Move slightly north-east
 *  println(movedCoordinates) // Output: Coordinates(latitude=40.8128, longitude=-73.806)
 *  ```
 *
 * @param deltaLatitude change in latitude
 * @param deltaLongitude change in longitude
 * @return [Coordinate] with increased latitude and longitude
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
