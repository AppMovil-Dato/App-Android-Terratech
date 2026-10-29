
package com.novatech.terratech.monitoring.presentation.component

import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.domain.valueobject.FertilityLevel

data class FertilityZoneOverlay(
    val zoneId: Int,
    val fieldId: Int,
    val deviceId: Int,
    val name: String,
    val boundary: List<Coordinates>,
    val level: FertilityLevel? = null,
)
