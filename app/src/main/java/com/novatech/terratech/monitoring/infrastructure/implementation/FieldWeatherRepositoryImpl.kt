package com.novatech.terratech.monitoring.infrastructure.implementation

import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.monitoring.domain.entity.FieldWeather
import com.novatech.terratech.monitoring.domain.entity.WeatherUnavailableException
import com.novatech.terratech.monitoring.domain.repository.FieldWeatherRepository
import com.novatech.terratech.monitoring.infrastructure.mapper.domain
import com.novatech.terratech.monitoring.infrastructure.mapper.row
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FieldWeatherRepositoryImpl(
    private val api: MonitoringApi,
    private val db: TerraDatabase,
    private val cacheDurationMs: Long = 3 * 60 * 60 * 1000L,
) : FieldWeatherRepository {
    private val dao = db.dao()

    override fun weather(fieldId: Int): Flow<FieldWeather?> =
        dao.weather(fieldId).map { row -> row?.domain(isOffline = true) }

    override suspend fun getWeather(fieldId: Int, forceRefresh: Boolean): Result<FieldWeather> {
        val cached = dao.findWeather(fieldId)
        val now = System.currentTimeMillis()
        if (!forceRefresh && cached != null && (now - cached.downloadedAt) < cacheDurationMs) {
            return Result.success(cached.domain(isOffline = false))
        }

        return try {
            val response = api.getFieldWeather(fieldId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val row = body.row(now)
                dao.putWeather(row)
                Result.success(body.domain(downloadedAt = now, isOffline = false))
            } else if (response.code() == 502) {
                if (cached != null) {
                    Result.success(cached.domain(isOffline = true))
                } else {
                    Result.failure(WeatherUnavailableException("Servicio de clima temporalmente no disponible"))
                }
            } else {
                if (cached != null) {
                    Result.success(cached.domain(isOffline = true))
                } else {
                    Result.failure(Exception("Error al obtener clima: HTTP " + response.code()))
                }
            }
        } catch (exception: Exception) {
            if (cached != null) {
                Result.success(cached.domain(isOffline = true))
            } else {
                Result.failure(exception)
            }
        }
    }
}
