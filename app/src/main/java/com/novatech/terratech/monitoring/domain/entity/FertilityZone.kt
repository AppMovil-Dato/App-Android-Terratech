
package com.novatech.terratech.monitoring.domain.entity

import com.novatech.terratech.monitoring.domain.valueobject.Coordinates

data class FertilityZone(
    val id: Int,
    val fieldId: Int,
    val deviceId: Int,
    val name: String,
    val boundary: List<Coordinates>,
)
