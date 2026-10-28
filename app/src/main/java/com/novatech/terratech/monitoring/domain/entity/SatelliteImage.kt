package com.novatech.terratech.monitoring.domain.entity

data class SatelliteImage(
    val fieldId: Int,
    val capturedAt: String,
    val imageUrl: String,
    val ndviAverage: Double,
    val cloudCoveragePercent: Double,
    val bounds: SatelliteBounds,
    val downloadedAt: Long = 0L,
    val isOffline: Boolean = false,
)
