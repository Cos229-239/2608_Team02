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
import com.cos229239.team02.oto.ui.features.SafetyLevel
//Use OTO's shared MapLibre map component.
import com.cos229239.team02.oto.ui.components.map.OtoMap

import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onPlanTripClick: () -> Unit,
    onBackClick: () -> Unit,
    safetyView: AreaSafetyView = viewModel()
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val darkGreen = Color(0xFF063D24)
    val mediumGreen = Color(0xFF0B5D1E)
    val lightBackground = Color(0xFFF7F8F6)

    // Uses the same Area Safety ViewModel as the full Area Safety screen.
    val safetyState by safetyView.uiState.collectAsStateWithLifecycle()

    // Uses the location system already created for Crisis Mode.
    val locationRepository = remember(context) {
        AndroidLocationRepository(
            context.applicationContext
        )
    }

    var currentLocation by remember {
        mutableStateOf<OtoLocation?>(null)
    }

    var locationStatus by remember {
        mutableStateOf("Location not loaded")
    }

    var loadingLocation by remember {
        mutableStateOf(false)
    }

    /**
     * Loads the device's current GPS location.
     */
    fun loadCurrentLocation() {
        scope.launch {

            loadingLocation = true
            locationStatus = "Finding your location..."

            val location =
                locationRepository.getCurrentLocation()

            if (location != null) {

                currentLocation = location
                locationStatus = "Current location found"

            } else {

                locationStatus =
                    "Unable to determine current location"
            }

            loadingLocation = false
        }
    }

    /**
     * Handles Android's location permission request.
     */
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

                loadCurrentLocation()

            } else {

                locationStatus =
                    "Location permission denied"
            }
        }

    /**
     * Checks permission before trying to locate the user.
     */
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

            loadCurrentLocation()

        } else {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * If the user already granted location permission elsewhere
     * in the app, automatically populate Explorer when it opens.
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
            loadCurrentLocation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground)
    ) {

        /*
         * ---------------------------------------------------------
         * EXPLORER HEADER
         * ---------------------------------------------------------
         */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkGreen)
                .statusBarsPadding()
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBackClick
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 26.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "EXPLORER MODE",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Plan • Navigate • Report • Stay Safe",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "🔔",
                    fontSize = 24.sp
                )
            }
        }

        /*
         * ---------------------------------------------------------
         * SCROLLABLE DASHBOARD
         * ---------------------------------------------------------
         */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            /*
             * -----------------------------------------------------
             * MAP AREA
             * -----------------------------------------------------
             */

        //Show OTO's shared interactive MapLibre map.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {

                OtoMap(
                    modifier = Modifier.fillMaxSize()
                )

                //Keep Explorer's existing location information and Locate Me control over the map.
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.92f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (loadingLocation) {

                            CircularProgressIndicator()

                        } else {

                            currentLocation?.let { location ->

                                Text(
                                    text = formatExplorerLocation(location),
                                    textAlign = TextAlign.Center,
                                    color = darkGreen
                                )

                            } ?: Text(
                                text = locationStatus,
                                textAlign = TextAlign.Center,
                                color = darkGreen
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {
                                requestLocation()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mediumGreen
                            )
                        ) {

                            Text(
                                text = "📍 Locate Me"
                            )
                        }
                    }
                }
            }


        //Keep the rest of the Explorer dashboard padded below the map.
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
                /*
                 * -------------------------------------------------
                 * QUICK ACTION CARDS
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
                        title = "PLAN TRIP",
                        description =
                            "Plan or edit a trip",
                        icon = "📍",
                        modifier =
                            Modifier.weight(1f),
                        onClick =
                            onPlanTripClick
                    )

                    ExplorerActionCard(
                        title =
                            "CREATE ROUTE",
                        description =
                            "Build a custom route",
                        icon = "➕",
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            // Future feature.
                        }
                    )

                    ExplorerActionCard(
                        title =
                            "OFFLINE MAPS",
                        description =
                            "Save maps offline",
                        icon = "⬇️",
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            // Future feature.
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                /*
                 * -------------------------------------------------
                 * LIVE SAFETY OVERVIEW
                 * -------------------------------------------------
                 */

                SafetyOverviewCard(
                    areaName =
                        safetyState.areaName,
                    alertCount =
                        safetyState.notifications.size,
                    severeCount =
                        safetyState.notifications.count {
                            it.level ==
                                    SafetyLevel.SEVERE
                        },
                    moderateCount =
                        safetyState.notifications.count {
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
                        Modifier.height(16.dp)
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
                        Modifier.height(12.dp)
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
                            text = "👥  CHECK-IN",
                            fontSize = 18.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color = darkGreen
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
                            fontSize = 14.sp
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
                                // Future Check-In feature.
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
                                "CHECK IN"
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
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
                        Modifier.height(30.dp)
                )
            }
        }
    }
}


/**
 * Small action card used for Plan Trip,
 * Create Route and Offline Maps.
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
        Color(0xFF063D24)

    Card(
        modifier = modifier
            .height(150.dp)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text = icon,
                fontSize = 30.sp
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = title,
                color = darkGreen,
                fontWeight =
                    FontWeight.Bold,
                fontSize = 14.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text = description,
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
 * Displays live information from AreaSafetyView.
 */
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
        Color(0xFF063D24)

    Card(
        modifier = Modifier
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

        Column(
            modifier =
                Modifier.padding(18.dp)
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
                    color = darkGreen,
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "View Area Safety ›",
                    color = darkGreen,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text = areaName,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (isLoading) {

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
                            alertCount.toString(),
                        label =
                            "Active Alerts"
                    )

                    SafetyStat(
                        value =
                            severeCount.toString(),
                        label =
                            "Severe"
                    )

                    SafetyStat(
                        value =
                            moderateCount.toString(),
                        label =
                            "Moderate"
                    )
                }
            }

            if (isOffline) {

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

            if (isSample) {

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


/**
 * One statistic inside Safety Overview.
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
            text = value,
            fontSize = 26.sp,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text = label,
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
 * Large full-width dashboard action.
 */
@Composable
private fun DashboardWideCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    val darkGreen =
        Color(0xFF063D24)

    Card(
        modifier = Modifier
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    color = darkGreen,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 17.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text = subtitle
                )
            }

            Text(
                text = "›",
                fontSize = 28.sp,
                color = darkGreen
            )
        }
    }
}


/**
 * Formats the GPS coordinates shown in
 * the Explorer map placeholder.
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