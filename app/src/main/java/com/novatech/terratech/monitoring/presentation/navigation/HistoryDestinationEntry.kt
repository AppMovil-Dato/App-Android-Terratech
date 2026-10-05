package com.novatech.terratech.monitoring.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.state.forSensor
import com.novatech.terratech.monitoring.presentation.ui.HistoryScreen

fun NavGraphBuilder.historyDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    actions: MonitoringNavigationActions,
) {
    composable<HistoryDestination> { entry ->
        val destination = entry.toRoute<HistoryDestination>()
        LaunchedEffect(destination.deviceId, state.sensors) {
            if (state.deviceId != destination.deviceId) actions.selectSensor(destination.deviceId)
        }
        HistoryScreen(
            state = state.forSensor(destination.deviceId),
            onDays = actions.changeDays,
            onDetail = { readingId -> navigator.reading(destination.deviceId, readingId) },
            onRefresh = actions.refresh,
        )
    }
}
