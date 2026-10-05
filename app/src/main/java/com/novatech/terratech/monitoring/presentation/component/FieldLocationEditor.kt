package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.format.areaNumber
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.presentation.state.FieldEditorState
import com.novatech.terratech.monitoring.presentation.state.MapSearchState

@Composable
fun FieldLocationEditor(
    state: FieldEditorState,
    search: MapSearchState,
    onSearch: (String) -> Unit,
    onChange: (FieldEditorState) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var manual by rememberSaveable { mutableStateOf(false) }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var coordinateError by remember { mutableStateOf(false) }
    var manualTarget by remember { mutableStateOf<Coordinates?>(null) }
    val focus = LocalFocusManager.current
    val searchLocation: (String) -> Unit = {
        focus.clearFocus()
        manualTarget = null
        onSearch(it)
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            query,
            { query = it },
            label = { Text(stringResource(R.string.map_search)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { searchLocation(query) }),
            trailingIcon = {
                TextButton(
                    onClick = { searchLocation(query) },
                    enabled = !search.searching && query.trim().length >= 2,
                ) {
                    Text(stringResource(R.string.search))
                }
            },
        )
        if (search.searching) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (search.error)
            Text(
                stringResource(R.string.map_search_empty),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                !state.drawing,
                { onChange(state.copy(drawing = false, points = emptyList())) },
                label = { Text(stringResource(R.string.map_pin)) },
            )
            FilterChip(
                state.drawing,
                { onChange(state.copy(drawing = true, points = emptyList())) },
                label = { Text(stringResource(R.string.map_draw)) },
            )
        }
        Text(
            stringResource(if (state.drawing) R.string.map_draw_help else R.string.map_pin_help),
            style = MaterialTheme.typography.bodySmall,
        )
        if (state.drawing) {
            Text(
                if (state.polygon != null)
                    stringResource(R.string.map_area, areaNumber(state.polygon!!.areaM2 / 10000))
                else
                    stringResource(
                        if (state.points.size >= 3) R.string.map_invalid_boundary
                        else R.string.map_need_points
                    ),
                color =
                    if (state.points.size >= 3 && state.polygon == null)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
            )
        }
        FieldMapView(
            state.points,
            state.drawing,
            Modifier.fillMaxWidth().height(300.dp),
            manualTarget ?: search.result,
        ) { point ->
            onChange(
                state.copy(
                    points = if (state.drawing) (state.points + point).take(100) else listOf(point)
                )
            )
        }
        if (state.drawing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.map_points, state.points.size))
                TextButton(
                    { onChange(state.copy(points = state.points.dropLast(1))) },
                    enabled = state.points.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.undo))
                }
                TextButton(
                    { onChange(state.copy(points = emptyList())) },
                    enabled = state.points.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.clear))
                }
            }
        } else
            OutlinedTextField(
                state.hectares,
                { onChange(state.copy(hectares = it)) },
                label = { Text(stringResource(R.string.area_ha)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        TextButton({ manual = !manual }) { Text(stringResource(R.string.map_manual)) }
        if (manual) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    latitude,
                    { latitude = it },
                    label = { Text(stringResource(R.string.latitude)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    longitude,
                    { longitude = it },
                    label = { Text(stringResource(R.string.longitude)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            if (coordinateError)
                Text(stringResource(R.string.error_coords), color = MaterialTheme.colorScheme.error)
            TextButton({
                val lat = latitude.replace(',', '.').toDoubleOrNull()
                val lon = longitude.replace(',', '.').toDoubleOrNull()
                val point =
                    if (lat == null || lon == null) null
                    else runCatching { Coordinates.of(lat, lon) }.getOrNull()
                coordinateError = point == null
                if (point != null) {
                    focus.clearFocus()
                    manualTarget = point
                    onChange(
                        state.copy(
                            points =
                                if (state.drawing) (state.points + point).take(100)
                                else listOf(point)
                        )
                    )
                }
            }) {
                Text(stringResource(R.string.map_use_coordinates))
            }
        }
    }
}
