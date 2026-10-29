package com.novatech.terratech.monitoring.domain.repository

import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import kotlinx.coroutines.flow.Flow

interface SatelliteImageRepository {
    fun latestImage(fieldId: Int): Flow<SatelliteImage?>

    suspend fun getLatestImage(fieldId: Int, forceRefresh: Boolean = false): Result<SatelliteImage>

    suspend fun getImageByDate(fieldId: Int, targetDate: String): Result<SatelliteImage>
}
