
package com.novatech.terratech.monitoring.domain.entity

data class FertilityZoneThresholds(
    val zoneId: Int,
    val moistureMin: Double,
    val moistureMax: Double,
    val nitrogenMin: Double,
    val nitrogenMax: Double,
    val phosphorusMin: Double,
    val phosphorusMax: Double,
    val potassiumMin: Double,
    val potassiumMax: Double
)
