package com.novatech.terratech.monitoring.application.usecase

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.monitoring.domain.valueobject.*

class MonitoringActions(private val repository: MonitoringRepository) {
    fun selection(user: Int) = repository.selection(user)

    suspend fun select(user: Int, field: Int?, device: Int?) =
        repository.select(user, field, device)

    fun fields(user: Int) = repository.fields(user)

    fun sensors(user: Int) = repository.sensors(user)

    fun readings(user: Int, device: Int) = repository.readings(user, device)

    fun downloadState(user: Int, device: Int, days: Int) =
        repository.downloadState(user, device, days)

    suspend fun refreshFields(user: Int) = repository.refreshFields(user)

    suspend fun refreshSensors(user: Int, field: Int) = repository.refreshSensors(user, field)

    suspend fun refreshReadings(user: Int, device: Int, days: Int) {
        if (days !in listOf(7, 30)) throw Failure("INVALID_RANGE")
        repository.refreshReadings(user, device, days)
    }

    suspend fun refreshDetail(user: Int, device: Int, reading: Int) =
        repository.refreshDetail(user, device, reading)

    suspend fun createField(
        user: Int,
        profile: Int,
        name: String,
        crop: String,
        hectares: Double,
        soil: String,
        latitude: Double,
        longitude: Double,
        boundary: List<com.novatech.terratech.monitoring.domain.valueobject.Coordinates> =
            emptyList(),
    ): com.novatech.terratech.monitoring.domain.entity.Field {
        val draft = FieldDraft.of(name, crop, hectares, soil, latitude, longitude, boundary)
        return repository.createField(
            user,
            profile,
            draft.name.value,
            draft.crop,
            draft.area.value,
            draft.soil,
            draft.coordinates.latitude,
            draft.coordinates.longitude,
            draft.boundary,
        )
    }

    suspend fun registerSensor(
        user: Int,
        field: Int,
        code: String,
        name: String,
    ): com.novatech.terratech.monitoring.domain.entity.Sensor {
        val sensorName = FieldName.of(name)
        return repository.registerSensor(user, field, SensorCode.of(code).value, sensorName.value)
    }
}
