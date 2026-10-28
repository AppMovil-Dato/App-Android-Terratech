package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.Entity

@Entity(tableName = "satellite_images", primaryKeys = ["fieldId", "capturedAt"])
data class SatelliteImageRow(
    val fieldId: Int,
    val capturedAt: String,
    val imageUrl: String,
    val ndviAverage: Double,
    val cloudCoveragePercent: Double,
    val boundsNorth: Double,
    val boundsSouth: Double,
    val boundsEast: Double,
    val boundsWest: Double,
    val downloadedAt: Long,
)
