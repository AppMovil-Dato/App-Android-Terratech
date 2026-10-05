package com.novatech.terratech.core.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.novatech.terratech.ui.theme.*

@Composable
fun BrandMark() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(48.dp).background(LeafLight, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(32.dp)) {
                val leaf =
                    Path().apply {
                        moveTo(size.width * .18f, size.height * .78f)
                        cubicTo(
                            0f,
                            size.height * .22f,
                            size.width * .55f,
                            size.height * .12f,
                            size.width * .9f,
                            size.height * .1f,
                        )
                        cubicTo(
                            size.width * .95f,
                            size.height * .7f,
                            size.width * .65f,
                            size.height,
                            size.width * .18f,
                            size.height * .78f,
                        )
                    }
                drawPath(leaf, FarmGreen)
                drawLine(
                    LeafLight,
                    Offset(size.width * .2f, size.height * .78f),
                    Offset(size.width * .72f, size.height * .28f),
                    strokeWidth = 3f,
                )
            }
        }
        Text("TerraTech", style = MaterialTheme.typography.titleLarge, color = FarmGreen)
    }
}
