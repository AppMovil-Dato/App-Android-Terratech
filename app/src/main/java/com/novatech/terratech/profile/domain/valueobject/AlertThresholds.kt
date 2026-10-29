package com.novatech.terratech.profile.domain.valueobject

import com.novatech.terratech.core.domain.Failure

data class AlertThresholds(
    val moisture: Double,
    val temperature: Double,
) {
    companion object {
        const val DEFAULT_MOISTURE = 30.0
        const val DEFAULT_TEMPERATURE = 35.0

        fun of(moisture: Double, temperature: Double): AlertThresholds {
            if (!moisture.isFinite() || moisture !in 0.0..100.0) {
                throw Failure("INVALID_MOISTURE_THRESHOLD", 400)
            }
            if (!temperature.isFinite() || temperature !in 0.0..60.0) {
                throw Failure("INVALID_TEMP_THRESHOLD", 400)
            }
            return AlertThresholds(moisture, temperature)
        }
    }
}
