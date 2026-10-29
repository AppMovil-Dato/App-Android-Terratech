package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_weather")
data class FieldWeatherRow(
    @PrimaryKey val fieldId: Int,
    val fieldName: String,
    val forecastTimestamp: String,
    val currentTemperatureC: Double,
    val relativeHumidityPercent: Double,
    val windSpeedKmh: Double,
    val precipitationProbability24h: Double,
    val expectedPrecipitationMm: Double,
    val suspensionRecommended: Boolean,
    val estimatedWaterSavingsLiters: Int,
    val recommendationMessage: String,
    val downloadedAt: Long,
)
