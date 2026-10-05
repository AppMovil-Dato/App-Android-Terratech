package com.novatech.terratech.core.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.HomeScreen
import com.novatech.terratech.profile.presentation.state.ProfileState

fun NavGraphBuilder.homeGraph(
    navigator: AppNavigator,
    session: Session,
    profileState: ProfileState,
    monitoringState: MonitoringState,
) {
    navigation<HomeGraph>(startDestination = HomeDestination) {
        composable<HomeDestination> {
            HomeScreen(
                name =
                    profileState.profile?.fullName?.ifBlank { session.fullName }
                        ?: session.fullName,
                state = monitoringState,
                hasProfile = profileState.profile != null,
                onFields = { navigator.topLevel(TopLevelDestination.FIELDS) },
                onProfile = { navigator.topLevel(TopLevelDestination.PROFILE) },
                onHistory = { navigator.history(monitoringState.deviceId) },
                onSensor = { navigator.sensor(monitoringState.deviceId) },
                onCreateField = { navigator.createField(profileState.profile != null) },
                onConnectSensor = { navigator.registerSensor(monitoringState.fieldId) },
                onSwitchSensor = {
                    monitoringState.fieldId?.let(navigator::sensors)
                        ?: navigator.topLevel(TopLevelDestination.FIELDS)
                },
            )
        }
    }
}
