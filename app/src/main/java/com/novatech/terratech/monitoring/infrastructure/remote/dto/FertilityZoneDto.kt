
package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class FertilityZoneDto(
    val id: Int,
    val fieldId: Int,
    val deviceId: Int,
    val name: String,
    val boundary: List<FieldVertexDto>,
)
