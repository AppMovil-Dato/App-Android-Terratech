package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.component.ReadingMetrics
import com.novatech.terratech.monitoring.presentation.component.ReadingSummary
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.*

@Composable
fun SensorScreen(state: MonitoringState, onHistory: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageTitle(
                state.selectedSensor?.name ?: stringResource(R.string.sensor_detail),
                state.selectedSensor?.sensorCode,
            )
        }
        state.latest?.let { r ->
            item { ReadingSummary(r, state) }
            item { ReadingMetrics(r) }
        }
            ?: item {
                EmptyCard(
                    stringResource(R.string.no_readings),
                    stringResource(R.string.no_readings_body),
                )
            }
        item { PrimaryButton(stringResource(R.string.view_history), onClick = onHistory) }
    }
}
