package com.cos229239.team02.oto.ui.screens.explorer

import android.graphics.BitmapFactory
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cos229239.team02.oto.data.hazard.HazardPriority
import com.cos229239.team02.oto.data.hazard.HazardReport
import com.cos229239.team02.oto.data.hazard.HazardReportViewModel
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * -------------------------------------------------------------
 * FIELD REPORTS SCREEN
 * -------------------------------------------------------------
 *
 * Displays locally stored community hazard reports.
 *
 * The screen can show:
 *
 * - Every active report
 * - Only a selected group of reports from a map marker
 *
 * Reports are currently stored locally on the device.
 */

@Composable
fun FieldReportsScreen(
    onBackClick: () -> Unit,
    hazardReportViewModel: HazardReportViewModel,

    /*
     * Optional IDs supplied by a map marker.
     *
     * Empty means:
     * show all active reports.
     */
    selectedReportIds: Set<String> = emptySet()
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    val lightBackground =
        Color(
            0xFFF7F8F6
        )

    /*
     * ---------------------------------------------------------
     * REPORTS TO DISPLAY
     * ---------------------------------------------------------
     */

    val activeReports =
        hazardReportViewModel
            .activeHazardReports

    val reports =
        if (
            selectedReportIds.isEmpty()
        ) {

            activeReports

        } else {

            activeReports.filter {
                it.id in selectedReportIds
            }
        }

    /*
     * Report currently expanded by the user.
     */
    var selectedReportId by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
    ) {

        /*
         * -----------------------------------------------------
         * HEADER
         * -----------------------------------------------------
         */

        OtoTopAppBar(
            title =
                "FIELD REPORTS",

            onBackClick =
                onBackClick
        )

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
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            /*
             * -------------------------------------------------
             * SUMMARY
             * -------------------------------------------------
             */

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            lightBackground
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
                            if (
                                selectedReportIds.isEmpty()
                            ) {

                                "ACTIVE FIELD REPORTS"

                            } else {

                                "REPORTS AT THIS LOCATION"
                            },

                        color =
                            darkGreen,

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
                            when (
                                reports.size
                            ) {

                                0 ->
                                    "No active reports"

                                1 ->
                                    "1 active report"

                                else ->
                                    "${reports.size} active reports"
                            },

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            "Community reports shown here are currently stored on this device. Shared community reporting will require the future backend.",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            Color(
                                0xFF666666
                            )
                    )
                }
            }

            /*
             * -------------------------------------------------
             * EMPTY STATE
             * -------------------------------------------------
             */

            if (
                reports.isEmpty()
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
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    32.dp
                                ),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "📋",

                            fontSize =
                                42.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    10.dp
                                )
                        )

                        Text(
                            text =
                                "No active field reports",

                            color =
                                darkGreen,

                            fontWeight =
                                FontWeight.Bold,

                            textAlign =
                                TextAlign.Center
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    6.dp
                                )
                        )

                        Text(
                            text =
                                "Submitted hazard reports will appear here.",

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

            /*
             * -------------------------------------------------
             * REPORT LIST
             * -------------------------------------------------
             */

            reports
                .sortedByDescending {
                    it.createdAt
                }
                .forEach { report ->

                    val expanded =
                        selectedReportId ==
                                report.id

                    FieldReportCard(
                        report =
                            report,

                        expanded =
                            expanded,

                        onClick = {

                            selectedReportId =
                                if (
                                    expanded
                                ) {

                                    null

                                } else {

                                    report.id
                                }
                        },

                        onStillHereClick = {

                            hazardReportViewModel
                                .confirmHazard(
                                    report.id
                                )
                        },

                        onNoLongerHereClick = {

                            hazardReportViewModel
                                .markHazardResolved(
                                    report.id
                                )

                            if (
                                selectedReportId ==
                                report.id
                            ) {

                                selectedReportId =
                                    null
                            }
                        },

                        onIncorrectClick = {

                            hazardReportViewModel
                                .markHazardIncorrect(
                                    report.id
                                )

                            if (
                                selectedReportId ==
                                report.id
                            ) {

                                selectedReportId =
                                    null
                            }
                        }
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
}


/*
 * -------------------------------------------------------------
 * FIELD REPORT CARD
 * -------------------------------------------------------------
 */

@Composable
private fun FieldReportCard(
    report: HazardReport,
    expanded: Boolean,
    onClick: () -> Unit,
    onStillHereClick: () -> Unit,
    onNoLongerHereClick: () -> Unit,
    onIncorrectClick: () -> Unit
) {

    val darkGreen =
        Color(
            0xFF063D24
        )

    val categoryColor =
        fieldReportCategoryColor(
            report.category
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

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
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
                    Alignment.Top
            ) {

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            report.category.uppercase(),

                        color =
                            categoryColor,

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                3.dp
                            )
                    )

                    Text(
                        text =
                            report.reportType,

                        color =
                            darkGreen,

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                PriorityBadge(
                    priority =
                        report.priority
                )
            }

            Text(
                text =
                    "Reported ${formatReportDate(report.createdAt)}",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    Color(
                        0xFF666666
                    )
            )

            /*
             * -------------------------------------------------
             * QUICK SUMMARY
             * -------------------------------------------------
             */

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Text(
                    text =
                        "Severity: ${report.severity}",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    fontWeight =
                        FontWeight.Medium
                )

                if (
                    report.isVerified
                ) {

                    Text(
                        text =
                            "✓ Confirmed",

                        color =
                            Color(
                                0xFF149447
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Text(
                text =
                    if (
                        expanded
                    ) {

                        "Tap to collapse ▲"

                    } else {

                        "Tap to view report ›"
                    },

                color =
                    Color(
                        0xFF0B5D1E
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                fontWeight =
                    FontWeight.Bold
            )

            /*
             * -------------------------------------------------
             * EXPANDED DETAILS
             * -------------------------------------------------
             */

            if (
                expanded
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                /*
                 * PHOTO
                 */

                if (
                    !report.photoPath.isNullOrBlank()
                ) {

                    val photoFile =
                        File(
                            report.photoPath
                        )

                    if (
                        photoFile.exists()
                    ) {

                        val bitmap =
                            remember(
                                report.photoPath
                            ) {

                                BitmapFactory
                                    .decodeFile(
                                        report.photoPath
                                    )
                            }

                        if (
                            bitmap != null
                        ) {

                            Image(
                                bitmap =
                                    bitmap
                                        .asImageBitmap(),

                                contentDescription =
                                    "Field report photo",

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
                    }
                }

                /*
                 * DESCRIPTION
                 */

                FieldReportDetail(
                    title =
                        "DESCRIPTION",

                    value =
                        if (
                            report.description.isBlank()
                        ) {

                            "No additional description"

                        } else {

                            report.description
                        }
                )

                /*
                 * LANDMARK
                 */

                FieldReportDetail(
                    title =
                        "LANDMARK",

                    value =
                        if (
                            report.landmark.isBlank()
                        ) {

                            "No landmark provided"

                        } else {

                            report.landmark
                        }
                )

                /*
                 * LOCATION
                 */

                FieldReportDetail(
                    title =
                        "REPORT LOCATION",

                    value =
                        String.format(
                            Locale.US,
                            "%.5f, %.5f",
                            report.latitude,
                            report.longitude
                        )
                )

                /*
                 * CONFIRMATIONS
                 */

                FieldReportDetail(
                    title =
                        "COMMUNITY CONFIRMATIONS",

                    value =
                        report.confirmationCount
                            .toString()
                )

                /*
                 * -------------------------------------------------
                 * COMMUNITY STATUS BUTTONS
                 * -------------------------------------------------
                 */

                Text(
                    text =
                        "Is this report still accurate?",

                    color =
                        darkGreen,

                    fontWeight =
                        FontWeight.Bold
                )

                Button(
                    onClick = {

                        onStillHereClick()
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(
                                    0xFF0B5D1E
                                )
                        )
                ) {

                    Text(
                        text =
                            "STILL HERE",

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {

                        onNoLongerHereClick()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            "NO LONGER HERE"
                    )
                }

                OutlinedButton(
                    onClick = {

                        onIncorrectClick()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            "INCORRECT REPORT",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        }
    }
}


/*
 * -------------------------------------------------------------
 * DETAIL ROW
 * -------------------------------------------------------------
 */

@Composable
private fun FieldReportDetail(
    title: String,
    value: String
) {

    Column {

        Text(
            text =
                title,

            fontSize =
                9.sp,

            color =
                Color(
                    0xFF65726A
                ),

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
                value,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}


/*
 * -------------------------------------------------------------
 * PRIORITY BADGE
 * -------------------------------------------------------------
 */

@Composable
private fun PriorityBadge(
    priority: HazardPriority
) {

    val label =
        when (
            priority
        ) {

            HazardPriority.CRITICAL ->
                "CRITICAL"

            HazardPriority.HIGH ->
                "HIGH"

            HazardPriority.NORMAL ->
                "NORMAL"
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
        colors =
            CardDefaults.cardColors(
                containerColor =
                    backgroundColor
            ),

        shape =
            RoundedCornerShape(
                20.dp
            )
    ) {

        Text(
            text =
                label,

            modifier =
                Modifier.padding(
                    horizontal =
                        10.dp,

                    vertical =
                        5.dp
                ),

            color =
                textColor,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


/*
 * -------------------------------------------------------------
 * CATEGORY COLOR
 * -------------------------------------------------------------
 *
 * Uses the same category color system as the map.
 */

private fun fieldReportCategoryColor(
    category: String
): Color {

    return when (
        category
    ) {

        "Accident / Emergency" ->
            Color(
                0xFFD32F2F
            )

        "Wildlife" ->
            Color(
                0xFFF57C00
            )

        "Nature / Plants" ->
            Color(
                0xFF8D6E36
            )

        "Trail / Road Hazard" ->
            Color(
                0xFFFBC02D
            )

        "Weather / Environmental" ->
            Color(
                0xFF1976D2
            )

        "Facility / Infrastructure" ->
            Color(
                0xFF7E57C2
            )

        else ->
            Color(
                0xFF757575
            )
    }
}


/*
 * -------------------------------------------------------------
 * FORMAT DATE
 * -------------------------------------------------------------
 */

private fun formatReportDate(
    timeMillis: Long
): String {

    if (
        timeMillis <= 0L
    ) {

        return "recently"
    }

    val formatter =
        SimpleDateFormat(
            "MMM d, yyyy • h:mm a",
            Locale.US
        )

    return formatter.format(
        Date(
            timeMillis
        )
    )
}