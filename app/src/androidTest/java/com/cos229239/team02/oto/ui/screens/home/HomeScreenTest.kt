package com.cos229239.team02.oto.ui.screens.home

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.cos229239.team02.oto.ui.theme.OTOTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

//Test Home screen controls used by app preferences.
class HomeScreenTest {

    @get:Rule
    val composeTestRule =
        createComposeRule()

    //Check that the Preferences control is accessible and clickable.
    @Test
    fun preferencesControlIsAccessibleAndClickable() {

        var preferencesClicked =
            false

        composeTestRule.setContent {

            OTOTheme {

                HomeScreen(
                    onExplorerClick = {},
                    onCrisisClick = {},
                    onOfflineToolsClick = {},
                    onPreferencesClick = {
                        preferencesClicked =
                            true
                    }
                )
            }
        }

        val preferencesButton =
            composeTestRule
                .onNodeWithContentDescription(
                    "Preferences"
                )

        //Check that TalkBack can identify the Preferences control.
        preferencesButton
            .assertContentDescriptionEquals(
                "Preferences"
            )

        //Check that the Preferences control calls its navigation callback.
        preferencesButton
            .performClick()

        composeTestRule.runOnIdle {

            assertTrue(
                preferencesClicked
            )
        }
    }
}