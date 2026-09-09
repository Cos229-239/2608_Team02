package com.cos229239.team02.oto.ui.screens.crisis

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cos229239.team02.oto.data.route.RoutePoint
import com.cos229239.team02.oto.data.route.toOtoLocation
import com.cos229239.team02.oto.ui.components.map.OtoMap
import com.cos229239.team02.oto.ui.features.OfflineMapBacktrackViewModel
import com.cos229239.team02.oto.ui.features.OfflineRegionOption
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed
import com.cos229239.team02.oto.ui.theme.OtoLocationBlue
import com.cos229239.team02.oto.ui.theme.OtoSuccess
import com.cos229239.team02.oto.ui.theme.OtoWarningAmber
import java.util.Locale
import org.maplibre.compose.offline.DownloadProgress
import org.maplibre.compose.offline.DownloadStatus
import org.maplibre.compose.offline.OfflinePack

@Composable
fun OfflineMapBacktrackScreen(
    onBackClick: () -> Unit,
    viewModel: OfflineMapBacktrackViewModel =
        viewModel()
) {

    val context = LocalContext.current

    // -------------------------------------------------------------
    // Location permission
    // -------------------------------------------------------------

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true

            if (granted) {
                viewModel.refreshCurrentLocation()
            } else {
                viewModel.locationPermissionDenied()
            }
        }

    /**
     * Begins tracking/tracking location the first time the screen opens
     * if permission was already granted elsewhere in the app.
     */
    LaunchedEffect(Unit) {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.refreshCurrentLocation()
        }
    }

    fun requestLocation() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.refreshCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val isTracking = viewModel.isTracking
    val isBacktracking = viewModel.isBacktracking
    val currentLocation = viewModel.currentLocation

    // The map shows the recorded route, or the reversed route
    // while the user is backtracking.
    val displayedRoutePoints =
        if (isBacktracking) {
            viewModel.routePoints.asReversed()
        } else {
            viewModel.routePoints
        }

    val mapRoutePoints =
        remember(displayedRoutePoints) {
            displayedRoutePoints.map(
                RoutePoint::toOtoLocation
            )
        }

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

            // ----- Live Map -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {

                    OtoMap(
                        modifier = Modifier.fillMaxSize(),
                        latitude = currentLocation?.latitude,
                        longitude = currentLocation?.longitude,
                        routePoints = mapRoutePoints,
                        followCamera = true
                    )

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.92f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            val locationText =
                                currentLocation?.let { location ->
                                    "Lat ${"%.5f".format(Locale.US, location.latitude)}  •  " +
                                        "Lon ${"%.5f".format(Locale.US, location.longitude)}"
                                } ?: viewModel.locationStatus

                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )

                            if (
                                isBacktracking &&
                                viewModel.guideTarget != null
                            ) {
                                Text(
                                    text = viewModel.backtrackGuidance,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OtoLocationBlue,
                                    textAlign = TextAlign.Center
                                )
                            }

                            TextButton(onClick = { requestLocation() }) {
                                Text("📍 Locate Me")
                            }
                        }
                    }
                }
            }

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
                        text = viewModel.trackingStatus,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (viewModel.previousSessionRouteLoaded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This route was recorded earlier " +
                                "and is ready to backtrack.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OtoWarningAmber
                        )
                    }
                    if (viewModel.saveRouteError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.saveRouteError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = OtoCrisisRed
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isTracking) {
                                viewModel.stopTracking()
                            } else {
                                viewModel.startTracking()
                            }
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
                        text = viewModel.backtrackStatus,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isBacktracking && !viewModel.backtrackGuidance.isNullOrEmpty()
                        && viewModel.backtrackStatus != "You are back at the start of your route"
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.backtrackGuidance,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = OtoSuccess
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isBacktracking) {
                                viewModel.stopBacktrack()
                            } else {
                                viewModel.startBacktrack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = viewModel.routePoints.size >= 2,
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

                    if (viewModel.downloadError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.downloadError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = OtoCrisisRed
                        )
                    }

                    if (currentLocation == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use Locate Me above to enable downloads " +
                                "for your area.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OtoWarningAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OfflineMapBacktrackViewModel.DEFAULT_REGION_OPTIONS.forEach { option ->
                        val pack =
                            viewModel.offlinePacks.firstOrNull {
                                viewModel.regionNameFor(it) == option.name
                            }

                        OfflineRegionRow(
                            option = option,
                            pack = pack,
                            hasLocation = currentLocation != null,
                            onDownload = {
                                viewModel.downloadRegion(option)
                            },
                            onDelete = {
                                pack?.let(viewModel::deleteRegion)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineRegionRow(
    option: OfflineRegionOption,
    pack: OfflinePack?,
    hasLocation: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = option.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall
            )

            if (pack != null) {
                Text(
                    text = offlinePackStatusText(pack),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (isPackComplete(pack)) OtoSuccess
                        else if (isPackInError(pack)) OtoCrisisRed
                        else OtoWarningAmber
                )
            }
        }

        if (pack == null) {

            Button(
                onClick = onDownload,
                enabled = hasLocation
            ) {
                Text("Download")
            }

        } else {

            TextButton(onClick = onDelete) {
                Text(
                    text =
                        if (isPackDownloading(pack)) "Cancel"
                        else "Delete",
                    color = OtoCrisisRed
                )
            }
        }
    }
}

private fun isPackComplete(pack: OfflinePack): Boolean {
    val progress = pack.downloadProgress
    return progress is DownloadProgress.Healthy &&
        progress.status == DownloadStatus.Complete
}

private fun isPackDownloading(pack: OfflinePack): Boolean {
    val progress = pack.downloadProgress
    return progress is DownloadProgress.Healthy &&
        progress.status == DownloadStatus.Downloading
}

private fun isPackInError(pack: OfflinePack): Boolean =
    pack.downloadProgress is DownloadProgress.Error ||
        pack.downloadProgress is DownloadProgress.TileLimitExceeded

private fun offlinePackStatusText(pack: OfflinePack): String =
    when (val progress = pack.downloadProgress) {

        is DownloadProgress.Healthy ->
            when (progress.status) {

                DownloadStatus.Complete ->
                    "Downloaded (${formatSize(progress.completedTileBytes)})"

                DownloadStatus.Downloading ->
                    "Downloading ${progress.completedTileCount} tiles " +
                        "(${formatSize(progress.completedTileBytes)})"

                DownloadStatus.Paused ->
                    "Paused (${formatSize(progress.completedTileBytes)})"
            }

        is DownloadProgress.TileLimitExceeded ->
            "Too many tiles for this area"

        is DownloadProgress.Error ->
            "Download failed: ${progress.message}"

        DownloadProgress.Unknown ->
            "Starting download..."
    }

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${bytes} B"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[unitIndex])
    }
}