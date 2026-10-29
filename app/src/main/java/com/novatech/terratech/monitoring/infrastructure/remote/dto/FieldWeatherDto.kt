package com.novatech.terratech.monitoring.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class FieldWeatherDto(
    @SerializedName("fieldId") val fieldId: Int,
    @SerializedName("fieldName") val fieldName: String,
    @SerializedName("forecastTimestamp") val forecastTimestamp: String,
    @SerializedName("currentTemperatureC") val currentTemperatureC: Double,
    @SerializedName("relativeHumidityPercent") val relativeHumidityPercent: Double,
    @SerializedName("windSpeedKmh") val windSpeedKmh: Double,
    @SerializedName("precipitationProbability24h") val precipitationProbability24h: Double,
    @SerializedName("expectedPrecipitationMm") val expectedPrecipitationMm: Double,
    @SerializedName("combinedRecommendation") val combinedRecommendation: CombinedRecommendationDto,
)
