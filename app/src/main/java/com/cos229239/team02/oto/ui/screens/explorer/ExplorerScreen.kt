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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.cos229239.team02.oto.data.route.RouteClient
import com.cos229239.team02.oto.data.route.RouteResult
import com.cos229239.team02.oto.ui.components.map.OtoMap
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onPlanTripClick: () -> Unit,

    /*
     * Opens the Weather Report screen.
     */
    onWeatherClick: () -> Unit,

    onBackClick: () -> Unit,
    tripViewModel: PlanTripViewModel,
    safetyView: AreaSafetyView = viewModel()
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val darkGreen =
        Color(0xFF063D24)

    val mediumGreen =
        Color(0xFF0B5D1E)

    val lightBackground =
        Color(0xFFF7F8F6)

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
     * ROUTE NAVIGATION
     * ---------------------------------------------------------
     */

    val routeClient =
        remember {
            RouteClient()
        }

    var routes by remember {
        mutableStateOf<List<RouteResult>>(
            emptyList()
        )
    }

    var selectedRouteIndex by remember {
        mutableIntStateOf(0)
    }

    var routeLoading by remember {
        mutableStateOf(false)
    }

    var routeError by remember {
        mutableStateOf<String?>(null)
    }

    var isTripCardExpanded by remember {
        mutableStateOf(true)
    }

    /*
     * Retrieve route geometry whenever
     * the saved trip changes.
     */
    LaunchedEffect(
        savedTrip?.startingLatitude,
        savedTrip?.startingLongitude,
        savedTrip?.destinationLatitude,
        savedTrip?.destinationLongitude
    ) {

        val trip =
            savedTrip

        if (
            trip == null
        ) {

            routes =
                emptyList()

            selectedRouteIndex =
                0

            routeLoading =
                false

            routeError =
                null

            isTripCardExpanded =
                true

            return@LaunchedEffect
        }

        routeLoading =
            true

        routeError =
            null

        routes =
            emptyList()

        selectedRouteIndex =
            0

        val results =
            routeClient.getRoutes(
                startingLatitude =
                    trip.startingLatitude,

                startingLongitude =
                    trip.startingLongitude,

                destinationLatitude =
                    trip.destinationLatitude,

                destinationLongitude =
                    trip.destinationLongitude
            )

        if (
            results.isNotEmpty()
        ) {

            routes =
                results

        } else {

            routeError =
                "Route unavailable"
        }

        routeLoading =
            false
    }

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

    /*
     * Used by OtoMap when the user manually
     * requests their current location.
     */
    var locationFocusRequest by remember {
        mutableIntStateOf(0)
    }

    /*
     * Remembers whether the map should center
     * after Android location permission is granted.
     */
    var focusAfterPermission by remember {
        mutableStateOf(false)
    }

    var hasLocationPermission by remember {

        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    /*
     * ---------------------------------------------------------
     * REQUEST FRESH LOCATION
     * ---------------------------------------------------------
     */

    fun loadCurrentLocation(
        focusOnMap: Boolean = false
    ) {

        scope.launch {

            loadingLocation =
                true

            locationStatus =
                "Finding your location..."

            val location =
                locationRepository
                    .getCurrentLocation()

            if (
                location != null
            ) {

                currentLocation =
                    location

                locationStatus =
                    "Current location found"

                if (
                    focusOnMap
                ) {

                    locationFocusRequest++
                }

            } else {

                locationStatus =
                    "Unable to determine current location"
            }

            loadingLocation =
                false
        }
    }

    /*
     * ---------------------------------------------------------
     * LOCATION PERMISSION
     * ---------------------------------------------------------
     */

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true

            hasLocationPermission =
                granted

            if (
                granted
            ) {

                loadCurrentLocation(
                    focusOnMap =
                        focusAfterPermission
                )

            } else {

                locationStatus =
                    "Location permission denied"
            }

            focusAfterPermission =
                false
        }

    /*
     * ---------------------------------------------------------
     * REQUEST LOCATION
     * ---------------------------------------------------------
     */

    fun requestLocation(
        focusOnMap: Boolean = false
    ) {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED

        if (
            fineGranted ||
            coarseGranted
        ) {

            hasLocationPermission =
                true

            loadCurrentLocation(
                focusOnMap =
                    focusOnMap
            )

        } else {

            focusAfterPermission =
                focusOnMap

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /*
     * ---------------------------------------------------------
     * INITIAL LOCATION
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        hasLocationPermission
    ) {

        if (
            hasLocationPermission
        ) {

            val initialLocation =
                locationRepository
                    .getCurrentLocation()

            if (
                initialLocation != null
            ) {

                currentLocation =
                    initialLocation

                locationStatus =
                    "Live location active"
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * CONTINUOUS LOCATION UPDATES
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        hasLocationPermission
    ) {

        if (
            !hasLocationPermission
        ) {

            return@LaunchedEffect
        }

        locationRepository
            .observeLocationUpdates()
            .collect { newLocation ->

                currentLocation =
                    newLocation

                locationStatus =
                    "Live location active"

                loadingLocation =
                    false
            }
    }

    /*
     * ---------------------------------------------------------
     * SCREEN
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
        * HEADER
        * -----------------------------------------------------
        */

        //Use OTO's shared Material 3 top app bar.
        OtoTopAppBar(
            title = "EXPLORER MODE",
            onBackClick = onBackClick
        )

        /*
         * -----------------------------------------------------
         * MAP
         * -----------------------------------------------------
         */

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        440.dp
                    )
        ) {

            OtoMap(
                modifier =
                    Modifier.fillMaxSize(),

                latitude =
                    currentLocation
                        ?.latitude,

                longitude =
                    currentLocation
                        ?.longitude,

                startingLatitude =
                    savedTrip
                        ?.startingLatitude,

                startingLongitude =
                    savedTrip
                        ?.startingLongitude,

                destinationLatitude =
                    savedTrip
                        ?.destinationLatitude,

                destinationLongitude =
                    savedTrip
                        ?.destinationLongitude,

                routes =
                    routes,

                selectedRouteIndex =
                    selectedRouteIndex,

                onMyLocationClick = {

                    requestLocation(
                        focusOnMap =
                            true
                    )
                },

                locationFocusRequest =
                    locationFocusRequest
            )

            /*
             * -------------------------------------------------
             * CURRENT LOCATION CARD
             * -------------------------------------------------
             */

            if (
                savedTrip == null
            ) {

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
                                    Color.White.copy(
                                        alpha =
                                            0.92f
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

                                } ?: Text(
                                text =
                                    locationStatus,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }

                        if (
                            currentLocation == null &&
                            !loadingLocation
                        ) {

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        8.dp
                                    )
                            )

                            Button(
                                onClick = {

                                    requestLocation(
                                        focusOnMap =
                                            true
                                    )
                                },

                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                mediumGreen
                                        )
                            ) {

                                Text(
                                    text =
                                        "Locate Me"
                                )
                            }
                        }
                    }
                }
            }

            /*
             * -------------------------------------------------
             * EXPANDED ACTIVE TRIP
             * -------------------------------------------------
             */

            if (
                savedTrip != null &&
                isTripCardExpanded
            ) {

                Card(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .fillMaxWidth()
                            .padding(
                                12.dp
                            ),

                    colors =
                        CardDefaults
                            .cardColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha =
                                            0.95f
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
                                12.dp
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
                                    "ACTIVE TRIP",

                                color =
                                    darkGreen,

                                fontSize =
                                    12.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            TextButton(
                                onClick = {

                                    isTripCardExpanded =
                                        false
                                }
                            ) {

                                Text(
                                    text =
                                        "−",

                                    color =
                                        darkGreen,

                                    fontSize =
                                        24.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text =
                                "Start: ${savedTrip.startingPointName}",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )

                        Text(
                            text =
                                "Destination: ${savedTrip.destinationName}",

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

                        when {

                            routeLoading -> {

                                Text(
                                    text =
                                        "Calculating routes...",

                                    color =
                                        darkGreen
                                )
                            }

                            routes.isNotEmpty() -> {

                                val selectedRoute =
                                    routes[
                                        selectedRouteIndex
                                    ]

                                Text(
                                    text =
                                        "${
                                            formatRouteDistance(
                                                selectedRoute.distanceMeters
                                            )
                                        } • ${
                                            formatRouteDuration(
                                                selectedRoute.durationSeconds
                                            )
                                        }",

                                    color =
                                        darkGreen,

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        if (
                                            routes.size > 1
                                        ) {

                                            "Fastest Route • ${routes.size} options"

                                        } else {

                                            "Fastest Route"
                                        },

                                    color =
                                        mediumGreen,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )
                            }

                            routeError != null -> {

                                Text(
                                    text =
                                        routeError
                                            ?: "Route unavailable",

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                            }
                        }

                        TextButton(
                            onClick =
                                onPlanTripClick
                        ) {

                            Text(
                                text =
                                    "View Trip ›",

                                color =
                                    mediumGreen,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }

            /*
             * -------------------------------------------------
             * MINIMIZED ACTIVE TRIP
             * -------------------------------------------------
             */

            if (
                savedTrip != null &&
                !isTripCardExpanded
            ) {

                Card(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomCenter
                            )
                            .padding(
                                12.dp
                            ),

                    colors =
                        CardDefaults
                            .cardColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha =
                                            0.95f
                                    )
                            ),

                    shape =
                        RoundedCornerShape(
                            22.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    16.dp,

                                vertical =
                                    7.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        Column {

                            Text(
                                text =
                                    "ACTIVE TRIP",

                                color =
                                    darkGreen,

                                fontSize =
                                    10.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            if (
                                routes.isNotEmpty()
                            ) {

                                val selectedRoute =
                                    routes[
                                        selectedRouteIndex
                                    ]

                                Text(
                                    text =
                                        "${
                                            formatRouteDistance(
                                                selectedRoute.distanceMeters
                                            )
                                        } • ${
                                            formatRouteDuration(
                                                selectedRoute.durationSeconds
                                            )
                                        }",

                                    color =
                                        darkGreen,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                            } else if (
                                routeLoading
                            ) {

                                Text(
                                    text =
                                        "Calculating..."
                                )

                            } else {

                                Text(
                                    text =
                                        "Route unavailable"
                                )
                            }
                        }

                        TextButton(
                            onClick = {

                                isTripCardExpanded =
                                    true
                            }
                        ) {

                            Text(
                                text =
                                    "▲",

                                color =
                                    darkGreen,

                                fontSize =
                                    18.sp
                            )
                        }
                    }
                }
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
                    .weight(
                        1f
                    )
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        16.dp
                    )
        ) {

            /*
             * -------------------------------------------------
             * QUICK ACTIONS
             * -------------------------------------------------
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

            /*
             * -------------------------------------------------
             * SAFETY OVERVIEW
             * -------------------------------------------------
             */

            SafetyOverviewCard(
                areaName =
                    safetyState.areaName,

                isLoading =
                    safetyState.isLoading,

                isOffline =
                    safetyState.isOffline,

                isSample =
                    safetyState.isSampleData,

                onAreaSafetyClick =
                    onAreaSafetyClick,

                onWeatherClick =
                    onWeatherClick
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

            /*
             * -------------------------------------------------
             * CHECK-IN
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
                            "Trusted Contact"
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
                            ButtonDefaults.buttonColors(
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

            /*
             * -------------------------------------------------
             * FIELD REPORTS
             * -------------------------------------------------
             */

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


/**
 * Small Explorer action card.
 */
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
            CardDefaults.cardColors(
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
                Alignment.CenterHorizontally,

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


/**
 * Explorer Safety Overview summary.
 *
 * Weather opens its own Weather Report screen.
 *
 * "View Area Safety" still opens Eric's existing
 * Area Safety screen.
 */
@Composable
private fun SafetyOverviewCard(
    areaName: String,
    isLoading: Boolean,
    isOffline: Boolean,
    isSample: Boolean,
    onAreaSafetyClick: () -> Unit,
    onWeatherClick: () -> Unit
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    val mediumGreen =
        Color(
            0xFF0B5D1E
        )

    val dividerColor =
        Color(
            0xFFE1E5E1
        )

    /*
     * The entire card is intentionally NOT clickable.
     *
     * Individual features can now have separate
     * destinations.
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
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            /*
             * -------------------------------------------------
             * HEADER
             * -------------------------------------------------
             */

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            "🛡️",

                        fontSize =
                            22.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    4.dp
                            )
                    )

                    Text(
                        text =
                            "SAFETY OVERVIEW",

                        color =
                            darkGreen,

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                /*
                 * This remains connected to Eric's
                 * Area Safety screen.
                 */
                Text(
                    text =
                        "View Area Safety ›",

                    modifier =
                        Modifier.clickable {
                            onAreaSafetyClick()
                        },

                    color =
                        mediumGreen,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            /*
             * -------------------------------------------------
             * AREA
             * -------------------------------------------------
             */

            if (
                areaName.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        areaName,

                    color =
                        Color(
                            0xFF4A554F
                        ),

                    fontSize =
                        12.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            /*
             * -------------------------------------------------
             * SUMMARY CONTENT
             * -------------------------------------------------
             */

            if (
                isLoading
            ) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                120.dp
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color =
                            mediumGreen
                    )
                }

            } else {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.Top
                ) {

                    /*
                     * -------------------------------------------------
                     * WEATHER
                     * -------------------------------------------------
                     *
                     * Weather now has its own click action.
                     */
                    SafetyOverviewItem(
                        title =
                            "WEATHER",

                        icon =
                            "☀️",

                        mainValue =
                            "—",

                        description =
                            "View weather",

                        modifier =
                            Modifier
                                .weight(
                                    1f
                                )
                                .clickable {
                                    onWeatherClick()
                                }
                    )

                    SafetyOverviewDivider(
                        color =
                            dividerColor
                    )

                    /*
                     * HAZARDS
                     */
                    SafetyOverviewItem(
                        title =
                            "HAZARDS",

                        icon =
                            "⚠️",

                        mainValue =
                            "—",

                        description =
                            "Not connected",

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    SafetyOverviewDivider(
                        color =
                            dividerColor
                    )

                    /*
                     * CLOSURES
                     */
                    SafetyOverviewItem(
                        title =
                            "CLOSURES",

                        icon =
                            "⛔",

                        mainValue =
                            "—",

                        description =
                            "Not connected",

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    SafetyOverviewDivider(
                        color =
                            dividerColor
                    )

                    /*
                     * AIR QUALITY
                     */
                    SafetyOverviewItem(
                        title =
                            "AIR QUALITY",

                        icon =
                            "🍃",

                        mainValue =
                            "—",

                        description =
                            "Not connected",

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )
                }
            }

            /*
             * -------------------------------------------------
             * STATUS INFORMATION
             * -------------------------------------------------
             */

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
                        "⚠ Offline — live safety information may be unavailable",

                    color =
                        MaterialTheme
                            .colorScheme
                            .error,

                    fontSize =
                        11.sp,

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
                        "Area Alerts currently contains sample safety data",

                    color =
                        Color(
                            0xFF737373
                        ),

                    fontSize =
                        10.sp
                )
            }
        }
    }
}


/**
 * One Safety Overview category.
 */
@Composable
private fun SafetyOverviewItem(
    title: String,
    icon: String,
    mainValue: String,
    description: String,
    modifier: Modifier = Modifier
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    Column(
        modifier =
            modifier
                .padding(
                    horizontal =
                        4.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                title,

            color =
                darkGreen,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.Bold,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        Text(
            text =
                icon,

            fontSize =
                27.sp,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    5.dp
                )
        )

        Text(
            text =
                mainValue,

            color =
                darkGreen,

            fontSize =
                20.sp,

            fontWeight =
                FontWeight.Bold,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    3.dp
                )
        )

        Text(
            text =
                description,

            color =
                Color(
                    0xFF707070
                ),

            fontSize =
                8.sp,

            lineHeight =
                10.sp,

            textAlign =
                TextAlign.Center
        )
    }
}


/**
 * Divider between Safety Overview categories.
 */
@Composable
private fun SafetyOverviewDivider(
    color: Color
) {

    Box(
        modifier =
            Modifier
                .padding(
                    top =
                        4.dp
                )
                .height(
                    105.dp
                )
                .fillMaxWidth(
                    0.003f
                )
                .background(
                    color
                )
    )
}


/**
 * Full-width dashboard card.
 */
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
            CardDefaults.cardColors(
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
                Alignment.CenterVertically
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


/**
 * Formats GPS coordinates.
 */
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
    }\nAccuracy: ±${
        String.format(
            Locale.US,
            "%.0f",
            location.accuracyMeters
        )
    } m"
}


/**
 * Converts meters to miles.
 */
private fun formatRouteDistance(
    distanceMeters: Double
): String {

    val miles =
        distanceMeters /
                1609.344

    return String.format(
        Locale.US,
        "%.1f mi",
        miles
    )
}


/**
 * Converts seconds to minutes/hours.
 */
private fun formatRouteDuration(
    durationSeconds: Double
): String {

    val totalMinutes =
        (
                durationSeconds /
                        60.0
                ).toInt()

    return if (
        totalMinutes < 60
    ) {

        "$totalMinutes min"

    } else {

        val hours =
            totalMinutes /
                    60

        val minutes =
            totalMinutes %
                    60

        if (
            minutes == 0
        ) {

            "$hours hr"

        } else {

            "$hours hr $minutes min"
        }
    }
}