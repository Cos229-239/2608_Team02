package com.cos229239.team02.oto.ui.features


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

data class SafetyNotification (
    val id: String,
    val title: String,
    val details: String,
    val instruct: String,
    val affectedArea: String,
    val category: SafetyCategory,
    val level: SafetyLevel,

    val sourceID: String,
    val sourceURL: String,
    val retrievedTime: String,
    val lastVerification: String,
    val expires: String?, //null value declared if no expiration on data (offline mode)

    val dataExpired: Boolean = false,
    val sampleData: Boolean = false
)