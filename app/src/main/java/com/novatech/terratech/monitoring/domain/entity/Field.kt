package com.novatech.terratech.monitoring.domain.entity

data class Field(
  val id: Int,
  val profileId: Int,
  val name: String,
  val sizeM2: Double,
  val soilType: String,
  val latitude: Double,
  val longitude: Double,
  val cropName: String?,
  val boundary: List<com.novatech.terratech.monitoring.domain.valueobject.Coordinates> =
    emptyList(),
)
