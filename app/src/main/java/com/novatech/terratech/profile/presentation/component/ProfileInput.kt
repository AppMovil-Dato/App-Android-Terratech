package com.novatech.terratech.profile.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.novatech.terratech.R

@Composable
internal fun ProfileInput(
    value: String,
    change: (String) -> Unit,
    label: Int,
    type: KeyboardType = KeyboardType.Text,
    invalid: Boolean = false,
    enabled: Boolean = true,
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value,
        change,
        label = { Text(stringResource(label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = type, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        enabled = enabled,
        isError = invalid,
        supportingText = if (invalid) ({ Text(stringResource(R.string.error_required)) }) else null,
    )
}
