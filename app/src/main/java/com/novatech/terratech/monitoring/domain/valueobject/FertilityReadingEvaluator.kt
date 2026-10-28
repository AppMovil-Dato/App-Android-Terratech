
package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.monitoring.domain.entity.Reading
import java.time.Instant

data class NpkRanges(
    val nitrogen: NutrientRanges,
    val phosphorus: NutrientRanges,
    val potassium: NutrientRanges,
)

fun evaluateReadingFertility(
    reading: Reading,
    now: Instant,
    ranges: NpkRanges?,
): FertilityLevel? {

    if (ranges == null) {
        return null
    }

    if (reading.recordedAt.isAfter(now)) {
        return null
    }

    if (reading.isStale(now)) {
        return null
    }

    if (classifyMoisture(reading.moisturePercent) == null) {
        return null
    }

    val nitrogen = classifyNutrient(
        reading.nitrogenPpm,
        ranges.nitrogen,
    ) ?: return null

    val phosphorus = classifyNutrient(
        reading.phosphorusPpm,
        ranges.phosphorus,
    ) ?: return null

    val potassium = classifyNutrient(
        reading.potassiumPpm,
        ranges.potassium,
    ) ?: return null

    return classifyFertility(
        reading.moisturePercent,
        nitrogen,
        phosphorus,
        potassium,
    )
}
