package com.novatech.terratech.profile.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.profile.presentation.ui.ProfileScreen

fun NavGraphBuilder.profileGraph(
    navigator: AppNavigator,
    session: Session,
    state: ProfileState,
    actions: ProfileNavigationActions,
    hasFields: Boolean,
) {
    navigation<ProfileGraph>(startDestination = ProfileDestination) {
        composable<ProfileDestination> {
            ProfileScreen(
                state = state,
                session = session,
                onSave = actions.save,
                onRefresh = actions.refresh,
                onLogout = actions.logout,
                onContinue =
                    if (hasFields) null else ({ navigator.createField(state.profile != null) }),
            )
        }
    }
}
