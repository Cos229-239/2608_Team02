package com.cos229239.team02.oto.ui.screens.explorer

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cos229239.team02.oto.data.safety.SafetySourceState
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.weatherIcon

/**
 * Weather details screen for Explorer.
 *
 * UI only for now.
 * Real NWS data will be connected later.
 */
@Composable
fun WeatherReportScreen(
    onBackClick: () -> Unit,
    safetyView: AreaSafetyView
) {
    val uiState by safetyView.uiState.collectAsStateWithLifecycle()
    val forecast = uiState.forecast
    val airQuality = uiState.airQuality

    val forecastStatus = uiState.sources.firstOrNull{
        it.source == "Open-Meteo"
    }
    val airQualityStatus = uiState.sources.firstOrNull{
        it.source == "Open-Meteo Air Quality"
    }

   val nwsStatus = uiState.sources.firstOrNull{
       it.source == "NWS"
   }

    val darkGreen =
        Color(0xFF063D24)

    val mediumGreen =
        Color(0xFF0B5D1E)

    val lightBackground =
        Color(0xFFF7F8F6)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    lightBackground
                )
    ) {

        /*
         * -----------------------------------------------------
         * HEADER
         * -----------------------------------------------------
         */

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        darkGreen
                    )
                    .statusBarsPadding()
                    .padding(
                        horizontal =
                            12.dp,

                        vertical =
                            12.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick =
                    onBackClick
            ) {

                Text(
                    text =
                        "←",

                    color =
                        Color.White,

                    fontSize =
                        26.sp
                )
            }

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        "WEATHER REPORT",

                    color =
                        Color.White,

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Explorer weather conditions",

                    color =
                        Color.White.copy(
                            alpha =
                                0.90f
                        ),

                    fontSize =
                        13.sp
                )
            }
        }

        /*
         * -----------------------------------------------------
         * CONTENT
         * -----------------------------------------------------
         */

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        16.dp
                    )
        ) {

            /*
             * -------------------------------------------------
             * CURRENT AREA
             * -------------------------------------------------
             */

            Text(
                text =
                    "CURRENT AREA",

                color =
                    darkGreen,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(

                    text = uiState.areaName,


                color =
                    Color(
                        0xFF555555
                    ),

                fontSize =
                    14.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            /*
             * -------------------------------------------------
             * CURRENT WEATHER
             * -------------------------------------------------
             */

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),

                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            20.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            weatherIcon(
                                code = forecast?.weatherCode,
                                isDay = forecast?.isDay
                            ),


                        fontSize =
                            52.sp
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )



                    when{
                        uiState.isLoading -> {
                            CircularProgressIndicator()
                            Text("Loading forecast...")
                        }

                        !uiState.hasLocation -> {
                            Text(
                                "Select Trip Destination or Use Locate Me."
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
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = forecast.shortForecast
                            )

                            Text(
                                text = "Forecast time: ${forecast.endTime}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> {
                            Text(
                                text = uiState.errorMessage
                                    ?: forecastStatus?.message
                                    ?: "Forecast did not load successfully"
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            /*
             * -------------------------------------------------
             * WEATHER DETAILS
             * -------------------------------------------------
             */

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),

                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            18.dp
                        )
                ) {

                    Text(
                        text =
                            "WEATHER DETAILS",

                        color =
                            darkGreen,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    Text(
                        text = forecast?.detailedForecast ?: "Forecast details unavailable."
                    )

                    Spacer(
                        modifier =
                            Modifier.padding(20.dp)
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        WeatherDetailItem(
                            icon =
                                "🌧️",

                            label =
                                "Precipitation",

                            value =
                                forecast?.precipitationPercent
                                    ?.let{"$it%"}
                                    ?:"_"
                        )

                        WeatherDetailItem(
                            icon =
                                "💨",

                            label =
                                "Wind",

                            value =
                                forecast?.let {
                                    "${it.windDirection} ${it.windSpeed}"
                                } ?: "—"
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                18.dp
                            )
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        WeatherDetailItem(
                            icon =
                                "💧",

                            label =
                                "Humidity",

                            value =
                                forecast?.humidityPercent
                                    ?.let { "$it%" }
                                    ?: "—"
                        )

                        WeatherDetailItem(
                            icon =
                                "🌡️",

                            label =
                                "Feels Like",

                            value = forecast?.let { weather ->
                                weather.feelsLike?.let{  temp -> "$temp°${weather.tempUnit}"

                                }
                            }  ?: "—"
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "AIR QUALITY ESTIMATE",
                    fontWeight = FontWeight.Bold
                )
                Text(text = uiState.areaName)

                when {
                      uiState.isLoading -> {
                          CircularProgressIndicator()
                      }

                    !uiState.hasLocation -> {
                        Text("Click Locate Me for Location")
                    }

                    airQuality != null -> {
                        Text(
                            text = "US AQI: ${airQuality.usAqi ?: "_"}",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(text = airQuality.category)

                        Text(
                            text = "PM2.5" +
                                    (airQuality.pm25?.let {"$it µg/m³"} ?: "Unavailable")
                        )
                        Text(
                              text = "PM10: " +
                                      (airQuality.pm10?.let {"$it µg/m³"} ?: "Unavailable")

                          )

                         Text(
                                text = "Valid time: ${airQuality.validTime}",
                             style = MaterialTheme.typography.bodySmall

                            )
                         }

                   else -> {
                        Text(
                            text = uiState.errorMessage
                                ?: airQualityStatus?.message
                                ?: "Air quality has not loaded"

                        )
                   }
                }
                Text(
                    text = "Air-quality data: CAMS via Open-Meteo",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            /*
             * -------------------------------------------------
             * WEATHER ALERTS
             * -------------------------------------------------
             */

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),

                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            18.dp
                        )
                ) {

                    Text(
                        text =
                            "⚠️ WEATHER ALERTS",

                        color =
                            darkGreen,

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                10.dp
                            )
                    )



                               when {
                                   uiState.isLoading -> Text("Loading alerts...")

                                   !uiState.hasLocation ->
                                       Text("Select a location in Explorer.")

                                   uiState.errorMessage != null ->
                                       Text(uiState.errorMessage.orEmpty())

                                   nwsStatus?.state == SafetySourceState.SUCCESS -> {
                                       if (uiState.weatherNotifications.isEmpty()) {
                                           Text("No active NWS alerts returned for this location.")
                                       } else {
                                           uiState.weatherNotifications.forEach { alert ->
                                               Text(
                                                   text = alert.title,
                                                   fontWeight = FontWeight.Bold
                                               )
                                               Text(text = alert.details)
                                               Text(text = alert.instruct)
                                               Spacer(modifier = Modifier.height(12.dp))
                                           }
                                       }
                                   }

                                   else -> Text(nwsStatus?.message ?: "Alerts not loaded.")
                               }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            /*
             * -------------------------------------------------
             * CONNECTION STATUS
             * -------------------------------------------------
             */

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(
                                0xFFEFF5F0
                            )
                    ),

                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            16.dp
                        )
                ) {

                    Text(
                        text =
                            "Weather service",

                        color =
                            darkGreen,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                5.dp
                            )
                    )

                    Text(
                        text = when{
                            uiState.isLoading -> "Loading Forecast..."
                            !uiState.hasLocation -> "Select a location in Explorer."
                            uiState.errorMessage != null -> uiState.errorMessage.orEmpty()
                            else -> forecastStatus?.message ?:"Forecast not loaded"
                        },


                        color =
                            mediumGreen,

                        fontSize =
                            13.sp
                    )
                    Text(
                        text = "Weather data by Open-Meteo.com",
                        style = MaterialTheme.typography.bodySmall
                    )

                    TextButton(
                        onClick = safetyView::refreshNotifications,
                        enabled = uiState.hasLocation && !uiState.isLoading
                    ) {
                         Text("Refresh safety updates")
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        30.dp
                    )
            )
        }
    }
}


/**
 * Small weather detail item.
 */
@Composable
private fun WeatherDetailItem(
    icon: String,
    label: String,
    value: String
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    Column(
        modifier =
            Modifier.padding(
                horizontal =
                    8.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                icon,

            fontSize =
                27.sp
        )

        Spacer(
            modifier =
                Modifier.height(
                    5.dp
                )
        )

        Text(
            text =
                value,

            color =
                darkGreen,

            fontSize =
                19.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(
                    2.dp
                )
        )

        Text(
            text =
                label,

            color =
                Color(
                    0xFF666666
                ),

            fontSize =
                11.sp,

            textAlign =
                TextAlign.Center
        )
    }
}