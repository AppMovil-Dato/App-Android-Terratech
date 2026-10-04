package com.novatech.terratech.profile.infrastructure.local.entity

import androidx.room.Entity

@Entity(tableName = "profiles", primaryKeys = ["userId"])
data class ProfileRow(
  val userId: Int,
  val id: Int,
  val fullName: String,
  val email: String,
  val fundoName: String,
  val contactPhone: String,
  val location: String,
  val sizeM2: Double?,
  val minimumMoisture: Double,
)
