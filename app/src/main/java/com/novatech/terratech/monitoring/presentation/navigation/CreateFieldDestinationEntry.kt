package com.novatech.terratech.monitoring.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.CreateFieldRoute
import com.novatech.terratech.profile.presentation.state.ProfileState

fun NavGraphBuilder.createFieldDestination(
    navigator: AppNavigator,
    state: MonitoringState,
    profileState: ProfileState,
    actions: MonitoringNavigationActions,
) {
    composable<CreateFieldDestination> {
        CreateFieldRoute(
            busy = state.busy,
            onSave = { draft -> profileState.profile?.let { actions.createField(it.id, draft) } },
            onCancel = {
                navigator.back()
                actions.clearMessage()
            },
        )
    }
}
