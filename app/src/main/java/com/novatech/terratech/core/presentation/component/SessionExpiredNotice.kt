package com.novatech.terratech.core.presentation.component

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R

@Composable
fun SessionExpiredNotice(visible: Boolean, onSignIn: () -> Unit) {
    if (visible) {
        TextButton(onClick = onSignIn) { Text(stringResource(R.string.reauth)) }
    }
}
