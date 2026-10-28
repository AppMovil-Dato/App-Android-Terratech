package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.novatech.terratech.monitoring.presentation.component.IrrigationSuspensionBanner
import com.novatech.terratech.monitoring.presentation.component.SatelliteOverlayViewer
import com.novatech.terratech.monitoring.presentation.component.WeatherForecastCard
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.Muted

@Composable
fun SensorsScreen(
    state: MonitoringState,
    onChoose: (Int) -> Unit,
    onRegister: () -> Unit,
    onRefreshWeather: () -> Unit = {},
    onSelectSatelliteDate: (String) -> Unit = {},
    onToggleSatelliteMode: () -> Unit = {},
    onCompareVegetation: () -> Unit = {},
) {
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
            if (state.weather?.combinedRecommendation?.suspensionRecommended == true) {
                item {
                    IrrigationSuspensionBanner(state.weather.combinedRecommendation)
                }
            }
            item {
                WeatherForecastCard(
                    weather = state.weather,
                    isLoading = state.weatherBusy,
                    errorMessage = state.weatherError,
                    onRefresh = onRefreshWeather,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                if (state.isSatelliteView) {
                    SatelliteOverlayViewer(
                        satelliteImage = state.satelliteImage,
                        isSatelliteMode = true,
                        isLoading = state.satelliteBusy,
                        errorMessage = state.satelliteError,
                        onToggleMode = onToggleSatelliteMode,
                        onSelectDate = onSelectSatelliteDate,
                        onNavigateToCompare = onCompareVegetation,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.map_view),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            TextButton(onClick = onToggleSatelliteMode) {
                                Text(stringResource(R.string.satellite_view))
                            }
                        }
                        FieldMapView(
                            field.boundary.ifEmpty {
                                listOf(Coordinates.of(field.latitude, field.longitude))
                            },
                            field.boundary.isNotEmpty(),
                            Modifier.fillMaxWidth().height(210.dp),
                            interactive = false,
                        )
                    }
                }
            }
            item {
                Text(
                    areaNumber(field.sizeM2 / 10000) + " ha · " + field.cropName.orEmpty(),
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
