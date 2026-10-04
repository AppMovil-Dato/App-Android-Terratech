package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.component.FormInput
import com.novatech.terratech.ui.theme.*

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
