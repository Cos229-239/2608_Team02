package com.cos229239.team02.oto.ui.screens.crisis

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed

@Composable
fun OfflineMapBacktrackScreen(
    onBackClick: () -> Unit
) {
    var isTracking by remember { mutableStateOf(false) }
    var isBacktracking by remember { mutableStateOf(false) }
    var trackingStatus by remember { mutableStateOf("Not tracking") }
    var backtrackStatus by remember { mutableStateOf("Backtrack not active") }
    var downloadedMaps by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text("Back")
                }
                Text(
                    text = "Offline Maps & Backtrack",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Track your route and find your way back, even without a signal.",
                style = MaterialTheme.typography.bodyMedium
            )

            // ----- Route Tracking -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Route Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = trackingStatus,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isTracking = !isTracking
                            isBacktracking = false
                            trackingStatus = if (isTracking) {
                                "Tracking route... (placeholder)"
                            } else {
                                "Not tracking"
                            }
                            backtrackStatus = "Backtrack not active"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isTracking) {
                            ButtonDefaults.buttonColors(
                                containerColor = OtoCrisisRed
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(
                            text = if (isTracking) "Stop Tracking" else "Start Tracking"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Route tracking records your path so you can backtrack later.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ----- Backtrack -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backtrack",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = backtrackStatus,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isTracking) {
                                isBacktracking = !isBacktracking
                                backtrackStatus = if (isBacktracking) {
                                    "Guiding you back... (placeholder)"
                                } else {
                                    "Backtrack not active"
                                }
                            } else {
                                backtrackStatus = "Start tracking first before backtracking"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isTracking,
                        colors = if (isBacktracking) {
                            ButtonDefaults.buttonColors(
                                containerColor = OtoCrisisRed
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(
                            text = if (isBacktracking) "Stop Backtrack" else "Start Backtrack"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Backtrack guides you back along the route you came from.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ----- Offline Maps -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Offline Maps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Download maps before you head out so they work without a signal.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OfflineMapItem(
                        regionName = "Current Area",
                        onDownload = {
                            if (!downloadedMaps.contains("Current Area")) {
                                downloadedMaps = downloadedMaps + "Current Area"
                            }
                        },
                        isDownloaded = downloadedMaps.contains("Current Area")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OfflineMapItem(
                        regionName = "Nearby Trails",
                        onDownload = {
                            if (!downloadedMaps.contains("Nearby Trails")) {
                                downloadedMaps = downloadedMaps + "Nearby Trails"
                            }
                        },
                        isDownloaded = downloadedMaps.contains("Nearby Trails")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OfflineMapItem(
                        regionName = "Full Region",
                        onDownload = {
                            if (!downloadedMaps.contains("Full Region")) {
                                downloadedMaps = downloadedMaps + "Full Region"
                            }
                        },
                        isDownloaded = downloadedMaps.contains("Full Region")
                    )
                }
            }

        }
    }
}

@Composable
private fun OfflineMapItem(
    regionName: String,
    onDownload: () -> Unit,
    isDownloaded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = regionName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isDownloaded) "Downloaded" else "Not downloaded",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(
            onClick = onDownload,
            enabled = !isDownloaded
        ) {
            Text(
                text = if (isDownloaded) "Downloaded" else "Download"
            )
        }
    }
}
