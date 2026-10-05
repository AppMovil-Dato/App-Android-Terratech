package com.novatech.terratech.core.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.novatech.terratech.iam.presentation.viewmodel.AccountViewModel
import com.novatech.terratech.monitoring.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.viewmodel.FieldLocationViewModel
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import com.novatech.terratech.profile.presentation.ui.ProfileScreen
import com.novatech.terratech.profile.presentation.viewmodel.ProfileViewModel

@Composable
internal fun TerraTechNavHost(
    nav: NavHostController,
    session: com.novatech.terratech.iam.domain.entity.Session,
    p: com.novatech.terratech.profile.presentation.state.ProfileState,
    m: com.novatech.terratech.monitoring.presentation.state.MonitoringState,
    account: AccountViewModel,
    profile: ProfileViewModel,
    monitoring: MonitoringViewModel,
    fieldLocation: FieldLocationViewModel = viewModel(),
) {
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
                onCreateField = { nav.navigate(if (p.profile != null) "new-field" else "profile") },
                onConnectSensor = {
                    if (m.selectedField != null) nav.navigate("register-sensor")
                    else nav.navigate("fields")
                },
                onSwitchSensor = { nav.navigate("sensors") },
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
            ProfileScreen(
                p,
                session,
                profile::save,
                profile::refresh,
                account::logout,
                if (m.fields.isEmpty()) ({ nav.navigate("new-field") }) else null,
            )
        }
        composable("new-field") {
            CreateFieldScreen(
                m.busy,
                location = fieldLocation,
                onCancel = {
                    nav.popBackStack()
                    monitoring.clearMessage()
                },
                onSave = { draft ->
                    p.profile?.let {
                        monitoring.createField(
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
                m,
                {
                    monitoring.selectDevice(it)
                    nav.navigate("sensor")
                },
                { nav.navigate("register-sensor") },
            )
        }
        composable("register-sensor") {
            RegisterSensorScreen(
                m.busy,
                monitoring::registerSensor,
                m.selectedField?.name.orEmpty(),
                m.error,
                monitoring::clearMessage,
            )
        }
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
