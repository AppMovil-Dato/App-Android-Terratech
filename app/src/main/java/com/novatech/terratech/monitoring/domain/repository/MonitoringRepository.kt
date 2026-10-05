package com.novatech.terratech.monitoring.domain.repository

import com.novatech.terratech.monitoring.domain.entity.*
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
  fun selection(user: Int): Flow<Pair<Int?, Int?>>

  suspend fun select(user: Int, field: Int?, device: Int?)

  fun fields(user: Int): Flow<List<Field>>

  fun sensors(user: Int): Flow<List<Sensor>>

  fun readings(user: Int, device: Int): Flow<List<Reading>>

  fun downloadState(user: Int, device: Int, days: Int): Flow<DownloadState?>

  suspend fun refreshFields(user: Int)

  suspend fun refreshSensors(user: Int, field: Int)

  suspend fun createField(
    user: Int,
    profile: Int,
    name: String,
    crop: String,
    area: Double,
    soil: String,
    latitude: Double,
    longitude: Double,
    boundary: List<com.novatech.terratech.monitoring.domain.valueobject.Coordinates> = emptyList(),
  ): Field

  suspend fun registerSensor(user: Int, field: Int, code: String, name: String): Sensor

  suspend fun refreshReadings(user: Int, device: Int, days: Int)

  suspend fun refreshDetail(user: Int, device: Int, reading: Int)
}
