package com.cos229239.team02.oto.ui.features

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.cos229239.team02.oto.data.location.PlaceSuggestion
import com.cos229239.team02.oto.data.trip.TripPlan

/**
 * Holds Plan Trip information and saves completed trips locally.
 */
class PlanTripViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferences =
        application.getSharedPreferences(
            "oto_trip_plan",
            Application.MODE_PRIVATE
        )

    var startingPoint by mutableStateOf("")
        private set

    var destination by mutableStateOf("")
        private set

    var notes by mutableStateOf("")
        private set

    var isRoundTrip by mutableStateOf(true)
        private set

    var verifiedStartingPoint by mutableStateOf<PlaceSuggestion?>(null)
        private set

    var verifiedDestination by mutableStateOf<PlaceSuggestion?>(null)
        private set

    var departureDateMillis by mutableStateOf<Long?>(null)
        private set

    var returnDateMillis by mutableStateOf<Long?>(null)
        private set

    var saveError by mutableStateOf<String?>(null)
        private set

    var savedTrip by mutableStateOf<TripPlan?>(null)
        private set

    init {
        loadSavedTrip()
    }

    fun updateStartingPoint(value: String) {
        startingPoint = value

        if (verifiedStartingPoint?.name != value) {
            verifiedStartingPoint = null
        }

        saveError = null
    }

    fun updateDestination(value: String) {
        destination = value

        if (verifiedDestination?.name != value) {
            verifiedDestination = null
        }

        saveError = null
    }

    fun updateNotes(value: String) {
        notes = value
    }

    fun updateRoundTrip(value: Boolean) {
        isRoundTrip = value

        if (!value) {
            returnDateMillis = null
        }
    }

    fun verifyStartingPoint(
        suggestion: PlaceSuggestion
    ) {
        startingPoint = suggestion.name
        verifiedStartingPoint = suggestion
        saveError = null
    }

    fun verifyDestination(
        suggestion: PlaceSuggestion
    ) {
        destination = suggestion.name
        verifiedDestination = suggestion
        saveError = null
    }

    fun updateDates(
        departureMillis: Long?,
        returnMillis: Long?
    ) {
        departureDateMillis = departureMillis

        returnDateMillis =
            if (isRoundTrip) {
                returnMillis
            } else {
                null
            }
    }

    /**
     * Validates and saves the active trip.
     */
    fun saveTrip(): Boolean {

        val start = verifiedStartingPoint

        if (
            start == null ||
            start.latitude == null ||
            start.longitude == null
        ) {
            saveError =
                "Please select and verify a valid starting location."

            return false
        }

        val end = verifiedDestination

        if (
            end == null ||
            end.latitude == null ||
            end.longitude == null
        ) {
            saveError =
                "Please select and verify a valid destination."

            return false
        }

        val departure =
            departureDateMillis

        if (departure == null) {
            saveError =
                "Please select a trip date."

            return false
        }

        if (
            isRoundTrip &&
            returnDateMillis == null
        ) {
            saveError =
                "Please select a return date."

            return false
        }

        val trip =
            TripPlan(
                startingPointName = start.name,
                startingLatitude = start.latitude,
                startingLongitude = start.longitude,

                destinationName = end.name,
                destinationLatitude = end.latitude,
                destinationLongitude = end.longitude,

                isRoundTrip = isRoundTrip,

                departureDateMillis = departure,
                returnDateMillis = returnDateMillis,

                notes = notes
            )

        savedTrip = trip

        preferences
            .edit()
            .putBoolean(
                KEY_HAS_TRIP,
                true
            )
            .putString(
                KEY_START_NAME,
                trip.startingPointName
            )
            .putString(
                KEY_START_LAT,
                trip.startingLatitude.toString()
            )
            .putString(
                KEY_START_LON,
                trip.startingLongitude.toString()
            )
            .putString(
                KEY_DESTINATION_NAME,
                trip.destinationName
            )
            .putString(
                KEY_DESTINATION_LAT,
                trip.destinationLatitude.toString()
            )
            .putString(
                KEY_DESTINATION_LON,
                trip.destinationLongitude.toString()
            )
            .putBoolean(
                KEY_ROUND_TRIP,
                trip.isRoundTrip
            )
            .putLong(
                KEY_DEPARTURE,
                trip.departureDateMillis
            )
            .putLong(
                KEY_RETURN,
                trip.returnDateMillis
                    ?: NO_RETURN_DATE
            )
            .putString(
                KEY_NOTES,
                trip.notes
            )
            .apply()

        saveError = null

        return true
    }

    /**
     * Permanently removes the saved trip and
     * resets Plan Trip back to a blank form.
     */
    fun clearTrip() {

        preferences
            .edit()
            .clear()
            .apply()

        savedTrip = null

        startingPoint = ""
        destination = ""
        notes = ""

        isRoundTrip = true

        verifiedStartingPoint = null
        verifiedDestination = null

        departureDateMillis = null
        returnDateMillis = null

        saveError = null
    }

    /**
     * Loads a previously saved trip.
     */
    private fun loadSavedTrip() {

        if (
            !preferences.getBoolean(
                KEY_HAS_TRIP,
                false
            )
        ) {
            return
        }

        val startName =
            preferences.getString(
                KEY_START_NAME,
                ""
            ).orEmpty()

        val startLatitude =
            preferences.getString(
                KEY_START_LAT,
                null
            )?.toDoubleOrNull()

        val startLongitude =
            preferences.getString(
                KEY_START_LON,
                null
            )?.toDoubleOrNull()

        val destinationName =
            preferences.getString(
                KEY_DESTINATION_NAME,
                ""
            ).orEmpty()

        val destinationLatitude =
            preferences.getString(
                KEY_DESTINATION_LAT,
                null
            )?.toDoubleOrNull()

        val destinationLongitude =
            preferences.getString(
                KEY_DESTINATION_LON,
                null
            )?.toDoubleOrNull()

        val roundTrip =
            preferences.getBoolean(
                KEY_ROUND_TRIP,
                true
            )

        val departure =
            preferences.getLong(
                KEY_DEPARTURE,
                0L
            )

        val returnDate =
            preferences.getLong(
                KEY_RETURN,
                NO_RETURN_DATE
            )

        val savedNotes =
            preferences.getString(
                KEY_NOTES,
                ""
            ).orEmpty()

        if (
            startLatitude == null ||
            startLongitude == null ||
            destinationLatitude == null ||
            destinationLongitude == null ||
            departure == 0L
        ) {
            return
        }

        val trip =
            TripPlan(
                startingPointName =
                    startName,

                startingLatitude =
                    startLatitude,

                startingLongitude =
                    startLongitude,

                destinationName =
                    destinationName,

                destinationLatitude =
                    destinationLatitude,

                destinationLongitude =
                    destinationLongitude,

                isRoundTrip =
                    roundTrip,

                departureDateMillis =
                    departure,

                returnDateMillis =
                    if (
                        returnDate ==
                        NO_RETURN_DATE
                    ) {
                        null
                    } else {
                        returnDate
                    },

                notes =
                    savedNotes
            )

        savedTrip = trip

        startingPoint =
            trip.startingPointName

        destination =
            trip.destinationName

        notes =
            trip.notes

        isRoundTrip =
            trip.isRoundTrip

        departureDateMillis =
            trip.departureDateMillis

        returnDateMillis =
            trip.returnDateMillis

        verifiedStartingPoint =
            PlaceSuggestion(
                name =
                    trip.startingPointName,

                latitude =
                    trip.startingLatitude,

                longitude =
                    trip.startingLongitude
            )

        verifiedDestination =
            PlaceSuggestion(
                name =
                    trip.destinationName,

                latitude =
                    trip.destinationLatitude,

                longitude =
                    trip.destinationLongitude
            )
    }

    companion object {

        private const val KEY_HAS_TRIP =
            "has_trip"

        private const val KEY_START_NAME =
            "starting_name"

        private const val KEY_START_LAT =
            "starting_lat"

        private const val KEY_START_LON =
            "starting_lon"

        private const val KEY_DESTINATION_NAME =
            "destination_name"

        private const val KEY_DESTINATION_LAT =
            "destination_lat"

        private const val KEY_DESTINATION_LON =
            "destination_lon"

        private const val KEY_ROUND_TRIP =
            "round_trip"

        private const val KEY_DEPARTURE =
            "departure_date"

        private const val KEY_RETURN =
            "return_date"

        private const val KEY_NOTES =
            "notes"

        private const val NO_RETURN_DATE =
            -1L
    }
}