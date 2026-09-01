package com.cos229239.team02.oto.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import com.cos229239.team02.oto.ui.screens.explorer.AreaSafetyRoute
@Serializable
sealed interface OtoRoute : NavKey {

    @Serializable
    data object Home : OtoRoute

    @Serializable
    data object Explorer : OtoRoute
    @Serializable
    data object AreaSafety : OtoRoute
    @Serializable
    data object PlanTrip : OtoRoute

    @Serializable
    data object ExplorerMap : OtoRoute

    @Serializable
    data object TrailDetail : OtoRoute

    @Serializable
    data object OfflineArea : OtoRoute

    @Serializable
    data object Conditions : OtoRoute

    @Serializable
    data object TripPlanCheckIn : OtoRoute

    @Serializable
    data object Crisis : OtoRoute

    @Serializable
    data object EmergencyHelp : OtoRoute

    @Serializable
    data object ShareStatusLocation : OtoRoute

    @Serializable
    data object OfflineMapBacktrack : OtoRoute

    @Serializable
    data object OfficialHazardsAlerts : OtoRoute

    @Serializable
    data object NearbyResources : OtoRoute

    @Serializable
    data object FirstAidSurvival : OtoRoute
}