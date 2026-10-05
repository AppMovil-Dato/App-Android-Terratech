package com.novatech.terratech.monitoring.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.RegisterSensorScreen

fun NavGraphBuilder.registerSensorDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    actions: MonitoringNavigationActions,
) {
    composable<RegisterSensorDestination> { entry ->
        val destination = entry.toRoute<RegisterSensorDestination>()
        LaunchedEffect(destination.fieldId, state.fields) {
            if (state.fieldId != destination.fieldId) actions.selectField(destination.fieldId)
        }
        RegisterSensorScreen(
            busy = state.busy,
            onSave = actions.registerSensor,
            fieldName = state.fields.find { it.id == destination.fieldId }?.name.orEmpty(),
            error = state.error,
            onClear = actions.clearMessage,
        )
    }
}
