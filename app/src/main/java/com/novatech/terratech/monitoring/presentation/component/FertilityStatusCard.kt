package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.core.presentation.format.timestamp
import com.novatech.terratech.monitoring.domain.valueobject.MoistureLevel
import com.novatech.terratech.monitoring.domain.valueobject.classifyMoisture
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.Amber
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.Muted

@Composable
fun FertilityStatusCard(state: MonitoringState) {
    FarmCard {
        Text(
            stringResource(R.string.fertility_title),
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            stringResource(R.string.fertility_scope),
            color = Muted,
        )

        val sensor = state.selectedSensor
        val reading = state.latest?.takeIf {
            it.deviceId == sensor?.id
        }

        when {
            state.fieldSensors.isEmpty() -> {
                Text(
                    stringResource(R.string.fertility_no_sensors),
                    color = Muted,
                )
            }

            sensor == null -> {
                Text(
                    stringResource(R.string.fertility_select_sensor),
                    color = Muted,
                )
            }

            reading == null -> {
                Text(
                    stringResource(R.string.fertility_no_readings),
                    color = Muted,
                )
            }

            else -> {
                Text(
                    stringResource(
                        R.string.fertility_sensor_name,
                        sensor.name
                            ?: sensor.sensorCode
                            ?: sensor.macAddress,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    stringResource(
                        R.string.fertility_measured_at,
                        timestamp(reading.recordedAt),
                    ),
                    color = Muted,
                )

                if (
                    reading.source.equals(
                        "SIMULATED",
                        ignoreCase = true,
                    )
                ) {
                    Text(
                        stringResource(R.string.simulated),
                        color = Muted,
                    )
                }

                if (state.offline) {
                    Text(
                        stringResource(R.string.offline),
                        color = Muted,
                    )
                }

                Text(
                    stringResource(
                        R.string.fertility_moisture_value,
                        number(reading.moisturePercent),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )

                val moistureLevel = classifyMoisture(
                    reading.moisturePercent
                )

                if (moistureLevel == null) {
                    Text(
                        stringResource(R.string.fertility_invalid),
                        color = Amber,
                    )
                } else if (reading.isStale(state.now)) {
                    Text(
                        stringResource(R.string.fertility_stale),
                        color = Amber,
                    )
                } else {
                    val critical =
                        moistureLevel == MoistureLevel.CRITICAL

                    val moderate =
                        moistureLevel == MoistureLevel.MODERATE

                    val statusRes = when {
                        critical -> R.string.fertility_critical
                        moderate -> R.string.fertility_moderate
                        else -> R.string.fertility_optimal
                    }

                    val actionRes = when {
                        critical -> R.string.fertility_action_critical
                        moderate -> R.string.fertility_action_moderate
                        else -> R.string.fertility_action_optimal
                    }

                    val statusColor = when {
                        critical -> Color(0xFFC62828)
                        moderate -> Amber
                        else -> FarmGreen
                    }

                    Text(
                        stringResource(statusRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                    )

                    Text(
                        stringResource(actionRes)
                    )
                }

                Text(
                    stringResource(
                        R.string.fertility_nutrients,
                        number(reading.nitrogenPpm),
                        number(reading.phosphorusPpm),
                        number(reading.potassiumPpm),
                    ),
                )

                Text(
                    stringResource(R.string.fertility_nutrients_note),
                    color = Muted,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.fertility_legend_title),
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                stringResource(R.string.fertility_legend_optimal),
                color = FarmGreen,
            )

            Text(
                stringResource(R.string.fertility_legend_moderate),
                color = Amber,
            )

            Text(
                stringResource(R.string.fertility_legend_critical),
                color = Color(0xFFC62828),
            )
        }
    }
}
