package com.cos229239.team02.oto.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import com.cos229239.team02.oto.MainActivity
import org.junit.Rule
import org.junit.Test

//Test Preferences navigation through the real OTO activity.
class PreferencesNavigationTest {

    @get:Rule
    val composeTestRule =
        createAndroidComposeRule<MainActivity>()

    //Check that Home opens Preferences and Back returns Home.
    @Test
    fun homePreferencesNavigationAndBackWorks() {

        //Confirm the app starts on Home.
        composeTestRule
            .onNodeWithText(
                "OUT IN THE OPEN"
            )
            .assertIsDisplayed()

        //Open Preferences from the Home settings control.
        composeTestRule
            .onNodeWithContentDescription(
                "Preferences"
            )
            .performClick()

        //Confirm the Preferences screen opened.
        composeTestRule
            .onNodeWithText(
                "DISTANCE UNITS"
            )
            .assertIsDisplayed()

        //Use Android Back to return to Home.
        pressBack()

        //Confirm Home is displayed again.
        composeTestRule
            .onNodeWithText(
                "OUT IN THE OPEN"
            )
            .assertIsDisplayed()
    }
}