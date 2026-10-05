package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "readings",
    primaryKeys = ["userId", "deviceId", "id"],
    indices = [Index(value = ["userId", "deviceId", "recordedAt"])],
)
data class ReadingRow(
    val userId: Int,
    val deviceId: Int,
    val id: Int,
    val recordedAt: String,
    val moisturePercent: Double,
    val soilTemperatureC: Double,
    val nitrogenPpm: Double,
    val phosphorusPpm: Double,
    val potassiumPpm: Double,
    val source: String,
)
