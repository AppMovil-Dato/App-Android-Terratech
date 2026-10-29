package com.novatech.terratech.monitoring.domain.entity

data class FieldWeather(
    val fieldId: Int,
    val fieldName: String,
    val forecastTimestamp: String,
    val currentTemperatureC: Double,
    val relativeHumidityPercent: Double,
    val windSpeedKmh: Double,
    val precipitationProbability24h: Double,
    val expectedPrecipitationMm: Double,
    val combinedRecommendation: CombinedRecommendation,
    val downloadedAt: Long = 0L,
    val isOffline: Boolean = false,
)
