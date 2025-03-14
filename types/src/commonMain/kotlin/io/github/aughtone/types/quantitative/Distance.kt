package io.github.aughtone.types.quantitative

/**
 * Represents a distance value in meters, optionally with an associated accuracy.
 *
 * @property meters The distance in meters. Must be non-negative.
 * @property accuracy The estimated accuracy of the distance measurement, as a fraction of the measured distance.
 *                   For example, an accuracy of 0.05 means the measurement is accurate to within 5%.
 *                   If null, the accuracy is unknown or not applicable.
 * @throws IllegalArgumentException if [meters] is negative.
 */
data class Distance(val meters: Double, val accuracy: Float? = null)
