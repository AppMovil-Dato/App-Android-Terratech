package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

class FakeMonitoringRepository : MonitoringRepository {
  val plots = MutableStateFlow(listOf(Field(1, 1, "North", 5000.0, "Loam", -12.0, -77.0, "Potato")))
  val devices =
    MutableStateFlow(
      listOf(Sensor(11, 1, "North sensor", "TT-ZZZ001", "02:00:00:00:00:01", "OFFLINE"))
    )
  val samples =
    MutableStateFlow(
      listOf(Reading(9, 11, Instant.now(), 42.0, 23.0, 35.0, 18.0, 60.0, "SIMULATED"))
    )
  var offline = false
  var registerCalls = 0
  var occupied = false
  var selected: Pair<Int?, Int?> = null to null

  private fun connected() {
    if (offline) throw Failure("OFFLINE")
  }

  override fun selection(user: Int) = flowOf(selected)

  override suspend fun select(user: Int, field: Int?, device: Int?) {
    selected = field to device
  }

  override fun fields(user: Int) = if (user == 1) plots else flowOf(emptyList())

  override fun sensors(user: Int) = if (user == 1) devices else flowOf(emptyList())

  override fun readings(user: Int, device: Int) =
    if (user == 1 && device == 11) samples else flowOf(emptyList())

  override fun downloadState(user: Int, device: Int, days: Int) =
    flowOf(DownloadState(Instant.now(), null, null, 30.0))

  override suspend fun refreshFields(user: Int) {
    connected()
  }

  override suspend fun refreshSensors(user: Int, field: Int) {
    connected()
  }

  override suspend fun refreshReadings(user: Int, device: Int, days: Int) {
    connected()
  }

  override suspend fun refreshDetail(user: Int, device: Int, reading: Int) {
    connected()
  }

  override suspend fun createField(
    user: Int,
    profile: Int,
    name: String,
    crop: String,
    area: Double,
    soil: String,
    latitude: Double,
    longitude: Double,
  ) {
    connected()
    plots.value = plots.value + Field(2, profile, name, area, soil, latitude, longitude, crop)
  }

  override suspend fun registerSensor(user: Int, field: Int, code: String, name: String) {
    connected()
    registerCalls++
    if (occupied) throw Failure("SENSOR_OCCUPIED", 409)
  }
}
