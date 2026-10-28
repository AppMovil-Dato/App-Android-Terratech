
package com.novatech.terratech.monitoring.infrastructure.implementation

import androidx.room.withTransaction
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.core.infrastructure.remote.apiCall
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.monitoring.domain.entity.FertilityZone
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.entity.Sensor
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.infrastructure.local.entity.DownloadRow
import com.novatech.terratech.monitoring.infrastructure.mapper.domain
import com.novatech.terratech.monitoring.infrastructure.mapper.row
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import com.novatech.terratech.monitoring.infrastructure.remote.dto.CreateFieldDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FertilityZoneThresholdsDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldVertexDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.RegisterSensorDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.UpdateFertilityZoneThresholdsDto
import java.time.Instant
import kotlinx.coroutines.flow.map

class MonitoringRepositoryImpl(
    private val api: MonitoringApi,
    private val db: TerraDatabase,
    private val store: SessionStore,
) : MonitoringRepository {

    private val dao = db.dao()

    override fun selection(user: Int) = store.selection(user)

    override suspend fun select(user: Int, field: Int?, device: Int?) =
        store.select(user, field, device)

    override fun fields(user: Int) =
        dao.fields(user).map { rows -> rows.map { it.domain() } }

    override fun sensors(user: Int) =
        dao.sensors(user).map { rows -> rows.map { it.domain() } }

    override fun readings(user: Int, device: Int) =
        dao.readings(user, device).map { rows -> rows.map { it.domain() } }

    override fun downloadState(user: Int, device: Int, days: Int) =
        dao.download(user, device, days).map { it?.domain() }

    override suspend fun refreshFields(user: Int) {
        val rows = apiCall { api.fields() }.map { it.row(user) }
        val sensors = apiCall { api.allSensors() }.map { it.row(user) }

        db.withTransaction {
            dao.deleteFields(user)
            dao.putFields(rows)
            dao.deleteAllSensors(user)
            dao.putSensors(sensors)
        }
    }

    override suspend fun refreshSensors(user: Int, field: Int) {
        val rows = apiCall { api.sensors(field) }.map { it.row(user) }

        db.withTransaction {
            dao.deleteSensors(user, field)
            dao.putSensors(rows)
        }
    }

    // US13: consultar las zonas de fertilidad
    override suspend fun fertilityZones(
        user: Int,
        field: Int
    ): List<FertilityZone> {
        return apiCall { api.fertilityZones(field) }.map { zone ->
            FertilityZone(
                id = zone.id,
                fieldId = zone.fieldId,
                deviceId = zone.deviceId,
                name = zone.name,
                boundary = zone.boundary.map { vertex ->
                    Coordinates.of(
                        vertex.latitude,
                        vertex.longitude
                    )
                }
            )
        }
    }

    // US18: obtener umbrales del backend
    override suspend fun fertilityZoneThresholds(
        user: Int,
        field: Int,
        zone: Int
    ): FertilityZoneThresholds {
        val response = apiCall {
            api.fertilityZoneThresholds(field, zone)
        }

        return response.toDomain()
    }

    // US18: guardar umbrales en MySQL mediante la API
    override suspend fun updateFertilityZoneThresholds(
        user: Int,
        field: Int,
        thresholds: FertilityZoneThresholds
    ): FertilityZoneThresholds {
        val body = UpdateFertilityZoneThresholdsDto(
            moistureMin = thresholds.moistureMin,
            moistureMax = thresholds.moistureMax,
            nitrogenMin = thresholds.nitrogenMin,
            nitrogenMax = thresholds.nitrogenMax,
            phosphorusMin = thresholds.phosphorusMin,
            phosphorusMax = thresholds.phosphorusMax,
            potassiumMin = thresholds.potassiumMin,
            potassiumMax = thresholds.potassiumMax
        )

        val response = apiCall {
            api.updateFertilityZoneThresholds(
                field,
                thresholds.zoneId,
                body
            )
        }

        return response.toDomain()
    }

    // US18: recuperar los valores predeterminados
    override suspend fun resetFertilityZoneThresholds(
        user: Int,
        field: Int,
        zone: Int
    ) {
        apiCall {
            api.resetFertilityZoneThresholds(field, zone)
        }
    }

    private fun FertilityZoneThresholdsDto.toDomain(): FertilityZoneThresholds {
        return FertilityZoneThresholds(
            zoneId = zoneId,
            moistureMin = moistureMin,
            moistureMax = moistureMax,
            nitrogenMin = nitrogenMin,
            nitrogenMax = nitrogenMax,
            phosphorusMin = phosphorusMin,
            phosphorusMax = phosphorusMax,
            potassiumMin = potassiumMin,
            potassiumMax = potassiumMax
        )
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
        boundary: List<Coordinates>,
    ): Field {
        val row =
            apiCall {
                api.createField(
                    CreateFieldDto(
                        profile,
                        name,
                        area,
                        soil,
                        latitude,
                        longitude,
                        crop,
                        boundary.map {
                            FieldVertexDto(it.latitude, it.longitude)
                        },
                    )
                )
            }.row(user)

        dao.putFields(listOf(row))
        return row.domain()
    }

    override suspend fun registerSensor(
        user: Int,
        field: Int,
        code: String,
        name: String
    ): Sensor {
        val row =
            apiCall {
                api.registerSensor(
                    RegisterSensorDto(code, field, name)
                )
            }.row(user)

        dao.putSensors(listOf(row))
        return row.domain()
    }

    override suspend fun refreshReadings(
        user: Int,
        device: Int,
        days: Int
    ) {
        val latest =
            try {
                apiCall { api.latest(device) }.reading
            } catch (exception: Failure) {
                if (
                    exception.code == "NO_READINGS" &&
                    exception.status == 404
                ) null
                else throw exception
            }

        val history = apiCall { api.history(device, days) }

        val rows =
            (history.readings + listOfNotNull(latest))
                .distinctBy { it.id }
                .map { it.row(user) }

        db.withTransaction {
            dao.putReadings(rows)
            dao.putDownload(
                DownloadRow(
                    user,
                    device,
                    days,
                    Instant.now().toString(),
                    history.fromUtc,
                    history.toUtc,
                    history.minimumMoisturePercent,
                )
            )
        }
    }

    override suspend fun refreshDetail(
        user: Int,
        device: Int,
        reading: Int
    ) {
        dao.putReadings(
            listOf(
                apiCall {
                    api.detail(device, reading)
                }.row(user)
            )
        )
    }
}
