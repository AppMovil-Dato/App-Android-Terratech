package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.ui.theme.*

@Composable
fun MoistureChart(rows: List<Reading>, minimum: Double?) {
  val description = stringResource(R.string.chart_description)
  Canvas(Modifier.fillMaxWidth().height(170.dp).semantics { contentDescription = description }) {
    val left = 12.dp.toPx()
    val width = size.width - left * 2
    val height = size.height - 20.dp.toPx()
    for (i in 0..4) {
      val y = height * i / 4
      drawLine(Color(0xFFE5EDE8), Offset(left, y), Offset(left + width, y), 1.dp.toPx())
    }
    minimum?.let {
      val y = height * (1 - it.coerceIn(0.0, 100.0).toFloat() / 100)
      drawLine(
        Amber,
        Offset(left, y),
        Offset(left + width, y),
        2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
      )
    }
    if (rows.isNotEmpty()) {
      val first = rows.first().recordedAt.toEpochMilli()
      val range = (rows.last().recordedAt.toEpochMilli() - first).coerceAtLeast(1)
      fun point(r: Reading) =
        Offset(
          left + width * ((r.recordedAt.toEpochMilli() - first).toDouble() / range).toFloat(),
          height * (1 - r.moisturePercent.coerceIn(0.0, 100.0).toFloat() / 100),
        )
      val path = Path()
      rows.forEachIndexed { i, r ->
        val p = point(r)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
      }
      val fill =
        Path().apply {
          addPath(path)
          lineTo(point(rows.last()).x, height)
          lineTo(point(rows.first()).x, height)
          close()
        }
      drawPath(fill, FarmGreen.copy(alpha = .1f))
      drawPath(path, FarmGreen, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
      if (rows.size == 1) drawCircle(FarmGreen, 4.dp.toPx(), point(rows.first()))
    }
  }
}
