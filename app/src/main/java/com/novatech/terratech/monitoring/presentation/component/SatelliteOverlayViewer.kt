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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.novatech.terratech.core.presentation.component.PrimaryButton
import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import com.novatech.terratech.ui.theme.Amber
import com.novatech.terratech.ui.theme.AmberLight
import com.novatech.terratech.ui.theme.FarmGreen
import com.novatech.terratech.ui.theme.LeafLight
import com.novatech.terratech.ui.theme.Muted

@Composable
fun SatelliteOverlayViewer(
    satelliteImage: SatelliteImage?,
    isSatelliteMode: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleMode: () -> Unit,
    onSelectDate: (String) -> Unit,
    onNavigateToCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        SatelliteDatePickerDialog(
            onDateSelected = { date -> onSelectDate(date) },
            onDismiss = { showDatePicker = false },
        )
    }

    FarmCard(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(if (isSatelliteMode) R.string.satellite_view else R.string.map_view),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onToggleMode) {
                Text(
                    text =
                        stringResource(
                            if (isSatelliteMode) R.string.map_view else R.string.satellite_view,
                        ),
                )
            }
        }

        if (isSatelliteMode) {
            if (isLoading && satelliteImage == null) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                }
            } else if (satelliteImage != null) {
                AsyncImage(
                    model = satelliteImage.imageUrl,
                    contentDescription = stringResource(R.string.satellite_view),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.captured_at, satelliteImage.capturedAt.take(10)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Text(
                        text = stringResource(R.string.cloud_coverage, satelliteImage.cloudCoveragePercent),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }

                val (vigorLabel, vigorColor, vigorBg) =
                    when {
                        satelliteImage.ndviAverage >= 0.6 ->
                            Triple(
                                stringResource(R.string.ndvi_high),
                                FarmGreen,
                                LeafLight,
                            )
                        satelliteImage.ndviAverage >= 0.3 ->
                            Triple(
                                stringResource(R.string.ndvi_moderate),
                                Amber,
                                AmberLight,
                            )
                        else ->
                            Triple(
                                stringResource(R.string.ndvi_low),
                                Color(0xFFD32F2F),
                                Color(0xFFFFEBEE),
                            )
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.ndvi_label, satelliteImage.ndviAverage),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Box(
                        modifier =
                            Modifier
                                .background(vigorBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = vigorLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = vigorColor,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.select_date))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PrimaryButton(
                            text = stringResource(R.string.compare_vegetation),
                            enabled = true,
                            onClick = onNavigateToCompare,
                        )
                    }
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_satellite_image),
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(stringResource(R.string.select_date))
                    }
                }
            }
        }
    }
}
