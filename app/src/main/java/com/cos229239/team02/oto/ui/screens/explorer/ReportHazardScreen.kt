package com.cos229239.team02.oto.ui.screens.explorer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.cos229239.team02.oto.data.hazard.HazardPriority
import com.cos229239.team02.oto.data.hazard.HazardReport
import com.cos229239.team02.oto.data.hazard.HazardReportViewModel
import com.cos229239.team02.oto.data.hazard.calculateHazardPriority
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

/*
 * Broad report category.
 */
private data class HazardCategory(
    val name: String,
    val description: String,
    val symbol: String,
    val reportTypes: List<String>
)

@Composable
fun ReportHazardScreen(
    onBackClick: () -> Unit,
    onFirstAidSurvivalClick: () -> Unit = {},
    hazardReportViewModel: HazardReportViewModel
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

    /*
     * ---------------------------------------------------------
     * LOCATION REPOSITORY
     * ---------------------------------------------------------
     */

    val locationRepository =
        remember(context) {

            AndroidLocationRepository(
                context.applicationContext
            )
        }

    /*
     * ---------------------------------------------------------
     * REPORT FLOW STATE
     * ---------------------------------------------------------
     */

    var selectedCategory by remember {
        mutableStateOf<HazardCategory?>(null)
    }

    var selectedReportType by remember {
        mutableStateOf<String?>(null)
    }

    var selectedSeverity by remember {
        mutableStateOf<String?>(null)
    }

    var landmarkDescription by remember {
        mutableStateOf("")
    }

    var reportDescription by remember {
        mutableStateOf("")
    }

    var isReviewingReport by remember {
        mutableStateOf(false)
    }

    var reportSubmitted by remember {
        mutableStateOf(false)
    }

    /*
     * Stores the priority of the report
     * that was successfully submitted.
     */
    var submittedPriority by remember {
        mutableStateOf<HazardPriority?>(null)
    }

    /*
     * Remembers whether the submitted report
     * successfully saved a photo.
     */
    var submittedPhotoSaved by remember {
        mutableStateOf(false)
    }

    /*
     * ---------------------------------------------------------
     * PHOTO STATE
     * ---------------------------------------------------------
     */

    var reportPhoto by remember {
        mutableStateOf<Bitmap?>(null)
    }

    /*
     * ---------------------------------------------------------
     * CAMERA
     * ---------------------------------------------------------
     */

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (
                bitmap != null
            ) {

                reportPhoto =
                    bitmap
            }
        }

    /*
     * ---------------------------------------------------------
     * LOCATION STATE
     * ---------------------------------------------------------
     */

    var reportLocation by remember {
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
     * ---------------------------------------------------------
     * LOAD CURRENT LOCATION
     * ---------------------------------------------------------
     */

    fun loadCurrentLocation() {

        scope.launch {

            loadingLocation =
                true

            locationStatus =
                "Finding your current location..."

            val location =
                locationRepository
                    .getCurrentLocation()

            if (
                location != null
            ) {

                reportLocation =
                    location

                locationStatus =
                    "Current location detected"

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
                ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true

            if (
                granted
            ) {

                loadCurrentLocation()

            } else {

                locationStatus =
                    "Location permission denied"
            }
        }

    /*
     * ---------------------------------------------------------
     * REQUEST LOCATION
     * ---------------------------------------------------------
     */

    fun requestCurrentLocation() {

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

    /*
     * ---------------------------------------------------------
     * REPORT CATEGORIES
     * ---------------------------------------------------------
     */

    val categories =
        remember {
            listOf(

                HazardCategory(
                    name = "Accident / Emergency",
                    description =
                        "Accidents, injuries, lost people, or other urgent situations.",
                    symbol = "⚠",
                    reportTypes =
                        listOf(
                            "Motor Vehicle Accident",
                            "Bicycle Accident",
                            "Hiking Injury",
                            "Fall / Injury",
                            "Medical Emergency",
                            "Lost Person",
                            "Other Accident / Emergency"
                        )
                ),

                HazardCategory(
                    name = "Wildlife",
                    description =
                        "Animal sightings, injured wildlife, or animals blocking a route.",
                    symbol = "🐾",
                    reportTypes =
                        listOf(
                            "Bear Sighting",
                            "Coyote Sighting",
                            "Snake Sighting",
                            "Injured Animal",
                            "Dead Animal",
                            "Animal Blocking Trail / Road",
                            "Other Wildlife"
                        )
                ),

                HazardCategory(
                    name = "Nature / Plants",
                    description =
                        "Fallen trees, dangerous plants, rockfall, landslides, or other natural hazards.",
                    symbol = "🌲",
                    reportTypes =
                        listOf(
                            "Fallen Tree",
                            "Poison Ivy / Poison Oak",
                            "Poisonous Plant",
                            "Invasive Plant",
                            "Rockfall",
                            "Landslide",
                            "Other Natural Hazard"
                        )
                ),

                HazardCategory(
                    name = "Trail / Road Hazard",
                    description =
                        "Blocked trails, road closures, flooding, damaged bridges, washouts, or debris.",
                    symbol = "🚧",
                    reportTypes =
                        listOf(
                            "Trail Blocked",
                            "Road Blocked",
                            "Trail Closure",
                            "Road Closure",
                            "Washout",
                            "Flooded Trail",
                            "Flooded Road",
                            "Large Debris",
                            "Bridge Damaged",
                            "Route Change",
                            "Other Trail / Road Issue"
                        )
                ),

                HazardCategory(
                    name = "Weather / Environmental",
                    description =
                        "Flooding, ice, snow, smoke, high winds, poor visibility, or other environmental hazards.",
                    symbol = "🌧",
                    reportTypes =
                        listOf(
                            "Flooding",
                            "Ice",
                            "Heavy Snow",
                            "High Winds",
                            "Wildfire / Smoke",
                            "Extreme Heat",
                            "Poor Visibility",
                            "Other Environmental Hazard"
                        )
                ),

                HazardCategory(
                    name = "Facility / Infrastructure",
                    description =
                        "Damaged signs, shelters, railings, restrooms, parking areas, or other facilities.",
                    symbol = "🛠",
                    reportTypes =
                        listOf(
                            "Broken Bridge",
                            "Damaged Railing",
                            "Broken / Missing Sign",
                            "Closed Restroom",
                            "Damaged Shelter",
                            "Lighting Issue",
                            "Parking Issue",
                            "Other Facility Problem"
                        )
                ),

                HazardCategory(
                    name = "Other",
                    description =
                        "Report something that does not fit into the categories above.",
                    symbol = "＋",
                    reportTypes =
                        listOf(
                            "Other Hazard",
                            "Other Route Change",
                            "Other Safety Concern"
                        )
                )
            )
        }

    /*
     * ---------------------------------------------------------
     * AUTOMATIC LOCATION
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        selectedReportType
    ) {

        if (
            selectedReportType != null &&
            reportLocation == null
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

                loadCurrentLocation()

            } else {

                locationStatus =
                    "Location permission required"
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * RESET REPORT
     * ---------------------------------------------------------
     */

    fun resetReport() {

        selectedCategory =
            null

        selectedReportType =
            null

        selectedSeverity =
            null

        landmarkDescription =
            ""

        reportDescription =
            ""

        reportPhoto =
            null

        reportLocation =
            null

        locationStatus =
            "Location not loaded"

        isReviewingReport =
            false

        reportSubmitted =
            false

        submittedPriority =
            null

        submittedPhotoSaved =
            false
    }

    /*
     * ---------------------------------------------------------
     * SMART BACK BUTTON
     * ---------------------------------------------------------
     */

    fun handleBack() {

        when {

            reportSubmitted -> {

                onBackClick()
            }

            isReviewingReport -> {

                isReviewingReport =
                    false
            }

            selectedReportType != null -> {

                selectedReportType =
                    null

                selectedSeverity =
                    null

                landmarkDescription =
                    ""

                reportDescription =
                    ""

                reportPhoto =
                    null

                reportLocation =
                    null

                locationStatus =
                    "Location not loaded"
            }

            selectedCategory != null -> {

                selectedCategory =
                    null
            }

            else -> {

                onBackClick()
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * SCREEN TITLE
     * ---------------------------------------------------------
     */

    val screenTitle =
        when {

            reportSubmitted ->
                "REPORT SUBMITTED"

            isReviewingReport ->
                "REVIEW REPORT"

            selectedReportType != null ->
                "REPORT DETAILS"

            selectedCategory != null ->
                selectedCategory!!.name.uppercase()

            else ->
                "REPORT HAZARD"
        }

    Scaffold(
        topBar = {

            OtoTopAppBar(
                title =
                    screenTitle,

                onBackClick = {
                    handleBack()
                }
            )
        }
    ) { paddingValues ->

        when {

            /*
             * =================================================
             * STEP 5 - REPORT SUBMITTED
             * =================================================
             */

            reportSubmitted -> {

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                24.dp
                            ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.spacedBy(
                            16.dp
                        )
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                30.dp
                            )
                    )

                    Text(
                        text =
                            "✓",

                        color =
                            Color(
                                0xFF149447
                            ),

                        fontSize =
                            64.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Report submitted",

                        style =
                            MaterialTheme.typography.headlineSmall,

                        color =
                            darkGreen,

                        fontWeight =
                            FontWeight.Bold,

                        textAlign =
                            TextAlign.Center
                    )

                    Text(
                        text =
                            "Thanks for helping keep the area safer for other OTO users.",

                        style =
                            MaterialTheme.typography.bodyLarge,

                        textAlign =
                            TextAlign.Center
                    )

                    submittedPriority?.let { priority ->

                        PriorityCard(
                            priority =
                                priority
                        )
                    }

                    /*
                     * -------------------------------------------------
                     * SAVED LOCALLY
                     * -------------------------------------------------
                     */

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFF3F4F2
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
                                    "Saved on this device",

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    darkGreen
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        4.dp
                                    )
                            )

                            Text(
                                text =
                                    "This report is stored locally and will remain available after the app is restarted. Community sharing with other devices will require the future backend.",

                                style =
                                    MaterialTheme.typography.bodySmall,

                                color =
                                    Color(
                                        0xFF626262
                                    )
                            )

                            if (
                                submittedPhotoSaved
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            8.dp
                                        )
                                )

                                Text(
                                    text =
                                        "✓ Attached photo saved",

                                    color =
                                        mediumGreen,

                                    fontWeight =
                                        FontWeight.Bold,

                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    /*
                     * -------------------------------------------------
                     * SAFETY GUIDANCE
                     * -------------------------------------------------
                     */

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFFFF3E0
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
                                    "Need safety guidance?",

                                style =
                                    MaterialTheme.typography.titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(
                                        0xFF8A3B12
                                    )
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        4.dp
                                    )
                            )

                            Text(
                                text =
                                    "Visit First Aid & Survival in Crisis Mode for emergency and outdoor safety information.",

                                style =
                                    MaterialTheme.typography.bodyMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        12.dp
                                    )
                            )

                            Button(
                                onClick = {

                                    onFirstAidSurvivalClick()
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color(
                                                0xFF8A3B12
                                            )
                                    )
                            ) {

                                Text(
                                    text =
                                        "VIEW FIRST AID & SURVIVAL",

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {

                            onBackClick()
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    54.dp
                                ),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    mediumGreen
                            )
                    ) {

                        Text(
                            text =
                                "RETURN TO EXPLORER",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {

                            resetReport()
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "REPORT ANOTHER HAZARD"
                        )
                    }
                }
            }

            /*
             * =================================================
             * STEP 4 - REVIEW REPORT
             * =================================================
             */

            isReviewingReport -> {

                val category =
                    selectedCategory!!

                val reportType =
                    selectedReportType!!

                val severity =
                    selectedSeverity!!

                val location =
                    reportLocation!!

                val priority =
                    calculateHazardPriority(
                        reportType =
                            reportType,

                        severity =
                            severity
                    )

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                16.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    Text(
                        text =
                            "Review your report",

                        style =
                            MaterialTheme.typography.headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Make sure the information below is correct before submitting.",

                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                    ReviewCard(
                        label =
                            "REPORT",

                        value =
                            "${category.symbol} $reportType",

                        secondaryValue =
                            category.name
                    )

                    ReviewCard(
                        label =
                            "SEVERITY",

                        value =
                            severity,

                        secondaryValue =
                            when (
                                severity
                            ) {

                                "High" ->
                                    "Nearby users should be warned quickly."

                                "Moderate" ->
                                    "May affect safety or travel through the area."

                                else ->
                                    "Important information without immediate danger."
                            }
                    )

                    PriorityCard(
                        priority =
                            priority
                    )

                    ReviewCard(
                        label =
                            "LOCATION",

                        value =
                            formatReportLocation(
                                location
                            ),

                        secondaryValue =
                            if (
                                landmarkDescription.isBlank()
                            ) {

                                "No landmark added"

                            } else {

                                landmarkDescription
                            }
                    )

                    ReviewCard(
                        label =
                            "DESCRIPTION",

                        value =
                            if (
                                reportDescription.isBlank()
                            ) {

                                "No description added"

                            } else {

                                reportDescription
                            },

                        secondaryValue =
                            null
                    )

                    /*
                     * -------------------------------------------------
                     * PHOTO
                     * -------------------------------------------------
                     */

                    if (
                        reportPhoto != null
                    ) {

                        Text(
                            text =
                                "PHOTO",

                            fontSize =
                                11.sp,

                            color =
                                Color(
                                    0xFF52665A
                                ),

                            fontWeight =
                                FontWeight.Bold
                        )

                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(
                                    14.dp
                                )
                        ) {

                            Image(
                                bitmap =
                                    reportPhoto!!
                                        .asImageBitmap(),

                                contentDescription =
                                    "Hazard report photo",

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(
                                            220.dp
                                        ),

                                contentScale =
                                    ContentScale.Crop
                            )
                        }

                    } else {

                        ReviewCard(
                            label =
                                "PHOTO",

                            value =
                                "No photo attached",

                            secondaryValue =
                                null
                        )
                    }

                    /*
                     * -------------------------------------------------
                     * EDIT REPORT
                     * -------------------------------------------------
                     */

                    OutlinedButton(
                        onClick = {

                            isReviewingReport =
                                false
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    52.dp
                                )
                    ) {

                        Text(
                            text =
                                "EDIT REPORT"
                        )
                    }

                    /*
                     * -------------------------------------------------
                     * SUBMIT REPORT
                     * -------------------------------------------------
                     */

                    Button(
                        onClick = {

                            /*
                             * Generate the report ID first.
                             *
                             * The same ID is used for both:
                             *
                             * - HazardReport
                             * - Saved photo filename
                             */
                            val reportId =
                                UUID.randomUUID()
                                    .toString()

                            /*
                             * -------------------------------------------------
                             * SAVE PHOTO
                             * -------------------------------------------------
                             *
                             * If the user attached a photo, save
                             * the actual Bitmap to internal storage.
                             */

                            val savedPhotoPath =
                                if (
                                    reportPhoto != null
                                ) {

                                    hazardReportViewModel
                                        .saveHazardPhoto(
                                            bitmap =
                                                reportPhoto!!,

                                            reportId =
                                                reportId
                                        )

                                } else {

                                    null
                                }

                            /*
                             * Create the persistent report.
                             */
                            val newReport =
                                HazardReport(

                                    id =
                                        reportId,

                                    category =
                                        category.name,

                                    reportType =
                                        reportType,

                                    severity =
                                        severity,

                                    priority =
                                        priority,

                                    latitude =
                                        location.latitude,

                                    longitude =
                                        location.longitude,

                                    accuracyMeters =
                                        location.accuracyMeters
                                            ?: 0f,

                                    landmark =
                                        landmarkDescription,

                                    description =
                                        reportDescription,

                                    /*
                                     * Only mark the report as having
                                     * a photo if the Bitmap was
                                     * successfully written to storage.
                                     */
                                    hasPhoto =
                                        savedPhotoPath != null,

                                    /*
                                     * Persistent image location.
                                     */
                                    photoPath =
                                        savedPhotoPath,

                                    createdAt =
                                        System.currentTimeMillis()
                                )

                            /*
                             * -------------------------------------------------
                             * SAVE REPORT
                             * -------------------------------------------------
                             *
                             * addHazardReport now also writes
                             * the report list to local JSON storage.
                             */
                            hazardReportViewModel
                                .addHazardReport(
                                    newReport
                                )

                            submittedPriority =
                                priority

                            submittedPhotoSaved =
                                savedPhotoPath != null

                            reportSubmitted =
                                true

                            isReviewingReport =
                                false
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    54.dp
                                ),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    mediumGreen
                            )
                    ) {

                        Text(
                            text =
                                "SUBMIT REPORT",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFFFF3E0
                                    )
                            ),

                        shape =
                            RoundedCornerShape(
                                12.dp
                            )
                    ) {

                        Text(
                            text =
                                "Reports do not replace emergency services. If there is immediate danger, use Crisis Mode or contact emergency services.",

                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),

                            style =
                                MaterialTheme.typography.bodySmall,

                            color =
                                Color(
                                    0xFF8A3B12
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )
                }
            }

            /*
             * =================================================
             * STEP 3 - REPORT DETAILS
             * =================================================
             */

            selectedReportType != null -> {

                val category =
                    selectedCategory!!

                val reportType =
                    selectedReportType!!

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                16.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    Text(
                        text =
                            "Report details",

                        style =
                            MaterialTheme.typography.headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Add information that may help other people understand the hazard.",

                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFEAF5EE
                                    )
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
                                        16.dp
                                    ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    category.symbol,

                                fontSize =
                                    30.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.padding(
                                        horizontal =
                                            8.dp
                                    )
                            )

                            Column {

                                Text(
                                    text =
                                        category.name.uppercase(),

                                    fontSize =
                                        10.sp,

                                    color =
                                        Color(
                                            0xFF52665A
                                        ),

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        reportType,

                                    style =
                                        MaterialTheme.typography.titleMedium,

                                    color =
                                        darkGreen,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }

                    /*
                     * -------------------------------------------------
                     * SEVERITY
                     * -------------------------------------------------
                     */

                    Text(
                        text =
                            "How serious is it?",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    SeverityCard(
                        title =
                            "Low",

                        description =
                            "Something people should know about, but it does not create an immediate danger.",

                        isSelected =
                            selectedSeverity == "Low",

                        onClick = {

                            selectedSeverity =
                                "Low"
                        }
                    )

                    SeverityCard(
                        title =
                            "Moderate",

                        description =
                            "Could affect someone's route or safety and should be approached carefully.",

                        isSelected =
                            selectedSeverity == "Moderate",

                        onClick = {

                            selectedSeverity =
                                "Moderate"
                        }
                    )

                    SeverityCard(
                        title =
                            "High",

                        description =
                            "A serious hazard that nearby users should be warned about quickly.",

                        isSelected =
                            selectedSeverity == "High",

                        onClick = {

                            selectedSeverity =
                                "High"
                        }
                    )

                    /*
                     * -------------------------------------------------
                     * CURRENT LOCATION
                     * -------------------------------------------------
                     */

                    Text(
                        text =
                            "Report location",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFF3F7F4
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

                            when {

                                loadingLocation -> {

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        CircularProgressIndicator()

                                        Spacer(
                                            modifier =
                                                Modifier.padding(
                                                    horizontal =
                                                        8.dp
                                                )
                                        )

                                        Text(
                                            text =
                                                "Finding your current location..."
                                        )
                                    }
                                }

                                reportLocation != null -> {

                                    Text(
                                        text =
                                            "📍 Current location detected",

                                        color =
                                            mediumGreen,

                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                6.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            formatReportLocation(
                                                reportLocation!!
                                            ),

                                        style =
                                            MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                10.dp
                                            )
                                    )

                                    Button(
                                        onClick = {
                                            requestCurrentLocation()
                                        },

                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor =
                                                    mediumGreen
                                            )
                                    ) {

                                        Text(
                                            text =
                                                "REFRESH LOCATION"
                                        )
                                    }
                                }

                                else -> {

                                    Text(
                                        text =
                                            locationStatus
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                10.dp
                                            )
                                    )

                                    Button(
                                        onClick = {
                                            requestCurrentLocation()
                                        }
                                    ) {

                                        Text(
                                            text =
                                                "LOCATE ME"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    /*
                     * -------------------------------------------------
                     * LANDMARK
                     * -------------------------------------------------
                     */

                    Text(
                        text =
                            "Landmark",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Optional — add something nearby that could help another user identify the exact area.",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            Color(
                                0xFF666666
                            )
                    )

                    OutlinedTextField(
                        value =
                            landmarkDescription,

                        onValueChange = {

                            landmarkDescription =
                                it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Nearby landmark"
                            )
                        },

                        placeholder = {

                            Text(
                                text =
                                    "Example: Near the north trail entrance"
                            )
                        }
                    )

                    /*
                     * -------------------------------------------------
                     * DESCRIPTION
                     * -------------------------------------------------
                     */

                    Text(
                        text =
                            "Description",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Optional",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            Color(
                                0xFF777777
                            )
                    )

                    OutlinedTextField(
                        value =
                            reportDescription,

                        onValueChange = {

                            reportDescription =
                                it
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    140.dp
                                ),

                        label = {

                            Text(
                                text =
                                    "Additional details"
                            )
                        },

                        placeholder = {

                            Text(
                                text =
                                    "Describe what you saw and anything nearby users should know."
                            )
                        }
                    )

                    /*
                     * -------------------------------------------------
                     * PHOTO
                     * -------------------------------------------------
                     */

                    Text(
                        text =
                            "Photo",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Optional",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            Color(
                                0xFF777777
                            )
                    )

                    if (
                        reportPhoto == null
                    ) {

                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        cameraLauncher.launch(
                                            null
                                        )
                                    },

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(
                                            0xFFF3F4F2
                                        )
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
                                        .padding(
                                            22.dp
                                        ),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                Text(
                                    text =
                                        "📷",

                                    fontSize =
                                        38.sp
                                )

                                Text(
                                    text =
                                        "Take a photo",

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        darkGreen
                                )

                                Text(
                                    text =
                                        "Tap to open the camera",

                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                    } else {

                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(
                                    14.dp
                                )
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(
                                        12.dp
                                    )
                            ) {

                                Image(
                                    bitmap =
                                        reportPhoto!!
                                            .asImageBitmap(),

                                    contentDescription =
                                        "Hazard report photo",

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(
                                                220.dp
                                            ),

                                    contentScale =
                                        ContentScale.Crop
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            10.dp
                                        )
                                )

                                Text(
                                    text =
                                        "✓ Photo attached",

                                    color =
                                        mediumGreen,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            10.dp
                                        )
                                )

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.spacedBy(
                                            10.dp
                                        )
                                ) {

                                    Button(
                                        onClick = {

                                            cameraLauncher.launch(
                                                null
                                            )
                                        },

                                        modifier =
                                            Modifier.weight(
                                                1f
                                            )
                                    ) {

                                        Text(
                                            text =
                                                "RETAKE"
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {

                                            reportPhoto =
                                                null
                                        },

                                        modifier =
                                            Modifier.weight(
                                                1f
                                            )
                                    ) {

                                        Text(
                                            text =
                                                "REMOVE"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    /*
                     * -------------------------------------------------
                     * REVIEW REPORT
                     * -------------------------------------------------
                     */

                    Button(
                        onClick = {

                            isReviewingReport =
                                true
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    54.dp
                                ),

                        enabled =
                            selectedSeverity != null &&
                                    reportLocation != null,

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    mediumGreen
                            )
                    ) {

                        Text(
                            text =
                                "REVIEW REPORT",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    if (
                        selectedSeverity == null
                    ) {

                        Text(
                            text =
                                "Choose a severity level before continuing.",

                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }

                    if (
                        reportLocation == null
                    ) {

                        Text(
                            text =
                                "A location is required before the report can be reviewed.",

                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )
                }
            }

            /*
             * =================================================
             * STEP 2 - REPORT TYPE
             * =================================================
             */

            selectedCategory != null -> {

                val category =
                    selectedCategory!!

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                16.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    Text(
                        text =
                            "What happened?",

                        style =
                            MaterialTheme.typography.headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Choose the option that best describes the ${category.name.lowercase()} report."
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFEAF5EE
                                    )
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    14.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    category.symbol,

                                fontSize =
                                    26.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.padding(
                                        6.dp
                                    )
                            )

                            Text(
                                text =
                                    category.name,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    darkGreen
                            )
                        }
                    }

                    category.reportTypes.forEach { reportType ->

                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        selectedReportType =
                                            reportType
                                    }
                        ) {

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            18.dp
                                        ),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text =
                                        reportType,

                                    fontWeight =
                                        FontWeight.Medium
                                )

                                Text(
                                    text =
                                        "›",

                                    color =
                                        darkGreen,

                                    fontSize =
                                        24.sp
                                )
                            }
                        }
                    }
                }
            }

            /*
             * =================================================
             * STEP 1 - CATEGORY
             * =================================================
             */

            else -> {

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                paddingValues
                            )
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                16.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    Text(
                        text =
                            "What would you like to report?",

                        style =
                            MaterialTheme.typography.headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Choose the category that best describes what you encountered."
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(
                                        0xFFFFF3E0
                                    )
                            )
                    ) {

                        Text(
                            text =
                                "For an immediate life-threatening emergency, use Crisis Mode or contact emergency services.",

                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),

                            color =
                                Color(
                                    0xFF8A3B12
                                )
                        )
                    }

                    categories.forEach { category ->

                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        selectedCategory =
                                            category
                                    }
                        ) {

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            16.dp
                                        ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    text =
                                        category.symbol,

                                    fontSize =
                                        28.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.padding(
                                            8.dp
                                        )
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(
                                        text =
                                            category.name,

                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Text(
                                        text =
                                            category.description,

                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    text =
                                        "›",

                                    color =
                                        darkGreen,

                                    fontSize =
                                        26.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/*
 * -------------------------------------------------------------
 * SEVERITY CARD
 * -------------------------------------------------------------
 */

@Composable
private fun SeverityCard(
    title: String,
    description: String,
    isSelected: Boolean,
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
                    if (
                        isSelected
                    ) {

                        Color(
                            0xFFE2F3E9
                        )

                    } else {

                        Color.White
                    }
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween,

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

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        description,

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            if (
                isSelected
            ) {

                Text(
                    text =
                        "✓",

                    color =
                        Color(
                            0xFF149447
                        ),

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


/*
 * -------------------------------------------------------------
 * REVIEW CARD
 * -------------------------------------------------------------
 */

@Composable
private fun ReviewCard(
    label: String,
    value: String,
    secondaryValue: String?
) {

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

            Text(
                text =
                    label,

                color =
                    Color(
                        0xFF52665A
                    ),

                fontSize =
                    10.sp,

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
                    value,

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            if (
                !secondaryValue.isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        secondaryValue,

                    style =
                        MaterialTheme.typography.bodySmall,

                    color =
                        Color(
                            0xFF666666
                        )
                )
            }
        }
    }
}


/*
 * -------------------------------------------------------------
 * PRIORITY CARD
 * -------------------------------------------------------------
 */

@Composable
private fun PriorityCard(
    priority: HazardPriority
) {

    val title =
        when (
            priority
        ) {

            HazardPriority.CRITICAL ->
                "CRITICAL PRIORITY"

            HazardPriority.HIGH ->
                "HIGH PRIORITY"

            HazardPriority.NORMAL ->
                "NORMAL PRIORITY"
        }

    val description =
        when (
            priority
        ) {

            HazardPriority.CRITICAL ->
                "This report may represent an immediate safety concern and should receive priority attention."

            HazardPriority.HIGH ->
                "This hazard could significantly affect nearby users and should be shown prominently."

            HazardPriority.NORMAL ->
                "This report will appear as a standard community hazard."
        }

    val backgroundColor =
        when (
            priority
        ) {

            HazardPriority.CRITICAL ->
                Color(
                    0xFFFFE5E5
                )

            HazardPriority.HIGH ->
                Color(
                    0xFFFFF3E0
                )

            HazardPriority.NORMAL ->
                Color(
                    0xFFEAF5EE
                )
        }

    val textColor =
        when (
            priority
        ) {

            HazardPriority.CRITICAL ->
                Color(
                    0xFFB00020
                )

            HazardPriority.HIGH ->
                Color(
                    0xFF8A3B12
                )

            HazardPriority.NORMAL ->
                Color(
                    0xFF0B5D1E
                )
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    backgroundColor
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
                    title,

                fontWeight =
                    FontWeight.Bold,

                color =
                    textColor
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
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}


/*
 * -------------------------------------------------------------
 * FORMAT REPORT LOCATION
 * -------------------------------------------------------------
 */

private fun formatReportLocation(
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