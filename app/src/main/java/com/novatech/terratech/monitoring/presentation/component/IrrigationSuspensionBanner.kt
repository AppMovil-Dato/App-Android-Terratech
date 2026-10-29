package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.monitoring.domain.entity.CombinedRecommendation
import com.novatech.terratech.ui.theme.Amber
import com.novatech.terratech.ui.theme.AmberLight
import com.novatech.terratech.ui.theme.Ink

@Composable
fun IrrigationSuspensionBanner(
    recommendation: CombinedRecommendation?,
    modifier: Modifier = Modifier,
) {
    if (recommendation == null || !recommendation.suspensionRecommended) return

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(AmberLight, RoundedCornerShape(16.dp))
                .border(1.dp, Amber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.irrigation_recommendation_title),
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
        Text(
            text = recommendation.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
        )
        if (recommendation.estimatedWaterSavingsLiters > 0) {
            Text(
                text = stringResource(R.string.water_savings_label, recommendation.estimatedWaterSavingsLiters),
                style = MaterialTheme.typography.labelLarge,
                color = Amber,
            )
        }
    }
}
