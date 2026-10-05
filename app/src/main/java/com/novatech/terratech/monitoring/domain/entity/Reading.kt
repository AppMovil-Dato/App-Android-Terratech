package com.novatech.terratech.monitoring.domain.entity

import java.time.Instant

data class Reading(
    val id: Int,
    val deviceId: Int,
    val recordedAt: Instant,
    val moisturePercent: Double,
    val soilTemperatureC: Double,
    val nitrogenPpm: Double,
    val phosphorusPpm: Double,
    val potassiumPpm: Double,
    val source: String,
) {
    fun isStale(now: Instant) =
        java.time.Duration.between(recordedAt, now) > java.time.Duration.ofMinutes(30)

    fun inRange(days: Int, now: Instant): Boolean {
        require(days == 7 || days == 30)
        return !recordedAt.isBefore(now.minusSeconds(days * 86400L)) && !recordedAt.isAfter(now)
    }
}
