package com.novatech.terratech.core.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.navigation.AppDestination
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.profile.presentation.state.ProfileState

@Composable
fun AppToolbar(
    destination: AppDestination,
    monitoringState: MonitoringState,
    profileState: ProfileState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    if (!destination.showToolbar) return
    val title =
        when (destination) {
            AppDestination.SENSORS -> monitoringState.selectedField?.name.orEmpty()
            AppDestination.SENSOR,
            AppDestination.HISTORY,
            AppDestination.READING -> monitoringState.selectedSensor?.name.orEmpty()
            AppDestination.REGISTER_SENSOR -> stringResource(R.string.associate)
            else -> profileState.profile?.fundoName ?: "TerraTech"
        }
    TerraTechToolbar(
        title = title,
        busy = monitoringState.busy,
        onBack = if (destination.isRoot) null else onBack,
        onRefresh = if (destination.canRefresh) onRefresh else null,
    )
}
