package com.novatech.terratech.monitoring.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.profile.presentation.state.ProfileState

fun NavGraphBuilder.monitoringGraph(
    navigator: AppNavigator,
    state: MonitoringState,
    profileState: ProfileState,
    actions: MonitoringNavigationActions,
) {
    navigation<FieldsGraph>(startDestination = FieldsDestination) {
        fieldsDestination(navigator, state, profileState)
        createFieldDestination(navigator, state, profileState, actions)
        fieldSensorsDestination(navigator, state, actions)
        registerSensorDestination(navigator, state, actions)
        sensorDestination(navigator, state, actions)
        historyDestination(navigator, state, actions)
        readingDestination(state, actions)
    }
}
