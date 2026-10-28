package com.novatech.terratech.monitoring.application.usecase

import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import com.novatech.terratech.monitoring.domain.repository.SatelliteImageRepository
import kotlinx.coroutines.flow.Flow

class GetSatelliteImage(
    private val repository: SatelliteImageRepository,
) {
    fun observeLatest(fieldId: Int): Flow<SatelliteImage?> = repository.latestImage(fieldId)

    suspend fun latest(fieldId: Int, forceRefresh: Boolean = false): Result<SatelliteImage> =
        repository.getLatestImage(fieldId, forceRefresh)

    suspend fun byDate(fieldId: Int, targetDate: String): Result<SatelliteImage> =
        repository.getImageByDate(fieldId, targetDate)
}
