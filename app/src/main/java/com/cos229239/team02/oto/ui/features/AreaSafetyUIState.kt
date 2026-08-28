package com.cos229239.team02.oto.ui.features

enum class SafetyFilter (
    val displayName: String
)
{
    ALL ("All"),
    WEATHER("Weather"),
    AREA("Location"),
    COMMUNITY("OTO Outsiders")//Location could be relative to local landmarks or general region name
}

data class AreaSafetyUIState(
    val areaName: String = "Current Area",
    val notifications: List <SafetyNotification> = emptyList(),
    val filterSelected: SafetyFilter = SafetyFilter.ALL,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSampleData: Boolean = true,
    val errorMessage: String? = null

)