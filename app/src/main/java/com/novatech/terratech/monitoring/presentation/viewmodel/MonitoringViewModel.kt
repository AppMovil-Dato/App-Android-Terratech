package com.novatech.terratech.monitoring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class MonitoringViewModel
@Inject
constructor(private val actions: MonitoringActions, private val account: AccountRepository) :
  ViewModel() {
  private val mutable = MutableStateFlow(MonitoringState())
  val state = mutable.asStateFlow()
  private var pendingRefresh = false
  private var savedSelection: Pair<Int?, Int?> = null to null
  private var sessionJob: Job? = null
  private var readingsJob: Job? = null
  private var downloadJob: Job? = null
  private var requestJob: Job? = null

  init {
    viewModelScope.launch {
      while (isActive) {
        delay(1000)
        mutable.update { it.copy(now = Instant.now()) }
      }
    }
    viewModelScope.launch {
      account.session.collect { s ->
        sessionJob?.cancel()
        readingsJob?.cancel()
        downloadJob?.cancel()
        requestJob?.cancel()
        mutable.value = MonitoringState(userId = s?.userId)
        if (s != null) {
          sessionJob = viewModelScope.launch {
            savedSelection = actions.selection(s.userId).first()
            launch {
              actions.fields(s.userId).collect { fields ->
                mutable.update { it.copy(fields = fields) }
                if (fields.isNotEmpty() && fields.none { it.id == mutable.value.fieldId })
                  selectField(
                    fields.find { it.id == savedSelection.first }?.id ?: fields.first().id
                  )
              }
            }
            launch {
              actions.sensors(s.userId).collect { sensors ->
                mutable.update { it.copy(sensors = sensors) }
                val candidates = mutable.value.fieldSensors
                if (candidates.isNotEmpty() && candidates.none { it.id == mutable.value.deviceId })
                  selectDevice(
                    candidates.find { it.id == savedSelection.second }?.id ?: candidates.first().id
                  )
              }
            }
          }
          refresh()
        }
      }
    }
  }

  fun clearMessage() {
    mutable.update { it.copy(error = null, created = false) }
  }

  fun selectField(id: Int) {
    if (mutable.value.fields.none { it.id == id }) return
    readingsJob?.cancel()
    downloadJob?.cancel()
    mutable.update {
      it.copy(fieldId = id, deviceId = null, readings = emptyList(), download = null)
    }
    persistSelection()
    val cached =
      mutable.value.fieldSensors.find { it.id == savedSelection.second }
        ?: mutable.value.fieldSensors.firstOrNull()
    if (cached != null) selectDevice(cached.id)
    refresh()
  }

  fun selectDevice(id: Int) {
    if (mutable.value.fieldSensors.none { it.id == id }) return
    mutable.update { it.copy(deviceId = id, readings = emptyList(), download = null) }
    persistSelection()
    observeReadings()
    refresh()
  }

  fun days(days: Int) {
    if (days !in listOf(7, 30)) return
    mutable.update { it.copy(days = days, download = null) }
    observeDownload()
    refresh()
  }

  private fun persistSelection() {
    val user = account.session.value?.userId ?: return
    val field = mutable.value.fieldId
    val device = mutable.value.deviceId
    viewModelScope.launch { actions.select(user, field, device) }
  }

  private fun observeReadings() {
    readingsJob?.cancel()
    val user = account.session.value?.userId ?: return
    val device = mutable.value.deviceId ?: return
    readingsJob = viewModelScope.launch {
      actions.readings(user, device).collect { rows -> mutable.update { it.copy(readings = rows) } }
    }
    observeDownload()
  }

  private fun observeDownload() {
    downloadJob?.cancel()
    val user = account.session.value?.userId ?: return
    val device = mutable.value.deviceId ?: return
    val days = mutable.value.days
    downloadJob = viewModelScope.launch {
      actions.downloadState(user, device, days).collect { row ->
        mutable.update { it.copy(download = row) }
      }
    }
  }

  fun refresh() {
    if (mutable.value.busy) {
      pendingRefresh = true
      return
    }
    request {
      actions.refreshFields(it)
      mutable.value.fieldId?.let { field -> actions.refreshSensors(it, field) }
      mutable.value.deviceId?.let { device ->
        actions.refreshReadings(it, device, mutable.value.days)
      }
    }
  }

  fun detail(id: Int) = request { user ->
    mutable.value.deviceId?.let { actions.refreshDetail(user, it, id) }
  }

  fun createField(
    profile: Int,
    name: String,
    crop: String,
    ha: Double,
    soil: String,
    lat: Double,
    lon: Double,
  ) = request {
    actions.createField(it, profile, name, crop, ha, soil, lat, lon)
    mutable.update { it.copy(created = true) }
  }

  fun registerSensor(code: String, name: String) = request { user ->
    val field = mutable.value.fieldId ?: throw Failure("FIELD_NOT_FOUND")
    actions.registerSensor(user, field, code, name)
    mutable.update { it.copy(created = true) }
  }

  private fun request(block: suspend (Int) -> Unit) {
    val user = account.session.value?.userId ?: return
    if (mutable.value.busy) return
    requestJob = viewModelScope.launch {
      mutable.update { it.copy(busy = true, error = null) }
      try {
        block(user)
        mutable.update { it.copy(offline = false) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        val code = (e as? Failure)?.code ?: "UNKNOWN_ERROR"
        mutable.update { it.copy(error = code, offline = code == "OFFLINE") }
      } finally {
        mutable.update { it.copy(busy = false) }
        if (pendingRefresh) {
          pendingRefresh = false
          refresh()
        }
      }
    }
  }
}
