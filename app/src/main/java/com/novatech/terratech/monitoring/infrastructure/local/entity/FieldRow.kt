package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "fields", primaryKeys = ["userId", "id"])
data class FieldRow(
    val userId: Int,
    val id: Int,
    val profileId: Int,
    val name: String,
    val sizeM2: Double,
    val soilType: String,
    val latitude: Double,
    val longitude: Double,
    val cropName: String?,
    @ColumnInfo(defaultValue = "'[]'") val boundaryJson: String = "[]",
)
