
package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.entity.Reading
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingFreshnessTest {

    @Test
    fun readingExpiresAfterThirtyMinutes() {
        val recordedAt = Instant.parse("2026-10-28T12:00:00Z")

        val reading = Reading(
            id = 1,
            deviceId = 1,
            recordedAt = recordedAt,
            moisturePercent = 45.0,
            soilTemperatureC = 22.0,
            nitrogenPpm = 10.0,
            phosphorusPpm = 10.0,
            potassiumPpm = 10.0,
            source = "SIMULATED",
        )

        assertFalse(
            reading.isStale(recordedAt.plusSeconds(29 * 60L))
        )

        assertFalse(
            reading.isStale(recordedAt.plusSeconds(30 * 60L))
        )

        assertTrue(
            reading.isStale(recordedAt.plusSeconds(31 * 60L))
        )
    }
}
