package com.cos229239.team02.oto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.data.hazard.HazardReportViewModel
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
     * APPLICATION CONTEXT
     * ---------------------------------------------------------
     *
     * Used by the shared Area Safety ViewModel factory.
     */

    val context =
        LocalContext
            .current
            .applicationContext

    /*
     * ---------------------------------------------------------
     * SHARED AREA SAFETY VIEW MODEL
     * ---------------------------------------------------------
     *
     * Shared between:
     *
     * - Explorer
     * - Weather Report
     * - Area Safety
     */

    val safetyFactory =
        remember(
            context
        ) {

            AreaSafetyViewFactory(
                context =
                    context
            )
        }

    val safetyView:
            AreaSafetyView =
        viewModel(
            factory =
                safetyFactory
        )

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
     * - Selected map-marker reports
     *
     * The ViewModel also loads locally stored
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
                        },

                        /*
                         * Team update:
                         * Opens preparedness/offline tools
                         * directly from Home.
                         */
                        onOfflineToolsClick = {

                            backStack.add(
                                OtoRoute.OfflineMapBacktrack
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

                            if (
                                backStack.size > 1
                            ) {

                                backStack.removeLastOrNull()
                            }
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
                            hazardReportViewModel,

                        /*
                         * Team's shared safety/weather state.
                         */
                        safetyView =
                            safetyView
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

                            if (
                                backStack.size > 1
                            ) {

                                backStack.removeLastOrNull()
                            }
                        },

                        /*
                         * Team update:
                         * Uses the same shared safety ViewModel.
                         */
                        safetyView =
                            safetyView
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

                            if (
                                backStack.size > 1
                            ) {

                                backStack.removeLastOrNull()
                            }
                        },

                        /*
                         * Team update:
                         * Area Safety shares the same safety state
                         * as Explorer and Weather.
                         */
                        safetyView =
                            safetyView
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