package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.monitoring.presentation.state.MonitoringState

@Composable
fun VegetationComparisonScreen(
    monitoringState: MonitoringState,
    onBack: () -> Unit,
    onCompare: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageTitle(
                stringResource(R.string.vegetation_comparison_title),
                monitoringState.selectedField?.name,
            )
        }
        item {
            VegetationComparisonContent(
                monitoringState = monitoringState,
                onCompare = onCompare,
            )
        }
        item {
            PrimaryButton(
                text = stringResource(R.string.back),
                enabled = true,
                onClick = onBack,
            )
        }
    }
}
