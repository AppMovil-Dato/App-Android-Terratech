
package com.novatech.terratech.monitoring.application.usecase

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.monitoring.domain.entity.FertilityZone
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.entity.Sensor
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.domain.valueobject.FieldName
import com.novatech.terratech.monitoring.domain.valueobject.SensorCode

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

    suspend fun refreshSensors(user: Int, field: Int) =
        repository.refreshSensors(user, field)

    // US13: consultar zonas de fertilidad
    suspend fun fertilityZones(
        user: Int,
        field: Int
    ): List<FertilityZone> =
        repository.fertilityZones(user, field)

    // US18: consultar umbrales
    suspend fun fertilityZoneThresholds(
        user: Int,
        field: Int,
        zone: Int
    ): FertilityZoneThresholds =
        repository.fertilityZoneThresholds(user, field, zone)

    // US18: actualizar umbrales
    suspend fun updateFertilityZoneThresholds(
        user: Int,
        field: Int,
        thresholds: FertilityZoneThresholds
    ): FertilityZoneThresholds =
        repository.updateFertilityZoneThresholds(
            user,
            field,
            thresholds
        )

    // US18: restablecer umbrales
    suspend fun resetFertilityZoneThresholds(
        user: Int,
        field: Int,
        zone: Int
    ) = repository.resetFertilityZoneThresholds(user, field, zone)

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
        boundary: List<Coordinates> = emptyList(),
    ): Field {
        val draft = FieldDraft.of(
            name,
            crop,
            hectares,
            soil,
            latitude,
            longitude,
            boundary
        )

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
        name: String
    ): Sensor {
        val sensorName = FieldName.of(name)

        return repository.registerSensor(
            user,
            field,
            SensorCode.of(code).value,
            sensorName.value
        )
    }

    fun recommendations(user: Int, deviceId: Int) = repository.recommendations(user, deviceId)
}
