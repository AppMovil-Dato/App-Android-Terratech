package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class FieldDto(
  val id: Int,
  val profileId: Int,
  val name: String,
  val sizeM2: Double,
  val soilType: String,
  val latitude: Double,
  val longitude: Double,
  val cropName: String?,
  val boundary: List<FieldVertexDto>? = null,
)
