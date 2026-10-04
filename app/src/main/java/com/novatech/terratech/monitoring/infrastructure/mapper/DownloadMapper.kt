package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.infrastructure.local.entity.DownloadRow
import java.time.Instant

fun DownloadRow.domain() =
  DownloadState(
    Instant.parse(downloadedAt),
    fromUtc?.let(Instant::parse),
    toUtc?.let(Instant::parse),
    minimumMoisture,
  )
