package com.cos229239.team02.oto.ui.preferences

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cos229239.team02.oto.data.preferences.DistanceUnit
import com.cos229239.team02.oto.data.preferences.OtoPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//Test app-wide preferences state on Android.
@RunWith(AndroidJUnit4::class)
class AppPreferencesViewModelInstrumentedTest {

    private lateinit var application: Application

    //Start each test with clean app preferences.
    @Before
    fun setUp() {

        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        application =
            context.applicationContext as Application

        context
            .getSharedPreferences(
                "oto_app_preferences",
                Context.MODE_PRIVATE
            )
            .edit()
            .clear()
            .commit()
    }

    //Check that the ViewModel starts with miles by default.
    @Test
    fun defaultDistanceUnitIsMiles() {

        val viewModel =
            AppPreferencesViewModel(
                application
            )

        assertEquals(
            DistanceUnit.MILES,
            viewModel.distanceUnit
        )
    }

    //Check that changing the unit updates the UI state and saved preference.
    @Test
    fun updateDistanceUnitSavesKilometers() {

        val viewModel =
            AppPreferencesViewModel(
                application
            )

        viewModel.updateDistanceUnit(
            DistanceUnit.KILOMETERS
        )

        assertEquals(
            DistanceUnit.KILOMETERS,
            viewModel.distanceUnit
        )

        val savedPreferences =
            OtoPreferences(
                application
            )

        assertEquals(
            DistanceUnit.KILOMETERS,
            savedPreferences.getDistanceUnit()
        )
    }
}