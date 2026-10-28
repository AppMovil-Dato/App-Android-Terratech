
package com.novatech.terratech.monitoring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class MonitoringViewModel
@Inject
constructor(
    private val actions: MonitoringActions,
    private val account: AccountRepository
) : ViewModel() {

    private val mutable = MutableStateFlow(MonitoringState())
    val state = mutable.asStateFlow()

    private var pendingRefresh = false
    private var savedSelection: Pair<Int?, Int?> = null to null

    private var sessionJob: Job? = null
    private var readingsJob: Job? = null
    private var downloadJob: Job? = null
    private var requestJob: Job? = null

    // US13: consulta de zonas de fertilidad
    private var fertilityZonesJob: Job? = null

    // US18: operaciones de umbrales
    private var thresholdsJob: Job? = null

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                mutable.update {
                    it.copy(now = Instant.now())
                }
            }
        }

        viewModelScope.launch {
            account.session.collect { s ->
                sessionJob?.cancel()
                readingsJob?.cancel()
                downloadJob?.cancel()
                requestJob?.cancel()
                fertilityZonesJob?.cancel()
                thresholdsJob?.cancel()

                mutable.value = MonitoringState(userId = s?.userId)

                if (s != null) {
                    sessionJob =
                        viewModelScope.launch {
                            savedSelection =
                                actions.selection(s.userId).first()

                            launch {
                                actions.fields(s.userId).collect { fields ->
                                    mutable.update {
                                        it.copy(fields = fields)
                                    }

                                    if (
                                        fields.isNotEmpty() &&
                                        fields.none {
                                            it.id == mutable.value.fieldId
                                        }
                                    ) {
                                        selectField(
                                            fields.find {
                                                it.id == savedSelection.first
                                            }?.id ?: fields.first().id
                                        )
                                    }
                                }
                            }

                            launch {
                                actions.sensors(s.userId).collect { sensors ->
                                    mutable.update {
                                        it.copy(sensors = sensors)
                                    }

                                    val candidates =
                                        mutable.value.fieldSensors

                                    if (
                                        candidates.isNotEmpty() &&
                                        candidates.none {
                                            it.id == mutable.value.deviceId
                                        }
                                    ) {
                                        selectDevice(
                                            candidates.find {
                                                it.id == savedSelection.second
                                            }?.id ?: candidates.first().id
                                        )
                                    }
                                }
                            }
                        }

                    refresh()
                }
            }
        }
    }

    fun clearMessage() {
        mutable.update {
            it.copy(
                error = null,
                created = false,
                createdFieldId = null,
                createdSensorId = null
            )
        }
    }

    fun selectField(id: Int) {
        if (mutable.value.fields.none { it.id == id }) return

        readingsJob?.cancel()
        downloadJob?.cancel()
        fertilityZonesJob?.cancel()
        thresholdsJob?.cancel()

        mutable.update {
            it.copy(
                fieldId = id,
                deviceId = null,
                readings = emptyList(),
                download = null,
                fertilityZones = emptyList(),
                fertilityZonesError = null,
                thresholdZoneId = null,
                zoneThresholds = null,
                thresholdsBusy = false,
                thresholdsError = null,
                thresholdsSaved = false
            )
        }

        persistSelection()

        val cached =
            mutable.value.fieldSensors.find {
                it.id == savedSelection.second
            } ?: mutable.value.fieldSensors.firstOrNull()

        if (cached != null) {
            selectDevice(cached.id)
        }

        refresh()
    }

    fun selectDevice(id: Int) {
        if (mutable.value.fieldSensors.none { it.id == id }) return

        mutable.update {
            it.copy(
                deviceId = id,
                readings = emptyList(),
                download = null
            )
        }

        persistSelection()
        observeReadings()
        refresh()
    }

    fun selectSensor(deviceId: Int) {
        val sensor =
            mutable.value.sensors.find {
                it.id == deviceId
            } ?: return

        if (mutable.value.fieldId != sensor.fieldId) {
            selectField(sensor.fieldId)
        }

        if (mutable.value.deviceId != deviceId) {
            selectDevice(deviceId)
        }
    }

    fun createField(
        profileId: Int,
        draft: FieldDraft
    ) = createField(
        profileId,
        draft.name.value,
        draft.crop,
        draft.area.value / 10000,
        draft.soil,
        draft.coordinates.latitude,
        draft.coordinates.longitude,
        draft.boundary
    )

    fun days(days: Int) {
        if (days !in listOf(7, 30)) return

        mutable.update {
            it.copy(
                days = days,
                download = null
            )
        }

        observeDownload()
        refresh()
    }

    private fun persistSelection() {
        val user = account.session.value?.userId ?: return
        val field = mutable.value.fieldId
        val device = mutable.value.deviceId

        viewModelScope.launch {
            actions.select(user, field, device)
        }
    }

    private fun observeReadings() {
        readingsJob?.cancel()

        val user = account.session.value?.userId ?: return
        val device = mutable.value.deviceId ?: return

        readingsJob =
            viewModelScope.launch {
                actions.readings(user, device).collect { rows ->
                    mutable.update {
                        it.copy(readings = rows)
                    }
                }
            }

        observeDownload()
    }

    private fun observeDownload() {
        downloadJob?.cancel()

        val user = account.session.value?.userId ?: return
        val device = mutable.value.deviceId ?: return
        val days = mutable.value.days

        downloadJob =
            viewModelScope.launch {
                actions.downloadState(
                    user,
                    device,
                    days
                ).collect { row ->
                    mutable.update {
                        it.copy(download = row)
                    }
                }
            }
    }

    // US13: obtener las zonas desde el backend
    private fun refreshFertilityZones() {
        fertilityZonesJob?.cancel()

        val user = account.session.value?.userId ?: return
        val field = mutable.value.fieldId ?: return

        fertilityZonesJob =
            viewModelScope.launch {
                try {
                    val zones =
                        actions.fertilityZones(user, field)

                    mutable.update { current ->
                        if (
                            current.userId == user &&
                            current.fieldId == field
                        ) {
                            current.copy(
                                fertilityZones = zones,
                                fertilityZonesError = null
                            )
                        } else {
                            current
                        }
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    val code =
                        (exception as? Failure)?.code
                            ?: "UNKNOWN_ERROR"

                    mutable.update { current ->
                        if (
                            current.userId == user &&
                            current.fieldId == field
                        ) {
                            current.copy(
                                fertilityZones = emptyList(),
                                fertilityZonesError = code
                            )
                        } else {
                            current
                        }
                    }
                }
            }
    }

    // US18: consultar umbrales de una zona
    fun loadZoneThresholds(zoneId: Int) {
        val user = account.session.value?.userId ?: return
        val field = mutable.value.fieldId ?: return

        if (
            mutable.value.fertilityZones.none {
                it.id == zoneId && it.fieldId == field
            }
        ) return

        thresholdsJob?.cancel()

        mutable.update {
            it.copy(
                thresholdZoneId = zoneId,
                zoneThresholds = null,
                thresholdsBusy = true,
                thresholdsError = null,
                thresholdsSaved = false
            )
        }

        thresholdsJob = viewModelScope.launch {
            try {
                val thresholds = actions.fertilityZoneThresholds(
                    user,
                    field,
                    zoneId
                )

                mutable.update { current ->
                    if (
                        current.userId == user &&
                        current.fieldId == field &&
                        current.thresholdZoneId == zoneId
                    ) {
                        current.copy(
                            zoneThresholds = thresholds,
                            thresholdsBusy = false,
                            thresholdsError = null
                        )
                    } else {
                        current
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val code =
                    (exception as? Failure)?.code
                        ?: "UNKNOWN_ERROR"

                mutable.update { current ->
                    if (
                        current.userId == user &&
                        current.fieldId == field &&
                        current.thresholdZoneId == zoneId
                    ) {
                        current.copy(
                            thresholdsBusy = false,
                            thresholdsError = code
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    // US18: guardar los umbrales editados
    fun saveZoneThresholds(thresholds: FertilityZoneThresholds) {
        val user = account.session.value?.userId ?: return
        val field = mutable.value.fieldId ?: return
        val zoneId = mutable.value.thresholdZoneId ?: return

        if (thresholds.zoneId != zoneId) return

        thresholdsJob?.cancel()

        mutable.update {
            it.copy(
                thresholdsBusy = true,
                thresholdsError = null,
                thresholdsSaved = false
            )
        }

        thresholdsJob = viewModelScope.launch {
            try {
                val saved = actions.updateFertilityZoneThresholds(
                    user,
                    field,
                    thresholds
                )

                mutable.update { current ->
                    if (
                        current.userId == user &&
                        current.fieldId == field &&
                        current.thresholdZoneId == zoneId
                    ) {
                        current.copy(
                            zoneThresholds = saved,
                            thresholdsBusy = false,
                            thresholdsError = null,
                            thresholdsSaved = true
                        )
                    } else {
                        current
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val code =
                    (exception as? Failure)?.code
                        ?: "UNKNOWN_ERROR"

                mutable.update { current ->
                    if (
                        current.userId == user &&
                        current.fieldId == field &&
                        current.thresholdZoneId == zoneId
                    ) {
                        current.copy(
                            thresholdsBusy = false,
                            thresholdsError = code
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    // US18: restablecer los valores predeterminados
    fun resetZoneThresholds() {
        val user = account.session.value?.userId ?: return
        val field = mutable.value.fieldId ?: return
        val zoneId = mutable.value.thresholdZoneId ?: return

        thresholdsJob?.cancel()

        mutable.update {
            it.copy(
                thresholdsBusy = true,
                thresholdsError = null,
                thresholdsSaved = false
            )
        }

        thresholdsJob = viewModelScope.launch {
            try {
                actions.resetFertilityZoneThresholds(
                    user,
                    field,
                    zoneId
                )

                val defaults = actions.fertilityZoneThresholds(
                    user,
                    field,
                    zoneId
                )

                mutable.update { current ->
                    if (
                        current.userId == user &&
                        current.fieldId == field &&
                        current.thresholdZoneId == zoneId
                    ) {
                        current.copy(
                            zoneThresholds = defaults,
                            thresholdsBusy = false,
                            thresholdsError = null,
                            thresholdsSaved = true
                        )
                    } else {
                        current
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val code =
                    (exception as? Failure)?.code
                        ?: "UNKNOWN_ERROR"

                mutable.update { current ->
                    if (
                        current.userId == user &&
                        current.fieldId == field &&
                        current.thresholdZoneId == zoneId
                    ) {
                        current.copy(
                            thresholdsBusy = false,
                            thresholdsError = code
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    // US18: cerrar la configuracion de la zona
    fun closeZoneThresholds() {
        thresholdsJob?.cancel()

        mutable.update {
            it.copy(
                thresholdZoneId = null,
                zoneThresholds = null,
                thresholdsBusy = false,
                thresholdsError = null,
                thresholdsSaved = false
            )
        }
    }

    fun refresh() {
        // US13: actualizar las zonas de la parcela
        refreshFertilityZones()

        if (mutable.value.busy) {
            pendingRefresh = true
            return
        }

        request {
            actions.refreshFields(it)

            mutable.value.fieldId?.let { field ->
                actions.refreshSensors(it, field)
            }

            mutable.value.deviceId?.let { device ->
                actions.refreshReadings(
                    it,
                    device,
                    mutable.value.days
                )
            }
        }
    }

    fun detail(id: Int) = request { user ->
        mutable.value.deviceId?.let {
            actions.refreshDetail(user, it, id)
        }
    }

    fun createField(
        profile: Int,
        name: String,
        crop: String,
        ha: Double,
        soil: String,
        lat: Double,
        lon: Double,
        boundary: List<Coordinates> = emptyList()
    ) = request { user ->

        readingsJob?.cancel()
        downloadJob?.cancel()
        fertilityZonesJob?.cancel()
        thresholdsJob?.cancel()

        val field = actions.createField(
            user,
            profile,
            name,
            crop,
            ha,
            soil,
            lat,
            lon,
            boundary
        )

        mutable.update {
            it.copy(
                fields = (
                    it.fields.filterNot { old -> old.id == field.id } + field
                    ),
                fieldId = field.id,
                deviceId = null,
                readings = emptyList(),
                download = null,
                fertilityZones = emptyList(),
                fertilityZonesError = null,
                thresholdZoneId = null,
                zoneThresholds = null,
                thresholdsBusy = false,
                thresholdsError = null,
                thresholdsSaved = false,
                created = true,
                createdFieldId = field.id
            )
        }

        persistSelection()
        refreshFertilityZones()
    }

    fun registerSensor(
        code: String,
        name: String
    ) = request { user ->

        val field =
            mutable.value.fieldId
                ?: throw Failure("FIELD_NOT_FOUND")

        val sensor = actions.registerSensor(
            user,
            field,
            code,
            name
        )

        mutable.update {
            it.copy(
                sensors = (
                    it.sensors.filterNot { old -> old.id == sensor.id } + sensor
                    ),
                deviceId = sensor.id,
                created = true,
                createdSensorId = sensor.id
            )
        }

        persistSelection()
        observeReadings()
        pendingRefresh = true
    }

    private fun request(
        block: suspend (Int) -> Unit
    ) {
        val user = account.session.value?.userId ?: return

        if (mutable.value.busy) return

        requestJob =
            viewModelScope.launch {
                mutable.update {
                    it.copy(
                        busy = true,
                        error = null
                    )
                }

                try {
                    block(user)

                    mutable.update {
                        it.copy(offline = false)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    val code =
                        (exception as? Failure)?.code
                            ?: "UNKNOWN_ERROR"

                    mutable.update {
                        it.copy(
                            error = code,
                            offline = code == "OFFLINE"
                        )
                    }
                } finally {
                    mutable.update {
                        it.copy(busy = false)
                    }

                    if (pendingRefresh) {
                        pendingRefresh = false
                        refresh()
                    }
                }
            }
    }
}
