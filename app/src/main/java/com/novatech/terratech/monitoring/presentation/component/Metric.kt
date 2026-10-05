package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.ui.theme.Muted

@Composable
internal fun Metric(label: String, value: String, modifier: Modifier) {
    FarmCard(modifier) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}
