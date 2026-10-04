package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.infrastructure.local.entity.SensorRow
import com.novatech.terratech.monitoring.infrastructure.remote.dto.SensorDto

fun SensorDto.row(user: Int) = SensorRow(user, id, fieldId, name, sensorCode, macAddress, status)

fun SensorRow.domain() = Sensor(id, fieldId, name, sensorCode, macAddress, status)
