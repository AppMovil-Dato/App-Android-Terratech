
package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds

@Composable
fun FertilityZoneThresholdsContent(
    thresholds: FertilityZoneThresholds,
    busy: Boolean,
    error: String?,
    saved: Boolean,
    onSave: (FertilityZoneThresholds) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    var moistureMin by remember(thresholds) {
        mutableStateOf(thresholds.moistureMin.toString())
    }
    var moistureMax by remember(thresholds) {
        mutableStateOf(thresholds.moistureMax.toString())
    }
    var nitrogenMin by remember(thresholds) {
        mutableStateOf(thresholds.nitrogenMin.toString())
    }
    var nitrogenMax by remember(thresholds) {
        mutableStateOf(thresholds.nitrogenMax.toString())
    }
    var phosphorusMin by remember(thresholds) {
        mutableStateOf(thresholds.phosphorusMin.toString())
    }
    var phosphorusMax by remember(thresholds) {
        mutableStateOf(thresholds.phosphorusMax.toString())
    }
    var potassiumMin by remember(thresholds) {
        mutableStateOf(thresholds.potassiumMin.toString())
    }
    var potassiumMax by remember(thresholds) {
        mutableStateOf(thresholds.potassiumMax.toString())
    }

    var validationError by remember(thresholds) {
        mutableStateOf<String?>(null)
    }

    FarmCard {
        Text(
            text = "Configuracion de umbrales",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Humedad del suelo (%)",
            style = MaterialTheme.typography.titleSmall
        )

        ThresholdNumberField(
            "Humedad minima",
            moistureMin,
            { moistureMin = it; validationError = null }
        )
        ThresholdNumberField(
            "Humedad maxima",
            moistureMax,
            { moistureMax = it; validationError = null }
        )

        Text(
            text = "Nitrogeno (ppm)",
            style = MaterialTheme.typography.titleSmall
        )

        ThresholdNumberField(
            "Nitrogeno minimo",
            nitrogenMin,
            { nitrogenMin = it; validationError = null }
        )
        ThresholdNumberField(
            "Nitrogeno maximo",
            nitrogenMax,
            { nitrogenMax = it; validationError = null }
        )

        Text(
            text = "Fosforo (ppm)",
            style = MaterialTheme.typography.titleSmall
        )

        ThresholdNumberField(
            "Fosforo minimo",
            phosphorusMin,
            { phosphorusMin = it; validationError = null }
        )
        ThresholdNumberField(
            "Fosforo maximo",
            phosphorusMax,
            { phosphorusMax = it; validationError = null }
        )

        Text(
            text = "Potasio (ppm)",
            style = MaterialTheme.typography.titleSmall
        )

        ThresholdNumberField(
            "Potasio minimo",
            potassiumMin,
            { potassiumMin = it; validationError = null }
        )
        ThresholdNumberField(
            "Potasio maximo",
            potassiumMax,
            { potassiumMax = it; validationError = null }
        )

        validationError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        error?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error
            )
        }

        if (saved) {
            Text(
                text = "Umbrales guardados correctamente",
                color = MaterialTheme.colorScheme.primary
            )
        }

        PrimaryButton(
            "Guardar umbrales",
            !busy
        ) {
            val values = listOf(
                moistureMin,
                moistureMax,
                nitrogenMin,
                nitrogenMax,
                phosphorusMin,
                phosphorusMax,
                potassiumMin,
                potassiumMax
            ).map { value ->
                value.trim().replace(',', '.').toDoubleOrNull()
            }

            if (values.any { it == null }) {
                validationError = "Introduce valores numericos validos."
            } else {
                val numbers = values.map { it!! }

                val moistureValid = validRange(
                    numbers[0],
                    numbers[1],
                    100.0
                )

                val nitrogenValid = validRange(
                    numbers[2],
                    numbers[3]
                )

                val phosphorusValid = validRange(
                    numbers[4],
                    numbers[5]
                )

                val potassiumValid = validRange(
                    numbers[6],
                    numbers[7]
                )

                if (
                    !moistureValid ||
                    !nitrogenValid ||
                    !phosphorusValid ||
                    !potassiumValid
                ) {
                    validationError =
                        "Cada minimo debe ser menor que su maximo. " +
                            "La humedad debe estar entre 0 y 100."
                } else {
                    validationError = null

                    onSave(
                        thresholds.copy(
                            moistureMin = numbers[0],
                            moistureMax = numbers[1],
                            nitrogenMin = numbers[2],
                            nitrogenMax = numbers[3],
                            phosphorusMin = numbers[4],
                            phosphorusMax = numbers[5],
                            potassiumMin = numbers[6],
                            potassiumMax = numbers[7]
                        )
                    )
                }
            }
        }

        PrimaryButton(
            "Restablecer valores",
            !busy,
            onReset
        )

        PrimaryButton(
            "Cerrar configuracion",
            !busy,
            onClose
        )
    }
}

private fun validRange(
    minimum: Double,
    maximum: Double,
    maximumAllowed: Double = Double.MAX_VALUE
): Boolean {
    return minimum.isFinite() &&
        maximum.isFinite() &&
        minimum >= 0.0 &&
        maximum <= maximumAllowed &&
        minimum < maximum
}
