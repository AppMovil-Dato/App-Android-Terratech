package com.novatech.terratech.monitoring.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.core.presentation.navigation.TopLevelDestination
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.FieldsScreen
import com.novatech.terratech.profile.presentation.state.ProfileState

fun NavGraphBuilder.fieldsDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    profileState: ProfileState,
) {
    composable<FieldsDestination> {
        FieldsScreen(
            state = state,
            hasProfile = profileState.profile != null,
            onChoose = { fieldId -> navigator.sensors(fieldId) },
            onCreate = { navigator.createField(profileState.profile != null) },
            onProfile = { navigator.topLevel(TopLevelDestination.PROFILE) },
        )
    }
}
