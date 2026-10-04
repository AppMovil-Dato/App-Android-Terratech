package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.ui.theme.*

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
        EmptyCard(stringResource(R.string.no_fields), stringResource(R.string.no_fields_body))
      }
    items(state.fields, key = { it.id }) { field ->
      FarmCard {
        Pill(field.cropName ?: stringResource(R.string.crop))
        Text(field.name, style = MaterialTheme.typography.titleLarge)
        Text(areaNumber(field.sizeM2 / 10000) + " ha · " + field.soilType, color = Muted)
        Text(
          stringResource(R.string.sensor_count, state.sensors.count { it.fieldId == field.id }),
          color = Muted,
        )
        PrimaryButton(stringResource(R.string.choose_field), !state.busy) { onChoose(field.id) }
      }
    }
  }
}

@Composable
fun SensorsScreen(state: MonitoringState, onChoose: (Int) -> Unit, onRegister: () -> Unit) {
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
    item { PrimaryButton(stringResource(R.string.associate), !state.busy, onRegister) }
    if (state.fieldSensors.isEmpty()) item { EmptyCard(stringResource(R.string.no_sensors)) }
    items(state.fieldSensors, key = { it.id }) { sensor ->
      FarmCard {
        Text(
          sensor.name ?: stringResource(R.string.sensor),
          style = MaterialTheme.typography.titleLarge,
        )
        Text(sensor.sensorCode ?: sensor.macAddress, color = Muted)
        PrimaryButton(stringResource(R.string.view_sensor), !state.busy) { onChoose(sensor.id) }
      }
    }
  }
}

@Composable
fun CreateFieldScreen(
  busy: Boolean,
  onSave: (String, String, Double, String, Double, Double) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf("") }
  var crop by rememberSaveable { mutableStateOf("") }
  var area by rememberSaveable { mutableStateOf("") }
  var soil by rememberSaveable { mutableStateOf("") }
  var lat by rememberSaveable { mutableStateOf("") }
  var lon by rememberSaveable { mutableStateOf("") }
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item { PageTitle(stringResource(R.string.new_field)) }
    item {
      FarmCard {
        FormInput(name, { name = it }, R.string.field_name)
        FormInput(crop, { crop = it }, R.string.crop)
        FormInput(area, { area = it }, R.string.area_ha, KeyboardType.Decimal)
        FormInput(soil, { soil = it }, R.string.soil)
        FormInput(lat, { lat = it }, R.string.latitude, KeyboardType.Text)
        FormInput(lon, { lon = it }, R.string.longitude, KeyboardType.Text)
        PrimaryButton(stringResource(R.string.save), !busy) {
          onSave(name, crop, area.decimal(), soil, lat.decimal(), lon.decimal())
        }
      }
    }
  }
}

@Composable
fun RegisterSensorScreen(busy: Boolean, onSave: (String, String) -> Unit) {
  var code by rememberSaveable { mutableStateOf("") }
  var name by rememberSaveable { mutableStateOf("") }
  Column(Modifier.padding(16.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    PageTitle(stringResource(R.string.associate))
    FarmCard {
      FormInput(code, { code = it }, R.string.sensor_code)
      FormInput(name, { name = it }, R.string.sensor_name)
      PrimaryButton(stringResource(R.string.associate), !busy) { onSave(code, name) }
    }
  }
}

@Composable
private fun FormInput(
  value: String,
  change: (String) -> Unit,
  label: Int,
  type: KeyboardType = KeyboardType.Text,
) {
  OutlinedTextField(
    value,
    change,
    label = { Text(stringResource(label)) },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = type),
  )
}

private fun String.decimal() = replace(',', '.').toDoubleOrNull() ?: Double.NaN
