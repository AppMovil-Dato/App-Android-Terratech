package com.novatech.terratech.core.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.novatech.terratech.ui.theme.Muted

@Composable
fun EmptyCard(title: String, body: String? = null, action: (@Composable () -> Unit)? = null) {
    FarmCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        body?.let { Text(it, color = Muted) }
        action?.invoke()
    }
}
