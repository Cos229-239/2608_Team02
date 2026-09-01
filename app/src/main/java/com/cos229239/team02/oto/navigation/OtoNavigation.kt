package com.cos229239.team02.oto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.cos229239.team02.oto.ui.screens.crisis.CrisisScreen
import com.cos229239.team02.oto.ui.screens.crisis.FirstAidSurvivalScreen
import com.cos229239.team02.oto.ui.screens.crisis.NearbyResourceScreen
import com.cos229239.team02.oto.ui.screens.crisis.ShareStatusLocationScreen
import com.cos229239.team02.oto.ui.screens.explorer.AreaSafetyRoute
import com.cos229239.team02.oto.ui.screens.explorer.ExplorerScreen
import com.cos229239.team02.oto.ui.screens.explorer.PlanTripScreen
import com.cos229239.team02.oto.ui.screens.home.HomeScreen

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

            // Home screen
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

            // Explorer main screen
            entry<OtoRoute.Explorer> {
                ExplorerScreen(
                    onAreaSafetyClick = {
                        backStack.add(OtoRoute.AreaSafety)
                    },
                    onPlanTripClick = {
                        backStack.add(OtoRoute.PlanTrip)
                    },
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Plan Trip screen
            entry<OtoRoute.PlanTrip> {
                PlanTripScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    onSaveClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Area Safety screen
            entry<OtoRoute.AreaSafety> {
                AreaSafetyRoute(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Crisis main screen
            entry<OtoRoute.Crisis> {
                CrisisScreen(
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

            // Crisis - Share Status & Location
            entry<OtoRoute.ShareStatusLocation> {
                ShareStatusLocationScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Crisis - First Aid & Survival
            entry<OtoRoute.FirstAidSurvival> {
                FirstAidSurvivalScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Crisis - Nearby Resources
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