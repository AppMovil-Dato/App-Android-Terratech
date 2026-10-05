package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.core.presentation.format.errorText

@Composable
fun RegisterSensorContent(
    code: String,
    name: String,
    attempted: Boolean,
    validCode: Boolean,
    validName: Boolean,
    busy: Boolean,
    fieldName: String,
    error: String?,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (fieldName.isNotBlank())
            Pill(stringResource(R.string.sensor_registration_help, fieldName))
        FarmCard {
            OutlinedTextField(
                code,
                onCodeChange,
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
                onNameChange,
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
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )
            PrimaryButton(
                stringResource(if (busy) R.string.loading else R.string.associate),
                !busy,
                onSubmit,
            )
        }
    }
}
