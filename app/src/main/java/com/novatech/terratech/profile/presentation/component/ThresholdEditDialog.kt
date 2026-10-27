package com.novatech.terratech.profile.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.Muted

@Composable
internal fun ThresholdEditDialog(
    initialMoisture: Double,
    initialTemperature: Double,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double, Double) -> Unit,
) {
    var moistureText by
        rememberSaveable {
            mutableStateOf(
                if (initialMoisture % 1.0 == 0.0) {
                    initialMoisture.toInt().toString()
                } else {
                    initialMoisture.toString()
                }
            )
        }
    var temperatureText by
        rememberSaveable {
            mutableStateOf(
                if (initialTemperature % 1.0 == 0.0) {
                    initialTemperature.toInt().toString()
                } else {
                    initialTemperature.toString()
                }
            )
        }
    var attempted by rememberSaveable { mutableStateOf(false) }

    val parsedMoisture = moistureText.replace(',', '.').toDoubleOrNull()
    val parsedTemperature = temperatureText.replace(',', '.').toDoubleOrNull()

    val validMoisture =
        parsedMoisture != null && parsedMoisture.isFinite() && parsedMoisture in 0.0..100.0
    val validTemperature =
        parsedTemperature != null && parsedTemperature.isFinite() && parsedTemperature in 0.0..60.0
    val isValid = validMoisture && validTemperature

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.moisture_threshold_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.thresholds_help),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                OutlinedTextField(
                    value = moistureText,
                    onValueChange = { moistureText = it },
                    label = { Text(stringResource(R.string.min_moisture_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !busy,
                    suffix = { Text("%") },
                    isError = attempted && !validMoisture,
                    supportingText = {
                        Text(
                            if (attempted && !validMoisture) {
                                stringResource(R.string.error_moisture_threshold)
                            } else {
                                stringResource(R.string.moisture_threshold_help)
                            }
                        )
                    },
                )
                OutlinedTextField(
                    value = temperatureText,
                    onValueChange = { temperatureText = it },
                    label = { Text(stringResource(R.string.max_temp_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !busy,
                    suffix = { Text("°C") },
                    isError = attempted && !validTemperature,
                    supportingText = {
                        Text(
                            if (attempted && !validTemperature) {
                                stringResource(R.string.error_temp_threshold)
                            } else {
                                stringResource(R.string.temp_threshold_help)
                            }
                        )
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    attempted = true
                    if (isValid) {
                        onSave(parsedMoisture, parsedTemperature)
                    }
                },
                enabled = !busy,
            ) {
                Text(
                    stringResource(R.string.save),
                    color = FarmGreen,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !busy,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
