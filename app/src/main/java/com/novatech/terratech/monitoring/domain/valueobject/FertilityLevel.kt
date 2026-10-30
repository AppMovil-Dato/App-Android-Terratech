
package com.novatech.terratech.monitoring.domain.valueobject

enum class FertilityLevel {
    CRITICAL,
    MODERATE,
    OPTIMAL,
}

fun classifyFertility(
    moisturePercent: Double,
    nitrogen: NutrientLevel?,
    phosphorus: NutrientLevel?,
    potassium: NutrientLevel?,
): FertilityLevel? {

    val moistureLevel = classifyMoisture(moisturePercent)
        ?: return null

    if (
        moistureLevel == MoistureLevel.CRITICAL ||
        nitrogen == NutrientLevel.LOW ||
        phosphorus == NutrientLevel.LOW ||
        potassium == NutrientLevel.LOW
    ) {
        return FertilityLevel.CRITICAL
    }

    if (
        nitrogen == null ||
        phosphorus == null ||
        potassium == null
    ) {
        return null
    }

    if (
        moistureLevel == MoistureLevel.MODERATE ||
        nitrogen == NutrientLevel.MEDIUM ||
        phosphorus == NutrientLevel.MEDIUM ||
        potassium == NutrientLevel.MEDIUM
    ) {
        return FertilityLevel.MODERATE
    }

    return FertilityLevel.OPTIMAL
}
