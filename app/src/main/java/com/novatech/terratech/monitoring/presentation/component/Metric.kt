package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.ui.theme.*

@Composable
internal fun Metric(label: String, value: String, modifier: Modifier) {
    FarmCard(modifier) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}
