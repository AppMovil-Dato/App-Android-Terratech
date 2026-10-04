package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.domain.entity.Reading
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

@Composable
fun SensorScreen(state: MonitoringState, onHistory: () -> Unit) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      PageTitle(
        state.selectedSensor?.name ?: stringResource(R.string.sensor_detail),
        state.selectedSensor?.sensorCode,
      )
    }
    state.latest?.let { r ->
      item { ReadingSummary(r, state) }
      item { ReadingMetrics(r) }
    }
      ?: item {
        EmptyCard(stringResource(R.string.no_readings), stringResource(R.string.no_readings_body))
      }
    item { PrimaryButton(stringResource(R.string.view_history), onClick = onHistory) }
  }
}

@Composable
private fun ReadingSummary(reading: Reading, state: MonitoringState) {
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

@Composable
fun ReadingMetrics(r: Reading) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Metric(
        stringResource(R.string.temperature),
        number(r.soilTemperatureC) + " °C",
        Modifier.weight(1f),
      )
      Metric(stringResource(R.string.nitrogen), number(r.nitrogenPpm) + " ppm", Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Metric(
        stringResource(R.string.phosphorus),
        number(r.phosphorusPpm) + " ppm",
        Modifier.weight(1f),
      )
      Metric(
        stringResource(R.string.potassium),
        number(r.potassiumPpm) + " ppm",
        Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier) {
  FarmCard(modifier) {
    Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
    Text(value, style = MaterialTheme.typography.titleLarge)
  }
}

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
          label = { Text(stringResource(if (days == 7) R.string.days_7 else R.string.days_30)) },
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
          OutlinedButton(onClick = onRefresh) { Text(stringResource(R.string.range_download)) }
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

@Composable
fun ReadingDetailScreen(reading: Reading?) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item { PageTitle(stringResource(R.string.reading_detail)) }
    if (reading == null) item { EmptyCard(stringResource(R.string.error_missing)) }
    else {
      item {
        FarmCard {
          Text(
            stringResource(R.string.reading_id, reading.id),
            style = MaterialTheme.typography.titleLarge,
          )
          Text(timestamp(reading.recordedAt))
          Pill(reading.source)
          Text(stringResource(R.string.moisture) + ": " + number(reading.moisturePercent) + " %")
        }
      }
      item { ReadingMetrics(reading) }
      item {
        Text(
          stringResource(R.string.units_note),
          color = Muted,
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }
  }
}

@Composable
private fun MoistureRing(value: Double, warning: Boolean) {
  val label = number(value) + " %"
  Box(
    Modifier.size(130.dp).semantics(mergeDescendants = true) { contentDescription = label },
    contentAlignment = Alignment.Center,
  ) {
    Canvas(Modifier.fillMaxSize()) {
      val stroke = 10.dp.toPx()
      val inset = stroke / 2
      val size = Size(size.width - stroke, size.height - stroke)
      drawArc(LeafLight, 0f, 360f, false, Offset(inset, inset), size, style = Stroke(stroke))
      drawArc(
        if (warning) Amber else FarmGreen,
        -90f,
        (value.coerceIn(0.0, 100.0) * 3.6).toFloat(),
        false,
        Offset(inset, inset),
        size,
        style = Stroke(stroke, cap = StrokeCap.Round),
      )
    }
    Text(label, style = MaterialTheme.typography.headlineMedium)
  }
}

@Composable
fun MoistureChart(rows: List<Reading>, minimum: Double?) {
  val description = stringResource(R.string.chart_description)
  Canvas(Modifier.fillMaxWidth().height(170.dp).semantics { contentDescription = description }) {
    val left = 12.dp.toPx()
    val width = size.width - left * 2
    val height = size.height - 20.dp.toPx()
    for (i in 0..4) {
      val y = height * i / 4
      drawLine(Color(0xFFE5EDE8), Offset(left, y), Offset(left + width, y), 1.dp.toPx())
    }
    minimum?.let {
      val y = height * (1 - it.coerceIn(0.0, 100.0).toFloat() / 100)
      drawLine(
        Amber,
        Offset(left, y),
        Offset(left + width, y),
        2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
      )
    }
    if (rows.isNotEmpty()) {
      val first = rows.first().recordedAt.toEpochMilli()
      val range = (rows.last().recordedAt.toEpochMilli() - first).coerceAtLeast(1)
      fun point(r: Reading) =
        Offset(
          left + width * ((r.recordedAt.toEpochMilli() - first).toDouble() / range).toFloat(),
          height * (1 - r.moisturePercent.coerceIn(0.0, 100.0).toFloat() / 100),
        )
      val path = Path()
      rows.forEachIndexed { i, r ->
        val p = point(r)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
      }
      val fill =
        Path().apply {
          addPath(path)
          lineTo(point(rows.last()).x, height)
          lineTo(point(rows.first()).x, height)
          close()
        }
      drawPath(fill, FarmGreen.copy(alpha = .1f))
      drawPath(path, FarmGreen, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
      if (rows.size == 1) drawCircle(FarmGreen, 4.dp.toPx(), point(rows.first()))
    }
  }
}
