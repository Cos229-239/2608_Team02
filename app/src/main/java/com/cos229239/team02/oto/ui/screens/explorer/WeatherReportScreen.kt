package com.cos229239.team02.oto.ui.screens.explorer

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Weather details screen for Explorer.
 *
 * UI only for now.
 * Real NWS data will be connected later.
 */
@Composable
fun WeatherReportScreen(
    onBackClick: () -> Unit
) {

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
                text =
                    "Weather location will come from the active trip or current Explorer location.",

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
                            "☀️",

                        fontSize =
                            52.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Text(
                        text =
                            "—°",

                        color =
                            darkGreen,

                        fontSize =
                            44.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Weather not connected",

                        color =
                            Color(
                                0xFF666666
                            ),

                        fontSize =
                            16.sp,

                        textAlign =
                            TextAlign.Center
                    )
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
                                "—"
                        )

                        WeatherDetailItem(
                            icon =
                                "💨",

                            label =
                                "Wind",

                            value =
                                "—"
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
                                "—"
                        )

                        WeatherDetailItem(
                            icon =
                                "🌡️",

                            label =
                                "Feels Like",

                            value =
                                "—"
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

                    Text(
                        text =
                            "Live NWS weather warnings will appear here when connected.",

                        color =
                            Color(
                                0xFF666666
                            ),

                        fontSize =
                            14.sp
                    )
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
                        text =
                            "Waiting for the team's NWS weather client.",

                        color =
                            mediumGreen,

                        fontSize =
                            13.sp
                    )
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