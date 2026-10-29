package com.novatech.terratech.monitoring.infrastructure.implementation

import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import com.novatech.terratech.monitoring.domain.entity.SatelliteNotFoundException
import com.novatech.terratech.monitoring.domain.repository.SatelliteImageRepository
import com.novatech.terratech.monitoring.infrastructure.mapper.domain
import com.novatech.terratech.monitoring.infrastructure.mapper.row
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SatelliteImageRepositoryImpl(
    private val api: MonitoringApi,
    private val db: TerraDatabase,
) : SatelliteImageRepository {
    private val dao = db.dao()

    override fun latestImage(fieldId: Int): Flow<SatelliteImage?> =
        dao.latestImage(fieldId).map { row -> row?.domain(isOffline = true) }

    override suspend fun getLatestImage(fieldId: Int, forceRefresh: Boolean): Result<SatelliteImage> {
        val cached = dao.findLatestImage(fieldId)
        val now = System.currentTimeMillis()

        return try {
            val response = api.getLatestSatelliteImage(fieldId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val row = body.row(now)
                dao.putImage(row)
                Result.success(body.domain(downloadedAt = now, isOffline = false))
            } else if (cached != null) {
                Result.success(cached.domain(isOffline = true))
            } else {
                Result.failure(Exception("Error al obtener imagen satelital: HTTP " + response.code()))
            }
        } catch (exception: Exception) {
            if (cached != null) {
                Result.success(cached.domain(isOffline = true))
            } else {
                Result.failure(exception)
            }
        }
    }

    override suspend fun getImageByDate(fieldId: Int, targetDate: String): Result<SatelliteImage> {
        val cached = dao.findImageByDate(fieldId, targetDate)
        val now = System.currentTimeMillis()

        return try {
            val response = api.getSatelliteImageByDate(fieldId, targetDate)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val row = body.row(now)
                dao.putImage(row)
                Result.success(body.domain(downloadedAt = now, isOffline = false))
            } else if (response.code() == 404) {
                if (cached != null) {
                    Result.success(cached.domain(isOffline = true))
                } else {
                    Result.failure(
                        SatelliteNotFoundException(
                            "No se encontraron imágenes satelitales para la fecha: " + targetDate,
                        ),
                    )
                }
            } else if (cached != null) {
                Result.success(cached.domain(isOffline = true))
            } else {
                Result.failure(Exception("Error al obtener imagen satelital: HTTP " + response.code()))
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
