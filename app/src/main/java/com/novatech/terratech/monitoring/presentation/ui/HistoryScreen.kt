package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.core.presentation.format.timestamp
import com.novatech.terratech.monitoring.presentation.component.MoistureChart
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.Muted

@Composable
fun HistoryScreen(
    state: MonitoringState,
    onDays: (Int) -> Unit,
    onDetail: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { PageTitle(stringResource(R.string.history), state.selectedSensor?.name) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (days in listOf(7, 30)) FilterChip(
                    selected = state.days == days,
                    onClick = { onDays(days) },
                    label = {
                        Text(stringResource(if (days == 7) R.string.days_7 else R.string.days_30))
                    },
                )
            }
        }
        item {
            FarmCard {
                state.download?.let { d ->
                    d.fromUtc?.let {
                        Text(
                            timestamp(it) + " — " + timestamp(d.toUtc ?: state.now),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                    d.minimumMoisture?.let {
                        Text(
                            stringResource(R.string.threshold, number(it)),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Pill(stringResource(R.string.reading_count, state.history.size))
                MoistureChart(state.history, state.download?.minimumMoisture)
                if (state.download == null) {
                    Text(stringResource(R.string.not_downloaded))
                    OutlinedButton(onClick = onRefresh) {
                        Text(stringResource(R.string.range_download))
                    }
                }
            }
        }
        if (state.history.isEmpty()) item { EmptyCard(stringResource(R.string.no_history)) }
        items(state.history, key = { it.id }) { r ->
            Card(
                onClick = { onDetail(r.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(timestamp(r.recordedAt))
                        Text(r.source, style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                    Text(
                        number(r.moisturePercent) + " %",
                        color = FarmGreen,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}
