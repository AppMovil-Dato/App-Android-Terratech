package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
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
    RegisterSensorContent(
        code = code,
        name = name,
        attempted = attempted,
        validCode = validCode,
        validName = validName,
        busy = busy,
        fieldName = fieldName,
        error = error,
        onCodeChange = {
            code = it.uppercase().take(9)
            onClear()
        },
        onNameChange = { name = it },
        onSubmit = submit,
    )
}
