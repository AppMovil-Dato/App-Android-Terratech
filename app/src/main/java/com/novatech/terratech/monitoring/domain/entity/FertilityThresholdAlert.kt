
package com.novatech.terratech.monitoring.domain.entity

data class FertilityThresholdAlert(
    val parameter: String,
    val currentValue: Double,
    val limit: Double,
    val unit: String,
    val aboveMaximum: Boolean,
    val recommendation: String
)

fun Reading.evaluateThresholds(
    thresholds: FertilityZoneThresholds
): List<FertilityThresholdAlert> {

    val alerts = mutableListOf<FertilityThresholdAlert>()

    fun check(
        parameter: String,
        value: Double,
        minimum: Double,
        maximum: Double,
        unit: String,
        lowRecommendation: String,
        highRecommendation: String
    ) {
        if (!value.isFinite()) return

        if (value < minimum) {
            alerts.add(
                FertilityThresholdAlert(
                    parameter = parameter,
                    currentValue = value,
                    limit = minimum,
                    unit = unit,
                    aboveMaximum = false,
                    recommendation = lowRecommendation
                )
            )
        } else if (value > maximum) {
            alerts.add(
                FertilityThresholdAlert(
                    parameter = parameter,
                    currentValue = value,
                    limit = maximum,
                    unit = unit,
                    aboveMaximum = true,
                    recommendation = highRecommendation
                )
            )
        }
    }

    check(
        "Humedad",
        moisturePercent,
        thresholds.moistureMin,
        thresholds.moistureMax,
        "%",
        "Revisar la humedad del suelo y evaluar el riego.",
        "Revisar el exceso de humedad antes de continuar el riego."
    )

    check(
        "Nitrogeno",
        nitrogenPpm,
        thresholds.nitrogenMin,
        thresholds.nitrogenMax,
        "ppm",
        "Revisar el nivel de nitrogeno y el plan de fertilizacion.",
        "Revisar el exceso de nitrogeno antes de fertilizar."
    )

    check(
        "Fosforo",
        phosphorusPpm,
        thresholds.phosphorusMin,
        thresholds.phosphorusMax,
        "ppm",
        "Revisar el nivel de fosforo del suelo.",
        "Revisar el exceso de fosforo antes de fertilizar."
    )

    check(
        "Potasio",
        potassiumPpm,
        thresholds.potassiumMin,
        thresholds.potassiumMax,
        "ppm",
        "Revisar el nivel de potasio del suelo.",
        "Revisar el exceso de potasio antes de fertilizar."
    )

    return alerts
}
