package com.novatech.terratech.monitoring.application.usecase

import com.novatech.terratech.monitoring.domain.entity.FieldWeather
import com.novatech.terratech.monitoring.domain.repository.FieldWeatherRepository
import kotlinx.coroutines.flow.Flow

class GetFieldWeather(
    private val repository: FieldWeatherRepository,
) {
    fun observe(fieldId: Int): Flow<FieldWeather?> = repository.weather(fieldId)

    suspend operator fun invoke(fieldId: Int, forceRefresh: Boolean = false): Result<FieldWeather> =
        repository.getWeather(fieldId, forceRefresh)
}
