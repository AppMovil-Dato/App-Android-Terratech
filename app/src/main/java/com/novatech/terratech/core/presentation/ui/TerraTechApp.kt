package com.novatech.terratech.core.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.state.forUser
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.ui.ProfileScreen
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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

@Composable
private fun SignedInApp(
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
      NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
        listOf("home" to R.string.home, "fields" to R.string.fields, "profile" to R.string.profile)
          .forEach { (destination, label) ->
            NavigationBarItem(
              selected = route == destination,
              onClick = {
                monitoring.clearMessage()
                nav.navigate(destination) {
                  popUpTo("home")
                  launchSingleTop = true
                }
              },
              icon = {
                if (destination == "fields")
                  Icon(painterResource(R.drawable.ic_fields), contentDescription = null)
                else
                  Icon(
                    if (destination == "home") Icons.Outlined.Home else Icons.Outlined.Person,
                    contentDescription = null,
                  )
              },
              label = { Text(stringResource(label)) },
            )
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
        NavHost(navController = nav, startDestination = "home") {
          composable("home") {
            HomeScreen(
              p.profile?.fullName?.ifBlank { session.fullName } ?: session.fullName,
              m,
              p.profile != null,
              { nav.navigate("fields") },
              { nav.navigate("profile") },
              { nav.navigate("history") },
              { nav.navigate("sensor") },
            )
          }
          composable("fields") {
            FieldsScreen(
              m,
              p.profile != null,
              {
                monitoring.selectField(it)
                nav.navigate("sensors")
              },
              { nav.navigate("new-field") },
              { nav.navigate("profile") },
            )
          }
          composable("profile") {
            ProfileScreen(p, session, profile::save, profile::refresh, account::logout)
          }
          composable("new-field") {
            CreateFieldScreen(m.busy) { name, crop, area, soil, lat, lon ->
              p.profile?.let { monitoring.createField(it.id, name, crop, area, soil, lat, lon) }
            }
          }
          composable("sensors") {
            SensorsScreen(
              m,
              {
                monitoring.selectDevice(it)
                nav.navigate("sensor")
              },
              { nav.navigate("register-sensor") },
            )
          }
          composable("register-sensor") { RegisterSensorScreen(m.busy, monitoring::registerSensor) }
          composable("sensor") { SensorScreen(m) { nav.navigate("history") } }
          composable("history") {
            HistoryScreen(
              m,
              monitoring::days,
              { id ->
                nav.navigate("reading/$id")
                monitoring.detail(id)
              },
              monitoring::refresh,
            )
          }
          composable("reading/{id}") { backStack ->
            ReadingDetailScreen(
              m.readings.find { it.id == backStack.arguments?.getString("id")?.toIntOrNull() }
            )
          }
        }
      }
    }
  }
}
