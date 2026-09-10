package com.cos229239.team02.oto.data.route

import com.cos229239.team02.oto.data.location.OtoLocation
import kotlinx.serialization.Serializable

/**
 * A single recorded GPS point in a tracked route.
 */
@Serializable
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val timestampMillis: Long
)

/**
 * Converts a live location into a storable route point.
 */
fun OtoLocation.toRoutePoint(): RoutePoint =
    RoutePoint(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        altitudeMeters = altitudeMeters,
        timestampMillis = timestampMillis ?: System.currentTimeMillis()
    )

/**
 * Converts a recorded route point into the shared location model,
 * so the map and UI never have to understand route-specific data.
 */
fun RoutePoint.toOtoLocation(): OtoLocation =
    OtoLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        altitudeMeters = altitudeMeters,
        timestampMillis = timestampMillis
    )