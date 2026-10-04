package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.presentation.component.FormInput
import com.novatech.terratech.ui.theme.*

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
