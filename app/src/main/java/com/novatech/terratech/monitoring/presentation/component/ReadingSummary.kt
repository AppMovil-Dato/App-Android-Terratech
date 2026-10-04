package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.format.timestamp
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.*

@Composable
internal fun ReadingSummary(reading: Reading, state: MonitoringState) {
  val minimum = state.download?.minimumMoisture
  FarmCard {
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      MoistureRing(
        reading.moisturePercent,
        minimum != null && reading.moisturePercent < minimum,
      )
      Column(
        Modifier.weight(1f).padding(start = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(stringResource(R.string.moisture), style = MaterialTheme.typography.titleMedium)
        if (minimum != null) {
          Pill(
            stringResource(
              if (minimum != null && reading.moisturePercent < minimum) R.string.below_threshold
              else R.string.within_reference
            ),
            minimum != null && reading.moisturePercent < minimum,
          )
        }
      }
    }
    Pill(reading.source)
    Text(
      stringResource(R.string.updated, timestamp(reading.recordedAt)),
      color = Muted,
      style = MaterialTheme.typography.bodyMedium,
    )
    if (reading.isStale(state.now)) Pill(stringResource(R.string.old_data), true)
    state.download?.downloadedAt?.let {
      Text(
        stringResource(R.string.downloaded, timestamp(it)),
        color = Muted,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}
