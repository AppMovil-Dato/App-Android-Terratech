package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.*
import com.novatech.terratech.core.presentation.format.errorText
import com.novatech.terratech.monitoring.domain.valueobject.SensorCode

@Composable
fun RegisterSensorScreen(
    busy: Boolean,
    onSave: (String, String) -> Unit,
    fieldName: String = "",
    error: String? = null,
    onClear: () -> Unit = {},
) {
    var code by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val validCode = runCatching { SensorCode.of(code) }.isSuccess
    val validName = name.trim().length in 2..100
    val focus = LocalFocusManager.current
    val submit = {
        attempted = true
        if (validCode && validName && !busy) {
            focus.clearFocus()
            onSave(code, name)
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (fieldName.isNotBlank())
            Pill(stringResource(R.string.sensor_registration_help, fieldName))
        FarmCard {
            OutlinedTextField(
                code,
                {
                    code = it.uppercase().take(9)
                    onClear()
                },
                label = { Text(stringResource(R.string.sensor_code)) },
                placeholder = { Text("TT-ABC123") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
                isError =
                    attempted && !validCode ||
                        error in
                            listOf(
                                "SENSOR_OCCUPIED",
                                "UNKNOWN_SENSOR",
                                "SENSOR_NOT_FOUND",
                                "INVALID_SENSOR_CODE",
                            ),
                supportingText = {
                    Text(
                        if (attempted && !validCode) errorText("INVALID_SENSOR_CODE")
                        else if (
                            error in
                                listOf(
                                    "SENSOR_OCCUPIED",
                                    "UNKNOWN_SENSOR",
                                    "SENSOR_NOT_FOUND",
                                    "INVALID_SENSOR_CODE",
                                )
                        )
                            errorText(error!!)
                        else stringResource(R.string.sensor_code_help)
                    )
                },
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next,
                    ),
            )
            OutlinedTextField(
                name,
                { name = it },
                label = { Text(stringResource(R.string.sensor_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
                isError = attempted && !validName,
                supportingText = {
                    Text(
                        if (attempted && !validName) errorText("INVALID_NAME")
                        else stringResource(R.string.sensor_name_help)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            PrimaryButton(
                stringResource(if (busy) R.string.loading else R.string.associate),
                !busy,
                submit,
            )
        }
    }
}
