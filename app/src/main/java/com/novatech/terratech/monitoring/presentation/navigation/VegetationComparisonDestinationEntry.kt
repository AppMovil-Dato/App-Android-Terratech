package com.novatech.terratech.monitoring.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.state.forField
import com.novatech.terratech.monitoring.presentation.ui.VegetationComparisonScreen

fun NavGraphBuilder.vegetationComparisonDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    actions: MonitoringNavigationActions,
) {
    composable<VegetationComparisonDestination> { entry ->
        val destination = entry.toRoute<VegetationComparisonDestination>()
        LaunchedEffect(destination.fieldId, state.fields) {
            if (state.fieldId != destination.fieldId) actions.selectField(destination.fieldId)
        }
        VegetationComparisonScreen(
            monitoringState = state.forField(destination.fieldId),
            onBack = { navigator.back() },
            onCompare = actions.compareVegetation,
        )
    }
}
