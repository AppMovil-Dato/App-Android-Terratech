package com.novatech.terratech.core.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novatech.terratech.core.presentation.state.forUser
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun TerraTechApp(
    accountViewModel: AccountViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    monitoringViewModel: MonitoringViewModel = viewModel(),
) {
    val accountState by accountViewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val monitoringState by monitoringViewModel.state.collectAsStateWithLifecycle()
    if (!accountState.restored) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val session = accountState.session
    if (session == null) {
        AccountScreen(
            accountState,
            accountViewModel::login,
            accountViewModel::register,
            accountViewModel::clearMessage,
        )
        return
    }
    key(session.userId) {
        SignedInApp(
            session,
            accountState,
            profileState.forUser(session.userId),
            monitoringState.forUser(session.userId),
            accountViewModel,
            profileViewModel,
            monitoringViewModel,
        )
    }
}
