package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.Recommendation
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationSeverity
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationType
import com.novatech.terratech.monitoring.infrastructure.local.entity.RecommendationRow
import java.time.Instant

fun Recommendation.row(user: Int): RecommendationRow =
    RecommendationRow(
        userId = user,
        deviceId = deviceId,
        readingId = readingId,
        type = type.name,
        severity = severity.name,
        title = title,
        message = message,
        actionSuggested = actionSuggested,
        createdAt = createdAt.toString()
    )

fun RecommendationRow.domain(): Recommendation =
    Recommendation(
        deviceId = deviceId,
        readingId = readingId,
        type = RecommendationType.valueOf(type),
        severity = RecommendationSeverity.valueOf(severity),
        title = title,
        message = message,
        actionSuggested = actionSuggested,
        createdAt = Instant.parse(createdAt)
    )
