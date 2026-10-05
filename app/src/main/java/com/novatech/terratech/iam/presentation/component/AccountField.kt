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
import com.novatech.terratech.core.presentation.format.errorText

@Composable
fun AccountField(
    value: String,
    onChange: (String) -> Unit,
    label: Int,
    error: String?,
    email: Boolean = false,
    enabled: Boolean = true,
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value,
        onChange,
        modifier =
            Modifier.fillMaxWidth()
                .then(
                    if (email) Modifier.semantics { contentType = ContentType.EmailAddress }
                    else Modifier
                ),
        label = { Text(stringResource(label)) },
        singleLine = true,
        isError = error != null,
        enabled = enabled,
        supportingText =
            if (error != null) {
                { Text(errorText(error)) }
            } else null,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = if (email) KeyboardType.Email else KeyboardType.Text,
                capitalization =
                    if (email) KeyboardCapitalization.None else KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
    )
}
