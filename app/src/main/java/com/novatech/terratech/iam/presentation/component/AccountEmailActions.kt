package com.novatech.terratech.iam.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R

@Composable
internal fun AccountEmailActions(
    enabled: Boolean,
    showVerification: Boolean,
    onRequestVerification: () -> Unit,
    onRequestPasswordReset: () -> Unit,
) {
    Column {
        TextButton(onClick = onRequestPasswordReset, enabled = enabled) {
            Text(stringResource(R.string.forgot_password))
        }
        if (showVerification) {
            TextButton(onClick = onRequestVerification, enabled = enabled) {
                Text(stringResource(R.string.resend_verification))
            }
        }
    }
}
