package com.cos229239.team02.oto.ui.screens.explorer

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cos229239.team02.oto.data.hazard.HazardReportViewModel
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.route.RouteClient
import com.cos229239.team02.oto.data.route.RouteResult
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import com.cos229239.team02.oto.ui.components.map.OtoMap
import com.cos229239.team02.oto.ui.features.AreaSafetyUIState
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.ui.features.weatherIcon
import com.cos229239.team02.oto.ui.theme.OtoBackground
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreen
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreenDark
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onPlanTripClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onReportHazardClick: () -> Unit,

    /*
     * Opens Field Reports.
     *
     * Used by:
     *
     * - FIELD REPORTS dashboard card
     * - Hazard marker popup
     */
    onFieldReportsClick: () -> Unit,

    onBackClick: () -> Unit,

    tripViewModel: PlanTripViewModel,

    /*
     * Shared hazard report state.
     */
    hazardReportViewModel: HazardReportViewModel,

    /*
     * Shared safety / weather state.
     *
     * This comes from OtoNavigation so Explorer,
     * Weather Report, and Area Safety all use
     * the same AreaSafetyView.
     */
    safetyView: AreaSafetyView
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val darkGreen =
        Color(
            0xFF063D24
        )

    val mediumGreen =
        Color(
            0xFF0B5D1E
        )

    val screenBackground =
        MaterialTheme.colorScheme

    val primaryText =
        MaterialTheme.colorScheme.onSurface

    /*
     * ---------------------------------------------------------
     * SHARED DATA
     * ---------------------------------------------------------
     */

    val safetyState by
    safetyView
        .uiState
        .collectAsStateWithLifecycle()

    val savedTrip =
        tripViewModel.savedTrip

    /*
     * ---------------------------------------------------------
     * TRUSTED CONTACT CHECK-IN
     * ---------------------------------------------------------
     *
     * The trusted contact is saved with the trip.
     *
     * Check-in time is stored locally on the device.
     * SMS permission is requested the first time the
     * user tries to send a check-in message.
     */

    val checkInPreferences =
        remember(
            context
        ) {
            context.getSharedPreferences(
                "oto_check_in",
                Context.MODE_PRIVATE
            )
        }

    val trustedContactName =
        savedTrip
            ?.trustedContactName
            .orEmpty()

    val trustedContactPhone =
        savedTrip
            ?.trustedContactPhone
            .orEmpty()

    val hasTrustedContact =
        trustedContactName.isNotBlank() &&
                trustedContactPhone.isNotBlank()

    /*
     * Save the phone number with the check-in time so
     * an old check-in is not displayed for a different
     * trusted contact later.
     */
    val storedCheckInPhone =
        checkInPreferences
            .getString(
                KEY_LAST_CHECK_IN_PHONE,
                ""
            )
            .orEmpty()

    var lastCheckInTime by remember(
        trustedContactPhone
    ) {

        mutableStateOf<Long?>(
            if (
                hasTrustedContact &&
                storedCheckInPhone == trustedContactPhone
            ) {

                checkInPreferences
                    .getLong(
                        KEY_LAST_CHECK_IN_TIME,
                        0L
                    )
                    .takeIf {
                        it > 0L
                    }

            } else {

                null
            }
        )
    }

    var checkInMessageError by remember {

        mutableStateOf<String?>(
            null
        )
    }

    var checkInMessageStatus by remember {

        mutableStateOf<String?>(
            null
        )
    }

    var isCheckInSending by remember {

        mutableStateOf(
            false
        )
    }

    val smsSentAction =
        remember(
            context
        ) {

            "${context.packageName}.OTO_SMS_SENT"
        }

    val smsDeliveredAction =
        remember(
            context
        ) {

            "${context.packageName}.OTO_SMS_DELIVERED"
        }

    fun recordCheckIn(
        checkInTime: Long
    ) {

        if (
            !hasTrustedContact
        ) {

            return
        }

        lastCheckInTime =
            checkInTime

        checkInPreferences
            .edit()
            .putLong(
                KEY_LAST_CHECK_IN_TIME,
                checkInTime
            )
            .putString(
                KEY_LAST_CHECK_IN_PHONE,
                trustedContactPhone
            )
            .apply()
    }

    fun buildCheckInMessage(
        checkInTime: Long
    ): String {

        return "OTO check-in: Your traveler checked in ${
            formatCheckInTime(
                checkInTime
            )
        }."
    }

    fun openMessagingFallback(
        checkInTime: Long
    ) {

        if (
            !hasTrustedContact
        ) {

            return
        }

        val message =
            buildCheckInMessage(
                checkInTime
            )

        try {

            val messageIntent =
                Intent(
                    Intent.ACTION_SENDTO
                ).apply {

                    data =
                        Uri.fromParts(
                            "smsto",
                            trustedContactPhone,
                            null
                        )

                    putExtra(
                        "sms_body",
                        message
                    )
                }

            context.startActivity(
                messageIntent
            )

            isCheckInSending =
                false

            checkInMessageError =
                null

            checkInMessageStatus =
                "Message opened in your messaging app. Send it there to complete the check-in."

        } catch (
            exception: ActivityNotFoundException
        ) {

            isCheckInSending =
                false

            checkInMessageStatus =
                null

            checkInMessageError =
                "No messaging app is available on this device."

        } catch (
            exception: Exception
        ) {

            isCheckInSending =
                false

            checkInMessageStatus =
                null

            checkInMessageError =
                "Unable to open the messaging app."
        }
    }

    DisposableEffect(
        context,
        trustedContactPhone
    ) {

        val sentReceiver =
            object : BroadcastReceiver() {

                override fun onReceive(
                    receiverContext: Context?,
                    intent: Intent?
                ) {

                    val checkInTime =
                        intent
                            ?.getLongExtra(
                                EXTRA_CHECK_IN_TIME,
                                0L
                            )
                            ?: 0L

                    isCheckInSending =
                        false

                    when (
                        resultCode
                    ) {

                        Activity.RESULT_OK -> {

                            if (
                                checkInTime > 0L
                            ) {

                                recordCheckIn(
                                    checkInTime
                                )
                            }

                            checkInMessageError =
                                null

                            checkInMessageStatus =
                                "SMS sent."
                        }

                        SmsManager.RESULT_ERROR_NO_SERVICE -> {

                            checkInMessageStatus =
                                null

                            checkInMessageError =
                                "SMS could not be sent because there is no mobile service."
                        }

                        SmsManager.RESULT_ERROR_RADIO_OFF -> {

                            checkInMessageStatus =
                                null

                            checkInMessageError =
                                "SMS could not be sent because the mobile radio is turned off."
                        }

                        SmsManager.RESULT_ERROR_NULL_PDU -> {

                            checkInMessageStatus =
                                null

                            checkInMessageError =
                                "SMS could not be created by this device."
                        }

                        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {

                            checkInMessageStatus =
                                null

                            checkInMessageError =
                                "The SMS failed to send."
                        }

                        else -> {

                            checkInMessageStatus =
                                null

                            checkInMessageError =
                                "The SMS could not be sent."
                        }
                    }
                }
            }

        val deliveredReceiver =
            object : BroadcastReceiver() {

                override fun onReceive(
                    receiverContext: Context?,
                    intent: Intent?
                ) {

                    if (
                        resultCode ==
                        Activity.RESULT_OK
                    ) {

                        checkInMessageError =
                            null

                        checkInMessageStatus =
                            "SMS delivery confirmed."

                    } else {

                        checkInMessageStatus =
                            "SMS was sent, but delivery was not confirmed."
                    }
                }
            }

        ContextCompat.registerReceiver(
            context,
            sentReceiver,
            IntentFilter(
                smsSentAction
            ),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        ContextCompat.registerReceiver(
            context,
            deliveredReceiver,
            IntentFilter(
                smsDeliveredAction
            ),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {

            try {

                context.unregisterReceiver(
                    sentReceiver
                )

            } catch (
                exception: Exception
            ) {
            }

            try {

                context.unregisterReceiver(
                    deliveredReceiver
                )

            } catch (
                exception: Exception
            ) {
            }
        }
    }

    fun sendCheckInMessage() {

        if (
            !hasTrustedContact ||
            isCheckInSending
        ) {

            return
        }

        val checkInTime =
            System.currentTimeMillis()

        val message =
            buildCheckInMessage(
                checkInTime
            )

        try {

            isCheckInSending =
                true

            checkInMessageError =
                null

            checkInMessageStatus =
                "Sending check-in..."

            val sentIntent =
                Intent(
                    smsSentAction
                ).apply {

                    setPackage(
                        context.packageName
                    )

                    putExtra(
                        EXTRA_CHECK_IN_TIME,
                        checkInTime
                    )
                }

            val sentPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    checkInTime.hashCode(),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            val deliveredIntent =
                Intent(
                    smsDeliveredAction
                ).apply {

                    setPackage(
                        context.packageName
                    )

                    putExtra(
                        EXTRA_CHECK_IN_TIME,
                        checkInTime
                    )
                }

            val deliveredPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    checkInTime.hashCode() + 1,
                    deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

            @Suppress("DEPRECATION")
            val smsManager =
                SmsManager.getDefault()

            smsManager.sendTextMessage(
                trustedContactPhone,
                null,
                message,
                sentPendingIntent,
                deliveredPendingIntent
            )

        } catch (
            exception: UnsupportedOperationException
        ) {

            openMessagingFallback(
                checkInTime
            )

        } catch (
            exception: SecurityException
        ) {

            isCheckInSending =
                false

            checkInMessageStatus =
                null

            checkInMessageError =
                "SMS permission is required to send a check-in."

        } catch (
            exception: IllegalArgumentException
        ) {

            isCheckInSending =
                false

            checkInMessageStatus =
                null

            checkInMessageError =
                "The trusted contact phone number is not valid."

        } catch (
            exception: Exception
        ) {

            isCheckInSending =
                false

            checkInMessageStatus =
                null

            checkInMessageError =
                "Unable to send the check-in message on this device."
        }
    }

    val smsPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) { granted ->

            if (
                granted
            ) {

                sendCheckInMessage()

            } else {

                isCheckInSending =
                    false

                checkInMessageStatus =
                    null

                checkInMessageError =
                    "SMS permission was denied. OTO could not send the check-in."
            }
        }

    fun checkIn() {

        if (
            !hasTrustedContact ||
            isCheckInSending
        ) {

            return
        }

        val supportsDirectSms =
            context
                .packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_TELEPHONY_MESSAGING
                )

        if (
            !supportsDirectSms
        ) {

            openMessagingFallback(
                System.currentTimeMillis()
            )

            return
        }

        val smsPermissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) ==
                    PackageManager.PERMISSION_GRANTED

        if (
            smsPermissionGranted
        ) {

            sendCheckInMessage()

        } else {

            smsPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
            )
        }
    }

    val activeHazardReports =
        hazardReportViewModel
            .activeHazardReports

    val activeHazardCount =
        activeHazardReports.size

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

        mutableIntStateOf(
            0
        )
    }

    var routeLoading by remember {

        mutableStateOf(
            false
        )
    }

    var routeError by remember {

        mutableStateOf<String?>(
            null
        )
    }

    var isTripCardExpanded by remember {

        mutableStateOf(
            true
        )
    }

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

    val locationRepository =
        remember(
            context
        ) {

            AndroidLocationRepository(
                context.applicationContext
            )
        }

    var currentLocation by remember {

        mutableStateOf<OtoLocation?>(
            null
        )
    }

    val destinationLatitude =
        savedTrip
            ?.destinationLatitude

    val destinationLongitude =
        savedTrip
            ?.destinationLongitude

    val destinationName =
        savedTrip
            ?.destinationName

    LaunchedEffect(
        destinationLatitude,
        destinationLongitude,
        destinationName,
        currentLocation?.latitude,
        currentLocation?.longitude
    ) {

        val destination =
            if (
                destinationLatitude != null &&
                destinationLongitude != null
            ) {

                OtoLocation(
                    latitude =
                        destinationLatitude,

                    longitude =
                        destinationLongitude
                )

            } else {

                null
            }

        val selectedArea =
            destination
                ?: currentLocation

        if (
            selectedArea != null
        ) {

            val areaName =
                resolveAreaName(
                    context =
                        context,

                    location =
                        selectedArea
                )

            safetyView.setArea(
                location =
                    selectedArea,

                areaName =
                    if (
                        destination != null
                    ) {

                        destinationName
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Trip destination"

                    } else {

                        areaName
                    }
            )
        }
    }

    var locationStatus by remember {

        mutableStateOf(
            "Location not loaded"
        )
    }

    var loadingLocation by remember {

        mutableStateOf(
            false
        )
    }

    var locationFocusRequest by remember {

        mutableIntStateOf(
            0
        )
    }

    var focusAfterPermission by remember {

        mutableStateOf(
            false
        )
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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    screenBackground.background
                )
    ) {

        OtoTopAppBar(
            title =
                "EXPLORER MODE",

            onBackClick =
                onBackClick
        )

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

                hazardReports =
                    activeHazardReports,

                onViewHazardReportsClick = { selectedReports ->

                    hazardReportViewModel
                        .selectFieldReports(
                            selectedReports
                        )

                    onFieldReportsClick()
                },

                onMyLocationClick = {

                    requestLocation(
                        focusOnMap =
                            true
                    )
                },

                locationFocusRequest =
                    locationFocusRequest
            )

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
                                    MaterialTheme.colorScheme.surface,

                                contentColor =
                                    MaterialTheme.colorScheme.onSurface
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
                                MaterialTheme.colorScheme.onSurfaceVariant,

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
                                            MaterialTheme.colorScheme.onSurfaceVariant,

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
                                                MaterialTheme.colorScheme.onSurface,

                                            contentColor =
                                                MaterialTheme.colorScheme.surface
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
                                    MaterialTheme.colorScheme.onSurfaceVariant,

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
                                        MaterialTheme.colorScheme.onSurface,

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
                                        MaterialTheme.colorScheme.primary
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
                                        MaterialTheme.colorScheme.onSurface,

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
                                        MaterialTheme.colorScheme.onSurface,

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
                                    MaterialTheme.colorScheme.surface,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }

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
                                    MaterialTheme.colorScheme.surface,

                                contentColor =
                                    MaterialTheme.colorScheme.onSurface
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
                                    primaryText,

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
                                    MaterialTheme.colorScheme.onSurface,

                                fontSize =
                                    18.sp
                            )
                        }
                    }
                }
            }
        }

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
                uiState =
                    safetyState,

                hazardCount =
                    activeHazardCount,

                onAreaSafetyClick =
                    onAreaSafetyClick,

                onWeatherClick =
                    onWeatherClick,

                onReportHazardClick =
                    onReportHazardClick
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

                onClick =
                    onReportHazardClick
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
                            MaterialTheme.colorScheme.surface,

                        contentColor =
                            MaterialTheme.colorScheme.onSurface
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
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    if (
                        !hasTrustedContact
                    ) {

                        Text(
                            text =
                                "No trusted contact added",

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    10.dp
                                )
                        )

                        Button(
                            onClick =
                                onPlanTripClick,

                            modifier =
                                Modifier.fillMaxWidth(),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.onSurface,

                                    contentColor =
                                        MaterialTheme.colorScheme.surface
                                )
                        ) {

                            Text(
                                text =
                                    "ADD TRUSTED CONTACT"
                            )
                        }

                    } else {

                        Text(
                            text =
                                trustedContactName,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                trustedContactPhone,

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,

                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    8.dp
                                )
                        )

                        if (
                            lastCheckInTime != null
                        ) {

                            Text(
                                text =
                                    "✓ Checked in ${
                                        formatCheckInTime(
                                            lastCheckInTime!!
                                        )
                                    }",

                                color =
                                    MaterialTheme.colorScheme.primary,

                                fontWeight =
                                    FontWeight.Bold
                            )

                        } else {

                            Text(
                                text =
                                    "Not checked in",

                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant,

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

                        Button(
                            onClick = {

                                checkIn()
                            },

                            enabled =
                                !isCheckInSending,

                            modifier =
                                Modifier.fillMaxWidth(),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.onSurface,

                                    contentColor =
                                        MaterialTheme.colorScheme.surface
                                )
                        ) {

                            Text(
                                text =
                                    when {

                                        isCheckInSending -> {

                                            "SENDING..."
                                        }

                                        lastCheckInTime == null -> {

                                            "CHECK IN"
                                        }

                                        else -> {

                                            "CHECK IN AGAIN"
                                        }
                                    }
                            )
                        }

                        checkInMessageStatus
                            ?.let { status ->

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            8.dp
                                        )
                                )

                                Text(
                                    text =
                                        status,

                                    color =
                                        MaterialTheme.colorScheme.primary,

                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }

                        checkInMessageError
                            ?.let { error ->

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            8.dp
                                        )
                                )

                                Text(
                                    text =
                                        error,

                                    color =
                                        MaterialTheme.colorScheme.error,

                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }
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
                    when (
                        activeHazardCount
                    ) {

                        0 ->
                            "No active hazard reports"

                        1 ->
                            "1 active hazard report"

                        else ->
                            "$activeHazardCount active hazard reports"
                    },

                onClick = {

                    hazardReportViewModel
                        .clearFieldReportSelection()

                    onFieldReportsClick()
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


@Composable
private fun ExplorerActionCard(
    title: String,
    description: String,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier =
            modifier
                .height(
                    150.dp
                ),

        onClick =
            onClick,

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,

                contentColor =
                    MaterialTheme.colorScheme.onSurface
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
                        horizontal =
                            8.dp,

                        vertical =
                            10.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Text(
                text =
                    icon,

                fontSize =
                    30.sp
            )

            Text(
                text =
                    title,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    13.sp,

                textAlign =
                    TextAlign.Center,

                maxLines =
                    2,

                minLines =
                    2,

                modifier =
                    Modifier.fillMaxWidth()
            )

            Text(
                text =
                    description,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                fontSize =
                    12.sp,

                lineHeight =
                    14.sp,

                textAlign =
                    TextAlign.Center,

                maxLines =
                    3,

                minLines =
                    3,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun SafetyOverviewCard(
    uiState: AreaSafetyUIState,
    hazardCount: Int,
    onAreaSafetyClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onReportHazardClick: () -> Unit
) {

    val forecast =
        uiState.forecast

    val npsStatus =
        uiState.sources.firstOrNull {
            it.source == "NPS"
        }

    val parkSummary =
        when {

            uiState.selectedParkCode == null ->
                "Select a park"

            uiState.isLoading ->
                "Loading..."

            !uiState.hasLocation ->
                "Location required to load notices"

            uiState.errorMessage != null ->
                "Unavailable"

            else ->
                npsStatus?.message
                    ?: "Not Loaded"
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.onSurface,

                contentColor =
                    MaterialTheme.colorScheme.surface
            ),

        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.tertiary
                    )
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
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
                        "🦺  SAFETY OVERVIEW",

                    color =
                        MaterialTheme.colorScheme.surface,

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "CURRENT AREA",

                    modifier =
                        Modifier.clickable {
                            onAreaSafetyClick()
                        },

                    color =
                        MaterialTheme.colorScheme.surface,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            if (
                uiState.areaName.isNotBlank()
            ) {

                Text(
                    text =
                        uiState.areaName,

                    color =
                        MaterialTheme.colorScheme.surface,

                    fontSize =
                        20.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }

            if (
                uiState.isLoading
            ) {

                CircularProgressIndicator(
                    color =
                        OtoExplorerGreen
                )
            }

            Text(
                text =
                    "Park Notices: $parkSummary",

                modifier =
                    Modifier.fillMaxWidth(),

                color =
                    MaterialTheme.colorScheme.surface
            )

            uiState.selectedParkCode
                ?.let { code ->

                    Text(
                        text =
                            "Park: ${
                                uiState.selectedParkName
                                    ?: code
                            }",

                        modifier =
                            Modifier.fillMaxWidth(),

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            MaterialTheme.colorScheme.surface
                    )
                }

            Button(
                onClick =
                    onAreaSafetyClick
            ) {

                Text(
                    text =
                        "View Area Safety / Select Park >"
                )
            }

            if (
                uiState.hasUnavailableSources
            ) {

                Text(
                    text =
                        "Some sources are unavailable. " +
                                "Results may be incomplete.",

                    color =
                        OtoCrisisRed
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.tertiary
                    )
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    ),

                verticalAlignment =
                    Alignment.Top
            ) {

                SafetyOverviewItem(
                    title =
                        "WEATHER",

                    icon =
                        weatherIcon(
                            code =
                                forecast?.weatherCode,

                            isDay =
                                uiState.forecast?.isDay
                        ),

                    mainValue =
                        forecast
                            ?.let {
                                "${it.temp}°${it.tempUnit}"
                            }
                            ?: "_",

                    description =
                        uiState.forecast
                            ?.shortForecast
                            ?: "View weather",

                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .clickable(
                                onClick =
                                    onWeatherClick
                            )
                )

                SafetyOverviewItem(
                    title =
                        "HAZARDS",

                    icon =
                        "⚠️",

                    mainValue =
                        hazardCount
                            .toString(),

                    description =
                        when (
                            hazardCount
                        ) {

                            0 ->
                                "No active reports"

                            1 ->
                                "1 active report"

                            else ->
                                "$hazardCount active reports"
                        },

                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .clickable(
                                onClick =
                                    onReportHazardClick
                            )
                )
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    ),

                verticalAlignment =
                    Alignment.Top
            ) {

                SafetyOverviewItem(
                    title =
                        "CLOSURES",

                    icon =
                        "⛔",

                    mainValue =
                        uiState.selectedParkCode
                            ?.uppercase(
                                Locale.ROOT
                            )
                            ?: "—",

                    description =
                        when {

                            !uiState.hasLocation ->
                                "Select a location"

                            uiState.selectedParkCode == null ->
                                "No park selected"

                            uiState.isLoading ->
                                "Loading notices..."

                            uiState.errorMessage != null ->
                                "Unable to load notices"

                            else ->
                                npsStatus?.message
                                    ?: "Notices not loaded"
                        },

                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .clickable(
                                onClick =
                                    onAreaSafetyClick
                            )
                )

                SafetyOverviewItem(
                    title =
                        "AIR QUALITY",

                    icon =
                        "🍃",

                    mainValue =
                        uiState.airQuality
                            ?.usAqi
                            ?.toString()
                            ?: "_",

                    description =
                        uiState.airQuality
                            ?.category
                            ?: "View air quality",

                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .fillMaxHeight()
                            .clickable(
                                onClick =
                                    onWeatherClick
                            )
                )
            }
        }
    }

    if (
        uiState.hasUnavailableSources
    ) {

        Text(
            text =
                "Some safety sources are unavailable. Results may be incomplete.",

            color =
                OtoCrisisRed,

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


@Composable
private fun SafetyOverviewItem(
    title: String,
    icon: String,
    mainValue: String,
    description: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier.height(
                200.dp
            ),

        shape =
            RoundedCornerShape(
                16.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,

                contentColor =
                    MaterialTheme.colorScheme.onSurface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    0.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        12.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(
                    space =
                        10.dp,

                    alignment =
                        Alignment.CenterVertically
                )
        ) {

            Text(
                text =
                    icon,

                fontSize =
                    30.sp
            )

            Text(
                text =
                    title,

                color =
                    MaterialTheme.colorScheme.onSurface,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Center
            )

            Text(
                text =
                    mainValue,

                color =
                    MaterialTheme.colorScheme.onSurface,

                fontSize =
                    26.sp,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Center
            )

            Text(
                text =
                    description,

                color =
                    MaterialTheme.colorScheme.onSurface,

                fontSize =
                    12.sp,

                textAlign =
                    TextAlign.Center,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


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


@Composable
private fun DashboardWideCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

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
                    MaterialTheme.colorScheme.surface,

                contentColor =
                    MaterialTheme.colorScheme.onSurface
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
                        MaterialTheme.colorScheme.onSurface,

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
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


private fun formatExplorerLocation(
    location: OtoLocation
): String {

    val accuracy =
        location.accuracyMeters
            ?: 0f

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
            accuracy
        )
    } m"
}


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


private fun formatCheckInTime(
    timeMillis: Long
): String {

    val formatter =
        SimpleDateFormat(
            "MMM d, yyyy 'at' h:mm a",
            Locale.getDefault()
        )

    return formatter.format(
        Date(
            timeMillis
        )
    )
}


private const val KEY_LAST_CHECK_IN_TIME =
    "last_check_in_time"

private const val KEY_LAST_CHECK_IN_PHONE =
    "last_check_in_phone"

private const val EXTRA_CHECK_IN_TIME =
    "check_in_time"


private suspend fun resolveAreaName(
    context: android.content.Context,
    location: OtoLocation
): String {

    val fallback =
        String.format(
            Locale.US,
            "%.4f, %.4f",
            location.latitude,
            location.longitude
        )

    if (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        !Geocoder.isPresent()
    ) {

        return fallback
    }

    return suspendCancellableCoroutine { continuation ->

        try {

            Geocoder(
                context,
                Locale.getDefault()
            ).getFromLocation(
                location.latitude,
                location.longitude,
                1,
                object : Geocoder.GeocodeListener {

                    override fun onGeocode(
                        addresses: MutableList<android.location.Address>
                    ) {

                        val address =
                            addresses.firstOrNull()

                        val name =
                            address
                                ?.let {

                                    listOfNotNull(
                                        it.locality
                                            ?: it.subAdminArea,

                                        it.adminArea
                                    )
                                        .distinct()
                                        .joinToString(
                                            " , "
                                        )
                                        .takeIf(
                                            String::isNotBlank
                                        )
                                }
                                ?: fallback

                        if (
                            continuation.isActive
                        ) {

                            continuation.resume(
                                name
                            )
                        }
                    }

                    override fun onError(
                        errorMessage: String?
                    ) {

                        if (
                            continuation.isActive
                        ) {

                            continuation.resume(
                                fallback
                            )
                        }
                    }
                }
            )

        } catch (
            _: Exception
        ) {

            if (
                continuation.isActive
            ) {

                continuation.resume(
                    fallback
                )
            }
        }
    }
}