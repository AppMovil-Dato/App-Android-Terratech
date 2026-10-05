package com.novatech.terratech.profile.presentation.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.component.StepProgress
import com.novatech.terratech.iam.domain.valueobject.FullName
import com.novatech.terratech.profile.domain.valueobject.FarmDetails

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
  var step by rememberSaveable { mutableIntStateOf(0) }
  var attempted by rememberSaveable { mutableStateOf(false) }
  var name by rememberSaveable { mutableStateOf(initialName) }
  var farm by rememberSaveable { mutableStateOf(initialFarm) }
  var phone by rememberSaveable { mutableStateOf(initialPhone) }
  var location by rememberSaveable { mutableStateOf(initialLocation) }
  var area by rememberSaveable { mutableStateOf(initialArea) }
  val validName = runCatching { FullName.of(name) }.isSuccess
  val validPersonal = validName && phone.trim().length in 1..30
  val hectares = area.replace(',', '.').toDoubleOrNull() ?: Double.NaN
  val validFarm =
    farm.trim().length in 1..100 &&
      location.trim().length in 1..250 &&
      hectares.isFinite() &&
      hectares > 0 &&
      hectares * 10000 <= 9999999
  BackHandler(step > 0) {
    if (!busy) {
      step--
      attempted = false
    }
  }
  FarmCard {
    StepProgress(
      step,
      listOf(
        stringResource(R.string.profile_step_personal),
        stringResource(R.string.profile_step_farm),
      ),
    )
    if (step == 0) {
      ProfileInput(
        name,
        { name = it },
        R.string.full_name,
        invalid = attempted && !validName,
        enabled = !busy,
      )
      ProfileInput(
        phone,
        { phone = it },
        R.string.phone,
        KeyboardType.Phone,
        attempted && phone.trim().length !in 1..30,
        !busy,
      )
    } else {
      ProfileInput(
        farm,
        { farm = it },
        R.string.farm_name,
        invalid = attempted && farm.trim().length !in 1..100,
        enabled = !busy,
      )
      ProfileInput(
        location,
        { location = it },
        R.string.location,
        invalid = attempted && location.trim().length !in 1..250,
        enabled = !busy,
      )
      ProfileInput(
        area,
        { area = it },
        R.string.area_ha,
        KeyboardType.Decimal,
        attempted && (!hectares.isFinite() || hectares <= 0 || hectares * 10000 > 9999999),
        !busy,
      )
      Text(stringResource(R.string.profile_area_help), style = MaterialTheme.typography.bodySmall)
      TextButton(
        {
          step = 0
          attempted = false
        },
        enabled = !busy,
      ) {
        Text(stringResource(R.string.back))
      }
    }
    PrimaryButton(
      stringResource(
        if (busy) R.string.loading
        else if (step == 0) R.string.continue_action else R.string.profile_finish
      ),
      !busy,
    ) {
      attempted = true
      if (step == 0 && validPersonal) {
        step = 1
        attempted = false
      } else if (step == 1 && validPersonal && validFarm) {
        val details = FarmDetails.of(name, farm, phone, location, hectares)
        onSave(details.name.value, details.farm, details.phone, details.location, hectares)
      }
    }
  }
}
