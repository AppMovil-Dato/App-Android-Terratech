package com.novatech.terratech.profile.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.ui.theme.*

@Composable
internal fun LogoutConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.logout_title)) },
    text = { Text(stringResource(R.string.logout_body)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(stringResource(R.string.logout)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    },
  )
}
