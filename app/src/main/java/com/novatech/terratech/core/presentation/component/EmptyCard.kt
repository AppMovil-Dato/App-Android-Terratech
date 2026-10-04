package com.novatech.terratech.core.presentation.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.novatech.terratech.ui.theme.*

@Composable
fun EmptyCard(title: String, body: String? = null, action: (@Composable () -> Unit)? = null) {
  FarmCard {
    Text(title, style = MaterialTheme.typography.titleMedium)
    body?.let { Text(it, color = Muted) }
    action?.invoke()
  }
}
