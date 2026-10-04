package com.novatech.terratech.monitoring.domain.entity

import java.time.Instant

data class DownloadState(
  val downloadedAt: Instant?,
  val fromUtc: Instant?,
  val toUtc: Instant?,
  val minimumMoisture: Double?,
)
