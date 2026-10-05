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
        state.selectedField?.let { field ->
            item {
                com.novatech.terratech.monitoring.presentation.component.FieldMapView(
                    field.boundary.ifEmpty {
                        listOf(
                            com.novatech.terratech.monitoring.domain.valueobject.Coordinates.of(
                                field.latitude,
                                field.longitude,
                            )
                        )
                    },
                    field.boundary.isNotEmpty(),
                    Modifier.fillMaxWidth().height(210.dp),
                    interactive = false,
                )
            }
            item {
                Text(
                    com.novatech.terratech.core.presentation.format.areaNumber(
                        field.sizeM2 / 10000
                    ) + " ha · " + field.cropName.orEmpty(),
                    color = Muted,
                )
            }
        }
        item { PrimaryButton(stringResource(R.string.associate), !state.busy, onRegister) }
        if (state.fieldSensors.isEmpty())
            item {
                EmptyCard(
                    stringResource(R.string.no_sensors),
                    stringResource(R.string.field_sensor_empty_help),
                )
            }
        items(state.fieldSensors, key = { it.id }) { sensor ->
            FarmCard {
                Text(
                    sensor.name ?: stringResource(R.string.sensor),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(sensor.sensorCode ?: sensor.macAddress, color = Muted)
                PrimaryButton(stringResource(R.string.view_sensor), !state.busy) {
                    onChoose(sensor.id)
                }
            }
        }
    }
}
