
package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.monitoring.domain.entity.DownloadState
import com.novatech.terratech.monitoring.domain.entity.FertilityZone
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.Sensor
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeMonitoringRepository : MonitoringRepository {

    val plots =
        MutableStateFlow(
            listOf(
                Field(1, 1, "North", 5000.0, "Loam", -12.0, -77.0, "Potato")
            )
        )

    val devices =
        MutableStateFlow(
            listOf(
                Sensor(
                    11,
                    1,
                    "North sensor",
                    "TT-ZZZ001",
                    "02:00:00:00:00:01",
                    "OFFLINE"
                )
            )
        )

    val samples =
        MutableStateFlow(
            listOf(
                Reading(
                    9,
                    11,
                    Instant.now(),
                    42.0,
                    23.0,
                    35.0,
                    18.0,
                    60.0,
                    "SIMULATED"
                )
            )
        )

    var offline = false
    var registerCalls = 0
    var occupied = false
    var selected: Pair<Int?, Int?> = null to null

    // US18: valores iniciales para las pruebas
    private val initialThresholds = FertilityZoneThresholds(
        zoneId = 1,
        moistureMin = 25.0,
        moistureMax = 75.0,
        nitrogenMin = 20.0,
        nitrogenMax = 40.0,
        phosphorusMin = 15.0,
        phosphorusMax = 30.0,
        potassiumMin = 30.0,
        potassiumMax = 50.0
    )

    private var savedThresholds = initialThresholds

    private fun connected() {
        if (offline) throw Failure("OFFLINE")
    }

    override fun selection(user: Int) = flowOf(selected)

    override suspend fun select(
        user: Int,
        field: Int?,
        device: Int?
    ) {
        selected = field to device
    }

    override fun fields(user: Int) =
        if (user == 1) plots else flowOf(emptyList())

    override fun sensors(user: Int) =
        if (user == 1) devices else flowOf(emptyList())

    override fun readings(user: Int, device: Int) =
        if (user == 1 && device == 11) {
            samples
        } else {
            flowOf(emptyList())
        }

    override fun downloadState(
        user: Int,
        device: Int,
        days: Int
    ) = flowOf(
        DownloadState(
            Instant.now(),
            null,
            null,
            30.0
        )
    )

    override suspend fun refreshFields(user: Int) {
        connected()
    }

    override suspend fun refreshSensors(
        user: Int,
        field: Int
    ) {
        connected()
    }

    // US13: zonas de fertilidad
    override suspend fun fertilityZones(
        user: Int,
        field: Int
    ): List<FertilityZone> {
        connected()
        return emptyList()
    }

    // US18: consultar umbrales guardados
    override suspend fun fertilityZoneThresholds(
        user: Int,
        field: Int,
        zone: Int
    ): FertilityZoneThresholds {
        connected()

        if (field != 1 || zone != 1) {
            throw Failure("ZONE_NOT_FOUND", 404)
        }

        return savedThresholds
    }

    // US18: guardar nuevos umbrales
    override suspend fun updateFertilityZoneThresholds(
        user: Int,
        field: Int,
        thresholds: FertilityZoneThresholds
    ): FertilityZoneThresholds {
        connected()

        if (field != 1 || thresholds.zoneId != 1) {
            throw Failure("ZONE_NOT_FOUND", 404)
        }

        if (
            !validRange(
                thresholds.moistureMin,
                thresholds.moistureMax,
                100.0
            ) ||
            !validRange(
                thresholds.nitrogenMin,
                thresholds.nitrogenMax
            ) ||
            !validRange(
                thresholds.phosphorusMin,
                thresholds.phosphorusMax
            ) ||
            !validRange(
                thresholds.potassiumMin,
                thresholds.potassiumMax
            )
        ) {
            throw Failure("INVALID_THRESHOLDS", 400)
        }

        savedThresholds = thresholds
        return savedThresholds
    }

    // US18: restablecer valores
    override suspend fun resetFertilityZoneThresholds(
        user: Int,
        field: Int,
        zone: Int
    ) {
        connected()

        if (field != 1 || zone != 1) {
            throw Failure("ZONE_NOT_FOUND", 404)
        }

        savedThresholds = initialThresholds
    }

    private fun validRange(
        minimum: Double,
        maximum: Double,
        limit: Double = Double.MAX_VALUE
    ): Boolean {
        return minimum.isFinite() &&
            maximum.isFinite() &&
            minimum >= 0.0 &&
            maximum <= limit &&
            minimum < maximum
    }

    override suspend fun refreshReadings(
        user: Int,
        device: Int,
        days: Int
    ) {
        connected()
    }

    override suspend fun refreshDetail(
        user: Int,
        device: Int,
        reading: Int
    ) {
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
        boundary: List<Coordinates>,
    ): Field {
        connected()

        val created = Field(
            2,
            profile,
            name,
            area,
            soil,
            latitude,
            longitude,
            crop,
            boundary
        )

        plots.value = plots.value + created

        return created
    }

    override suspend fun registerSensor(
        user: Int,
        field: Int,
        code: String,
        name: String
    ): Sensor {
        connected()

        registerCalls++

        if (occupied) {
            throw Failure("SENSOR_OCCUPIED", 409)
        }

        val created = Sensor(
            12,
            field,
            name,
            code,
            "02:00:00:00:00:02",
            "OFFLINE"
        )

        devices.value = devices.value + created

        return created
    }
}
