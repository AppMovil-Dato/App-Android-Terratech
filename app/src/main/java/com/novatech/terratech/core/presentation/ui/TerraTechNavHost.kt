package com.novatech.terratech.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.CreateFieldScreen
import com.novatech.terratech.monitoring.presentation.ui.FieldsScreen
import com.novatech.terratech.monitoring.presentation.ui.HistoryScreen
import com.novatech.terratech.monitoring.presentation.ui.HomeScreen
import com.novatech.terratech.monitoring.presentation.ui.ReadingDetailScreen
import com.novatech.terratech.monitoring.presentation.ui.RegisterSensorScreen
import com.novatech.terratech.monitoring.presentation.ui.SensorScreen
import com.novatech.terratech.monitoring.presentation.ui.SensorsScreen
import com.novatech.terratech.monitoring.presentation.viewmodel.FieldLocationViewModel
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.profile.presentation.ui.ProfileScreen
import com.novatech.terratech.profile.presentation.viewmodel.ProfileViewModel

@Composable
internal fun TerraTechNavHost(
    navController: NavHostController,
    session: Session,
    profileState: ProfileState,
    monitoringState: MonitoringState,
    accountViewModel: AccountViewModel,
    profileViewModel: ProfileViewModel,
    monitoringViewModel: MonitoringViewModel,
    fieldLocation: FieldLocationViewModel = viewModel(),
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                profileState.profile?.fullName?.ifBlank { session.fullName } ?: session.fullName,
                monitoringState,
                profileState.profile != null,
                { navController.navigate("fields") },
                { navController.navigate("profile") },
                { navController.navigate("history") },
                { navController.navigate("sensor") },
                onCreateField = {
                    navController.navigate(
                        if (profileState.profile != null) "new-field" else "profile"
                    )
                },
                onConnectSensor = {
                    if (monitoringState.selectedField != null)
                        navController.navigate("register-sensor")
                    else navController.navigate("fields")
                },
                onSwitchSensor = { navController.navigate("sensors") },
            )
        }
        composable("fields") {
            FieldsScreen(
                monitoringState,
                profileState.profile != null,
                {
                    monitoringViewModel.selectField(it)
                    navController.navigate("sensors")
                },
                { navController.navigate("new-field") },
                { navController.navigate("profile") },
            )
        }
        composable("profile") {
            ProfileScreen(
                profileState,
                session,
                profileViewModel::save,
                profileViewModel::refresh,
                accountViewModel::logout,
                if (monitoringState.fields.isEmpty()) ({ navController.navigate("new-field") })
                else null,
            )
        }
        composable("new-field") {
            CreateFieldScreen(
                monitoringState.busy,
                location = fieldLocation,
                onCancel = {
                    navController.popBackStack()
                    monitoringViewModel.clearMessage()
                },
                onSave = { draft ->
                    profileState.profile?.let {
                        monitoringViewModel.createField(
                            it.id,
                            draft.name.value,
                            draft.crop,
                            draft.area.value / 10000,
                            draft.soil,
                            draft.coordinates.latitude,
                            draft.coordinates.longitude,
                            draft.boundary,
                        )
                    }
                },
            )
        }
        composable("sensors") {
            SensorsScreen(
                monitoringState,
                {
                    monitoringViewModel.selectDevice(it)
                    navController.navigate("sensor")
                },
                { navController.navigate("register-sensor") },
            )
        }
        composable("register-sensor") {
            RegisterSensorScreen(
                monitoringState.busy,
                monitoringViewModel::registerSensor,
                monitoringState.selectedField?.name.orEmpty(),
                monitoringState.error,
                monitoringViewModel::clearMessage,
            )
        }
        composable("sensor") { SensorScreen(monitoringState) { navController.navigate("history") } }
        composable("history") {
            HistoryScreen(
                monitoringState,
                monitoringViewModel::days,
                { id ->
                    navController.navigate("reading/$id")
                    monitoringViewModel.detail(id)
                },
                monitoringViewModel::refresh,
            )
        }
        composable("reading/{id}") { backStack ->
            ReadingDetailScreen(
                monitoringState.readings.find {
                    it.id == backStack.arguments?.getString("id")?.toIntOrNull()
                }
            )
        }
    }
}
