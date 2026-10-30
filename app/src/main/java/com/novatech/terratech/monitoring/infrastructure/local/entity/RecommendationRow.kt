package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "recommendations",
    primaryKeys = ["userId", "deviceId", "readingId", "type"],
    indices = [Index(value = ["userId", "deviceId", "createdAt"])]
)
data class RecommendationRow(
    val userId: Int,
    val deviceId: Int,
    val readingId: Int,
    val type: String,
    val severity: String,
    val title: String,
    val message: String,
    val actionSuggested: String?,
    val createdAt: String,
)
