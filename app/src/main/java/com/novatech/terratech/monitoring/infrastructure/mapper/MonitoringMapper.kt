package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.core.infrastructure.local.*
import com.novatech.terratech.core.infrastructure.remote.*
import com.novatech.terratech.monitoring.domain.entity.*
import java.time.Instant

fun FieldDto.row(user: Int) =
  FieldRow(user, id, profileId, name, sizeM2, soilType, latitude, longitude, cropName)

fun FieldRow.domain() = Field(id, profileId, name, sizeM2, soilType, latitude, longitude, cropName)

fun SensorDto.row(user: Int) = SensorRow(user, id, fieldId, name, sensorCode, macAddress, status)

fun SensorRow.domain() = Sensor(id, fieldId, name, sensorCode, macAddress, status)

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

fun DownloadRow.domain() =
  DownloadState(
    Instant.parse(downloadedAt),
    fromUtc?.let(Instant::parse),
    toUtc?.let(Instant::parse),
    minimumMoisture,
  )
