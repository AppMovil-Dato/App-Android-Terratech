package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.monitoring.presentation.component.SatelliteDatePickerDialog
import com.novatech.terratech.monitoring.presentation.component.VegetationComparisonCard
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.Muted

@Composable
fun VegetationComparisonContent(
    monitoringState: MonitoringState,
    onCompare: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dateA by remember { mutableStateOf("") }
    var dateB by remember { mutableStateOf("") }
    var pickingForA by remember { mutableStateOf(false) }
    var pickingForB by remember { mutableStateOf(false) }

    if (pickingForA) {
        SatelliteDatePickerDialog(
            onDateSelected = { date ->
                dateA = date
                pickingForA = false
            },
            onDismiss = { pickingForA = false },
        )
    }

    if (pickingForB) {
        SatelliteDatePickerDialog(
            onDateSelected = { date ->
                dateB = date
                pickingForB = false
            },
            onDismiss = { pickingForB = false },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FarmCard {
            Text(
                text = stringResource(R.string.vegetation_comparison_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = dateA,
                    onValueChange = { dateA = it },
                    label = { Text(stringResource(R.string.select_period_a)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                TextButton(onClick = { pickingForA = true }) {
                    Text(stringResource(R.string.select_date))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = dateB,
                    onValueChange = { dateB = it },
                    label = { Text(stringResource(R.string.select_period_b)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                TextButton(onClick = { pickingForB = true }) {
                    Text(stringResource(R.string.select_date))
                }
            }

            PrimaryButton(
                text = stringResource(R.string.compare_action),
                enabled = dateA.isNotBlank() && dateB.isNotBlank() && !monitoringState.comparisonBusy,
                onClick = { onCompare(dateA.trim(), dateB.trim()) },
            )
        }

        if (monitoringState.comparisonBusy) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (monitoringState.comparisonError != null) {
            Text(
                text = monitoringState.comparisonError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        val result = monitoringState.comparisonResult
        if (result != null) {
            VegetationComparisonCard(comparison = result)
        }
    }
}
