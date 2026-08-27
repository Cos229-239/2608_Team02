package com.cos229239.team02.oto.ui.screens.explorer


enum class SafetyCategory {
    WEATHER,
    PARK,
    WILDLIFE,
    COMMUNITY
}

enum class SafetyLevel {
    SEVERE,
    MODERATE,
    MINOR,
    UNKNOWN
}

data class SafetyNotify (
    val id: String,
    val title: String,
    val details: String,
    val instruct: String,
    val affected_area: String,
    val category: SafetyCategory,
    val level: SafetyLevel,

    val sourceID: String,
    val sourceURL: String,
    val retrived_time: String,
    val last_verfication: String,
    val expires: String?, //null value declared if no expiration on data (offline mode)

    val data_expired: Boolean = false,
    val sample_data: Boolean = false
)