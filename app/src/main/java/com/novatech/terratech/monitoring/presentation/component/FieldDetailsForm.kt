package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.monitoring.presentation.state.FieldEditorState

@Composable
fun FieldDetailsForm(state: FieldEditorState, onChange: (FieldEditorState) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            state.name,
            { onChange(state.copy(name = it)) },
            label = { Text(stringResource(R.string.field_name)) },
            placeholder = { Text(stringResource(R.string.field_name_example)) },
            supportingText = { Text(stringResource(R.string.field_name_help)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            state.crop,
            { onChange(state.copy(crop = it)) },
            label = { Text(stringResource(R.string.crop)) },
            placeholder = { Text(stringResource(R.string.crop_example)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.soil_optional), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                    "" to R.string.soil_unknown,
                    "Franco" to R.string.soil_loam,
                    "Arenoso" to R.string.soil_sandy,
                    "Arcilloso" to R.string.soil_clay,
                )
                .forEach { (value, label) ->
                    FilterChip(
                        selected = state.soil == value,
                        onClick = { onChange(state.copy(soil = value)) },
                        label = { Text(stringResource(label)) },
                    )
                }
        }
        Text(
            stringResource(R.string.field_map_next),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
