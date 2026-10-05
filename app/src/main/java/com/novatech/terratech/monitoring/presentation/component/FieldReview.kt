package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.format.areaNumber
import com.novatech.terratech.monitoring.presentation.state.FieldEditorState

@Composable
fun FieldReview(state: FieldEditorState) {
    val draft = state.draft()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FarmCard {
            Text(draft.name.value, style = MaterialTheme.typography.headlineMedium)
            Text(draft.crop, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.map_area, areaNumber(draft.area.value / 10000)))
            Text(draft.soil)
            FieldMapView(
                state.points,
                state.drawing,
                Modifier.fillMaxWidth().height(230.dp),
                interactive = false,
            )
            Text(
                stringResource(
                    if (state.drawing) R.string.map_boundary_saved else R.string.map_pin_saved
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
