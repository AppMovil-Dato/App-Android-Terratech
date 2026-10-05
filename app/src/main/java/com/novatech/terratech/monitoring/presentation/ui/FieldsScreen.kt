package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.format.areaNumber
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.Muted

@Composable
fun FieldsScreen(
    state: MonitoringState,
    hasProfile: Boolean,
    onChoose: (Int) -> Unit,
    onCreate: () -> Unit,
    onProfile: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageTitle(
                stringResource(R.string.my_fields),
                stringResource(R.string.plot_count, state.fields.size),
            )
        }
        item {
            PrimaryButton(
                stringResource(if (hasProfile) R.string.new_field else R.string.complete_profile),
                !state.busy,
            ) {
                if (hasProfile) onCreate() else onProfile()
            }
        }
        if (state.fields.isEmpty())
            item {
                EmptyCard(
                    stringResource(R.string.no_fields),
                    stringResource(R.string.no_fields_body),
                )
            }
        items(state.fields, key = { it.id }) { field ->
            FarmCard {
                Pill(field.cropName ?: stringResource(R.string.crop))
                Text(field.name, style = MaterialTheme.typography.titleLarge)
                Text(areaNumber(field.sizeM2 / 10000) + " ha · " + field.soilType, color = Muted)
                Text(
                    stringResource(
                        R.string.sensor_count,
                        state.sensors.count { it.fieldId == field.id },
                    ),
                    color = Muted,
                )
                PrimaryButton(stringResource(R.string.view_field), !state.busy) {
                    onChoose(field.id)
                }
            }
        }
    }
}
