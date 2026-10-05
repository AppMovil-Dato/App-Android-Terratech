package com.novatech.terratech.monitoring.presentation.state

fun MonitoringState.forSensor(deviceId: Int): MonitoringState =
    if (this.deviceId == deviceId) this
    else
        copy(
            fieldId = sensors.find { it.id == deviceId }?.fieldId,
            deviceId = deviceId,
            readings = emptyList(),
            download = null,
        )
