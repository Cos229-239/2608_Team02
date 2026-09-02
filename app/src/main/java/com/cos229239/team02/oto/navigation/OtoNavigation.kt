package com.cos229239.team02.oto.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.ui.screens.crisis.CrisisScreen
import com.cos229239.team02.oto.ui.screens.crisis.FirstAidSurvivalScreen
import com.cos229239.team02.oto.ui.screens.crisis.NearbyResourceScreen
import com.cos229239.team02.oto.ui.screens.explorer.AreaSafetyRoute
import com.cos229239.team02.oto.ui.screens.crisis.ShareStatusLocationScreen
import com.cos229239.team02.oto.ui.screens.home.HomeScreen
import com.cos229239.team02.oto.ui.screens.explorer.ExplorerScreen
import com.cos229239.team02.oto.ui.screens.crisis.EmergencyHelpScreen



@Composable
fun OtoNavigation() {
    val backStack = rememberNavBackStack(OtoRoute.Home)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {

            entry<OtoRoute.Home> {
                HomeScreen(
                    onExplorerClick = {
                        backStack.add(OtoRoute.Explorer)
                    },
                    onCrisisClick = {
                        backStack.add(OtoRoute.Crisis)
                    }
                )
            }

            entry<OtoRoute.Explorer> {
                ExplorerScreen(
                    onAreaSafetyClick = {
                        backStack.add(OtoRoute.AreaSafety)
                    },
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<OtoRoute.AreaSafety> {
                AreaSafetyRoute(
                onBackClick = {
                    backStack.removeLastOrNull()
                }

                )
            }

            entry<OtoRoute.Crisis> {
                CrisisScreen(
                    //When Emergency Help is tapped on the Crisis screen,add the Emergency Help destination to the navigation stack.//
                    onEmergencyHelpClick = {
                        backStack.add(OtoRoute.EmergencyHelp)
                    },
                    onFirstAidSurvivalClick = {
                        backStack.add(OtoRoute.FirstAidSurvival)
                    },
                    onShareStatusLocationClick = {
                        backStack.add(OtoRoute.ShareStatusLocation)
                    },
                    onNearbyResourcesClick = {
                        backStack.add(OtoRoute.NearbyResources)
                    },
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }
    //This tells Android what screen to show when the Emergency Help route is added to the navigation stack.//
            entry<OtoRoute.EmergencyHelp> {

                //Open the Emergency Help screen.
                EmergencyHelpScreen(

                    //When the user taps Back, remove this screen from the navigation stack and return to Crisis Mode.//
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }





            entry<OtoRoute.ShareStatusLocation> {
                ShareStatusLocationScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<OtoRoute.FirstAidSurvival> {
                FirstAidSurvivalScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<OtoRoute.NearbyResources> {
                NearbyResourceScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}

@Composable
private fun ModePlaceholder(
    title: String,
    backStack: NavBackStack<NavKey>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title)

        Button(
            onClick = {
                backStack.removeLastOrNull()
            }
        ) {
            Text(text = "Back")
        }
    }
}