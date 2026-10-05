package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.ui.theme.*

@Composable
internal fun MoistureRing(value: Double, warning: Boolean) {
    val label = number(value) + " %"
    Box(
        Modifier.size(130.dp).semantics(mergeDescendants = true) { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2
            val size = Size(size.width - stroke, size.height - stroke)
            drawArc(LeafLight, 0f, 360f, false, Offset(inset, inset), size, style = Stroke(stroke))
            drawArc(
                if (warning) Amber else FarmGreen,
                -90f,
                (value.coerceIn(0.0, 100.0) * 3.6).toFloat(),
                false,
                Offset(inset, inset),
                size,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text(label, style = MaterialTheme.typography.headlineMedium)
    }
}
