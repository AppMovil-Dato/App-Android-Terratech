package com.novatech.terratech.core.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.novatech.terratech.core.presentation.state.forUser
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun TerraTechApp(
    account: AccountViewModel = viewModel(),
    profile: ProfileViewModel = viewModel(),
    monitoring: MonitoringViewModel = viewModel(),
) {
    val auth by account.state.collectAsStateWithLifecycle()
    val p by profile.state.collectAsStateWithLifecycle()
    val m by monitoring.state.collectAsStateWithLifecycle()
    if (!auth.restored) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val session = auth.session
    if (session == null) {
        AccountScreen(auth, account::login, account::register, account::clearMessage)
        return
    }
    key(session.userId) {
        SignedInApp(
            session,
            auth,
            p.forUser(session.userId),
            m.forUser(session.userId),
            account,
            profile,
            monitoring,
        )
    }
}
