package com.novatech.terratech.monitoring.domain.valueobject

data class NpkRanges(
    val nitrogen: NutrientRanges,
    val phosphorus: NutrientRanges,
    val potassium: NutrientRanges,
)

