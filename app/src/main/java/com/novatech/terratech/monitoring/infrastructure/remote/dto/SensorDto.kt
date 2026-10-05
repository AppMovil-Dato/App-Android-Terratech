package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class SensorDto(
    val id: Int,
    val fieldId: Int,
    val name: String?,
    val sensorCode: String?,
    val macAddress: String,
    val status: String,
)
