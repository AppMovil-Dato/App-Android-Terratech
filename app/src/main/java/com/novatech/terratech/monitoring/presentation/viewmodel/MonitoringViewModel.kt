package com.novatech.terratech.monitoring.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.monitoring.application.usecase.CompareVegetationPeriod
import com.novatech.terratech.monitoring.application.usecase.GetFieldWeather
import com.novatech.terratech.monitoring.application.usecase.GetSatelliteImage
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.none
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class MonitoringViewModel
@Inject
constructor(
    private val actions: MonitoringActions,
    private val account: AccountRepository,
    private val getFieldWeather: GetFieldWeather? = null,
    private val getSatelliteImage: GetSatelliteImage? = null,
    private val compareVegetationPeriod: CompareVegetationPeriod? = null,
) : ViewModel() {
    private val mutable = MutableStateFlow(MonitoringState())
    val state = mutable.asStateFlow()

    private var pendingRefresh = false
    private var recommendationsJob: Job? = null
    private var savedSelection: Pair<Int?, Int?> = null to null

    private var sessionJob: Job? = null
    private var readingsJob: Job? = null
    private var downloadJob: Job? = null
    private var requestJob: Job? = null

    private var fertilityZonesJob: Job? = null

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
                recommendationsJob?.cancel()
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
                createdSensorId = null,
                weatherError = null,
                satelliteError = null,
                comparisonError = null,
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

        loadWeather(id)
        loadLatestSatellite(id)
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
        observeRecommendations()
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

    private fun observeRecommendations() {
        recommendationsJob?.cancel()
        val user = account.session.value?.userId ?: return
        val device = mutable.value.deviceId ?: return
        recommendationsJob =
            viewModelScope.launch {
                actions.recommendations(user, device).collect { rows ->
                    mutable.update { it.copy(recommendations = rows) }
                }
            }
    }

    fun refresh() {
        refreshFertilityZones()

        if (mutable.value.busy) {
            pendingRefresh = true
            return
        }
        val currentField = mutable.value.fieldId
        if (currentField != null) {
            loadWeather(currentField)
            loadLatestSatellite(currentField)
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

    fun refreshWeather() {
        val fieldId = mutable.value.fieldId ?: return
        val weatherUseCase = getFieldWeather ?: return
        viewModelScope.launch {
            mutable.update { it.copy(weatherBusy = true, weatherError = null) }
            val result = weatherUseCase(fieldId, forceRefresh = true)
            result
                .onSuccess { weather ->
                    mutable.update { it.copy(weather = weather, weatherBusy = false) }
                }
                .onFailure { error ->
                    mutable.update { it.copy(weatherBusy = false, weatherError = error.message) }
                }
        }
    }

    fun loadWeather(fieldId: Int) {
        val weatherUseCase = getFieldWeather ?: return
        viewModelScope.launch {
            mutable.update { it.copy(weatherBusy = true, weatherError = null) }
            val result = weatherUseCase(fieldId, forceRefresh = false)
            result
                .onSuccess { weather ->
                    mutable.update { it.copy(weather = weather, weatherBusy = false) }
                }
                .onFailure { error ->
                    mutable.update { it.copy(weatherBusy = false, weatherError = error.message) }
                }
        }
    }

    fun loadLatestSatellite(fieldId: Int) {
        val satelliteUseCase = getSatelliteImage ?: return
        viewModelScope.launch {
            mutable.update { it.copy(satelliteBusy = true, satelliteError = null) }
            val result = satelliteUseCase.latest(fieldId, forceRefresh = false)
            result
                .onSuccess { image ->
                    mutable.update { it.copy(satelliteImage = image, satelliteBusy = false) }
                }
                .onFailure { error ->
                    mutable.update { it.copy(satelliteBusy = false, satelliteError = error.message) }
                }
        }
    }

    fun loadSatelliteByDate(date: String) {
        val fieldId = mutable.value.fieldId ?: return
        val satelliteUseCase = getSatelliteImage ?: return
        viewModelScope.launch {
            mutable.update { it.copy(satelliteBusy = true, satelliteError = null) }
            val result = satelliteUseCase.byDate(fieldId, date)
            result
                .onSuccess { image ->
                    mutable.update { it.copy(satelliteImage = image, satelliteBusy = false) }
                }
                .onFailure { error ->
                    mutable.update { it.copy(satelliteBusy = false, satelliteError = error.message) }
                }
        }
    }

    fun toggleSatelliteMode() {
        mutable.update { it.copy(isSatelliteView = !it.isSatelliteView) }
    }

    fun compareVegetation(dateA: String, dateB: String) {
        val fieldId = mutable.value.fieldId ?: return
        val satelliteUseCase = getSatelliteImage ?: return
        val comparisonUseCase = compareVegetationPeriod ?: return
        viewModelScope.launch {
            mutable.update { it.copy(comparisonBusy = true, comparisonError = null) }
            val resA = satelliteUseCase.byDate(fieldId, dateA)
            val resB = satelliteUseCase.byDate(fieldId, dateB)
            if (resA.isSuccess && resB.isSuccess) {
                val comparison = comparisonUseCase(resA.getOrThrow(), resB.getOrThrow())
                mutable.update { it.copy(comparisonResult = comparison, comparisonBusy = false) }
            } else {
                val error =
                    resA.exceptionOrNull()?.message
                        ?: resB.exceptionOrNull()?.message
                        ?: "Error comparing vegetation periods"
                mutable.update { it.copy(comparisonBusy = false, comparisonError = error) }
            }
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
