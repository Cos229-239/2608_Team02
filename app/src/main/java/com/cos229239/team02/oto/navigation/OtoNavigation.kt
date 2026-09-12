package com.cos229239.team02.oto.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.data.hazard.HazardReportViewModel
import com.cos229239.team02.oto.ui.features.PlanTripViewModel
import com.cos229239.team02.oto.ui.screens.crisis.CrisisScreen
import com.cos229239.team02.oto.ui.screens.crisis.EmergencyHelpScreen
import com.cos229239.team02.oto.ui.screens.crisis.FirstAidSurvivalScreen
import com.cos229239.team02.oto.ui.screens.crisis.NearbyResourceScreen
import com.cos229239.team02.oto.ui.screens.crisis.OfflineMapBacktrackScreen
import com.cos229239.team02.oto.ui.screens.crisis.ShareStatusLocationScreen
import com.cos229239.team02.oto.ui.screens.explorer.AreaSafetyRoute
import com.cos229239.team02.oto.ui.screens.explorer.ExplorerScreen
import com.cos229239.team02.oto.ui.screens.explorer.FieldReportsScreen
import com.cos229239.team02.oto.ui.screens.explorer.PlanTripScreen
import com.cos229239.team02.oto.ui.screens.explorer.ReportHazardScreen
import com.cos229239.team02.oto.ui.screens.explorer.WeatherReportScreen
import com.cos229239.team02.oto.ui.screens.home.HomeScreen

@Composable
fun OtoNavigation() {

    val backStack =
        rememberNavBackStack(
            OtoRoute.Home
        )

    /*
     * ---------------------------------------------------------
     * SHARED PLAN TRIP VIEW MODEL
     * ---------------------------------------------------------
     *
     * Plan Trip writes to it.
     * Explorer reads from it.
     */

    val planTripViewModel:
            PlanTripViewModel =
        viewModel()

    /*
     * ---------------------------------------------------------
     * SHARED HAZARD REPORT VIEW MODEL
     * ---------------------------------------------------------
     *
     * Report Hazard writes reports to it.
     *
     * Explorer reads the reports for:
     *
     * - Hazard count
     * - Map markers
     *
     * Field Reports reads the same reports for:
     *
     * - Saved photos
     * - Report details
     * - Priority
     * - Severity
     * - Community confirmations
     *
     * This ViewModel also loads locally stored
     * reports when the app starts.
     */

    val hazardReportViewModel:
            HazardReportViewModel =
        viewModel()

    /*
     * ---------------------------------------------------------
     * NAVIGATION DISPLAY
     * ---------------------------------------------------------
     */

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
                 * =================================================
                 * HOME
                 * =================================================
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
                 * =================================================
                 * EXPLORER
                 * =================================================
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
                         * Opens Weather Report.
                         */
                        onWeatherClick = {

                            backStack.add(
                                OtoRoute.WeatherReport
                            )
                        },

                        /*
                         * Opens Report Hazard.
                         */
                        onReportHazardClick = {

                            backStack.add(
                                OtoRoute.ReportHazard
                            )
                        },

                        /*
                         * Opens Field Reports.
                         *
                         * Used by:
                         *
                         * - FIELD REPORTS dashboard card
                         * - Hazard map popup
                         */
                        onFieldReportsClick = {

                            backStack.add(
                                OtoRoute.FieldReports
                            )
                        },

                        /*
                         * Back to Home.
                         */
                        onBackClick = {

                            backStack.removeLastOrNull()
                        },

                        /*
                         * Shared trip state.
                         */
                        tripViewModel =
                            planTripViewModel,

                        /*
                         * Shared hazard state.
                         */
                        hazardReportViewModel =
                            hazardReportViewModel
                    )
                }

                /*
                 * =================================================
                 * PLAN TRIP
                 * =================================================
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
                 * =================================================
                 * WEATHER REPORT
                 * =================================================
                 */

                entry<OtoRoute.WeatherReport> {

                    WeatherReportScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * =================================================
                 * REPORT HAZARD / ROUTE CHANGE
                 * =================================================
                 */

                entry<OtoRoute.ReportHazard> {

                    ReportHazardScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        },

                        /*
                         * Opens First Aid & Survival from
                         * the submitted report screen.
                         */
                        onFirstAidSurvivalClick = {

                            backStack.add(
                                OtoRoute.FirstAidSurvival
                            )
                        },

                        hazardReportViewModel =
                            hazardReportViewModel
                    )
                }

                /*
                 * =================================================
                 * FIELD REPORTS
                 * =================================================
                 */

                entry<OtoRoute.FieldReports> {

                    FieldReportsScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        },

                        hazardReportViewModel =
                            hazardReportViewModel
                    )
                }

                /*
                 * =================================================
                 * AREA SAFETY
                 * =================================================
                 */

                entry<OtoRoute.AreaSafety> {

                    AreaSafetyRoute(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * =================================================
                 * CRISIS MODE
                 * =================================================
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
                 * =================================================
                 * CRISIS - EMERGENCY HELP
                 * =================================================
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
                 * =================================================
                 * CRISIS - SHARE STATUS / LOCATION
                 * =================================================
                 */

                entry<OtoRoute.ShareStatusLocation> {

                    ShareStatusLocationScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * =================================================
                 * CRISIS - FIRST AID / SURVIVAL
                 * =================================================
                 */

                entry<OtoRoute.FirstAidSurvival> {

                    FirstAidSurvivalScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * =================================================
                 * CRISIS - NEARBY RESOURCES
                 * =================================================
                 */

                entry<OtoRoute.NearbyResources> {

                    NearbyResourceScreen(
                        onBackClick = {

                            backStack.removeLastOrNull()
                        }
                    )
                }

                /*
                 * =================================================
                 * CRISIS - OFFLINE MAPS & BACKTRACK
                 * =================================================
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