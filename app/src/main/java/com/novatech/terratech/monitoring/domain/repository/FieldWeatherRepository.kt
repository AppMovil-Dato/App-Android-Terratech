package com.novatech.terratech.monitoring.domain.repository

import com.novatech.terratech.monitoring.domain.entity.FieldWeather
import kotlinx.coroutines.flow.Flow

interface FieldWeatherRepository {
    fun weather(fieldId: Int): Flow<FieldWeather?>

    suspend fun getWeather(fieldId: Int, forceRefresh: Boolean = false): Result<FieldWeather>
}
