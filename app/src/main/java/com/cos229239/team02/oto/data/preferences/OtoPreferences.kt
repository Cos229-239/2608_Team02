package com.cos229239.team02.oto.data.preferences

import android.content.Context

//Define the distance units OTO can display.
enum class DistanceUnit {
    MILES,
    KILOMETERS
}

//Store small app-wide OTO preferences locally on the device.
class OtoPreferences(
    context: Context
) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    //Return the saved distance unit.
    //Miles remains the default so current app behavior does not change.
    fun getDistanceUnit(): DistanceUnit {

        val savedValue =
            preferences.getString(
                KEY_DISTANCE_UNIT,
                DistanceUnit.MILES.name
            )

        return runCatching {
            DistanceUnit.valueOf(
                savedValue ?: DistanceUnit.MILES.name
            )
        }.getOrDefault(
            DistanceUnit.MILES
        )
    }

    //Save the user's preferred distance unit.
    fun setDistanceUnit(
        distanceUnit: DistanceUnit
    ) {

        preferences
            .edit()
            .putString(
                KEY_DISTANCE_UNIT,
                distanceUnit.name
            )
            .apply()
    }

    companion object {

        private const val PREFERENCES_NAME =
            "oto_app_preferences"

        private const val KEY_DISTANCE_UNIT =
            "distance_unit"
    }
}