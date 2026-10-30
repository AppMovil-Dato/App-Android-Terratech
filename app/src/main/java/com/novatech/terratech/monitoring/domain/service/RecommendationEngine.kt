package com.novatech.terratech.monitoring.domain.service

import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.Recommendation
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationSeverity
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationType

object RecommendationEngine {
    fun evaluate(reading: Reading,
                 zoneName: String,
                 minimumMoisture: Double = 30.0,
                 maximumMoisture: Double = 80.0): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        evaluateWatering(reading, zoneName, minimumMoisture, maximumMoisture)?.let {
            recommendations.add(it)
        }

        evaluateFertilizer(reading, zoneName)?.let {
            recommendations.add(it)
        }

        return recommendations
    }
}

private fun evaluateWatering(reading: Reading,
                             zoneName: String,
                             minThreshold: Double,
                             maxThreshold: Double): Recommendation? {
    val moisture = reading.moisturePercent

    return when {
        moisture < minThreshold -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.WATERING,
                severity = RecommendationSeverity.CRITICAL,
                title = "Riego Urgente - Zona $zoneName",
                message = "La humedad actual es de ${moisture}%, por debajo del umbral mínimo de ${minThreshold}%.",
                actionSuggested = "Se sugiere iniciar riego por 25 minutos para la zona $zoneName.",
                createdAt = reading.recordedAt
            )
        }
        moisture > maxThreshold -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.WATERING,
                severity = RecommendationSeverity.WARNING,
                title = "Suspender Riego - Zona $zoneName",
                message = "La humedad reportada es de ${moisture}%, superando el máximo recomendado de ${maxThreshold}%.",
                actionSuggested = "Suspender riego para prevenir riesgo de pudrición de raíces.",
                createdAt = reading.recordedAt
            )
        }
        else -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.WATERING,
                severity = RecommendationSeverity.INFO,
                title = "Nivel de Humedad Óptimo",
                message = "La zona $zoneName reporta ${moisture}% de humedad (rango óptimo: $minThreshold% - $maxThreshold%).",
                actionSuggested = "No es necesario regar en este momento.",
                createdAt = reading.recordedAt
            )
        }
    }
}

private fun evaluateFertilizer(reading: Reading,
                               zoneName: String): Recommendation? {
    val n = reading.nitrogenPpm
    val p = reading.phosphorusPpm
    val k = reading.potassiumPpm

    val lowN = n < 20.0
    val lowP = p < 15.0
    val lowK = k < 30.0

    return when {
        (lowN && lowP) || (lowN && lowK) || (lowP && lowK) -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.FERTILIZER,
                severity = RecommendationSeverity.CRITICAL,
                title = "Deficiencia Múltiple de Nutrientes - Zona $zoneName",
                message = "Valores detectados: N=${n} ppm, P=${p} ppm, K=${k} ppm.",
                actionSuggested = "Aplicar 12 kg/ha de NPK 20-20-20 de forma prioritaria.",
                createdAt = reading.recordedAt
            )
        }
        lowN -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.FERTILIZER,
                severity = RecommendationSeverity.WARNING,
                title = "Deficiencia de Nitrógeno - Zona $zoneName",
                message = "Nivel de Nitrógeno bajo (${n} ppm). Rango óptimo: 20-40 ppm.",
                actionSuggested = "Aplicar 8 kg/ha de Nitrógeno (Urea 46-0-0) para la zona $zoneName.",
                createdAt = reading.recordedAt
            )
        }
        lowP -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.FERTILIZER,
                severity = RecommendationSeverity.WARNING,
                title = "Deficiencia de Fósforo - Zona $zoneName",
                message = "Nivel de Fósforo bajo (${p} ppm). Rango óptimo: 15-30 ppm.",
                actionSuggested = "Aplicar fertilizante rico en Fósforo (DAP / Superfosfato).",
                createdAt = reading.recordedAt
            )
        }
        lowK -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.FERTILIZER,
                severity = RecommendationSeverity.WARNING,
                title = "Deficiencia de Potasio - Zona $zoneName",
                message = "Nivel de Potasio bajo (${k} ppm). Rango óptimo: 30-50 ppm.",
                actionSuggested = "Aplicar Nitrato o Cloruro de Potasio.",
                createdAt = reading.recordedAt
            )
        }
        else -> {
            Recommendation(
                deviceId = reading.deviceId,
                readingId = reading.id,
                type = RecommendationType.FERTILIZER,
                severity = RecommendationSeverity.INFO,
                title = "Nutrientes Adecuados - Zona $zoneName",
                message = "Niveles actuales: N=${n} ppm, P=${p} ppm, K=${k} ppm.",
                actionSuggested = "Los niveles son adecuados. Mantener el plan de fertilización actual.",
                createdAt = reading.recordedAt
            )
        }
    }
}
