package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class HistoryDto(
  val deviceId: Int,
  val fromUtc: String,
  val toUtc: String,
  val minimumMoisturePercent: Double,
  val readings: List<ReadingDto>,
)
