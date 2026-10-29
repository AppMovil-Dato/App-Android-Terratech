
package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.evaluateThresholds
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FertilityThresholdAlertTest {

    private val thresholds = FertilityZoneThresholds(
        zoneId = 1,
        moistureMin = 25.0,
        moistureMax = 70.0,
        nitrogenMin = 20.0,
        nitrogenMax = 40.0,
        phosphorusMin = 15.0,
        phosphorusMax = 30.0,
        potassiumMin = 30.0,
        potassiumMax = 50.0
    )

    @Test
    fun moistureAboveMaximumCreatesAlert() {
        val reading = Reading(
            id = 1,
            deviceId = 1,
            recordedAt = Instant.now(),
            moisturePercent = 72.0,
            soilTemperatureC = 24.0,
            nitrogenPpm = 30.0,
            phosphorusPpm = 20.0,
            potassiumPpm = 40.0,
            source = "SIMULATED"
        )

        val alerts = reading.evaluateThresholds(thresholds)

        assertEquals(1, alerts.size)
        assertEquals("Humedad", alerts.first().parameter)
        assertTrue(alerts.first().aboveMaximum)
        assertEquals(70.0, alerts.first().limit, 0.0)
    }

    @Test
    fun normalReadingDoesNotCreateAlert() {
        val reading = Reading(
            id = 2,
            deviceId = 1,
            recordedAt = Instant.now(),
            moisturePercent = 50.0,
            soilTemperatureC = 24.0,
            nitrogenPpm = 30.0,
            phosphorusPpm = 20.0,
            potassiumPpm = 40.0,
            source = "SIMULATED"
        )

        val alerts = reading.evaluateThresholds(thresholds)

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun nutrientsOutsideRangeCreateAlerts() {
        val reading = Reading(
            id = 3,
            deviceId = 1,
            recordedAt = Instant.now(),
            moisturePercent = 50.0,
            soilTemperatureC = 24.0,
            nitrogenPpm = 15.0,
            phosphorusPpm = 35.0,
            potassiumPpm = 55.0,
            source = "SIMULATED"
        )

        val alerts = reading.evaluateThresholds(thresholds)

        assertEquals(3, alerts.size)
    }
}
