package com.novatech.terratech.monitoring.domain.entity

data class Sensor(
    val id: Int,
    val fieldId: Int,
    val name: String?,
    val sensorCode: String?,
    val macAddress: String,
    val status: String,
)
