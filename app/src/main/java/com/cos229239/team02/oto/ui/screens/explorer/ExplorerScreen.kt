package com.cos229239.team02.oto.ui.screens.explorer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.ui.features.SafetyLevel
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onPlanTripClick: () -> Unit,
    onBackClick: () -> Unit,
    tripViewModel: PlanTripViewModel,
    safetyView: AreaSafetyView = viewModel()
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val darkGreen = Color(0xFF063D24)
    val mediumGreen = Color(0xFF0B5D1E)
    val lightBackground = Color(0xFFF7F8F6)

    /*
     * ---------------------------------------------------------
     * SHARED DATA
     * ---------------------------------------------------------
     */

    val safetyState by
    safetyView.uiState.collectAsStateWithLifecycle()

    val savedTrip =
        tripViewModel.savedTrip

    /*
     * ---------------------------------------------------------
     * LOCATION
     * ---------------------------------------------------------
     */

    val locationRepository =
        remember(context) {
            AndroidLocationRepository(
                context.applicationContext
            )
        }

    var currentLocation by remember {
        mutableStateOf<OtoLocation?>(null)
    }

    var locationStatus by remember {
        mutableStateOf(
            "Location not loaded"
        )
    }

    var loadingLocation by remember {
        mutableStateOf(false)
    }

    fun loadCurrentLocation() {

        scope.launch {

            loadingLocation = true

            locationStatus =
                "Finding your location..."

            val location =
                locationRepository
                    .getCurrentLocation()

            if (location != null) {

                currentLocation =
                    location

                locationStatus =
                    "Current location found"

            } else {

                locationStatus =
                    "Unable to determine current location"
            }

            loadingLocation =
                false
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission
                                .ACCESS_COARSE_LOCATION
                        ] == true

            if (granted) {

                loadCurrentLocation()

            } else {

                locationStatus =
                    "Location permission denied"
            }
        }

    /*
     * If there is no saved trip,
     * Explorer falls back to current GPS location.
     */
    LaunchedEffect(
        savedTrip
    ) {

        if (savedTrip == null) {

            val fineGranted =
                ContextCompat
                    .checkSelfPermission(
                        context,
                        Manifest.permission
                            .ACCESS_FINE_LOCATION
                    ) ==
                        PackageManager
                            .PERMISSION_GRANTED

            val coarseGranted =
                ContextCompat
                    .checkSelfPermission(
                        context,
                        Manifest.permission
                            .ACCESS_COARSE_LOCATION
                    ) ==
                        PackageManager
                            .PERMISSION_GRANTED

            if (
                fineGranted ||
                coarseGranted
            ) {

                loadCurrentLocation()

            } else {

                locationPermissionLauncher
                    .launch(
                        arrayOf(
                            Manifest.permission
                                .ACCESS_FINE_LOCATION,

                            Manifest.permission
                                .ACCESS_COARSE_LOCATION
                        )
                    )
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * MAP STATE
     * ---------------------------------------------------------
     */

    var mapLoaded by remember {
        mutableStateOf(false)
    }

    var mapError by remember {
        mutableStateOf<String?>(null)
    }

    val cameraState =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target =
                        Position(
                            latitude =
                                39.9526,

                            longitude =
                                -75.1652
                        ),

                    zoom =
                        10.0
                )
        )

    /*
     * ---------------------------------------------------------
     * MAP CAMERA
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        savedTrip,
        currentLocation,
        mapLoaded
    ) {

        if (!mapLoaded) {
            return@LaunchedEffect
        }

        if (savedTrip != null) {

            var west =
                min(
                    savedTrip.startingLongitude,
                    savedTrip.destinationLongitude
                )

            var east =
                max(
                    savedTrip.startingLongitude,
                    savedTrip.destinationLongitude
                )

            var south =
                min(
                    savedTrip.startingLatitude,
                    savedTrip.destinationLatitude
                )

            var north =
                max(
                    savedTrip.startingLatitude,
                    savedTrip.destinationLatitude
                )

            /*
             * Prevent an overly close zoom if the two
             * points are almost on top of each other.
             */
            if (
                east - west < 0.005
            ) {
                west -= 0.0025
                east += 0.0025
            }

            if (
                north - south < 0.005
            ) {
                south -= 0.0025
                north += 0.0025
            }

            cameraState
                .jumpTo(
                    boundingBox =
                        BoundingBox(
                            west = west,
                            south = south,
                            east = east,
                            north = north
                        ),

                    padding =
                        PaddingValues(
                            45.dp
                        )
                )

        } else {

            currentLocation
                ?.let { location ->

                    cameraState.position =
                        CameraPosition(
                            target =
                                Position(
                                    latitude =
                                        location.latitude,

                                    longitude =
                                        location.longitude
                                ),

                            zoom =
                                14.0
                        )
                }
        }
    }

    /*
     * ---------------------------------------------------------
     * MAIN SCREEN
     * ---------------------------------------------------------
     */

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
         * EXPLORER HEADER
         * -----------------------------------------------------
         */

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        darkGreen
                    )
                    .statusBarsPadding()
                    .padding(
                        horizontal =
                            18.dp,

                        vertical =
                            14.dp
                    )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

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
                            "EXPLORER MODE",

                        color =
                            Color.White,

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Plan • Navigate • Report • Stay Safe",

                        color =
                            Color.White,

                        fontSize =
                            14.sp
                    )
                }

                Text(
                    text =
                        "🔔",

                    fontSize =
                        24.sp
                )
            }
        }

        /*
         * -----------------------------------------------------
         * SCROLLABLE DASHBOARD
         * -----------------------------------------------------
         */

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
        ) {

            /*
             * -------------------------------------------------
             * MAP
             * -------------------------------------------------
             */

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            300.dp
                        )
            ) {

                MaplibreMap(
                    modifier =
                        Modifier.fillMaxSize(),

                    baseStyle =
                        BaseStyle.Uri(
                            "https://tiles.openfreemap.org/styles/liberty"
                        ),

                    cameraState =
                        cameraState,

                    onMapLoadFinished = {

                        mapLoaded =
                            true

                        mapError =
                            null
                    },

                    onMapLoadFailed = { reason ->

                        mapLoaded =
                            false

                        mapError =
                            reason
                                ?: "Unknown map loading error"
                    }
                ) {

                    /*
                     * ---------------------------------------------
                     * SAVED TRIP MAP MARKERS
                     * ---------------------------------------------
                     */

                    savedTrip
                        ?.let { trip ->

                            /*
                             * Starting Point.
                             */
                            val startingPointSource =
                                rememberGeoJsonSource(
                                    data =
                                        GeoJsonData.JsonString(
                                            """
                                            {
                                              "type": "Feature",
                                              "geometry": {
                                                "type": "Point",
                                                "coordinates": [
                                                  ${trip.startingLongitude},
                                                  ${trip.startingLatitude}
                                                ]
                                              }
                                            }
                                            """.trimIndent()
                                        )
                                )

                            /*
                             * BLUE = Starting Point
                             */
                            CircleLayer(
                                id =
                                    "saved-trip-start",

                                source =
                                    startingPointSource,

                                radius =
                                    const(
                                        9.dp
                                    ),

                                color =
                                    const(
                                        Color(
                                            0xFF1976D2
                                        )
                                    ),

                                strokeColor =
                                    const(
                                        Color.White
                                    ),

                                strokeWidth =
                                    const(
                                        3.dp
                                    )
                            )

                            /*
                             * Destination.
                             */
                            val destinationSource =
                                rememberGeoJsonSource(
                                    data =
                                        GeoJsonData.JsonString(
                                            """
                                            {
                                              "type": "Feature",
                                              "geometry": {
                                                "type": "Point",
                                                "coordinates": [
                                                  ${trip.destinationLongitude},
                                                  ${trip.destinationLatitude}
                                                ]
                                              }
                                            }
                                            """.trimIndent()
                                        )
                                )

                            /*
                             * GREEN = Destination / Finish
                             */
                            CircleLayer(
                                id =
                                    "saved-trip-destination",

                                source =
                                    destinationSource,

                                radius =
                                    const(
                                        9.dp
                                    ),

                                color =
                                    const(
                                        Color(
                                            0xFF149447
                                        )
                                    ),

                                strokeColor =
                                    const(
                                        Color.White
                                    ),

                                strokeWidth =
                                    const(
                                        3.dp
                                    )
                            )
                        }
                }

                /*
                 * ---------------------------------------------
                 * MAP INFORMATION CARD
                 * ---------------------------------------------
                 */

                Card(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(
                                12.dp
                            ),

                    colors =
                        CardDefaults
                            .cardColors(
                                containerColor =
                                    Color.White
                                        .copy(
                                            alpha =
                                                0.93f
                                        )
                            ),

                    shape =
                        RoundedCornerShape(
                            12.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                10.dp
                            )
                    ) {

                        if (
                            savedTrip != null
                        ) {

                            Text(
                                text =
                                    "ACTIVE TRIP",

                                color =
                                    darkGreen,

                                fontSize =
                                    11.sp,

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
                                    "🔵 Start",

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    darkGreen
                            )

                            Text(
                                text =
                                    shortenLocationName(
                                        savedTrip
                                            .startingPointName
                                    ),

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        6.dp
                                    )
                            )

                            Text(
                                text =
                                    "🟢 Destination",

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    darkGreen
                            )

                            Text(
                                text =
                                    shortenLocationName(
                                        savedTrip
                                            .destinationName
                                    ),

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )

                        } else {

                            Text(
                                text =
                                    "CURRENT LOCATION",

                                color =
                                    darkGreen,

                                fontSize =
                                    11.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        3.dp
                                    )
                            )

                            if (
                                loadingLocation
                            ) {

                                CircularProgressIndicator()

                            } else {

                                currentLocation
                                    ?.let { location ->

                                        Text(
                                            text =
                                                formatExplorerLocation(
                                                    location
                                                ),

                                            color =
                                                darkGreen,

                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )
                                    }
                                    ?: Text(
                                        text =
                                            locationStatus,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall
                                    )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        5.dp
                                    )
                            )

                            Text(
                                text =
                                    "Plan a trip to display it on the map.",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }
                    }
                }

                /*
                 * ---------------------------------------------
                 * MAP ERROR / LOADING
                 * ---------------------------------------------
                 */

                if (
                    mapError != null
                ) {

                    Card(
                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center
                                )
                                .padding(
                                    16.dp
                                ),

                        colors =
                            CardDefaults
                                .cardColors(
                                    containerColor =
                                        Color.White
                                )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),

                            horizontalAlignment =
                                Alignment
                                    .CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "Map failed to load",

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error,

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
                                    mapError.orEmpty(),

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall,

                                textAlign =
                                    TextAlign.Center
                            )
                        }
                    }

                } else if (
                    !mapLoaded
                ) {

                    Card(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),

                        colors =
                            CardDefaults
                                .cardColors(
                                    containerColor =
                                        Color.White
                                            .copy(
                                                alpha =
                                                    0.85f
                                            )
                                )
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    10.dp
                                ),

                            verticalAlignment =
                                Alignment
                                    .CenterVertically,

                            horizontalArrangement =
                                Arrangement
                                    .spacedBy(
                                        8.dp
                                    )
                        ) {

                            CircularProgressIndicator()

                            Text(
                                text =
                                    "Loading map..."
                            )
                        }
                    }
                }
            }

            /*
             * -------------------------------------------------
             * DASHBOARD CONTENT
             * -------------------------------------------------
             */

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                /*
                 * QUICK ACTIONS
                 */

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    ExplorerActionCard(
                        title =
                            "PLAN TRIP",

                        description =
                            if (
                                savedTrip == null
                            ) {
                                "Plan a new trip"
                            } else {
                                "View or edit trip"
                            },

                        icon =
                            "📍",

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick =
                            onPlanTripClick
                    )

                    ExplorerActionCard(
                        title =
                            "CREATE ROUTE",

                        description =
                            "Build a custom route",

                        icon =
                            "➕",

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick = {
                            // Future feature.
                        }
                    )

                    ExplorerActionCard(
                        title =
                            "OFFLINE MAPS",

                        description =
                            "Save maps offline",

                        icon =
                            "⬇️",

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        onClick = {
                            // Future feature.
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                SafetyOverviewCard(
                    areaName =
                        safetyState.areaName,

                    alertCount =
                        safetyState
                            .notifications
                            .size,

                    severeCount =
                        safetyState
                            .notifications
                            .count {
                                it.level ==
                                        SafetyLevel.SEVERE
                            },

                    moderateCount =
                        safetyState
                            .notifications
                            .count {
                                it.level ==
                                        SafetyLevel.MODERATE
                            },

                    isLoading =
                        safetyState.isLoading,

                    isOffline =
                        safetyState.isOffline,

                    isSample =
                        safetyState.isSampleData,

                    onClick =
                        onAreaSafetyClick
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                DashboardWideCard(
                    title =
                        "⚠️  REPORT HAZARD / ROUTE CHANGE",

                    subtitle =
                        "Help keep trails safe for everyone",

                    onClick = {
                        // Future feature.
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults
                            .cardColors(
                                containerColor =
                                    Color.White
                            ),

                    shape =
                        RoundedCornerShape(
                            14.dp
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
                                "👥  CHECK-IN",

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                darkGreen
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    6.dp
                                )
                        )

                        Text(
                            text =
                                "Trusted Contact",

                            fontSize =
                                14.sp
                        )

                        Text(
                            text =
                                "Not checked in",

                            color =
                                Color(
                                    0xFFE67E22
                                ),

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    10.dp
                                )
                        )

                        Button(
                            onClick = {
                                // Future feature.
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            colors =
                                ButtonDefaults
                                    .buttonColors(
                                        containerColor =
                                            mediumGreen
                                    )
                        ) {

                            Text(
                                text =
                                    "CHECK IN"
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                DashboardWideCard(
                    title =
                        "📋  FIELD REPORTS",

                    subtitle =
                        "View recent reports from this area",

                    onClick = {
                        // Future feature.
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun ExplorerActionCard(
    title: String,
    description: String,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    Card(
        modifier =
            modifier
                .height(
                    150.dp
                )
                .clickable {
                    onClick()
                },

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                ),

        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        12.dp
                    ),

            horizontalAlignment =
                Alignment
                    .CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text =
                    icon,

                fontSize =
                    30.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    title,

                color =
                    darkGreen,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    14.sp,

                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text =
                    description,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}

@Composable
private fun SafetyOverviewCard(
    areaName: String,
    alertCount: Int,
    severeCount: Int,
    moderateCount: Int,
    isLoading: Boolean,
    isOffline: Boolean,
    isSample: Boolean,
    onClick: () -> Unit
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                ),

        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "🛡️  SAFETY OVERVIEW",

                    color =
                        darkGreen,

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "View Area Safety ›",

                    color =
                        darkGreen,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    areaName,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            if (
                isLoading
            ) {

                CircularProgressIndicator()

            } else {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {

                    SafetyStat(
                        value =
                            alertCount
                                .toString(),

                        label =
                            "Active Alerts"
                    )

                    SafetyStat(
                        value =
                            severeCount
                                .toString(),

                        label =
                            "Severe"
                    )

                    SafetyStat(
                        value =
                            moderateCount
                                .toString(),

                        label =
                            "Moderate"
                    )
                }
            }

            if (
                isOffline
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    text =
                        "⚠ Offline — showing saved safety information",

                    color =
                        MaterialTheme
                            .colorScheme
                            .error,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            if (
                isSample
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        "Sample safety data",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }
        }
    }
}

@Composable
private fun SafetyStat(
    value: String,
    label: String
) {

    Column(
        horizontalAlignment =
            Alignment
                .CenterHorizontally
    ) {

        Text(
            text =
                value,

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun DashboardWideCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                ),

        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        18.dp
                    ),

            verticalAlignment =
                Alignment
                    .CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        title,

                    color =
                        darkGreen,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        17.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        subtitle
                )
            }

            Text(
                text =
                    "›",

                fontSize =
                    28.sp,

                color =
                    darkGreen
            )
        }
    }
}

private fun formatExplorerLocation(
    location: OtoLocation
): String {

    return "Latitude: ${
        String.format(
            Locale.US,
            "%.5f",
            location.latitude
        )
    }\nLongitude: ${
        String.format(
            Locale.US,
            "%.5f",
            location.longitude
        )
    }"
}

private fun shortenLocationName(
    locationName: String
): String {

    val pieces =
        locationName
            .split(",")

    return pieces
        .take(2)
        .joinToString(",")
        .trim()
}