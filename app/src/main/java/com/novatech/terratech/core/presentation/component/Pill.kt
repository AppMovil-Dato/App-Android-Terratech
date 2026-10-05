package com.novatech.terratech.core.presentation.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.novatech.terratech.ui.theme.*

@Composable
fun Pill(text: String, warning: Boolean = false) {
    Surface(color = if (warning) AmberLight else LeafLight, shape = RoundedCornerShape(50.dp)) {
        Text(
            text,
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = if (warning) Color(0xFF8F4B00) else FarmGreen,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
