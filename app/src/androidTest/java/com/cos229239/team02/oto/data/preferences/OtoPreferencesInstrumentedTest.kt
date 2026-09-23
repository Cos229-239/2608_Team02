package com.cos229239.team02.oto.data.preferences

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//Test that OTO app preferences are saved and loaded on Android.
@RunWith(AndroidJUnit4::class)
class OtoPreferencesInstrumentedTest {

    private lateinit var context: Context
    private lateinit var otoPreferences: OtoPreferences

    //Start each test with a clean preference file.
    @Before
    fun setUp() {

        context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        context
            .getSharedPreferences(
                OtoPreferences.PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .clear()
            .commit()

        otoPreferences =
            OtoPreferences(context)
    }

    //Check that miles are used when nothing has been saved yet.
    @Test
    fun defaultDistanceUnitIsMiles() {

        assertEquals(
            DistanceUnit.MILES,
            otoPreferences.getDistanceUnit()
        )
    }

    //Check that kilometers can be saved and loaded again.
    @Test
    fun savedKilometersPersist() {

        otoPreferences.setDistanceUnit(
            DistanceUnit.KILOMETERS
        )

        val reloadedPreferences =
            OtoPreferences(context)

        assertEquals(
            DistanceUnit.KILOMETERS,
            reloadedPreferences.getDistanceUnit()
        )
    }

    //Check that miles can be saved after kilometers.
    @Test
    fun savedMilesPersist() {

        otoPreferences.setDistanceUnit(
            DistanceUnit.KILOMETERS
        )

        otoPreferences.setDistanceUnit(
            DistanceUnit.MILES
        )

        val reloadedPreferences =
            OtoPreferences(context)

        assertEquals(
            DistanceUnit.MILES,
            reloadedPreferences.getDistanceUnit()
        )
    }
}