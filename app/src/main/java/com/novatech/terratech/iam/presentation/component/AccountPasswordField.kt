package com.novatech.terratech.iam.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.*
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.format.errorText

@Composable
fun AccountPasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: Int,
    error: String?,
    visible: Boolean,
    onVisibility: () -> Unit,
    newPassword: Boolean,
    last: Boolean,
    busy: Boolean,
    onSubmit: () -> Unit,
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value,
        onChange,
        modifier =
            Modifier.fillMaxWidth().semantics {
                contentType = if (newPassword) ContentType.NewPassword else ContentType.Password
            },
        label = { Text(stringResource(label)) },
        singleLine = true,
        enabled = !busy,
        isError = error != null,
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onVisibility) {
                Text(stringResource(if (visible) R.string.hide else R.string.show))
            }
        },
        supportingText = {
            if (error != null) Text(errorText(error))
            else if (newPassword && label == R.string.password)
                Text(stringResource(R.string.password_hint))
        },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (last) ImeAction.Done else ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
                onNext = { focus.moveFocus(FocusDirection.Down) },
                onDone = { if (!busy) onSubmit() },
            ),
    )
}
