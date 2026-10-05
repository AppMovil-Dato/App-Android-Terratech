package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.EmptyCard
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.core.presentation.component.PageTitle
import com.novatech.terratech.core.presentation.component.Pill
import com.novatech.terratech.core.presentation.format.number
import com.novatech.terratech.core.presentation.format.timestamp
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.presentation.component.ReadingMetrics
import com.novatech.terratech.ui.theme.Muted

@Composable
fun ReadingDetailScreen(reading: Reading?) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { PageTitle(stringResource(R.string.reading_detail)) }
        if (reading == null) item { EmptyCard(stringResource(R.string.error_missing)) }
        else {
            item {
                FarmCard {
                    Text(
                        stringResource(R.string.reading_id, reading.id),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(timestamp(reading.recordedAt))
                    Pill(reading.source)
                    Text(
                        stringResource(R.string.moisture) +
                            ": " +
                            number(reading.moisturePercent) +
                            " %"
                    )
                }
            }
            item { ReadingMetrics(reading) }
            item {
                Text(
                    stringResource(R.string.units_note),
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
