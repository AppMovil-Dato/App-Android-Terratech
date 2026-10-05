package com.novatech.terratech.monitoring.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.state.forSensor
import com.novatech.terratech.monitoring.presentation.ui.SensorScreen

fun NavGraphBuilder.sensorDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    actions: MonitoringNavigationActions,
) {
    composable<SensorDestination> { entry ->
        val destination = entry.toRoute<SensorDestination>()
        LaunchedEffect(destination.deviceId, state.sensors) {
            if (state.deviceId != destination.deviceId) actions.selectSensor(destination.deviceId)
        }
        SensorScreen(
            state = state.forSensor(destination.deviceId),
            onHistory = { navigator.history(destination.deviceId) },
        )
    }
}
