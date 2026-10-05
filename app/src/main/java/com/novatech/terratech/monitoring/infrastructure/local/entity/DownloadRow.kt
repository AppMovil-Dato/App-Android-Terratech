package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.Entity

@Entity(tableName = "downloads", primaryKeys = ["userId", "deviceId", "days"])
data class DownloadRow(
    val userId: Int,
    val deviceId: Int,
    val days: Int,
    val downloadedAt: String,
    val fromUtc: String?,
    val toUtc: String?,
    val minimumMoisture: Double?,
)
