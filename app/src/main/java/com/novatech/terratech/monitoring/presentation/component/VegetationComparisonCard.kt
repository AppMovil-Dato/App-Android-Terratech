package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.monitoring.domain.entity.VegetationComparison
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.LeafLight
import com.novatech.terratech.ui.theme.Muted

@Composable
fun VegetationComparisonCard(
    comparison: VegetationComparison,
    modifier: Modifier = Modifier,
) {
    FarmCard(modifier) {
        val isGain = comparison.ndviDelta >= 0
        val deltaColor = if (isGain) FarmGreen else Color(0xFFD32F2F)
        val deltaBg = if (isGain) LeafLight else Color(0xFFFFEBEE)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.vegetation_comparison_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Box(
                modifier =
                    Modifier
                        .background(deltaBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.ndvi_variation,
                            comparison.ndviDelta,
                            comparison.percentageChange,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = deltaColor,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.earlier_capture),
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                )
                AsyncImage(
                    model = comparison.earlier.imageUrl,
                    contentDescription = stringResource(R.string.earlier_capture),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = comparison.earlier.capturedAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.ndvi_label, comparison.earlier.ndviAverage),
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.later_capture),
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                )
                AsyncImage(
                    model = comparison.later.imageUrl,
                    contentDescription = stringResource(R.string.later_capture),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = comparison.later.capturedAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.ndvi_label, comparison.later.ndviAverage),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}
