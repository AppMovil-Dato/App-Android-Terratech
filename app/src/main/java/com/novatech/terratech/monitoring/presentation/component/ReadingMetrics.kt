package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.ui.theme.*

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
