package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.monitoring.domain.entity.Recommendation
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationSeverity

@Composable
internal fun RecommendationCard(recommendation: Recommendation){
    val isCritical = recommendation.severity == RecommendationSeverity.CRITICAL
    val isWarning = recommendation.severity == RecommendationSeverity.WARNING

    FarmCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                recommendation.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Pill(recommendation.severity.name, isCritical || isWarning)
        }
        Text(
            recommendation.message,
            style = MaterialTheme.typography.bodyMedium,
        )
        recommendation.actionSuggested?.let { action ->
            Text(
                "Acción sugerida: $action",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
