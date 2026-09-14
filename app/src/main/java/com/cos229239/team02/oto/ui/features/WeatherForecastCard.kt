package com.cos229239.team02.oto.ui.features



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WeatherForecastCard(
    uiState: AreaSafetyUIState,
    onRefresh: () -> Unit
) {
    val forecast = uiState.forecast
    val forecastStatus = uiState.sources.firstOrNull {
        it.source == "Open-Meteo"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "WEATHER FORECAST",
                fontWeight = FontWeight.Bold
            )

            Text(text = uiState.areaName)

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                    Text("Loading forecast...")
                }

                !uiState.hasLocation -> {
                    Text(
                        "Select a trip destination or use Locate Me in Explorer."
                    )
                }

                forecast != null -> {
                    Text(
                        text = forecast.periodName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${forecast.temp}°${forecast.tempUnit}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = forecast.shortForecast,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Wind: ${forecast.windDirection} " +
                                forecast.windSpeed
                    )

                    val precipitation = forecast.precipitationPercent
                        ?.let { "$it%" }
                        ?: "Unavailable"

                    Text(
                        text = "Chance of precipitation: $precipitation"
                    )

                    Text(text = forecast.detailedForecast)

                    Text(
                        text = "Forecast period: ${forecast.startTime} " +
                                "to ${forecast.endTime}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                else -> {
                    Text(
                        text = uiState.errorMessage
                            ?: forecastStatus?.message
                            ?: "Forecast has not been loaded."
                    )
                }
            }

            TextButton(
                onClick = onRefresh,
                enabled = uiState.hasLocation && !uiState.isLoading
            ) {
                Text("Refresh safety updates")
            }
        }
    }
}