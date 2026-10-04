package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.*

@Composable
fun SensorsScreen(state: MonitoringState, onChoose: (Int) -> Unit, onRegister: () -> Unit) {
  LazyColumn(
    Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      PageTitle(
        state.selectedField?.name ?: stringResource(R.string.fields),
        stringResource(R.string.choose_sensor),
      )
    }
    item { PrimaryButton(stringResource(R.string.associate), !state.busy, onRegister) }
    if (state.fieldSensors.isEmpty()) item { EmptyCard(stringResource(R.string.no_sensors)) }
    items(state.fieldSensors, key = { it.id }) { sensor ->
      FarmCard {
        Text(
          sensor.name ?: stringResource(R.string.sensor),
          style = MaterialTheme.typography.titleLarge,
        )
        Text(sensor.sensorCode ?: sensor.macAddress, color = Muted)
        PrimaryButton(stringResource(R.string.view_sensor), !state.busy) { onChoose(sensor.id) }
      }
    }
  }
}
