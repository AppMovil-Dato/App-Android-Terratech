package com.novatech.terratech.core.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R

@Composable
fun DiscardChangesDialog(onDismiss: () -> Unit, onDiscard: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.unsaved_title)) },
        text = { Text(stringResource(R.string.unsaved_body)) },
        confirmButton = {
            TextButton(onClick = onDiscard) { Text(stringResource(R.string.leave)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.stay)) } },
    )
}
