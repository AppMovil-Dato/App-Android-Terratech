package com.novatech.terratech.monitoring.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.core.presentation.component.FarmCard
import com.novatech.terratech.monitoring.domain.entity.FieldWeather
import com.novatech.terratech.ui.theme.Amber
import com.novatech.terratech.ui.theme.AmberLight
import com.novatech.terratech.ui.theme.Muted

@Composable
fun WeatherForecastCard(
    weather: FieldWeather?,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FarmCard(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.weather_forecast),
                style = MaterialTheme.typography.titleMedium,
            )
            if (weather?.isOffline == true) {
                Box(
                    modifier =
                        Modifier
                            .background(AmberLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.weather_offline_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber,
                    )
                }
            }
        }

        if (isLoading && weather == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (weather != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.temperature_val, weather.currentTemperatureC),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = stringResource(R.string.expected_rain, weather.expectedPrecipitationMm),
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.precipitation_24h, weather.precipitationProbability24h),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.humidity_val, weather.relativeHumidityPercent),
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(R.string.wind_speed_val, weather.windSpeedKmh),
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } else if (errorMessage != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.weather_service_unavailable),
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onRefresh) {
                    Text(stringResource(R.string.refresh_weather))
                }
            }
        }
    }
}
