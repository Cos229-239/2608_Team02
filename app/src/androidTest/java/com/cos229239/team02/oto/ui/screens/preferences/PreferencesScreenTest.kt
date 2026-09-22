package com.cos229239.team02.oto.ui.screens.preferences

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.cos229239.team02.oto.ui.preferences.AppPreferencesViewModel
import com.cos229239.team02.oto.ui.theme.OTOTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

//Test the OTO Preferences screen UI.
class PreferencesScreenTest {

    @get:Rule
    val composeTestRule =
        createComposeRule()

    private lateinit var application:
            Application

    private lateinit var appPreferencesViewModel:
            AppPreferencesViewModel

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

        appPreferencesViewModel =
            AppPreferencesViewModel(
                application
            )
    }

    //Check that the distance-unit choices appear on screen.
    @Test
    fun distanceUnitOptionsAreDisplayed() {

        composeTestRule.setContent {

            OTOTheme {

                PreferencesScreen(
                    appPreferencesViewModel =
                        appPreferencesViewModel,
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                "DISTANCE UNITS"
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Miles"
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Kilometers"
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Use miles and feet."
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Use kilometers and meters."
            )
            .assertIsDisplayed()
    }

    //Check that selecting kilometers changes the selected distance unit.
    @Test
    fun selectingKilometersChangesSelection() {

        composeTestRule.setContent {

            OTOTheme {

                PreferencesScreen(
                    appPreferencesViewModel =
                        appPreferencesViewModel,
                    onBackClick = {}
                )
            }
        }

        val milesOption =
            composeTestRule.onNodeWithTag(
                "distance_unit_miles"
            )

        val kilometersOption =
            composeTestRule.onNodeWithTag(
                "distance_unit_kilometers"
            )

        //Miles should be selected by default.
        milesOption
            .assertIsSelected()

        kilometersOption
            .assertIsNotSelected()

        //Select kilometers.
        kilometersOption
            .performClick()

        //Kilometers should now be selected.
        milesOption
            .assertIsNotSelected()

        kilometersOption
            .assertIsSelected()
    }
}