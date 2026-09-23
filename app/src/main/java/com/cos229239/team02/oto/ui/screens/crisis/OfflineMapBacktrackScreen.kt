package com.cos229239.team02.oto.ui.screens.crisis

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cos229239.team02.oto.data.route.RoutePoint
import com.cos229239.team02.oto.data.route.toOtoLocation
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
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

    val requestLocation =
        rememberOfflineMapBacktrackLocation(
            viewModel
        )

    Scaffold(
        topBar = {

            //Use OTO's shared Material 3 top app bar.
            OtoTopAppBar(
                title = "OFFLINE MAPS",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->

        val configuration =
            LocalConfiguration.current

        val isLandscape =
            configuration.screenWidthDp >
                configuration.screenHeightDp

        // Short landscape screens can't fit the tall map and the
        // downloads stacked, so they split the map and downloads
        // side by side instead.
        val shortLandscape =
            isLandscape &&
                configuration.screenHeightDp < 520

        if (shortLandscape) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                // ----- Live Map (left) -----
                LiveTrackingMapCard(
                    viewModel = viewModel,
                    onLocateMeClick = requestLocation,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                    matchParentHeight = true
                )

                // ----- Scrollable Downloads (right) -----
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    OfflineDownloadsContent(
                        viewModel = viewModel
                    )
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ----- Live Map -----
                LiveTrackingMapCard(
                    viewModel = viewModel,
                    onLocateMeClick = requestLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )

                // ----- Scrollable Downloads -----
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    OfflineDownloadsContent(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/*
 * ---------------------------------------------------------
 * OFFLINE MAPS CONTENT
 * ---------------------------------------------------------
 *
 * The intro line and the downloadable map regions, shared by
 * the stacked and side-by-side layouts.
 */

@Composable
private fun OfflineDownloadsContent(
    viewModel: OfflineMapBacktrackViewModel
) {

    Text(
        text = "Download maps before you head out so they work without a signal.",
        style = MaterialTheme.typography.bodyMedium
    )

    // ----- Offline Maps -----
    OfflineMapsCard(
        viewModel = viewModel
    )
}

/*
 * ---------------------------------------------------------
 * SHARED LOCATION & TRACKING LIFECYCLE
 * ---------------------------------------------------------
 *
 * Provides the location permission flow, an initial "locate
 * if already granted" request, and the save-a-checkpoint-on-
 * background behavior used by both the Crisis hub and the
 * Offline Maps screen.
 *
 * Returns a requestLocation action for the map's Locate Me
 * button.
 */

@Composable
fun rememberOfflineMapBacktrackLocation(
    viewModel: OfflineMapBacktrackViewModel
): () -> Unit {

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
     * Begins receiving location the first time the screen opens
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

    // Persist a checkpoint whenever the app is backgrounded, so closing
    // the app never loses the current tracking session.
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    viewModel.saveNowIfTracking()
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return {
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
}

/*
 * ---------------------------------------------------------
 * LIVE MAP
 * ---------------------------------------------------------
 *
 * Reusable live map card showing the user's current location
 * and the recorded (or reversed) route, with a small status
 * overlay and Locate Me action.
 */

@Composable
fun LiveTrackingMapCard(
    viewModel: OfflineMapBacktrackViewModel,
    onLocateMeClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapHeight: Dp = 440.dp,
    matchParentHeight: Boolean = false,
    showFullscreenButton: Boolean = true
) {

    var mapFullscreen by remember {
        mutableStateOf(false)
    }

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

    // The map is fed a throttled copy of the location so rapid GPS
    // fixes do not force a camera move on every single update.
    var mapLatitude by remember {
        mutableStateOf(currentLocation?.latitude)
    }
    var mapLongitude by remember {
        mutableStateOf(currentLocation?.longitude)
    }
    var lastMapUpdateMillis by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(currentLocation) {

        val location = currentLocation

        if (location != null) {

            val now = SystemClock.uptimeMillis()

            if (now - lastMapUpdateMillis >= MAP_UPDATE_INTERVAL_MILLIS) {
                mapLatitude = location.latitude
                mapLongitude = location.longitude
                lastMapUpdateMillis = now
            }
        }
    }

    Card(
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (matchParentHeight) {
                        Modifier.fillMaxHeight()
                    } else {
                        Modifier.height(mapHeight)
                    }
                )
        ) {

            OtoMap(
                modifier = Modifier.fillMaxSize(),
                latitude = mapLatitude,
                longitude = mapLongitude,
                routePoints = mapRoutePoints,
                followCamera = true,
                showMyLocationButton = false
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

                    if (currentLocation == null) {
                        TextButton(onClick = onLocateMeClick) {
                            Text("📍 Locate Me")
                        }
                    }
                }
            }

            if (showFullscreenButton) {

                Button(
                    onClick = {
                        mapFullscreen = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(42.dp),
                    shape = CircleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF063D24)
                        ),
                    contentPadding =
                        PaddingValues(0.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Filled.Fullscreen,
                        contentDescription = "Expand map",
                        modifier =
                            Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (mapFullscreen) {

        Dialog(
            onDismissRequest = {
                mapFullscreen = false
            },
            properties =
                DialogProperties(
                    decorFitsSystemWindows = false,
                    usePlatformDefaultWidth = false
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111A14))
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
            ) {

                LiveTrackingMapCard(
                    viewModel = viewModel,
                    onLocateMeClick = onLocateMeClick,
                    modifier =
                        Modifier.fillMaxSize(),
                    matchParentHeight = true,
                    showFullscreenButton = false
                )

                Button(
                    onClick = {
                        mapFullscreen = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(42.dp),
                    shape = CircleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF063D24)
                        ),
                    contentPadding =
                        PaddingValues(0.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Filled.FullscreenExit,
                        contentDescription = "Collapse map",
                        modifier =
                            Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/*
 * ---------------------------------------------------------
 * ROUTE TRACKING
 * ---------------------------------------------------------
 */

@Composable
fun RouteTrackingCard(
    viewModel: OfflineMapBacktrackViewModel
) {

    val isTracking = viewModel.isTracking
    val isBacktracking = viewModel.isBacktracking

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
                if (!isTracking && !isBacktracking) {
                    TextButton(
                        onClick = {
                            viewModel.clearPreviousRoute()
                        }
                    ) {
                        Text("🗑 Clear Previous Route")
                    }
                }
            }
            if (viewModel.saveRouteError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.saveRouteError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = OtoCrisisRed
                )
            }
            if (viewModel.routeSummary != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.routeSummary.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = OtoSuccess
                )
            }
            if (
                !isTracking &&
                !isBacktracking &&
                !viewModel.previousSessionRouteLoaded
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        viewModel.loadLastSavedRoute()
                    }
                ) {
                    Text("📂 Load Last Route")
                }
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
}

/*
 * ---------------------------------------------------------
 * BACKTRACK
 * ---------------------------------------------------------
 */

@Composable
fun BacktrackCard(
    viewModel: OfflineMapBacktrackViewModel
) {

    val isBacktracking = viewModel.isBacktracking

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
}

/*
 * ---------------------------------------------------------
 * OFFLINE MAPS
 * ---------------------------------------------------------
 */

@Composable
fun OfflineMapsCard(
    viewModel: OfflineMapBacktrackViewModel
) {

    val currentLocation = viewModel.currentLocation

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
                    estimatedTiles =
                        currentLocation?.let { location ->
                            OfflineMapBacktrackViewModel.estimatedTilesFor(
                                option = option,
                                latitude = location.latitude
                            )
                        },
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

@Composable
private fun OfflineRegionRow(
    option: OfflineRegionOption,
    pack: OfflinePack?,
    hasLocation: Boolean,
    estimatedTiles: Long? = null,
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

            if (estimatedTiles != null && pack == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "~${String.format(Locale.US, "%,d", estimatedTiles)} tiles",
                    style = MaterialTheme.typography.bodySmall
                )
                if (
                    estimatedTiles >
                    OfflineMapBacktrackViewModel.TILE_WARNING_LIMIT
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Large download - may exceed the offline tile limit",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = OtoWarningAmber
                    )
                }
            }

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

private const val MAP_UPDATE_INTERVAL_MILLIS = 500L