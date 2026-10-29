
package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class FertilityZoneThresholdsDto(
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

data class UpdateFertilityZoneThresholdsDto(
    val moistureMin: Double,
    val moistureMax: Double,
    val nitrogenMin: Double,
    val nitrogenMax: Double,
    val phosphorusMin: Double,
    val phosphorusMax: Double,
    val potassiumMin: Double,
    val potassiumMax: Double
)
