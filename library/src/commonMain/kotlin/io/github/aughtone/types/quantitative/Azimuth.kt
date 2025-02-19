package io.github.aughtone.types.quantitative

import kotlinx.serialization.Serializable

/**
 * Represents an azimuth, which is a horizontal angle measured clockwise from a north base direction or meridian.
 *
 * @property degrees The azimuth angle in degrees. Must be a value between 0.0 (inclusive) and 360.0 (exclusive).
 *                  Values outside this range will be normalized to fit within the [0, 360) range.
 * @property accuracy An optional accuracy value representing the uncertainty of the azimuth measurement,
 *                    typically in degrees. A lower value indicates higher accuracy. If null, the accuracy is unknown.
 *
 * Examples:
 *  - An azimuth of 0.0 degrees represents due north.
 *  - An azimuth of 90.0 degrees represents due east.
 *  - An azimuth of 180.0 degrees represents due south.
 *  - An azimuth of 270.0 degrees represents due west.
 *  - An azimuth of 360.0 degrees is equivalent to 0.0 degrees.
 *  - An azimuth of 450.0 degrees will be normalized to 90.0 degrees.
 *  - An azimuth of -90.0 degrees will be normalized to 270.0 degrees.
 */
@Serializable
data class Azimuth(val degrees: Double, val accuracy: Double? = null)
