package com.novatech.terratech.core.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R
import com.novatech.terratech.monitoring.presentation.state.MonitoringState

@Composable
fun AppCreationEffect(
    state: MonitoringState,
    navigator: AppNavigator,
    snackbarHostState: SnackbarHostState,
    onConsumed: () -> Unit,
) {
    val fieldMessage = stringResource(R.string.field_created)
    val sensorMessage = stringResource(R.string.sensor_created)
    LaunchedEffect(state.createdFieldId, state.createdSensorId) {
        val message =
            when {
                state.createdFieldId != null -> {
                    navigator.fieldCreated(state.createdFieldId)
                    fieldMessage
                }
                state.createdSensorId != null -> {
                    navigator.sensorCreated(state.createdSensorId)
                    sensorMessage
                }
                else -> return@LaunchedEffect
            }
        onConsumed()
        snackbarHostState.showSnackbar(message)
    }
}
