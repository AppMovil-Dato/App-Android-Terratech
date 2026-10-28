
package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.evaluateThresholds
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.Muted
import java.time.Instant

@Composable
fun FertilityThresholdAlertsCard(
    reading: Reading?,
    thresholds: FertilityZoneThresholds,
    now: Instant
) {
    FarmCard {
        Text(
            text = "Alertas de la zona",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            reading == null -> {
                Text(
                    text = "No hay lecturas disponibles del sensor asociado. " +
                        "Las alertas se mostraran cuando existan mediciones.",
                    color = Muted
                )
            }

            reading.isStale(now) -> {
                Text(
                    text = "La ultima lectura tiene mas de 30 minutos. " +
                        "No se mostrara como una alerta actual.",
                    color = Muted
                )
            }

            else -> {
                if (
                    reading.source.equals(
                        "SIMULATED",
                        ignoreCase = true
                    )
                ) {
                    Text(
                        text = "Lectura simulada",
                        color = Muted
                    )
                }

                val alerts = reading.evaluateThresholds(thresholds)

                if (alerts.isEmpty()) {
                    Text(
                        text = "Los valores se encuentran dentro " +
                            "de los umbrales configurados.",
                        color = FarmGreen
                    )
                } else {
                    alerts.forEach { alert ->

                        val condition =
                            if (alert.aboveMaximum) {
                                "supera el maximo"
                            } else {
                                "esta por debajo del minimo"
                            }

                        Text(
                            text = "ALERTA: ${alert.parameter}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFC62828)
                        )

                        Text(
                            text = "${number(alert.currentValue)} " +
                                "${alert.unit} $condition de " +
                                "${number(alert.limit)} ${alert.unit}.",
                            color = Color(0xFFC62828)
                        )

                        Text(
                            text = alert.recommendation
                        )
                    }
                }
            }
        }
    }
}
