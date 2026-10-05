package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.monitoring.domain.entity.Reading

@Composable
fun ReadingMetrics(reading: Reading) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric(
                stringResource(R.string.temperature),
                number(reading.soilTemperatureC) + " °C",
                Modifier.weight(1f),
            )
            Metric(
                stringResource(R.string.nitrogen),
                number(reading.nitrogenPpm) + " ppm",
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric(
                stringResource(R.string.phosphorus),
                number(reading.phosphorusPpm) + " ppm",
                Modifier.weight(1f),
            )
            Metric(
                stringResource(R.string.potassium),
                number(reading.potassiumPpm) + " ppm",
                Modifier.weight(1f),
            )
        }
    }
}
