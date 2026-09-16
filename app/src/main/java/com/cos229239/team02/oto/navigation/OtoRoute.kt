package com.cos229239.team02.oto.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface OtoRoute : NavKey {

    /*
     * ---------------------------------------------------------
     * HOME
     * ---------------------------------------------------------
     */

    @Serializable
    data object Home : OtoRoute

    /*
     * ---------------------------------------------------------
     * EXPLORER
     * ---------------------------------------------------------
     */

    @Serializable
    data object Explorer : OtoRoute

    @Serializable
    data object AreaSafety : OtoRoute

    @Serializable
    data object PlanTrip : OtoRoute

    /*
     * Explorer weather details screen.
     */
    @Serializable
    data object WeatherReport : OtoRoute

    /*
     * Explorer hazard and route change reporting screen.
     */
    @Serializable
    data object ReportHazard : OtoRoute

    /*
     * Explorer field reports screen.
     *
     * Displays saved hazard reports, photos,
     * priority, severity, location, and status.
     */
    @Serializable
    data object FieldReports : OtoRoute

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

    /*
     * ---------------------------------------------------------
     * CRISIS
     * ---------------------------------------------------------
     */

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