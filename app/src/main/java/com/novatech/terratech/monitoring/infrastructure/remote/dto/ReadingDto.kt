package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class ReadingDto(
    val id: Int,
    val deviceId: Int,
    val recordedAt: String,
    val moisturePercent: Double,
    val soilTemperatureC: Double,
    val nitrogenPpm: Double,
    val phosphorusPpm: Double,
    val potassiumPpm: Double,
    val source: String,
)
