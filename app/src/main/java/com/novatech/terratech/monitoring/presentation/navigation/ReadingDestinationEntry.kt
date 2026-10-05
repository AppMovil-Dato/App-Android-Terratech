package com.novatech.terratech.monitoring.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.ReadingDetailScreen

fun NavGraphBuilder.readingDestination(
    state: MonitoringState,
    actions: MonitoringNavigationActions,
) {
    composable<ReadingDestination> { entry ->
        val destination = entry.toRoute<ReadingDestination>()
        LaunchedEffect(destination.deviceId, state.sensors) {
            if (state.deviceId != destination.deviceId) actions.selectSensor(destination.deviceId)
        }
        LaunchedEffect(destination.readingId, state.deviceId) {
            if (state.deviceId == destination.deviceId) actions.readDetail(destination.readingId)
        }
        ReadingDetailScreen(
            reading =
                state.readings.find {
                    it.id == destination.readingId && it.deviceId == destination.deviceId
                }
        )
    }
}
