package com.novatech.terratech.core.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.*
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.Notice
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.viewmodel.ProfileViewModel

@Composable
internal fun SignedInApp(
    session: com.novatech.terratech.iam.domain.entity.Session,
    auth: com.novatech.terratech.iam.presentation.state.AccountState,
    p: com.novatech.terratech.profile.presentation.state.ProfileState,
    m: com.novatech.terratech.monitoring.presentation.state.MonitoringState,
    account: AccountViewModel,
    profile: ProfileViewModel,
    monitoring: MonitoringViewModel,
) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    var reauth by remember { mutableStateOf(false) }
    var reauthToken by remember { mutableStateOf(session.token) }
    if (reauth) {
        BackHandler { reauth = false }
        AccountScreen(
            auth,
            onLogin = { email, password -> account.login(email, password) },
            onRegister = account::register,
            onClear = account::clearMessage,
            reauth = true,
        )
        LaunchedEffect(session.token) { if (session.token != reauthToken) reauth = false }
        return
    }
    val snackbar = remember { SnackbarHostState() }
    val createdFieldMessage = stringResource(R.string.field_created)
    val createdSensorMessage = stringResource(R.string.sensor_created)
    LaunchedEffect(m.created) {
        if (m.created) {
            val fieldCreated = m.createdFieldId != null
            nav.navigate(if (fieldCreated) "sensors" else "sensor") {
                popUpTo(if (fieldCreated) "new-field" else "register-sensor") { inclusive = true }
                launchSingleTop = true
            }
            monitoring.clearMessage()
            snackbar.showSnackbar(if (fieldCreated) createdFieldMessage else createdSensorMessage)
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (route !in listOf("new-field", "register-sensor"))
                TerraTechBottomBar(route) { destination ->
                    monitoring.clearMessage()
                    nav.navigate(destination) {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (m.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (route != "profile") Notice(m.error)
            if (m.error == "UNAUTHENTICATED" || p.error == "UNAUTHENTICATED" || session.expired())
                TextButton(
                    onClick = {
                        reauthToken = session.token
                        reauth = true
                    }
                ) {
                    Text(stringResource(R.string.reauth))
                }
            if (route != "new-field")
                com.novatech.terratech.core.presentation.component.TerraTechToolbar(
                    title =
                        when (route) {
                            "sensors" -> m.selectedField?.name.orEmpty()
                            "sensor",
                            "history" -> m.selectedSensor?.name.orEmpty()
                            "register-sensor" -> stringResource(R.string.associate)
                            else -> p.profile?.fundoName ?: "TerraTech"
                        },
                    busy = m.busy,
                    onBack =
                        if (route !in listOf("home", "fields", "profile"))
                            ({
                                nav.popBackStack()
                                monitoring.clearMessage()
                            })
                        else null,
                    onRefresh =
                        if (route !in listOf("profile", "register-sensor")) monitoring::refresh
                        else null,
                )
            Box(Modifier.weight(1f)) {
                TerraTechNavHost(nav, session, p, m, account, profile, monitoring)
            }
        }
    }
}
