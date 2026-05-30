package io.github.aughtone.types.quantitative

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Telemetry serialization and deserialization`() {
        val telemetry = Telemetry(
            coordinates = Coordinates(45.0, -90.0),
            azimuth = Azimuth(180.0),
            speed = Speed(10.0),
            altitude = Altitude(100.0),
            timestamp = 123456789L
        )

        val encoded = json.encodeToString(Telemetry.serializer(), telemetry)
        val decoded = json.decodeFromString(Telemetry.serializer(), encoded)

        assertEquals(telemetry, decoded)
        assertEquals(45.0, decoded.coordinates.latitude)
        assertEquals(180.0, decoded.azimuth?.degrees)
    }

    @Test
    fun `Telemetry default values`() {
        val telemetry = Telemetry(coordinates = Coordinates(0.0, 0.0))
        assertEquals(0L, telemetry.timestamp)
        assertEquals(null, telemetry.azimuth)
        assertEquals(null, telemetry.speed)
        assertEquals(null, telemetry.altitude)
    }
}
