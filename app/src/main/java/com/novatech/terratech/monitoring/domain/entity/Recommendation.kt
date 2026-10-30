package com.novatech.terratech.monitoring.domain.entity

import com.novatech.terratech.monitoring.domain.valueobject.RecommendationSeverity
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationType
import java.time.Instant

data class Recommendation(
    val id: Int = 0,
    val deviceId: Int,
    val readingId: Int,
    val type: RecommendationType,
    val severity: RecommendationSeverity,
    val title: String,
    val message: String,
    val actionSuggested: String?,
    val createdAt: Instant,
)
