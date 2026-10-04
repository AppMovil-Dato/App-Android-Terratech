package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.ui.theme.*

@Composable
internal fun FormInput(
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
