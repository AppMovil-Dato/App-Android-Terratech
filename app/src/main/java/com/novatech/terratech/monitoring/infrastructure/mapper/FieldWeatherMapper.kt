package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.CombinedRecommendation
import com.novatech.terratech.monitoring.domain.entity.FieldWeather
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldWeatherRow
import com.novatech.terratech.monitoring.infrastructure.remote.dto.CombinedRecommendationDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldWeatherDto

fun FieldWeatherDto.row(downloadedAt: Long): FieldWeatherRow =
    FieldWeatherRow(
        fieldId = fieldId,
        fieldName = fieldName,
        forecastTimestamp = forecastTimestamp,
        currentTemperatureC = currentTemperatureC,
        relativeHumidityPercent = relativeHumidityPercent,
        windSpeedKmh = windSpeedKmh,
        precipitationProbability24h = precipitationProbability24h,
        expectedPrecipitationMm = expectedPrecipitationMm,
        suspensionRecommended = combinedRecommendation.suspensionRecommended,
        estimatedWaterSavingsLiters = combinedRecommendation.estimatedWaterSavingsLiters,
        recommendationMessage = combinedRecommendation.message,
        downloadedAt = downloadedAt,
    )

fun FieldWeatherDto.domain(downloadedAt: Long = 0L, isOffline: Boolean = false): FieldWeather =
    FieldWeather(
        fieldId = fieldId,
        fieldName = fieldName,
        forecastTimestamp = forecastTimestamp,
        currentTemperatureC = currentTemperatureC,
        relativeHumidityPercent = relativeHumidityPercent,
        windSpeedKmh = windSpeedKmh,
        precipitationProbability24h = precipitationProbability24h,
        expectedPrecipitationMm = expectedPrecipitationMm,
        combinedRecommendation = combinedRecommendation.domain(),
        downloadedAt = downloadedAt,
        isOffline = isOffline,
    )

fun CombinedRecommendationDto.domain(): CombinedRecommendation =
    CombinedRecommendation(
        suspensionRecommended = suspensionRecommended,
        estimatedWaterSavingsLiters = estimatedWaterSavingsLiters,
        message = message,
    )

fun FieldWeatherRow.domain(isOffline: Boolean = true): FieldWeather =
    FieldWeather(
        fieldId = fieldId,
        fieldName = fieldName,
        forecastTimestamp = forecastTimestamp,
        currentTemperatureC = currentTemperatureC,
        relativeHumidityPercent = relativeHumidityPercent,
        windSpeedKmh = windSpeedKmh,
        precipitationProbability24h = precipitationProbability24h,
        expectedPrecipitationMm = expectedPrecipitationMm,
        combinedRecommendation =
            CombinedRecommendation(
                suspensionRecommended = suspensionRecommended,
                estimatedWaterSavingsLiters = estimatedWaterSavingsLiters,
                message = recommendationMessage,
            ),
        downloadedAt = downloadedAt,
        isOffline = isOffline,
    )
