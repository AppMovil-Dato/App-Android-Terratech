package com.novatech.terratech.core.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.Notice
import com.novatech.terratech.core.presentation.component.TerraTechToolbar
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.presentation.state.AccountState
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.profile.presentation.viewmodel.ProfileViewModel

@Composable
internal fun SignedInApp(
    session: Session,
    accountState: AccountState,
    profileState: ProfileState,
    monitoringState: MonitoringState,
    accountViewModel: AccountViewModel,
    profileViewModel: ProfileViewModel,
    monitoringViewModel: MonitoringViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: "home"
    var reauth by remember { mutableStateOf(false) }
    var reauthToken by remember { mutableStateOf(session.token) }
    if (reauth) {
        BackHandler { reauth = false }
        AccountScreen(
            accountState,
            onLogin = { email, password -> accountViewModel.login(email, password) },
            onRegister = accountViewModel::register,
            onClear = accountViewModel::clearMessage,
            reauth = true,
        )
        LaunchedEffect(session.token) { if (session.token != reauthToken) reauth = false }
        return
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val createdFieldMessage = stringResource(R.string.field_created)
    val createdSensorMessage = stringResource(R.string.sensor_created)
    LaunchedEffect(monitoringState.created) {
        if (monitoringState.created) {
            val fieldCreated = monitoringState.createdFieldId != null
            navController.navigate(if (fieldCreated) "sensors" else "sensor") {
                popUpTo(if (fieldCreated) "new-field" else "register-sensor") { inclusive = true }
                launchSingleTop = true
            }
            monitoringViewModel.clearMessage()
            snackbarHostState.showSnackbar(
                if (fieldCreated) createdFieldMessage else createdSensorMessage
            )
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (route !in listOf("new-field", "register-sensor"))
                TerraTechBottomBar(route) { destination ->
                    monitoringViewModel.clearMessage()
                    navController.navigate(destination) {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (monitoringState.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (route != "profile") Notice(monitoringState.error)
            if (
                monitoringState.error == "UNAUTHENTICATED" ||
                    profileState.error == "UNAUTHENTICATED" ||
                    session.expired()
            )
                TextButton(
                    onClick = {
                        reauthToken = session.token
                        reauth = true
                    }
                ) {
                    Text(stringResource(R.string.reauth))
                }
            if (route != "new-field")
                TerraTechToolbar(
                    title =
                        when (route) {
                            "sensors" -> monitoringState.selectedField?.name.orEmpty()
                            "sensor",
                            "history" -> monitoringState.selectedSensor?.name.orEmpty()
                            "register-sensor" -> stringResource(R.string.associate)
                            else -> profileState.profile?.fundoName ?: "TerraTech"
                        },
                    busy = monitoringState.busy,
                    onBack =
                        if (route !in listOf("home", "fields", "profile"))
                            ({
                                navController.popBackStack()
                                monitoringViewModel.clearMessage()
                            })
                        else null,
                    onRefresh =
                        if (route !in listOf("profile", "register-sensor"))
                            monitoringViewModel::refresh
                        else null,
                )
            Box(Modifier.weight(1f)) {
                TerraTechNavHost(
                    navController,
                    session,
                    profileState,
                    monitoringState,
                    accountViewModel,
                    profileViewModel,
                    monitoringViewModel,
                )
            }
        }
    }
}
