
package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.format.areaNumber
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.presentation.component.FieldMapView
import com.novatech.terratech.monitoring.presentation.component.FertilityStatusCard
import com.novatech.terratech.monitoring.presentation.component.FertilityZoneOverlay
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.Muted

@Composable
fun SensorsScreen(
    state: MonitoringState,
    onChoose: (Int) -> Unit,
    onRegister: () -> Unit,
) {
    var selectedZoneName by remember(state.fieldId) {
        mutableStateOf<String?>(null)
    }

    val zones = state.selectedField?.let { field ->
        state.fertilityZones
            .filter { it.fieldId == field.id }
            .map { zone ->
                FertilityZoneOverlay(
                    zoneId = zone.id,
                    fieldId = zone.fieldId,
                    deviceId = zone.deviceId,
                    name = zone.name,
                    boundary = zone.boundary,
                    level = null
                )
            }
    } ?: emptyList()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageTitle(
                state.selectedField?.name
                    ?: stringResource(R.string.fields),
                stringResource(R.string.choose_sensor),
            )
        }

        state.selectedField?.let { field ->
            item {
                FieldMapView(
                    points = field.boundary.ifEmpty {
                        listOf(
                            Coordinates.of(
                                field.latitude,
                                field.longitude
                            )
                        )
                    },
                    drawing = field.boundary.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    interactive = true,
                    fertilityZones = zones,
                    onZoneClick = { zone ->
                        selectedZoneName = zone.name
                    }
                )
            }

            // US13: mostrar las zonas recibidas del backend
            item {
                Text(
                    text = "Fertility zones: ${zones.size}",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            items(
                zones,
                key = { "fertility-zone-${it.zoneId}" }
            ) { zone ->
                Text(
                    text = zone.name,
                    color = Muted
                )
            }

            if (selectedZoneName != null) {
                item {
                    Text(
                        text = "Selected zone: $selectedZoneName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            state.fertilityZonesError?.let { error ->
                item {
                    Text(
                        text = "Error loading fertility zones: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Text(
                    areaNumber(field.sizeM2 / 10000) +
                        " ha · " +
                        field.cropName.orEmpty(),
                    color = Muted,
                )
            }

            item {
                FertilityStatusCard(state)
            }
        }

        item {
            PrimaryButton(
                stringResource(R.string.associate),
                !state.busy,
                onRegister,
            )
        }

        if (state.fieldSensors.isEmpty()) {
            item {
                EmptyCard(
                    stringResource(R.string.no_sensors),
                    stringResource(R.string.field_sensor_empty_help),
                )
            }
        }

        items(
            state.fieldSensors,
            key = { it.id },
        ) { sensor ->
            FarmCard {
                Text(
                    sensor.name
                        ?: stringResource(R.string.sensor),
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    sensor.sensorCode ?: sensor.macAddress,
                    color = Muted,
                )

                PrimaryButton(
                    stringResource(R.string.view_sensor),
                    !state.busy,
                ) {
                    onChoose(sensor.id)
                }
            }
        }
    }
}
