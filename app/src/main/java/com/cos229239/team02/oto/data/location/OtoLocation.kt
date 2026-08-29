package com.cos229239.team02.oto.data.location

/**
 * Basic location model used throughout OTO.
 *
 * This keeps Android/location-provider details out of
 * Explorer and Crisis screens.
 */
data class OtoLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val timestampMillis: Long? = null
)