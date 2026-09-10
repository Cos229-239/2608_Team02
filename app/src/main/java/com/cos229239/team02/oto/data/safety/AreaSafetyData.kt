package com.cos229239.team02.oto.data.safety

import com.cos229239.team02.oto.data.resource.ResourceResult


// Consolidated data class into one file for increase legibility of Safety data

enum class SafetyCategory {
    WEATHER,
    AREA,
    COMMUNITY
}
enum class SafetyLevel {
    SEVERE,
    MODERATE,
    MINOR,
    UNKNOWN
}
enum class SafetyFilter(val displayName: String) {
    ALL("All"),
    WEATHER("Weather"),
    AREA("Location"),
    COMMUNITY("OTO Outsiders")
}


enum class SafetySourceState {
    SUCCESS,
    FAILED,
    SKIPPED,
    CACHED
}

data class SafetySourceStatus(
    val source: String,
    val state: SafetySourceState,
    val message: String
)


data class SafetyNotification(
    val id: String,
    val title: String,
    val details: String,
    val instruct: String,
    val affectedArea: String,
    val category: SafetyCategory,
    val level: SafetyLevel,
    val sourceID: String,
    val sourceUrl: String,
    val retrievedTime: String,
    val lastVerification: String,
    val expires: String?,
    val dataExpired: Boolean = false,



    )





data class AreaSafetyData(
    val areaName: String,
    val notifications: List<SafetyNotification> = emptyList(),

    val resourceResult: ResourceResult? = null,

    val sources: List<SafetySourceStatus> = emptyList(),
    val checkedAtMillis: Long = System.currentTimeMillis(),

)

