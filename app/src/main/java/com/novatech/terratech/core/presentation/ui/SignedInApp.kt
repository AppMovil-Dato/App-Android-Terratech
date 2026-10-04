package com.novatech.terratech.core.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
  LaunchedEffect(m.created) {
    if (m.created) {
      if (route == "new-field") nav.popBackStack("fields", false)
      else if (route == "register-sensor") nav.popBackStack("sensors", false)
      monitoring.clearMessage()
    }
  }
  Scaffold(
    bottomBar = {
      TerraTechBottomBar(route) { destination ->
        monitoring.clearMessage()
        nav.navigate(destination) {
          popUpTo("home")
          launchSingleTop = true
        }
      }
    }
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
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (route !in listOf("home", "fields", "profile"))
          TextButton(
            onClick = {
              nav.popBackStack()
              monitoring.clearMessage()
            }
          ) {
            Text(stringResource(R.string.back))
          }
        Spacer(Modifier.weight(1f))
        if (route !in listOf("profile", "new-field", "register-sensor"))
          TextButton(onClick = monitoring::refresh, enabled = !m.busy) {
            Text(stringResource(R.string.retry))
          }
      }
      Box(Modifier.weight(1f)) {
        TerraTechNavHost(nav, session, p, m, account, profile, monitoring)
      }
    }
  }
}
