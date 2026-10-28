
package com.novatech.terratech.monitoring.domain.valueobject

enum class MoistureLevel {
    CRITICAL,
    MODERATE,
    OPTIMAL,
}

fun classifyMoisture(moisturePercent: Double): MoistureLevel? {
    if (!moisturePercent.isFinite()) {
        return null
    }

    if (moisturePercent < 0.0 || moisturePercent > 100.0) {
        return null
    }

    return when {
        moisturePercent < 30.0 -> MoistureLevel.CRITICAL
        moisturePercent <= 60.0 -> MoistureLevel.MODERATE
        else -> MoistureLevel.OPTIMAL
    }
}
