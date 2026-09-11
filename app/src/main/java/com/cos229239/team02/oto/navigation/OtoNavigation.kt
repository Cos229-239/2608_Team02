package com.cos229239.team02.oto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.AreaSafetyViewFactory
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.ui.screens.crisis.CrisisScreen
import com.cos229239.team02.oto.ui.screens.crisis.EmergencyHelpScreen
import com.cos229239.team02.oto.ui.screens.crisis.FirstAidSurvivalScreen
import com.cos229239.team02.oto.ui.screens.crisis.NearbyResourceScreen
import com.cos229239.team02.oto.ui.screens.crisis.OfflineMapBacktrackScreen
import com.cos229239.team02.oto.ui.screens.crisis.ShareStatusLocationScreen
import com.cos229239.team02.oto.ui.screens.explorer.AreaSafetyRoute
import com.cos229239.team02.oto.ui.screens.explorer.ExplorerScreen
import com.cos229239.team02.oto.ui.screens.explorer.PlanTripScreen
import com.cos229239.team02.oto.ui.screens.explorer.WeatherReportScreen
import com.cos229239.team02.oto.ui.screens.home.HomeScreen

@Composable
fun OtoNavigation() {

    val backStack =
        rememberNavBackStack(
            OtoRoute.Home
        )

    /*
     * One shared Plan Trip ViewModel.
     *
     * Plan Trip writes to it.
     * Explorer reads from it.
     */
    val planTripViewModel:
            PlanTripViewModel =
        viewModel()
    val context = LocalContext.current.applicationContext

    val safetyFactory = remember(context) {
        AreaSafetyViewFactory(
            context = context
        )
    }

    val safetyView: AreaSafetyView = viewModel(
        factory = safetyFactory
    )
    NavDisplay(
        backStack =
            backStack,

        onBack = {

            if (
                backStack.size > 1
            ) {

                backStack.removeLastOrNull()
            }
        },

        entryProvider =
            entryProvider {

                /*
                 * -------------------------------------------------
                 * HOME
                 * -------------------------------------------------
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
                 * -------------------------------------------------
                 * EXPLORER
                 * -------------------------------------------------
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

                        /*
                         * Opens the new Weather Report screen.
                         */
                        onWeatherClick = {

                            backStack.add(
                                OtoRoute.WeatherReport
                            )
                        },

                        onBackClick = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },

                        tripViewModel = planTripViewModel,
                        safetyView = safetyView
                    )
                }

                /*
                 * -------------------------------------------------
                 * PLAN TRIP
                 * -------------------------------------------------
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
                 * -------------------------------------------------
                 * WEATHER REPORT
                 * -------------------------------------------------
                 */

                entry<OtoRoute.WeatherReport> {

                    WeatherReportScreen(
                        onBackClick = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                        safetyView = safetyView

                    )
                }

                /*
                 * -------------------------------------------------
                 * AREA SAFETY
                 * -------------------------------------------------
                 */

                entry<OtoRoute.AreaSafety> {

                    AreaSafetyRoute(
                        onBackClick = {
                            if (backStack.size > 1) {

                                backStack.removeLastOrNull()
                            }
                        },
                        safetyView = safetyView
                    )
                }

                /*
                 * -------------------------------------------------
                 * CRISIS MODE
                 * -------------------------------------------------
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

                        onOfflineMapBacktrackClick = {

                            backStack.add(
                                OtoRoute.OfflineMapBacktrack
                            )
                        },

                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * -------------------------------------------------
                 * CRISIS - EMERGENCY HELP
                 * -------------------------------------------------
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
                 * -------------------------------------------------
                 * CRISIS - SHARE STATUS / LOCATION
                 * -------------------------------------------------
                 */

                entry<OtoRoute.ShareStatusLocation> {

                    ShareStatusLocationScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * -------------------------------------------------
                 * CRISIS - FIRST AID / SURVIVAL
                 * -------------------------------------------------
                 */

                entry<OtoRoute.FirstAidSurvival> {

                    FirstAidSurvivalScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * -------------------------------------------------
                 * CRISIS - NEARBY RESOURCES
                 * -------------------------------------------------
                 */

                entry<OtoRoute.NearbyResources> {

                    NearbyResourceScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * -------------------------------------------------
                 * CRISIS - OFFLINE MAPS & BACKTRACK
                 * -------------------------------------------------
                 */

                entry<OtoRoute.OfflineMapBacktrack> {

                    OfflineMapBacktrackScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
    )
}