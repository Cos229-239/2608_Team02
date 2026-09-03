package com.cos229239.team02.oto.ui.screens.explorer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cos229239.team02.oto.data.location.PlaceSearchClient
import com.cos229239.team02.oto.data.location.PlaceSuggestion
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTripScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    tripViewModel: PlanTripViewModel
) {

    BackHandler {
        onBackClick()
    }

    val darkGreen = Color(0xFF0B5D1E)

    val placeSearchClient = remember {
        PlaceSearchClient()
    }

    var startingSuggestions by remember {
        mutableStateOf<List<PlaceSuggestion>>(emptyList())
    }

    var destinationSuggestions by remember {
        mutableStateOf<List<PlaceSuggestion>>(emptyList())
    }

    var pendingStartingPoint by remember {
        mutableStateOf<PlaceSuggestion?>(null)
    }

    var pendingDestination by remember {
        mutableStateOf<PlaceSuggestion?>(null)
    }

    var showCalendar by remember {
        mutableStateOf(false)
    }

    val oneWayDateState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                tripViewModel.departureDateMillis
        )

    val roundTripDateState =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis =
                tripViewModel.departureDateMillis,
            initialSelectedEndDateMillis =
                tripViewModel.returnDateMillis
        )

    /*
     * Starting Point live search.
     */
    LaunchedEffect(
        tripViewModel.startingPoint
    ) {

        if (
            tripViewModel.startingPoint.length < 3 ||
            tripViewModel.verifiedStartingPoint != null
        ) {
            startingSuggestions =
                emptyList()

            return@LaunchedEffect
        }

        delay(700)

        startingSuggestions =
            placeSearchClient.search(
                tripViewModel.startingPoint
            )
    }

    /*
     * Destination live search.
     */
    LaunchedEffect(
        tripViewModel.destination
    ) {

        if (
            tripViewModel.destination.length < 3 ||
            tripViewModel.verifiedDestination != null
        ) {
            destinationSuggestions =
                emptyList()

            return@LaunchedEffect
        }

        delay(700)

        destinationSuggestions =
            placeSearchClient.search(
                tripViewModel.destination
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGreen)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            /*
             * Header
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGreen)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBackClick
                ) {

                    Text(
                        text = "← Back",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = "PLAN YOUR TRIP",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier =
                        Modifier.padding(
                            start = 16.dp
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(20.dp)
            ) {

                /*
                 * Round Trip / One Way
                 */
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    Button(
                        onClick = {
                            tripViewModel
                                .updateRoundTrip(
                                    true
                                )
                        },
                        modifier =
                            Modifier.weight(1f),
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        if (
                                            tripViewModel
                                                .isRoundTrip
                                        ) {
                                            darkGreen
                                        } else {
                                            Color.LightGray
                                        }
                                )
                    ) {

                        Text(
                            "Round Trip"
                        )
                    }

                    Button(
                        onClick = {
                            tripViewModel
                                .updateRoundTrip(
                                    false
                                )
                        },
                        modifier =
                            Modifier.weight(1f),
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        if (
                                            !tripViewModel
                                                .isRoundTrip
                                        ) {
                                            darkGreen
                                        } else {
                                            Color.LightGray
                                        }
                                )
                    ) {

                        Text(
                            "One Way"
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )

                /*
                 * Starting Point
                 */
                Text(
                    "STARTING POINT"
                )

                LocationAutocompleteField(
                    value =
                        tripViewModel.startingPoint,

                    onValueChange = {
                        tripViewModel
                            .updateStartingPoint(
                                it
                            )
                    },

                    suggestions =
                        startingSuggestions,

                    onSuggestionClick = {
                        pendingStartingPoint =
                            it
                    },

                    placeholder =
                        "Enter starting point"
                )

                if (
                    tripViewModel
                        .verifiedStartingPoint != null
                ) {

                    Text(
                        text =
                            "✓ Location verified",
                        color =
                            darkGreen
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                /*
                 * Destination
                 */
                Text(
                    "DESTINATION"
                )

                LocationAutocompleteField(
                    value =
                        tripViewModel.destination,

                    onValueChange = {
                        tripViewModel
                            .updateDestination(
                                it
                            )
                    },

                    suggestions =
                        destinationSuggestions,

                    onSuggestionClick = {
                        pendingDestination =
                            it
                    },

                    placeholder =
                        "Enter destination"
                )

                if (
                    tripViewModel
                        .verifiedDestination != null
                ) {

                    Text(
                        text =
                            "✓ Location verified",
                        color =
                            darkGreen
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                /*
                 * Dates
                 */
                Text(
                    text =
                        if (
                            tripViewModel
                                .isRoundTrip
                        ) {
                            "TRIP DATES"
                        } else {
                            "TRIP DATE"
                        }
                )

                OutlinedButton(
                    onClick = {
                        showCalendar =
                            true
                    },

                    modifier =
                        Modifier.fillMaxWidth()
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
                                if (
                                    tripViewModel
                                        .isRoundTrip
                                ) {

                                    val range =
                                        formatDateRange(
                                            roundTripDateState
                                                .selectedStartDateMillis,

                                            roundTripDateState
                                                .selectedEndDateMillis
                                        )

                                    if (
                                        range.isBlank()
                                    ) {
                                        "Select departure and return dates"
                                    } else {
                                        range
                                    }

                                } else {

                                    val date =
                                        formatDate(
                                            oneWayDateState
                                                .selectedDateMillis
                                        )

                                    if (
                                        date.isBlank()
                                    ) {
                                        "Select trip date"
                                    } else {
                                        date
                                    }
                                }
                        )

                        Text(
                            text =
                                "📅",
                            fontSize =
                                20.sp
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                /*
                 * Notes
                 */
                Text(
                    "NOTES"
                )

                OutlinedTextField(
                    value =
                        tripViewModel.notes,

                    onValueChange = {
                        tripViewModel
                            .updateNotes(
                                it
                            )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            120.dp
                        ),

                    placeholder = {
                        Text(
                            "Add notes about your trip"
                        )
                    }
                )

                tripViewModel
                    .saveError
                    ?.let { error ->

                        Spacer(
                            modifier =
                                Modifier.height(
                                    10.dp
                                )
                        )

                        Text(
                            text =
                                error,
                            color =
                                MaterialErrorColor
                        )
                    }

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )

                /*
                 * Save
                 */
                Button(
                    onClick = {

                        if (
                            tripViewModel
                                .isRoundTrip
                        ) {

                            tripViewModel
                                .updateDates(
                                    departureMillis =
                                        roundTripDateState
                                            .selectedStartDateMillis,

                                    returnMillis =
                                        roundTripDateState
                                            .selectedEndDateMillis
                                )

                        } else {

                            tripViewModel
                                .updateDates(
                                    departureMillis =
                                        oneWayDateState
                                            .selectedDateMillis,

                                    returnMillis =
                                        null
                                )
                        }

                        val saved =
                            tripViewModel
                                .saveTrip()

                        if (saved) {
                            onSaveClick()
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    darkGreen
                            )
                ) {

                    Text(
                        if (
                            tripViewModel
                                .savedTrip == null
                        ) {
                            "SAVE TRIP"
                        } else {
                            "UPDATE TRIP"
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )
            }
        }
    }

    /*
     * Starting Point verification dialog.
     */
    pendingStartingPoint
        ?.let { suggestion ->

            LocationVerificationDialog(
                title =
                    "Verify Starting Location",

                suggestion =
                    suggestion,

                onConfirm = {

                    tripViewModel
                        .verifyStartingPoint(
                            suggestion
                        )

                    startingSuggestions =
                        emptyList()

                    pendingStartingPoint =
                        null
                },

                onCancel = {
                    pendingStartingPoint =
                        null
                }
            )
        }

    /*
     * Destination verification dialog.
     */
    pendingDestination
        ?.let { suggestion ->

            LocationVerificationDialog(
                title =
                    "Verify Destination",

                suggestion =
                    suggestion,

                onConfirm = {

                    tripViewModel
                        .verifyDestination(
                            suggestion
                        )

                    destinationSuggestions =
                        emptyList()

                    pendingDestination =
                        null
                },

                onCancel = {
                    pendingDestination =
                        null
                }
            )
        }

    /*
     * Calendar
     */
    if (showCalendar) {

        if (
            tripViewModel
                .isRoundTrip
        ) {

            DatePickerDialog(
                onDismissRequest = {
                    showCalendar =
                        false
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            showCalendar =
                                false
                        }
                    ) {

                        Text(
                            "Done"
                        )
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showCalendar =
                                false
                        }
                    ) {

                        Text(
                            "Cancel"
                        )
                    }
                }
            ) {

                DateRangePicker(
                    state =
                        roundTripDateState,

                    modifier =
                        Modifier.height(
                            500.dp
                        )
                )
            }

        } else {

            DatePickerDialog(
                onDismissRequest = {
                    showCalendar =
                        false
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            showCalendar =
                                false
                        }
                    ) {

                        Text(
                            "Done"
                        )
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showCalendar =
                                false
                        }
                    ) {

                        Text(
                            "Cancel"
                        )
                    }
                }
            ) {

                DatePicker(
                    state =
                        oneWayDateState
                )
            }
        }
    }
}

@Composable
private fun LocationAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<PlaceSuggestion>,
    onSuggestionClick:
        (PlaceSuggestion) -> Unit,
    placeholder: String
) {

    Column {

        OutlinedTextField(
            value =
                value,

            onValueChange =
                onValueChange,

            modifier =
                Modifier.fillMaxWidth(),

            placeholder = {
                Text(
                    placeholder
                )
            }
        )

        suggestions
            .take(4)
            .forEach { suggestion ->

                Text(
                    text =
                        suggestion.name,

                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            onSuggestionClick(
                                suggestion
                            )
                        }
                        .padding(
                            12.dp
                        )
                )
            }
    }
}

@Composable
private fun LocationVerificationDialog(
    title: String,
    suggestion: PlaceSuggestion,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onCancel,

        title = {
            Text(
                title
            )
        },

        text = {
            Column {

                Text(
                    "Is this the correct location?"
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Text(
                    text =
                        suggestion.name
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onConfirm
            ) {

                Text(
                    "Use Location"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onCancel
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}

private fun formatDate(
    dateMillis: Long?
): String {

    if (
        dateMillis == null
    ) {
        return ""
    }

    val formatter =
        SimpleDateFormat(
            "MMM d, yyyy",
            Locale.getDefault()
        )

    return formatter.format(
        Date(
            dateMillis
        )
    )
}

private fun formatDateRange(
    startMillis: Long?,
    endMillis: Long?
): String {

    if (
        startMillis == null
    ) {
        return ""
    }

    if (
        endMillis == null
    ) {
        return formatDate(
            startMillis
        )
    }

    return "${formatDate(startMillis)} - ${formatDate(endMillis)}"
}

private val MaterialErrorColor =
    Color(0xFFB3261E)