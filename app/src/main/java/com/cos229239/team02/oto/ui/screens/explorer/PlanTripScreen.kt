package com.cos229239.team02.oto.ui.screens.explorer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cos229239.team02.oto.data.location.PlaceSearchClient
import com.cos229239.team02.oto.data.location.PlaceSuggestion
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
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

    val context = LocalContext.current

    val darkGreen =
        androidx.compose.ui.graphics.Color(
            0xFF0B5D1E
        )

    val dangerRed =
        MaterialTheme.colorScheme.error

    val screenBackground =
        MaterialTheme.colorScheme.background

    val primaryText =
        MaterialTheme.colorScheme.onBackground

    val secondaryText =
        MaterialTheme.colorScheme.onSurfaceVariant

    val unselectedButtonColor =
        MaterialTheme.colorScheme.surfaceVariant

    val unselectedButtonTextColor =
        MaterialTheme.colorScheme.onSurfaceVariant

    val placeSearchClient = remember {
        PlaceSearchClient()
    }

    var startingSuggestions by remember {
        mutableStateOf<List<PlaceSuggestion>>(
            emptyList()
        )
    }

    var destinationSuggestions by remember {
        mutableStateOf<List<PlaceSuggestion>>(
            emptyList()
        )
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

    /*
     * Controls the Add / Edit Trusted Contact dialog.
     */
    var showTrustedContactDialog by remember {
        mutableStateOf(false)
    }

    /*
     * Temporary Trusted Contact values.
     *
     * These are copied into the ViewModel when
     * the user presses Save Contact.
     */
    var trustedContactNameInput by remember {
        mutableStateOf("")
    }

    var trustedContactPhoneInput by remember {
        mutableStateOf("")
    }

    /*
     * Displays an error if the device cannot open
     * the Android contact picker.
     */
    var contactPickerError by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * Opens Android's contact picker.
     *
     * We request a specific phone-number contact
     * instead of reading the user's entire address book.
     */
    val contactPickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .StartActivityForResult()
        ) { result ->

            if (
                result.resultCode ==
                Activity.RESULT_OK
            ) {

                val contactUri =
                    result.data?.data

                if (contactUri != null) {

                    val projection =
                        arrayOf(
                            ContactsContract
                                .CommonDataKinds
                                .Phone
                                .DISPLAY_NAME,

                            ContactsContract
                                .CommonDataKinds
                                .Phone
                                .NUMBER
                        )

                    context.contentResolver
                        .query(
                            contactUri,
                            projection,
                            null,
                            null,
                            null
                        )
                        ?.use { cursor ->

                            if (
                                cursor.moveToFirst()
                            ) {

                                val nameIndex =
                                    cursor.getColumnIndex(
                                        ContactsContract
                                            .CommonDataKinds
                                            .Phone
                                            .DISPLAY_NAME
                                    )

                                val phoneIndex =
                                    cursor.getColumnIndex(
                                        ContactsContract
                                            .CommonDataKinds
                                            .Phone
                                            .NUMBER
                                    )

                                if (
                                    nameIndex >= 0
                                ) {
                                    trustedContactNameInput =
                                        cursor.getString(
                                            nameIndex
                                        )
                                            .orEmpty()
                                }

                                if (
                                    phoneIndex >= 0
                                ) {
                                    trustedContactPhoneInput =
                                        cursor.getString(
                                            phoneIndex
                                        )
                                            .orEmpty()
                                }

                                contactPickerError =
                                    null
                            }
                        }
                }
            }
        }

    /*
     * Opens the Android contact picker.
     */
    fun openContactPicker() {

        try {

            contactPickerError =
                null

            val contactIntent =
                Intent(
                    Intent.ACTION_PICK,
                    ContactsContract
                        .CommonDataKinds
                        .Phone
                        .CONTENT_URI
                )

            contactPickerLauncher
                .launch(
                    contactIntent
                )

        } catch (
            exception:
            ActivityNotFoundException
        ) {

            contactPickerError =
                "Contacts are not available on this device."

        } catch (
            exception: Exception
        ) {

            contactPickerError =
                "Unable to open contacts."
        }
    }

    // Controls the confirmation box before deleting a saved trip.
    var showClearTripDialog by remember {
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
            .background(
                darkGreen
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            /*
             * Header
             */
            OtoTopAppBar(
                title =
                    "PLAN YOUR TRIP",

                onBackClick =
                    onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        screenBackground
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        20.dp
                    )
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
                            Modifier.weight(
                                1f
                            ),

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
                                            unselectedButtonColor
                                        },

                                    contentColor =
                                        if (
                                            tripViewModel
                                                .isRoundTrip
                                        ) {
                                            androidx.compose.ui.graphics.Color.White
                                        } else {
                                            unselectedButtonTextColor
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
                            Modifier.weight(
                                1f
                            ),

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
                                            unselectedButtonColor
                                        },

                                    contentColor =
                                        if (
                                            !tripViewModel
                                                .isRoundTrip
                                        ) {
                                            androidx.compose.ui.graphics.Color.White
                                        } else {
                                            unselectedButtonTextColor
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
                    text =
                        "STARTING POINT",

                    color =
                        primaryText
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
                    text =
                        "DESTINATION",

                    color =
                        primaryText
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
                        },

                    color =
                        primaryText
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
                            20.dp
                        )
                )

                /*
                 * Trusted Contact
                 *
                 * The contact is optional.
                 * A trip can still be saved without one.
                 */
                Text(
                    text =
                        "TRUSTED CONTACT",

                    color =
                        primaryText
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                if (
                    tripViewModel
                        .trustedContactName
                        .isBlank() &&
                    tripViewModel
                        .trustedContactPhone
                        .isBlank()
                ) {

                    Text(
                        text =
                            "No trusted contact added.",

                        color =
                            secondaryText
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    OutlinedButton(
                        onClick = {

                            trustedContactNameInput =
                                ""

                            trustedContactPhoneInput =
                                ""

                            contactPickerError =
                                null

                            showTrustedContactDialog =
                                true
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            "ADD TRUSTED CONTACT"
                        )
                    }

                } else {

                    Text(
                        text =
                            tripViewModel
                                .trustedContactName,

                        color =
                            primaryText
                    )

                    Text(
                        text =
                            tripViewModel
                                .trustedContactPhone,

                        color =
                            secondaryText
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {

                        OutlinedButton(
                            onClick = {

                                trustedContactNameInput =
                                    tripViewModel
                                        .trustedContactName

                                trustedContactPhoneInput =
                                    tripViewModel
                                        .trustedContactPhone

                                contactPickerError =
                                    null

                                showTrustedContactDialog =
                                    true
                            },

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Text(
                                text =
                                    "EDIT",

                                color =
                                    darkGreen
                            )
                        }

                        OutlinedButton(
                            onClick = {

                                tripViewModel
                                    .clearTrustedContact()
                            },

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Text(
                                text =
                                    "REMOVE",

                                color =
                                    dangerRed
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )

                /*
                 * Notes
                 */
                Text(
                    text =
                        "NOTES",

                    color =
                        primaryText
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

                    textStyle =
                        TextStyle(
                            color =
                                primaryText
                        ),

                    placeholder = {

                        Text(
                            text =
                                "Add notes about your trip",

                            color =
                                secondaryText
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
                                dangerRed
                        )
                    }

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )

                /*
                 * Save / Update Trip
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
                                    darkGreen,

                                contentColor =
                                    androidx.compose.ui.graphics.Color.White
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

                /*
                 * Clear Trip only appears when
                 * a saved trip currently exists.
                 */
                if (
                    tripViewModel.savedTrip != null
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    TextButton(
                        onClick = {

                            showClearTripDialog =
                                true
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "CLEAR TRIP",

                            color =
                                dangerRed
                        )
                    }
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
     * Add / Edit Trusted Contact dialog.
     */
    if (
        showTrustedContactDialog
    ) {

        AlertDialog(
            onDismissRequest = {

                showTrustedContactDialog =
                    false
            },

            title = {

                Text(
                    text =
                        if (
                            tripViewModel
                                .trustedContactName
                                .isBlank()
                        ) {
                            "Add Trusted Contact"
                        } else {
                            "Edit Trusted Contact"
                        }
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "This contact will receive your trip check-ins.",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    /*
                     * Allows the user to select someone
                     * already saved in their phone.
                     */
                    OutlinedButton(
                        onClick = {
                            openContactPicker()
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            "CHOOSE FROM CONTACTS"
                        )
                    }

                    Text(
                        text =
                            "or",

                        modifier =
                            Modifier
                                .align(
                                    Alignment.CenterHorizontally
                                )
                                .padding(
                                    vertical =
                                        8.dp
                                ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    OutlinedTextField(
                        value =
                            trustedContactNameInput,

                        onValueChange = {

                            trustedContactNameInput =
                                it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                "Name"
                            )
                        },

                        placeholder = {

                            Text(
                                "Trusted contact name"
                            )
                        },

                        singleLine =
                            true
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                10.dp
                            )
                    )

                    OutlinedTextField(
                        value =
                            trustedContactPhoneInput,

                        onValueChange = {

                            trustedContactPhoneInput =
                                it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                "Phone Number"
                            )
                        },

                        placeholder = {

                            Text(
                                "Phone number"
                            )
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Phone
                            ),

                        singleLine =
                            true
                    )

                    contactPickerError
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
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )
                        }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        tripViewModel
                            .updateTrustedContactName(
                                trustedContactNameInput
                                    .trim()
                            )

                        tripViewModel
                            .updateTrustedContactPhone(
                                trustedContactPhoneInput
                                    .trim()
                            )

                        showTrustedContactDialog =
                            false
                    },

                    enabled =
                        trustedContactNameInput
                            .isNotBlank() &&
                                trustedContactPhoneInput
                                    .isNotBlank()
                ) {

                    Text(
                        "Save Contact"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showTrustedContactDialog =
                            false
                    }
                ) {

                    Text(
                        "Cancel"
                    )
                }
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

    /*
     * Confirm Clear Trip.
     */
    if (
        showClearTripDialog
    ) {

        AlertDialog(
            onDismissRequest = {

                showClearTripDialog =
                    false
            },

            title = {

                Text(
                    "Clear Saved Trip?"
                )
            },

            text = {

                Text(
                    "This will permanently delete the saved trip and clear all Plan Trip information."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        tripViewModel
                            .clearTrip()

                        showClearTripDialog =
                            false

                        onBackClick()
                    }
                ) {

                    Text(
                        text =
                            "Clear Trip",

                        color =
                            dangerRed
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showClearTripDialog =
                            false
                    }
                ) {

                    Text(
                        "Keep Trip"
                    )
                }
            }
        )
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

            textStyle =
                TextStyle(
                    color =
                        MaterialTheme
                            .colorScheme
                            .onBackground
                ),

            placeholder = {

                Text(
                    text =
                        placeholder,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        )

        suggestions
            .take(4)
            .forEach { suggestion ->

                Text(
                    text =
                        suggestion.name,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onBackground,

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