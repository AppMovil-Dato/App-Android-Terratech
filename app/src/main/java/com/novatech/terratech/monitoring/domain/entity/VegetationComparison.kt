package com.novatech.terratech.monitoring.domain.entity

data class VegetationComparison(
    val earlier: SatelliteImage,
    val later: SatelliteImage,
    val ndviDelta: Double,
    val percentageChange: Double,
)
