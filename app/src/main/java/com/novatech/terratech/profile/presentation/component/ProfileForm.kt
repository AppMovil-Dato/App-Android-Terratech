package com.novatech.terratech.profile.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.ui.theme.*

@Composable
internal fun ProfileForm(
  initialName: String,
  initialFarm: String,
  initialPhone: String,
  initialLocation: String,
  initialArea: String,
  busy: Boolean,
  onSave: (String, String, String, String, Double) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf(initialName) }
  var farm by rememberSaveable { mutableStateOf(initialFarm) }
  var phone by rememberSaveable { mutableStateOf(initialPhone) }
  var location by rememberSaveable { mutableStateOf(initialLocation) }
  var area by rememberSaveable { mutableStateOf(initialArea) }
  FarmCard {
    Text(stringResource(R.string.personal_data), style = MaterialTheme.typography.titleMedium)
    ProfileInput(name, { name = it }, R.string.full_name)
    ProfileInput(farm, { farm = it }, R.string.farm_name)
    ProfileInput(phone, { phone = it }, R.string.phone, KeyboardType.Phone)
    ProfileInput(location, { location = it }, R.string.location)
    ProfileInput(area, { area = it }, R.string.area_ha, KeyboardType.Decimal)
    PrimaryButton(stringResource(if (busy) R.string.loading else R.string.save), !busy) {
      onSave(name, farm, phone, location, area.replace(',', '.').toDoubleOrNull() ?: Double.NaN)
    }
  }
}
