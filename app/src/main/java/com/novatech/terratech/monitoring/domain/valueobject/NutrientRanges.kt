
package com.novatech.terratech.monitoring.domain.valueobject

data class NutrientRanges(
    val criticalBelowPpm: Double,
    val optimalMinPpm: Double,
    val optimalMaxPpm: Double,
) {
    init {
        require(criticalBelowPpm.isFinite())
        require(optimalMinPpm.isFinite())
        require(optimalMaxPpm.isFinite())

        require(criticalBelowPpm >= 0.0)
        require(criticalBelowPpm <= optimalMinPpm)
        require(optimalMinPpm <= optimalMaxPpm)
    }
}

fun classifyNutrient(
    valuePpm: Double,
    ranges: NutrientRanges,
): NutrientLevel? {

    if (!valuePpm.isFinite() || valuePpm < 0.0) {
        return null
    }

    return when {
        valuePpm < ranges.criticalBelowPpm ->
            NutrientLevel.LOW

        valuePpm < ranges.optimalMinPpm ->
            NutrientLevel.MEDIUM

        valuePpm <= ranges.optimalMaxPpm ->
            NutrientLevel.OPTIMAL

        else -> null
    }
}
