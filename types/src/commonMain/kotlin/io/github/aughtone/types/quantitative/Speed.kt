package io.github.aughtone.types.quantitative

import kotlinx.serialization.Serializable

/**
 * Represents a speed value, measured in meters per second (mps), along with an optional accuracy.
 *
 * @property mps The speed in meters per second. Must be a non-negative value.
 * @property accuracy An optional estimate of the accuracy of the speed, in meters per second.
 *                   If provided, it represents the possible error range of the `mps` value.
 *                   A null value indicates that no accuracy information is available. Must be non negative if provided.
 * @throws IllegalArgumentException if `mps` is negative or if `accuracy` is negative when provided.
 */
@Serializable
data class Speed(val mps: Float, val accuracy: Float? = null)
