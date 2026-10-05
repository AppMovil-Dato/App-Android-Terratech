package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.infrastructure.local.entity.ReadingRow
import com.novatech.terratech.monitoring.infrastructure.remote.dto.ReadingDto
import java.time.Instant

fun ReadingDto.row(user: Int) =
    ReadingRow(
        user,
        deviceId,
        id,
        recordedAt,
        moisturePercent,
        soilTemperatureC,
        nitrogenPpm,
        phosphorusPpm,
        potassiumPpm,
        source,
    )

fun ReadingRow.domain() =
    Reading(
        id,
        deviceId,
        Instant.parse(recordedAt),
        moisturePercent,
        soilTemperatureC,
        nitrogenPpm,
        phosphorusPpm,
        potassiumPpm,
        source,
    )
