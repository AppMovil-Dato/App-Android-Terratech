package com.novatech.terratech.monitoring.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.state.forField
import com.novatech.terratech.monitoring.presentation.ui.SensorsScreen

fun NavGraphBuilder.fieldSensorsDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    actions: MonitoringNavigationActions,
) {
    composable<FieldSensorsDestination> { entry ->
        val destination = entry.toRoute<FieldSensorsDestination>()
        LaunchedEffect(destination.fieldId, state.fields) {
            if (state.fieldId != destination.fieldId) actions.selectField(destination.fieldId)
        }
        SensorsScreen(
            state = state.forField(destination.fieldId),
            onChoose = { deviceId -> navigator.sensor(deviceId) },
            onRegister = { navigator.registerSensor(destination.fieldId) },
        )
    }
}
