package com.cos229239.team02.oto.ui.screens.explorer

//Use OTO's shared MapLibre map component.

import com.cos229239.team02.oto.ui.features.AreaSafetyUIState
import kotlinx.coroutines.CancellationException
import android.Manifest

import android.content.pm.PackageManager
import android.net.TetheringManager
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
import com.cos229239.team02.oto.ui.components.map.OtoMap
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.data.safety.SafetyLevel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.saveable.rememberSaveable
import com.cos229239.team02.oto.BuildConfig
import com.cos229239.team02.oto.data.resource.NpsAlertClient
import com.cos229239.team02.oto.data.resource.NpsParkPicker
import com.cos229239.team02.oto.data.safety.createSafetyHttpClient

import kotlinx.coroutines.launch
import java.util.Locale







@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onPlanTripClick: () -> Unit,
    onBackClick: () -> Unit,
    tripViewModel: PlanTripViewModel,
    safetyView: AreaSafetyView
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
val parkDirectoryHttp = remember {
    createSafetyHttpClient()
}
    val parkDirectoryClient = remember(parkDirectoryHttp){
        NpsAlertClient(
            http = parkDirectoryHttp,
            apiKey = BuildConfig.NPS_API_KEY
        )
    }
    val locationRepository =
        remember(context) {
            AndroidLocationRepository(
                context.applicationContext
            )
        }

    var currentLocation by remember {
        mutableStateOf<OtoLocation?>(null)
    }
    val destinationLatitude = savedTrip?.destinationLatitude
    val destinationLongitude = savedTrip?.destinationLongitude
    val destinationName = savedTrip?.destinationName
    var parkCodeInput by rememberSaveable{
        mutableStateOf("")
    }
    var selectedParkCode by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var parkCodeError by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(
        destinationLatitude,
        destinationLongitude,
        destinationName,
        currentLocation?.latitude,
        currentLocation?.longitude,
        selectedParkCode
    ) {

        val selectedLocation =
            if (
                destinationLatitude != null &&
                destinationLongitude != null
            ) {
                OtoLocation(
                    latitude = destinationLatitude,
                    longitude = destinationLongitude
                )
            } else {
                currentLocation
            }
        safetyView.setArea(
            location = selectedLocation,
            areaName = if (savedTrip != null) {
                destinationName
                    ?.takeIf { it.isNotBlank() }
                    ?: "Trip destination"
            } else {
                "Current area"
            },

            parkCode = selectedParkCode
        )
    }

    var locationStatus by remember {
        mutableStateOf(
            "Location not loaded"
        )
    }

    var loadingLocation by remember {
        mutableStateOf(false)
    }

    /**
     * Loads the device's current location.
     */
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

    /**
     * Handles Android's permission response.
     */
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

    /**
     * Requests location permission if needed.
     */
    fun requestLocation() {

        val fineGranted =
            ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) ==
                    PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) ==
                    PackageManager.PERMISSION_GRANTED

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

    /*
     * If there is no saved trip, Explorer can use
     * the device's current location instead.
     */
    LaunchedEffect(savedTrip) {

        if (savedTrip == null) {

            val fineGranted =
                ContextCompat
                    .checkSelfPermission(
                        context,
                        Manifest.permission
                            .ACCESS_FINE_LOCATION
                    ) ==
                        PackageManager.PERMISSION_GRANTED

            val coarseGranted =
                ContextCompat
                    .checkSelfPermission(
                        context,
                        Manifest.permission
                            .ACCESS_COARSE_LOCATION
                    ) ==
                        PackageManager.PERMISSION_GRANTED

            if (
                fineGranted ||
                coarseGranted
            ) {

                loadCurrentLocation()
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * SCREEN
     * ---------------------------------------------------------
     */

    Column(
        modifier = Modifier
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
        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    darkGreen
                )
                .statusBarsPadding()
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
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
                        text = "←",
                        color =
                            Color.White,
                        fontSize =
                            26.sp
                    )
                }

                Column(
                    modifier =
                        Modifier.weight(1f)
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
                    text = "🔔",
                    fontSize = 24.sp
                )
            }
        }

        /*
         * -----------------------------------------------------
         * SCROLLABLE DASHBOARD
         * -----------------------------------------------------
         */

        Column(
            modifier = Modifier
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        300.dp
                    )
            ) {

                OtoMap(
                    modifier =
                        Modifier.fillMaxSize(),

                    latitude =
                        currentLocation?.latitude,

                    longitude =
                        currentLocation?.longitude,

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
                            ?.destinationLongitude
                )

                /*
                 * Show location status only when
                 * there is no saved trip.
                 */
                if (savedTrip == null) {

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
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White.copy(
                                        alpha = 0.92f
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

                            if (loadingLocation) {

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
                                        requestLocation()
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
                 * Saved trip summary.
                 */
                savedTrip
                    ?.let { trip ->

                        Card(
                            modifier =
                                Modifier
                                    .align(
                                        Alignment.BottomStart
                                    )
                                    .padding(
                                        12.dp
                                    ),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White.copy(
                                            alpha = 0.94f
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
                                        "ACTIVE TRIP",

                                    color =
                                        darkGreen,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize =
                                        12.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            4.dp
                                        )
                                )

                                Text(
                                    text =
                                        "Start: ${trip.startingPointName}",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )

                                Text(
                                    text =
                                        "Destination: ${trip.destinationName}",

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
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

                /*
                 * LIVE SAFETY OVERVIEW
                 */
// Tested code dialog for NPS Park Code Entry still work in progress will probably be moved inside screen
// trying to get park codes to auto-generate once typing but still doing research
                NpsParkPicker(
                    client = parkDirectoryClient,
                    selectedParkCode = selectedParkCode,

                    onParkSelected = { code ->
                        if (selectedParkCode == code) {
                            safetyView.refreshNotifications()
                        } else {
                            selectedParkCode = code
                        }
                    }
                )
                if (!safetyState.hasLocation) {
                    Text(
                        text = "Use locate Me or select a trip destination " +
                        "to load safety notices. ",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                SafetyOverviewCard(
                    uiState = safetyState,
                    onClick = onAreaSafetyClick
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * -------------------------------------------------
                 * REPORT HAZARD
                 * -------------------------------------------------
                 */

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
                        Modifier.height(30.dp)
                )
            }
        }
    }

                DashboardWideCard(
                    title  =
                        "⚠️ REPORT HAZARD / ROUTE CHANGE",
                    subtitle =
                        "Help keep trails safe for everyone",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        ),
                    shape =
                        RoundedCornerShape(
                            14.dp
                        )) {
                            Column(
                                modifier = Modifier.padding(
                                    18.dp)
                            ) {
                                Text(
                                    text = "👥 CHECK-IN",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkGreen
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(
                                    text = "Trusted Contact",
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = "Not checked in",
                                    color = Color(
                                        0xFFE67E22
                                    ),
                                    fontWeight =
                                        FontWeight.Bold
                                )
                                Spacer(
                                    modifier = Modifier.height(10.dp)

                                )

                                Button(
                                    onClick = {

                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = mediumGreen
                                    )
                                ) {
                                    Text(
                                        "CHECK IN"
                                    )
                                }

                            }

                        }
                                Spacer( modifier = Modifier.height(12.dp)
                    )

                    DashboardWideCard(
                        title =
                            "📋 FIELD REPORTS",
                        subtitle =
                            "View recent reports from this area",
                        onClick = {

                        }
                    )
                    Spacer(modifier =
                        Modifier.height(30.dp)
                    )
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
 * Live Area Safety summary.
 */
@Composable
private fun SafetyOverviewCard(
    uiState: AreaSafetyUIState,
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
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    text = uiState.areaName,
                    fontWeight = FontWeight.Bold
                )

            when {
                !uiState.hasLocation -> {
                    Text(
                        text = "Select a trip destination or use Locate Me."
                    )
            }
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = darkGreen
                    )
                    Text(
                        text = "Checking safety sources..."
                    )
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage
                            ?: "Unable to update safety information.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                uiState.checkedAtMillis == null -> {
                    Text(
                        text = "Safety information has not been checked yet."
                    )
                }
                else -> {
                    Text(
                        text = "Filter: ${uiState.filterSelected.displayName}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SafetyStat(
                            value = uiState.notifications.size.toString(),
                            label = "Shown notices"
                        )

                        SafetyStat(
                            value = uiState.notifications.count {
                                it.level == SafetyLevel.SEVERE
                            }.toString(),
                            label = "Severe"
                        )

                        SafetyStat(
                            value = uiState.notifications.count {
                                it.level == SafetyLevel.MODERATE
                            }.toString(),
                            label = "Moderate"
                        )
                    }

                    uiState.sources.forEach { source ->
                        Text(
                            text = "${source.source}: ${source.message}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (uiState.hasUnavailableSources) {
                        Text(
                            text = "Some safety sources could not be updated.",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (uiState.resourcesFromCache) {
                        Text(
                            text = "Nearby resources include saved data.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "No returned notices does not establish " +
                        "that the area is safe.",
                        style =MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "View Area Safety >",
                color = darkGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


/**
 * One safety statistic.
 */
@Composable
private fun SafetyStat(
    value: String,
    label: String
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
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
 * Formats GPS coordinates for Explorer.
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
    }"
}
