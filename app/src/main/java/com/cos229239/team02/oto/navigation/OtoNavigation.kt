package com.cos229239.team02.oto.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.ui.screens.crisis.CrisisScreen
import com.cos229239.team02.oto.ui.screens.crisis.EmergencyHelpScreen
import com.cos229239.team02.oto.ui.screens.crisis.FirstAidSurvivalScreen
import com.cos229239.team02.oto.ui.screens.crisis.NearbyResourceScreen
import com.cos229239.team02.oto.ui.screens.crisis.ShareStatusLocationScreen
import com.cos229239.team02.oto.ui.screens.explorer.AreaSafetyRoute
import com.cos229239.team02.oto.ui.screens.explorer.ExplorerScreen
import com.cos229239.team02.oto.ui.screens.explorer.PlanTripScreen
import com.cos229239.team02.oto.ui.screens.home.HomeScreen

@Composable
fun OtoNavigation() {

    val backStack =
        rememberNavBackStack(
            OtoRoute.Home
        )

    // Shared Plan Trip ViewModel.
    // This keeps trip data available while navigating
    // and reloads the saved trip from local storage.
    val planTripViewModel:
        PlanTripViewModel =
        viewModel()

    NavDisplay(
        backStack = backStack,

        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },

        entryProvider =
            entryProvider {

                /*
                 * HOME
                 */
                entry<OtoRoute.Home> {

                    HomeScreen(
                        onExplorerClick = {
                            backStack.add(
                                OtoRoute.Explorer
                            )
                        },

                        onCrisisClick = {
                            backStack.add(
                                OtoRoute.Crisis
                            )
                        }
                    )
                }

                /*
                 * EXPLORER
                 */
                entry<OtoRoute.Explorer> {

                    ExplorerScreen(
                        onAreaSafetyClick = {
                            backStack.add(
                                OtoRoute.AreaSafety
                            )
                        },

                        onPlanTripClick = {
                            backStack.add(
                                OtoRoute.PlanTrip
                            )
                        },

                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * PLAN TRIP
                 */
                entry<OtoRoute.PlanTrip> {

                    PlanTripScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },

                        onSaveClick = {
                            backStack.removeLastOrNull()
                        },

                        tripViewModel =
                            planTripViewModel
                    )
                }

                /*
                 * AREA SAFETY
                 */
                entry<OtoRoute.AreaSafety> {

                    AreaSafetyRoute(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * CRISIS MODE
                 */
                entry<OtoRoute.Crisis> {

                    CrisisScreen(
                        onEmergencyHelpClick = {
                            backStack.add(
                                OtoRoute.EmergencyHelp
                            )
                        },

                        onFirstAidSurvivalClick = {
                            backStack.add(
                                OtoRoute.FirstAidSurvival
                            )
                        },

                        onShareStatusLocationClick = {
                            backStack.add(
                                OtoRoute.ShareStatusLocation
                            )
                        },

                        onNearbyResourcesClick = {
                            backStack.add(
                                OtoRoute.NearbyResources
                            )
                        },

                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * CRISIS - EMERGENCY HELP
                 */
                entry<OtoRoute.EmergencyHelp> {

                    EmergencyHelpScreen(
                        onShareStatusLocationClick = {
                            backStack.add(
                                OtoRoute.ShareStatusLocation
                            )
                        },

                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * CRISIS - SHARE STATUS / LOCATION
                 */
                entry<OtoRoute.ShareStatusLocation> {

                    ShareStatusLocationScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * CRISIS - FIRST AID / SURVIVAL
                 */
                entry<OtoRoute.FirstAidSurvival> {

                    FirstAidSurvivalScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * CRISIS - NEARBY RESOURCES
                 */
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