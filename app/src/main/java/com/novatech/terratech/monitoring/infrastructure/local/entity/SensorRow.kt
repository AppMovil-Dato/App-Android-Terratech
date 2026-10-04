package com.novatech.terratech.monitoring.infrastructure.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
  tableName = "sensors",
  primaryKeys = ["userId", "id"],
  indices = [Index(value = ["userId", "fieldId"])],
)
data class SensorRow(
  val userId: Int,
  val id: Int,
  val fieldId: Int,
  val name: String?,
  val sensorCode: String?,
  val macAddress: String,
  val status: String,
)
