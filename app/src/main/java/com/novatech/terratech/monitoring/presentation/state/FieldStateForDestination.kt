package com.novatech.terratech.monitoring.presentation.state

fun MonitoringState.forField(fieldId: Int): MonitoringState =
    if (this.fieldId == fieldId) this
    else copy(fieldId = fieldId, deviceId = null, readings = emptyList(), download = null)
