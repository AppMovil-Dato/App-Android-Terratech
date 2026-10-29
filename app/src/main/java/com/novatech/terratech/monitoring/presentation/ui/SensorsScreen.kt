
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.presentation.component.FieldMapView
import com.novatech.terratech.monitoring.presentation.component.FertilityStatusCard
import com.novatech.terratech.monitoring.presentation.component.FertilityZoneOverlay
import com.novatech.terratech.monitoring.presentation.component.FertilityZoneThresholdsContent
import com.novatech.terratech.monitoring.presentation.component.FertilityThresholdAlertsCard
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

    // US18: configuracion de umbrales
    onLoadZoneThresholds: (Int) -> Unit = {},
    onSaveZoneThresholds: (FertilityZoneThresholds) -> Unit = {},
    onResetZoneThresholds: () -> Unit = {},
    onCloseZoneThresholds: () -> Unit = {},

    // Clima e imagenes satelitales
    onRefreshWeather: () -> Unit = {},
    onSelectSatelliteDate: (String) -> Unit = {},
    onToggleSatelliteMode: () -> Unit = {},
    onCompareVegetation: () -> Unit = {},
) {

    var selectedZoneName by remember(state.fieldId) {
        mutableStateOf<String?>(null)
    }

    // US13: zonas de fertilidad de la parcela
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            PageTitle(
                state.selectedField?.name
                    ?: stringResource(R.string.fields),
                stringResource(R.string.choose_sensor)
            )
        }

        state.selectedField?.let { field ->

            // Recomendacion de suspension del riego
            if (
                state.weather
                    ?.combinedRecommendation
                    ?.suspensionRecommended == true
            ) {
                item {
                    IrrigationSuspensionBanner(
                        state.weather.combinedRecommendation
                    )
                }
            }

            // Pronostico del clima
            item {
                WeatherForecastCard(
                    weather = state.weather,
                    isLoading = state.weatherBusy,
                    errorMessage = state.weatherError,
                    onRefresh = onRefreshWeather,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mapa de la parcela e imagenes satelitales
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
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = stringResource(
                                    R.string.map_view
                                ),
                                style = MaterialTheme.typography.titleMedium
                            )

                            TextButton(
                                onClick = onToggleSatelliteMode
                            ) {
                                Text(
                                    stringResource(
                                        R.string.satellite_view
                                    )
                                )
                            }
                        }

                        // US13: mapa interactivo de fertilidad
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
                                onLoadZoneThresholds(zone.zoneId)
                            }
                        )
                    }
                }
            }

            // US13: cantidad de zonas de fertilidad
            item {
                Text(
                    text = "Fertility zones: ${zones.size}",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // US18: seleccionar zona para configurar umbrales
            items(
                items = zones,
                key = { "fertility-zone-${it.zoneId}" }
            ) { zone ->

                FarmCard {

                    Text(
                        text = zone.name,
                        color = Muted
                    )

                    PrimaryButton(
                        "Configurar umbrales",
                        !state.thresholdsBusy
                    ) {
                        selectedZoneName = zone.name
                        onLoadZoneThresholds(zone.zoneId)
                    }
                }
            }

            selectedZoneName?.let { zoneName ->
                item {
                    Text(
                        text = "Selected zone: $zoneName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // US18: configuracion de umbrales de la zona
            state.thresholdZoneId?.let { zoneId ->

                item {
                    Text(
                        text = "Umbrales de la zona $zoneId",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {

                    val thresholds = state.zoneThresholds

                    if (thresholds == null) {

                        Text(
                            text = if (state.thresholdsBusy) {
                                "Cargando umbrales..."
                            } else {
                                "No se pudieron cargar los umbrales."
                            }
                        )

                        state.thresholdsError?.let { error ->
                            Text(
                                text = "Error: $error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                    } else {

                        FertilityZoneThresholdsContent(
                            thresholds = thresholds,
                            busy = state.thresholdsBusy,
                            error = state.thresholdsError,
                            saved = state.thresholdsSaved,
                            onSave = onSaveZoneThresholds,
                            onReset = onResetZoneThresholds,
                            onClose = onCloseZoneThresholds
                        )
                    }
                }

                // US18: comparar lectura del sensor
                // con los umbrales de fertilidad
                state.zoneThresholds?.let { thresholds ->

                    val selectedZone = zones.find {
                        it.zoneId == zoneId
                    }

                    val zoneDeviceId = selectedZone?.deviceId

                    val zoneReading = state.readings
                        .filter {
                            it.deviceId == zoneDeviceId
                        }
                        .maxByOrNull {
                            it.recordedAt
                        }

                    item {

                        if (
                            zoneDeviceId != null &&
                            state.deviceId != zoneDeviceId
                        ) {

                            FarmCard {
                                Text(
                                    text = "Selecciona el sensor asociado " +
                                        "a esta zona para consultar sus alertas.",
                                    color = Muted
                                )
                            }

                        } else {

                            FertilityThresholdAlertsCard(
                                reading = zoneReading,
                                thresholds = thresholds,
                                now = state.now
                            )
                        }
                    }
                }
            }

            // US13: mostrar error al consultar zonas
            state.fertilityZonesError?.let { error ->

                item {
                    Text(
                        text = "Error loading fertility zones: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Datos generales de la parcela
            item {
                Text(
                    text = areaNumber(field.sizeM2 / 10000) +
                        " ha · " +
                        field.cropName.orEmpty(),
                    color = Muted
                )
            }

            // US13: estado general de fertilidad
            item {
                FertilityStatusCard(state)
            }
        }

        // Registro de sensores
        item {
            PrimaryButton(
                stringResource(R.string.associate),
                !state.busy,
                onRegister
            )
        }

        if (state.fieldSensors.isEmpty()) {

            item {
                EmptyCard(
                    stringResource(R.string.no_sensors),
                    stringResource(R.string.field_sensor_empty_help)
                )
            }

        }

        // Lista de sensores asociados a la parcela
        items(
            items = state.fieldSensors,
            key = { it.id }
        ) { sensor ->

            FarmCard {

                Text(
                    text = sensor.name
                        ?: stringResource(R.string.sensor),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = sensor.sensorCode ?: sensor.macAddress,
                    color = Muted
                )

                PrimaryButton(
                    stringResource(R.string.view_sensor),
                    !state.busy
                ) {
                    onChoose(sensor.id)
                }
            }
        }
    }
}
