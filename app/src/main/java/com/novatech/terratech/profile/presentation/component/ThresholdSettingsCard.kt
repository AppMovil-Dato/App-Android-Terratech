package com.novatech.terratech.profile.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.Muted

@Composable
internal fun ThresholdSettingsCard(
    minimumMoisture: Double,
    temperatureThreshold: Double,
    enabled: Boolean,
    onConfigure: () -> Unit,
) {
    val moistureDisplay =
        if (minimumMoisture % 1.0 == 0.0) {
            "${minimumMoisture.toInt()} %"
        } else {
            "$minimumMoisture %"
        }
    val temperatureDisplay =
        if (temperatureThreshold % 1.0 == 0.0) {
            "${temperatureThreshold.toInt()} °C"
        } else {
            "$temperatureThreshold °C"
        }

    FarmCard {
        Text(
            stringResource(R.string.alert_thresholds),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(R.string.thresholds_help),
            color = Muted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.min_moisture_label),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Pill(moistureDisplay)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.max_temp_label),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Pill(temperatureDisplay)
        }
        TextButton(
            onClick = onConfigure,
            enabled = enabled,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                stringResource(R.string.edit_thresholds),
                color = FarmGreen,
            )
        }
    }
}
