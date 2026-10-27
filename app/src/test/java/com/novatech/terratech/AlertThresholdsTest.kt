package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.profile.domain.valueobject.AlertThresholds
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AlertThresholdsTest {
    private fun invalid(code: String, block: () -> Unit) {
        try {
            block()
            fail("Expected validation failure for code: $code")
        } catch (exception: Failure) {
            assertEquals(code, exception.code)
        }
    }

    @Test
    fun validThresholdsAreAccepted() {
        val thresholds = AlertThresholds.of(35.0, 32.0)
        assertEquals(35.0, thresholds.moisture, 0.0)
        assertEquals(32.0, thresholds.temperature, 0.0)
    }

    @Test
    fun boundaryThresholdsAreAccepted() {
        val min = AlertThresholds.of(0.0, 0.0)
        assertEquals(0.0, min.moisture, 0.0)
        assertEquals(0.0, min.temperature, 0.0)

        val max = AlertThresholds.of(100.0, 60.0)
        assertEquals(100.0, max.moisture, 0.0)
        assertEquals(60.0, max.temperature, 0.0)
    }

    @Test
    fun invalidMoistureFails() {
        invalid("INVALID_MOISTURE_THRESHOLD") { AlertThresholds.of(-0.1, 30.0) }
        invalid("INVALID_MOISTURE_THRESHOLD") { AlertThresholds.of(100.1, 30.0) }
        invalid("INVALID_MOISTURE_THRESHOLD") { AlertThresholds.of(Double.NaN, 30.0) }
        invalid("INVALID_MOISTURE_THRESHOLD") { AlertThresholds.of(Double.POSITIVE_INFINITY, 30.0) }
    }

    @Test
    fun invalidTemperatureFails() {
        invalid("INVALID_TEMP_THRESHOLD") { AlertThresholds.of(30.0, -1.0) }
        invalid("INVALID_TEMP_THRESHOLD") { AlertThresholds.of(30.0, 60.1) }
        invalid("INVALID_TEMP_THRESHOLD") { AlertThresholds.of(30.0, Double.NaN) }
        invalid("INVALID_TEMP_THRESHOLD") { AlertThresholds.of(30.0, Double.NEGATIVE_INFINITY) }
    }

    @Test
    fun defaultThresholdConstantsAreStandard() {
        assertEquals(30.0, AlertThresholds.DEFAULT_MOISTURE, 0.0)
        assertEquals(35.0, AlertThresholds.DEFAULT_TEMPERATURE, 0.0)
    }
}
