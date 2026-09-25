package com.cos229239.team02.oto.ui.features


import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.resource.AirQualityData
import com.cos229239.team02.oto.data.resource.ResourceResult
import com.cos229239.team02.oto.data.resource.WeatherForecast
import com.cos229239.team02.oto.data.safety.SafetyFilter
import com.cos229239.team02.oto.data.safety.SafetyNotification
import com.cos229239.team02.oto.data.safety.SafetySourceState
import com.cos229239.team02.oto.data.safety.SafetySourceStatus


data class AreaSafetyUIState(
    val areaName: String = "Select an area",
    val notifications: List <SafetyNotification> = emptyList(),
    val resourceResult: ResourceResult? = null,
    val sources: List<SafetySourceStatus> = emptyList(),
    val filterSelected: SafetyFilter = SafetyFilter.ALL,
    val hasLocation: Boolean = false,
    val isLoading: Boolean = false,
    val checkedAtMillis: Long? = null,
    val errorMessage: String? = null,
    val selectedParkCode: String? = null,
    val weatherNotifications: List<SafetyNotification> = emptyList(),
    val forecast: WeatherForecast? = null,
    val airQuality: AirQualityData? = null,
    //override option for park selection if planning trip
    val selectedLocation: OtoLocation?  = null,
    //Automatic Park Selection variable
    val autoParkSelect: Boolean = true,
    val selectedParkName: String? = null,


){
    val hasUnavailableSources: Boolean
        get() = sources.any {
            it.state == SafetySourceState.FAILED
        }

    val resourcesFromCache: Boolean
        get() = resourceResult?.fromCache == true
}
