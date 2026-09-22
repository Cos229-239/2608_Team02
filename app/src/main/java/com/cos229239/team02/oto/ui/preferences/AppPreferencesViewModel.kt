package com.cos229239.team02.oto.ui.preferences

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.cos229239.team02.oto.data.preferences.DistanceUnit
import com.cos229239.team02.oto.data.preferences.OtoPreferences

//Manage app-wide OTO preferences for the UI.
class AppPreferencesViewModel(
    application: Application
) : AndroidViewModel(application) {

    //Use the shared local preferences storage.
    private val otoPreferences =
        OtoPreferences(
            application.applicationContext
        )

    //Load the saved distance unit when the ViewModel starts.
    var distanceUnit by mutableStateOf(
        otoPreferences.getDistanceUnit()
    )
        private set

    //Save and immediately update the selected distance unit.
    fun updateDistanceUnit(
        newDistanceUnit: DistanceUnit
    ) {

        otoPreferences.setDistanceUnit(
            newDistanceUnit
        )

        distanceUnit =
            newDistanceUnit
    }
}