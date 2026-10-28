package com.novatech.terratech.monitoring.presentation.state

import com.novatech.terratech.monitoring.domain.entity.DownloadState
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.entity.FieldWeather
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import com.novatech.terratech.monitoring.domain.entity.Sensor
import com.novatech.terratech.monitoring.domain.entity.VegetationComparison
import java.time.Instant

data class MonitoringState(
    val userId: Int? = null,
    val fields: List<Field> = emptyList(),
    val sensors: List<Sensor> = emptyList(),
    val readings: List<Reading> = emptyList(),
    val fieldId: Int? = null,
    val deviceId: Int? = null,
    val days: Int = 7,
    val download: DownloadState? = null,
    val busy: Boolean = false,
    val error: String? = null,
    val created: Boolean = false,
    val createdFieldId: Int? = null,
    val createdSensorId: Int? = null,
    val offline: Boolean = false,
    val now: Instant = Instant.now(),
    val weather: FieldWeather? = null,
    val weatherBusy: Boolean = false,
    val weatherError: String? = null,
    val satelliteImage: SatelliteImage? = null,
    val satelliteBusy: Boolean = false,
    val satelliteError: String? = null,
    val isSatelliteView: Boolean = false,
    val comparisonResult: VegetationComparison? = null,
    val comparisonBusy: Boolean = false,
    val comparisonError: String? = null,
) {
    val selectedField
        get() = fields.find { it.id == fieldId }

    val fieldSensors
        get() = sensors.filter { it.fieldId == fieldId }

    val selectedSensor
        get() = fieldSensors.find { it.id == deviceId }

    val latest
        get() = readings.maxByOrNull { it.recordedAt }

    val history
        get() = readings.filter { it.inRange(days, now) }.sortedBy { it.recordedAt }
}
