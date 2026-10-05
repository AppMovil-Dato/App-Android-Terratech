package com.novatech.terratech.core.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.novatech.terratech.core.presentation.component.AppToolbar
import com.novatech.terratech.core.presentation.component.Notice
import com.novatech.terratech.core.presentation.component.SessionExpiredNotice
import com.novatech.terratech.core.presentation.navigation.AppDestination
import com.novatech.terratech.core.presentation.navigation.TopLevelDestination
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.profile.presentation.state.ProfileState

@Composable
internal fun SignedInContent(
    destination: AppDestination,
    selectedTab: TopLevelDestination,
    monitoringState: MonitoringState,
    profileState: ProfileState,
    snackbarHostState: SnackbarHostState,
    sessionExpired: Boolean,
    onNavigate: (TopLevelDestination) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSignIn: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { if (destination.showBottomBar) TerraTechBottomBar(selectedTab, onNavigate) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (monitoringState.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (destination != AppDestination.PROFILE) Notice(monitoringState.error)
            SessionExpiredNotice(sessionExpired, onSignIn)
            AppToolbar(destination, monitoringState, profileState, onBack, onRefresh)
            Box(Modifier.weight(1f)) { content() }
        }
    }
}
