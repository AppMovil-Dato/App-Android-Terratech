package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.component.MoistureChart
import com.novatech.terratech.monitoring.presentation.component.ReadingMetrics
import com.novatech.terratech.monitoring.presentation.component.ReadingSummary
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.*

@Composable
fun HomeScreen(
  name: String,
  state: MonitoringState,
  hasProfile: Boolean,
  onFields: () -> Unit,
  onProfile: () -> Unit,
  onHistory: () -> Unit,
  onSensor: () -> Unit,
) {
  LazyColumn(
    Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      PageTitle(
        stringResource(R.string.hello, name.substringBefore(' ')),
        stringResource(R.string.farm_summary),
      )
    }
    if (!hasProfile)
      item {
        EmptyCard(
          stringResource(R.string.complete_profile),
          action = { PrimaryButton(stringResource(R.string.use_profile), onClick = onProfile) },
        )
      }
    item {
      FarmCard {
        Text(stringResource(R.string.active_field), color = Muted)
        Text(
          state.selectedField?.name ?: stringResource(R.string.choose_field),
          style = MaterialTheme.typography.titleLarge,
        )
        state.selectedSensor?.let { Text(it.name ?: it.sensorCode.orEmpty(), color = Muted) }
        OutlinedButton(onClick = onFields, modifier = Modifier.fillMaxWidth()) {
          Text(stringResource(R.string.choose_field))
        }
      }
    }
    val latest = state.latest
    if (latest != null) {
      item { ReadingSummary(latest, state) }
      item { ReadingMetrics(latest) }
      item {
        FarmCard {
          Text(stringResource(R.string.history), style = MaterialTheme.typography.titleMedium)
          MoistureChart(state.history, state.download?.minimumMoisture)
          PrimaryButton(stringResource(R.string.view_history), onClick = onHistory)
        }
      }
      item {
        OutlinedButton(onClick = onSensor, modifier = Modifier.fillMaxWidth()) {
          Text(stringResource(R.string.sensor_detail))
        }
      }
    } else
      item {
        EmptyCard(
          stringResource(R.string.no_readings),
          stringResource(if (state.offline) R.string.not_downloaded else R.string.no_readings_body),
        )
      }
  }
}
